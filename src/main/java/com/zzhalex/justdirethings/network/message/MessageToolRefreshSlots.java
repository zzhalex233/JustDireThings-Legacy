package com.zzhalex.justdirethings.network.message;

import com.zzhalex.justdirethings.common.container.ContainerToolSettings;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageToolRefreshSlots implements IMessage {

    private int slot;

    public MessageToolRefreshSlots() {
    }

    public MessageToolRefreshSlots(int slot) {
        this.slot = slot;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        slot = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(slot);
    }

    public static class Handler implements IMessageHandler<MessageToolRefreshSlots, IMessage> {

        @Override
        public IMessage onMessage(MessageToolRefreshSlots message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (!(player.openContainer instanceof ContainerToolSettings)) {
                    return;
                }
                ItemStack selected = player.inventory.getStackInSlot(message.slot);
                ((ContainerToolSettings) player.openContainer).refreshSlots(selected);
                player.openContainer.detectAndSendChanges();
            });
            return null;
        }
    }
}
