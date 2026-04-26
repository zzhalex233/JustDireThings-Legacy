package com.zzhalex.justdirethings.network.message;

import com.zzhalex.justdirethings.common.item.tool.ItemPortalGun;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessagePortalGunLeftClick implements IMessage {

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<MessagePortalGunLeftClick, IMessage> {

        @Override
        public IMessage onMessage(MessagePortalGunLeftClick message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> firePrimaryPortal(player));
            return null;
        }

        private static void firePrimaryPortal(EntityPlayerMP player) {
            ItemStack heldStack = ItemPortalGun.findHeldPortalGun(player);
            if (heldStack.isEmpty() || !(heldStack.getItem() instanceof ItemPortalGun)) {
                return;
            }

            ((ItemPortalGun) heldStack.getItem()).spawnProjectile(player.world, player, heldStack, true);
        }
    }
}
