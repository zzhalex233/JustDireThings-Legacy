package com.zzhalex.justdirethings.network.message;

import com.zzhalex.justdirethings.common.item.misc.ItemMachineSettingsCopier;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageCopyMachineSettings implements IMessage {

    private boolean area;
    private boolean offset;
    private boolean filter;
    private boolean redstone;

    public MessageCopyMachineSettings() {
    }

    public MessageCopyMachineSettings(boolean area, boolean offset, boolean filter, boolean redstone) {
        this.area = area;
        this.offset = offset;
        this.filter = filter;
        this.redstone = redstone;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        area = buf.readBoolean();
        offset = buf.readBoolean();
        filter = buf.readBoolean();
        redstone = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(area);
        buf.writeBoolean(offset);
        buf.writeBoolean(filter);
        buf.writeBoolean(redstone);
    }

    public static class Handler implements IMessageHandler<MessageCopyMachineSettings, IMessage> {

        @Override
        public IMessage onMessage(MessageCopyMachineSettings message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> applySettings(player, message));
            return null;
        }

        private static void applySettings(EntityPlayerMP player, MessageCopyMachineSettings message) {
            ItemStack stack = player.getHeldItemMainhand();
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemMachineSettingsCopier)) {
                stack = player.getHeldItemOffhand();
            }
            if (!stack.isEmpty() && stack.getItem() instanceof ItemMachineSettingsCopier) {
                ItemMachineSettingsCopier.setSettings(stack, message.area, message.offset, message.filter, message.redstone);
            }
        }
    }
}
