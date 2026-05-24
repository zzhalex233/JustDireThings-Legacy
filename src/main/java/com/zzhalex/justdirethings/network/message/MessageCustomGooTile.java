package com.zzhalex.justdirethings.network.message;

import com.zzhalex.justdirethings.JustDireThingsLegacy;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageCustomGooTile implements IMessage {

    private BlockPos pos;
    private NBTTagCompound tag;
    private boolean remove;

    public MessageCustomGooTile() {
        this(BlockPos.ORIGIN, new NBTTagCompound(), false);
    }

    public MessageCustomGooTile(BlockPos pos, NBTTagCompound tag, boolean remove) {
        this.pos = pos == null ? BlockPos.ORIGIN : pos.toImmutable();
        this.tag = tag == null ? new NBTTagCompound() : tag;
        this.remove = remove;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        remove = buf.readBoolean();
        tag = ByteBufUtils.readTag(buf);
        if (tag == null) {
            tag = new NBTTagCompound();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeBoolean(remove);
        ByteBufUtils.writeTag(buf, tag);
    }

    public static class Handler implements IMessageHandler<MessageCustomGooTile, IMessage> {

        @Override
        public IMessage onMessage(MessageCustomGooTile message, MessageContext ctx) {
            JustDireThingsLegacy.proxy.syncCustomGooTile(message.pos, message.tag, message.remove);
            return null;
        }
    }
}
