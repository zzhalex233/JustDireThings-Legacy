package com.zzhalex.justdirethings.common.event;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import com.zzhalex.justdirethings.data.tool.AbilityCooldown;
import com.zzhalex.justdirethings.data.tool.ToolState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public final class AbilityRuntimeEventHandler {

    public static final AbilityRuntimeEventHandler INSTANCE = new AbilityRuntimeEventHandler();

    private AbilityRuntimeEventHandler() {
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player == null || event.player.world.isRemote) {
            return;
        }

        for (ItemStack stack : inventoryStacks(event.player)) {
            tickCooldowns(stack);
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (player.world.isRemote || !hasActiveInvulnerability(player)) {
            return;
        }

        event.setCanceled(true);
        event.setAmount(0.0F);
    }

    private static void tickCooldowns(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        ToolState state = ToggleableTool.readToolState(stack);
        if (state.getAbilityCooldowns().isEmpty()) {
            return;
        }

        List<AbilityCooldown> updated = new ArrayList<>();
        boolean changed = false;
        for (AbilityCooldown cooldown : state.getAbilityCooldowns()) {
            int remaining = cooldown.getRemainingTicks() - 1;
            changed = true;
            if (remaining > 0) {
                updated.add(new AbilityCooldown(cooldown.getAbilityId(), remaining, cooldown.isActive()));
            }
        }

        if (changed) {
            state.getAbilityCooldowns().clear();
            state.getAbilityCooldowns().addAll(updated);
            ToggleableTool.writeToolState(stack, state);
        }
    }

    private static boolean hasActiveInvulnerability(EntityPlayer player) {
        for (ItemStack stack : equippedStacks(player)) {
            if (hasActiveCooldown(stack, Ability.INVULNERABILITY)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasActiveCooldown(ItemStack stack, Ability ability) {
        if (stack == null || stack.isEmpty() || ability == null) {
            return false;
        }

        ToolState state = ToggleableTool.readToolState(stack);
        for (AbilityCooldown cooldown : state.getAbilityCooldowns()) {
            if (ability.getId().equals(cooldown.getAbilityId()) && cooldown.isActive() && cooldown.getRemainingTicks() > 0) {
                return true;
            }
        }
        return false;
    }

    private static List<ItemStack> inventoryStacks(EntityPlayer player) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.addAll(player.inventory.mainInventory);
        stacks.addAll(player.inventory.armorInventory);
        stacks.addAll(player.inventory.offHandInventory);
        return stacks;
    }

    private static List<ItemStack> equippedStacks(EntityPlayer player) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.addAll(player.inventory.armorInventory);
        stacks.add(player.getHeldItemMainhand());
        stacks.add(player.getHeldItemOffhand());
        return stacks;
    }
}
