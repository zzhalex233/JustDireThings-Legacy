package com.zzhalex.justdirethings.client.event;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.ability.AbilityMethods;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class PhaseClientEventHandler {

    public static final PhaseClientEventHandler INSTANCE = new PhaseClientEventHandler();

    private PhaseClientEventHandler() {
    }

    @SubscribeEvent
    public void onPushOutOfBlocks(PlayerSPPushOutOfBlocksEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player == null) {
            return;
        }

        ItemStack leggings = player.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
        if (AbilityMethods.canUseAbility(leggings, Ability.PHASE)) {
            event.setCanceled(true);
        }
    }
}
