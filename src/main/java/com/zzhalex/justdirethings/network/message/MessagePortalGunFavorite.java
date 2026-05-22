package com.zzhalex.justdirethings.network.message;

import com.zzhalex.justdirethings.common.item.tool.ItemPortalGunV2;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessagePortalGunFavorite implements IMessage {

    private int favoriteIndex;
    private boolean stayOpen;

    public MessagePortalGunFavorite() {
    }

    public MessagePortalGunFavorite(int favoriteIndex, boolean stayOpen) {
        this.favoriteIndex = favoriteIndex;
        this.stayOpen = stayOpen;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        favoriteIndex = buf.readInt();
        stayOpen = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(favoriteIndex);
        buf.writeBoolean(stayOpen);
    }

    public static class Handler implements IMessageHandler<MessagePortalGunFavorite, IMessage> {

        @Override
        public IMessage onMessage(MessagePortalGunFavorite message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                ItemStack heldStack = ItemPortalGunV2.findHeldPortalGun(player);
                if (heldStack.isEmpty() || !(heldStack.getItem() instanceof ItemPortalGunV2)) {
                    return;
                }
                ItemPortalGunV2 item = (ItemPortalGunV2) heldStack.getItem();
                item.setFavoritePosition(heldStack, message.favoriteIndex);
                item.setStayOpen(heldStack, message.stayOpen);
            });
            return null;
        }
    }
}
