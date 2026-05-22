package com.zzhalex.justdirethings.network.message;

import com.zzhalex.justdirethings.JustDireThingsLegacy;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageItemFlowParticle implements IMessage {

    private double startX;
    private double startY;
    private double startZ;
    private double targetX;
    private double targetY;
    private double targetZ;
    private ItemStack stack;
    private int ticksPerBlock;

    public MessageItemFlowParticle() {
        this(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, ItemStack.EMPTY, 1);
    }

    public MessageItemFlowParticle(double startX, double startY, double startZ, double targetX, double targetY, double targetZ, ItemStack stack, int ticksPerBlock) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
        this.ticksPerBlock = Math.max(1, ticksPerBlock);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        startX = buf.readDouble();
        startY = buf.readDouble();
        startZ = buf.readDouble();
        targetX = buf.readDouble();
        targetY = buf.readDouble();
        targetZ = buf.readDouble();
        stack = ByteBufUtils.readItemStack(buf);
        ticksPerBlock = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(startX);
        buf.writeDouble(startY);
        buf.writeDouble(startZ);
        buf.writeDouble(targetX);
        buf.writeDouble(targetY);
        buf.writeDouble(targetZ);
        ByteBufUtils.writeItemStack(buf, stack);
        buf.writeInt(ticksPerBlock);
    }

    public static class Handler implements IMessageHandler<MessageItemFlowParticle, IMessage> {

        @Override
        public IMessage onMessage(MessageItemFlowParticle message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> JustDireThingsLegacy.proxy.spawnItemFlowParticle(
                    null,
                    message.startX,
                    message.startY,
                    message.startZ,
                    message.targetX,
                    message.targetY,
                    message.targetZ,
                    message.stack,
                    message.ticksPerBlock
            ));
            return null;
        }
    }
}
