package com.zzhalex.justdirethings.network.message;

import com.zzhalex.justdirethings.common.item.tool.ItemPortalGunV2;
import com.zzhalex.justdirethings.common.portal.PortalLinkData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessagePortalGunFavoriteChange implements IMessage {

    private int favoriteIndex;
    private boolean add;
    private String name;
    private boolean editing;
    private Vec3d coordinates;

    public MessagePortalGunFavoriteChange() {
        this(0, false, "", false, Vec3d.ZERO);
    }

    public MessagePortalGunFavoriteChange(int favoriteIndex, boolean add, String name, boolean editing, Vec3d coordinates) {
        this.favoriteIndex = favoriteIndex;
        this.add = add;
        this.name = name == null ? "" : name;
        this.editing = editing;
        this.coordinates = coordinates == null ? Vec3d.ZERO : coordinates;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        favoriteIndex = buf.readInt();
        add = buf.readBoolean();
        name = ByteBufUtils.readUTF8String(buf);
        editing = buf.readBoolean();
        coordinates = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(favoriteIndex);
        buf.writeBoolean(add);
        ByteBufUtils.writeUTF8String(buf, name);
        buf.writeBoolean(editing);
        buf.writeDouble(coordinates.x);
        buf.writeDouble(coordinates.y);
        buf.writeDouble(coordinates.z);
    }

    public static class Handler implements IMessageHandler<MessagePortalGunFavoriteChange, IMessage> {

        @Override
        public IMessage onMessage(MessagePortalGunFavoriteChange message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                ItemStack heldStack = ItemPortalGunV2.findHeldPortalGun(player);
                if (heldStack.isEmpty() || !(heldStack.getItem() instanceof ItemPortalGunV2)) {
                    return;
                }
                ItemPortalGunV2 item = (ItemPortalGunV2) heldStack.getItem();
                if (!message.add) {
                    item.removeFavorite(heldStack, message.favoriteIndex);
                    return;
                }
                PortalLinkData.PortalDestination destination = item.getFavorite(heldStack, message.favoriteIndex);
                if (!message.editing) {
                    item.addFavorite(heldStack, message.favoriteIndex, PortalLinkData.PortalDestination.fromPlayer(player, message.name));
                    return;
                }
                boolean empty = destination == null || destination.isEmpty();
                Vec3d position = isZero(message.coordinates)
                        ? player.getPositionVector()
                        : message.coordinates;
                EnumFacing facing = empty ? PortalLinkData.PortalDestination.facingFromPlayer(player) : destination.getFacing();
                int dimension = empty ? player.world.provider.getDimension() : destination.getDimension();
                item.addFavorite(heldStack, message.favoriteIndex, new PortalLinkData.PortalDestination(
                        message.name,
                        dimension,
                        position.x,
                        position.y,
                        position.z,
                        facing
                ));
            });
            return null;
        }

        private static boolean isZero(Vec3d vector) {
            return vector == null || (vector.x == 0.0D && vector.y == 0.0D && vector.z == 0.0D);
        }
    }
}
