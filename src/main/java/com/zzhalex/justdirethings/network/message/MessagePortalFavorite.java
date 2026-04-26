package com.zzhalex.justdirethings.network.message;

import com.zzhalex.justdirethings.common.item.tool.ItemPortalGunV2;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessagePortalFavorite implements IMessage {

    private int favoriteIndex;
    private NBTTagCompound favoriteTag;

    public MessagePortalFavorite() {
        this(0, new NBTTagCompound());
    }

    public MessagePortalFavorite(int favoriteIndex, NBTTagCompound favoriteTag) {
        this.favoriteIndex = favoriteIndex;
        this.favoriteTag = favoriteTag == null ? new NBTTagCompound() : favoriteTag;
    }

    public int getFavoriteIndex() {
        return favoriteIndex;
    }

    public NBTTagCompound getFavoriteTag() {
        return favoriteTag;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        favoriteIndex = buf.readInt();
        favoriteTag = ByteBufUtils.readTag(buf);
        if (favoriteTag == null) {
            favoriteTag = new NBTTagCompound();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(favoriteIndex);
        ByteBufUtils.writeTag(buf, favoriteTag);
    }

    public static class Handler implements IMessageHandler<MessagePortalFavorite, IMessage> {

        @Override
        public IMessage onMessage(MessagePortalFavorite message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                ItemStack heldStack = ItemPortalGunV2.findHeldPortalGun(player);
                if (heldStack.isEmpty() || !(heldStack.getItem() instanceof ItemPortalGunV2)) {
                    return;
                }
                ((ItemPortalGunV2) heldStack.getItem()).applyFavoriteUpdate(heldStack, message.getFavoriteIndex(), message.getFavoriteTag());
            });
            return null;
        }
    }
}
