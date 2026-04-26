package com.zzhalex.justdirethings.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSyncToolState implements IMessage {

    private int slot;
    private NBTTagCompound toolStateTag;

    public MessageSyncToolState() {
        this(0, new NBTTagCompound());
    }

    public MessageSyncToolState(int slot, NBTTagCompound toolStateTag) {
        this.slot = slot;
        this.toolStateTag = toolStateTag == null ? new NBTTagCompound() : toolStateTag;
    }

    public int getSlot() {
        return slot;
    }

    public NBTTagCompound getToolStateTag() {
        return toolStateTag;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        slot = buf.readInt();
        toolStateTag = ByteBufUtils.readTag(buf);
        if (toolStateTag == null) {
            toolStateTag = new NBTTagCompound();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(slot);
        ByteBufUtils.writeTag(buf, toolStateTag);
    }

    public static class Handler implements IMessageHandler<MessageSyncToolState, IMessage> {

        @Override
        public IMessage onMessage(MessageSyncToolState message, MessageContext ctx) {
            return null;
        }
    }
}
