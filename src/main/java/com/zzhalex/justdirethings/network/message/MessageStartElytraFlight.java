package com.zzhalex.justdirethings.network.message;

import com.zzhalex.justdirethings.common.event.AbilityRuntimeEventHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageStartElytraFlight implements IMessage {

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<MessageStartElytraFlight, IMessage> {

        @Override
        public IMessage onMessage(MessageStartElytraFlight message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> AbilityRuntimeEventHandler.requestElytraFlight(player));
            return null;
        }
    }
}
