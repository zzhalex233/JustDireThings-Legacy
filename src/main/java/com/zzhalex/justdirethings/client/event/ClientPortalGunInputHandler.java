package com.zzhalex.justdirethings.client.event;

import com.zzhalex.justdirethings.common.item.tool.ItemPortalGun;
import com.zzhalex.justdirethings.network.JDTNetwork;
import com.zzhalex.justdirethings.network.message.MessagePortalGunLeftClick;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ClientPortalGunInputHandler {

    public static final ClientPortalGunInputHandler INSTANCE = new ClientPortalGunInputHandler();

    private ClientPortalGunInputHandler() {
    }

    @SubscribeEvent
    public void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        sendLeftClickIfPortalGun(event);
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        sendLeftClickIfPortalGun(event);
    }

    public static boolean shouldSendLeftClick(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof ItemPortalGun;
    }

    private static void sendLeftClickIfPortalGun(PlayerInteractEvent event) {
        if (event.getSide() != Side.CLIENT || !shouldSendLeftClick(event.getItemStack())) {
            return;
        }
        JDTNetwork.getChannel().sendToServer(new MessagePortalGunLeftClick());
    }
}
