package com.zzhalex.justdirethings.network.message;

import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageGhostSlot implements IMessage {

    private int windowId;
    private int slotId;
    private ItemStack stack = ItemStack.EMPTY;

    public MessageGhostSlot() {
    }

    public MessageGhostSlot(int windowId, int slotId, ItemStack stack) {
        this.windowId = windowId;
        this.slotId = slotId;
        this.stack = stack == null ? ItemStack.EMPTY : stack;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        windowId = buf.readInt();
        slotId = buf.readInt();
        stack = ByteBufUtils.readItemStack(buf);
        if (stack == null) {
            stack = ItemStack.EMPTY;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(windowId);
        buf.writeInt(slotId);
        ByteBufUtils.writeItemStack(buf, stack);
    }

    public static class Handler implements IMessageHandler<MessageGhostSlot, IMessage> {

        @Override
        public IMessage onMessage(MessageGhostSlot message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> applyGhostSlot(player, message));
            return null;
        }

        private static void applyGhostSlot(EntityPlayerMP player, MessageGhostSlot message) {
            Container openContainer = player.openContainer;
            if (!(openContainer instanceof ContainerMachineBase) || openContainer.windowId != message.windowId) {
                return;
            }
            ((ContainerMachineBase) openContainer).applyGhostSlot(message.slotId, message.stack);
        }
    }
}
