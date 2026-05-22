package com.zzhalex.justdirethings.common.event;

import com.zzhalex.justdirethings.common.item.tool.ItemPolymorphicWand;
import com.zzhalex.justdirethings.common.item.tool.ItemPolymorphicWandV2;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class PolymorphicWandEventHandler {

    public static final PolymorphicWandEventHandler INSTANCE = new PolymorphicWandEventHandler();

    private PolymorphicWandEventHandler() {
    }

    @SubscribeEvent
    public void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        cancelEntityInteraction(event);
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        cancelEntityInteraction(event);
    }

    private static void cancelEntityInteraction(PlayerInteractEvent event) {
        if (!isPolymorphicWand(event.getItemStack())) {
            return;
        }
        event.setCancellationResult(EnumActionResult.PASS);
        event.setCanceled(true);
    }

    public static boolean isPolymorphicWand(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        return item instanceof ItemPolymorphicWand || item instanceof ItemPolymorphicWandV2;
    }
}
