package com.zzhalex.justdirethings.network.message;

import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import com.zzhalex.justdirethings.common.container.machine.ContainerSensor;
import com.zzhalex.justdirethings.common.tile.machine.TileSensor;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageBlockStateFilter implements IMessage {

    private int windowId;
    private int slot;
    private String propertyName;
    private String valueName;

    public MessageBlockStateFilter() {
    }

    public MessageBlockStateFilter(int windowId, int slot, String propertyName, String valueName) {
        this.windowId = windowId;
        this.slot = slot;
        this.propertyName = propertyName == null ? "" : propertyName;
        this.valueName = valueName == null ? "" : valueName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        windowId = buf.readInt();
        slot = buf.readInt();
        propertyName = ByteBufUtils.readUTF8String(buf);
        valueName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(windowId);
        buf.writeInt(slot);
        ByteBufUtils.writeUTF8String(buf, propertyName);
        ByteBufUtils.writeUTF8String(buf, valueName);
    }

    public static class Handler implements IMessageHandler<MessageBlockStateFilter, IMessage> {

        @Override
        public IMessage onMessage(MessageBlockStateFilter message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> apply(player, message));
            return null;
        }

        private static void apply(EntityPlayerMP player, MessageBlockStateFilter message) {
            Container openContainer = player.openContainer;
            if (!(openContainer instanceof ContainerSensor) || openContainer.windowId != message.windowId) {
                return;
            }
            ContainerMachineBase machineContainer = (ContainerMachineBase) openContainer;
            if (!(machineContainer.getMachine() instanceof TileSensor)) {
                return;
            }
            TileSensor sensor = (TileSensor) machineContainer.getMachine();
            if (message.propertyName.isEmpty()) {
                sensor.clearSensorProperties(message.slot);
                return;
            }
            ItemStack filterStack = sensor.getFilterHandler().getStackInSlot(message.slot);
            IBlockState state = sensor.getStateForStack(filterStack);
            if (state == null) {
                sensor.clearSensorProperties(message.slot);
                return;
            }
            IProperty<?> property = state.getBlock().getBlockState().getProperty(message.propertyName);
            if (property == null) {
                return;
            }
            if (message.valueName.isEmpty()) {
                sensor.setSensorProperty(message.slot, property, null, true);
                return;
            }
            Comparable<?> parsed = parsePropertyValue(property, message.valueName);
            if (parsed != null) {
                sensor.setSensorProperty(message.slot, property, parsed, false);
            }
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Comparable<?> parsePropertyValue(IProperty property, String valueName) {
            Object parsed = property.parseValue(valueName).orNull();
            return parsed instanceof Comparable ? (Comparable<?>) parsed : null;
        }
    }
}
