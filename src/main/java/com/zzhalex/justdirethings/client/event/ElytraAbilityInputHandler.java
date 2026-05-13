package com.zzhalex.justdirethings.client.event;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.ability.AbilityMethods;
import com.zzhalex.justdirethings.network.JDTNetwork;
import com.zzhalex.justdirethings.network.message.MessageStartElytraFlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public final class ElytraAbilityInputHandler {

    public static final ElytraAbilityInputHandler INSTANCE = new ElytraAbilityInputHandler();

    private boolean wasJumpDown;

    private ElytraAbilityInputHandler() {
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        if (player == null || mc.gameSettings == null) {
            wasJumpDown = false;
            return;
        }

        boolean jumpDown = mc.gameSettings.keyBindJump.isKeyDown();
        if (jumpDown && !wasJumpDown && shouldRequestFlight(player)) {
            JDTNetwork.getChannel().sendToServer(new MessageStartElytraFlight());
        }
        wasJumpDown = jumpDown;
    }

    private static boolean shouldRequestFlight(EntityPlayerSP player) {
        if (player.onGround
                || player.motionY >= 0.0D
                || player.isElytraFlying()
                || player.isInWater()
                || player.capabilities.isFlying
                || player.isRiding()) {
            return false;
        }

        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        return AbilityMethods.canUseAbilityAndDurability(chest, Ability.ELYTRA);
    }
}
