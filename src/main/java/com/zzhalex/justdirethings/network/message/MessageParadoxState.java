package com.zzhalex.justdirethings.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageParadoxState implements IMessage {

    private NBTTagCompound stateTag;

    public MessageParadoxState() {
        this(new NBTTagCompound());
    }

    public MessageParadoxState(NBTTagCompound stateTag) {
        this.stateTag = stateTag == null ? new NBTTagCompound() : stateTag;
    }

    public NBTTagCompound getStateTag() {
        return stateTag;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        stateTag = ByteBufUtils.readTag(buf);
        if (stateTag == null) {
            stateTag = new NBTTagCompound();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, stateTag);
    }

    public static class Handler implements IMessageHandler<MessageParadoxState, IMessage> {

        @Override
        public IMessage onMessage(MessageParadoxState message, MessageContext ctx) {
            return null;
        }
    }
}
