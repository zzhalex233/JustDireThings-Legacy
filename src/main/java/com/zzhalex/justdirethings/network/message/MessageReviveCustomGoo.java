package com.zzhalex.justdirethings.network.message;

import com.zzhalex.justdirethings.common.event.CustomGooEventHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageReviveCustomGoo implements IMessage {

    private BlockPos pos;
    private EnumHand hand;

    public MessageReviveCustomGoo() {
        this(BlockPos.ORIGIN, EnumHand.MAIN_HAND);
    }

    public MessageReviveCustomGoo(BlockPos pos, EnumHand hand) {
        this.pos = pos == null ? BlockPos.ORIGIN : pos.toImmutable();
        this.hand = hand == null ? EnumHand.MAIN_HAND : hand;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        hand = EnumHand.values()[buf.readByte()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeByte(hand.ordinal());
    }

    public static class Handler implements IMessageHandler<MessageReviveCustomGoo, IMessage> {

        @Override
        public IMessage onMessage(MessageReviveCustomGoo message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> CustomGooEventHandler.revive(player.getServerWorld(), message.pos, player, message.hand));
            return null;
        }
    }
}
