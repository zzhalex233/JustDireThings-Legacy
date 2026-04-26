package com.zzhalex.justdirethings.network.message;

import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import com.zzhalex.justdirethings.common.tile.base.MachineSettingApplier;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageMachineSetting implements IMessage {

    private int windowId;
    private String settingKey;
    private int settingValue;

    public MessageMachineSetting() {
        this(0, "", 0);
    }

    public MessageMachineSetting(int windowId, String settingKey, int settingValue) {
        this.windowId = windowId;
        this.settingKey = settingKey == null ? "" : settingKey;
        this.settingValue = settingValue;
    }

    public int getWindowId() {
        return windowId;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public int getSettingValue() {
        return settingValue;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        windowId = buf.readInt();
        settingKey = ByteBufUtils.readUTF8String(buf);
        settingValue = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(windowId);
        ByteBufUtils.writeUTF8String(buf, settingKey);
        buf.writeInt(settingValue);
    }

    public static class Handler implements IMessageHandler<MessageMachineSetting, IMessage> {

        @Override
        public IMessage onMessage(MessageMachineSetting message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> applySetting(player, message));
            return null;
        }

        private static void applySetting(EntityPlayerMP player, MessageMachineSetting message) {
            Container openContainer = player.openContainer;
            if (!(openContainer instanceof ContainerMachineBase) || openContainer.windowId != message.getWindowId()) {
                return;
            }

            ContainerMachineBase machineContainer = (ContainerMachineBase) openContainer;
            if (MachineSettingApplier.apply(machineContainer.getMachine(), message.getSettingKey(), message.getSettingValue(), player)) {
                machineContainer.getMachine().markDirtyClient();
            }
        }
    }
}
