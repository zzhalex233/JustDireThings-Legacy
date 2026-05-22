package com.zzhalex.justdirethings.common.item.ability;

import com.zzhalex.justdirethings.common.item.base.AbilityParams;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import com.zzhalex.justdirethings.data.tool.AbilityCooldown;
import com.zzhalex.justdirethings.network.JDTNetwork;
import com.zzhalex.justdirethings.network.message.MessageSyncAbilityCooldowns;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class AbilityCooldownTracker {

    private static final Map<ItemStack, List<AbilityCooldown>> COOLDOWNS = new WeakHashMap<>();
    private static final Map<UUID, String> LAST_HUD_SIGNATURES = new HashMap<>();
    private static final EntityEquipmentSlot[] HUD_SLOTS = {
            EntityEquipmentSlot.HEAD,
            EntityEquipmentSlot.CHEST,
            EntityEquipmentSlot.LEGS,
            EntityEquipmentSlot.FEET,
            EntityEquipmentSlot.MAINHAND,
            EntityEquipmentSlot.OFFHAND
    };

    private AbilityCooldownTracker() {
    }

    public static void addCooldown(@Nullable EntityPlayer player, ItemStack stack, Ability ability, int ticks, boolean active) {
        if (stack == null || stack.isEmpty() || ability == null || ticks <= 0) {
            return;
        }

        List<AbilityCooldown> cooldowns = new ArrayList<>(COOLDOWNS.getOrDefault(stack, Collections.emptyList()));
        cooldowns.removeIf(cooldown -> ability.getId().equals(cooldown.getAbilityId()));
        cooldowns.add(new AbilityCooldown(ability.getId(), ticks, active));
        COOLDOWNS.put(stack, cooldowns);
        syncEquippedCooldowns(player, true);
    }

    public static boolean hasCooldown(ItemStack stack, Ability ability) {
        if (stack == null || stack.isEmpty() || ability == null) {
            return false;
        }
        for (AbilityCooldown cooldown : COOLDOWNS.getOrDefault(stack, Collections.emptyList())) {
            if (ability.getId().equals(cooldown.getAbilityId()) && cooldown.getRemainingTicks() > 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasActiveCooldown(ItemStack stack, Ability ability) {
        if (stack == null || stack.isEmpty() || ability == null) {
            return false;
        }
        for (AbilityCooldown cooldown : COOLDOWNS.getOrDefault(stack, Collections.emptyList())) {
            if (ability.getId().equals(cooldown.getAbilityId()) && cooldown.isActive() && cooldown.getRemainingTicks() > 0) {
                return true;
            }
        }
        return false;
    }

    public static void tickCooldowns(ItemStack stack, EntityPlayer player) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        List<AbilityCooldown> current = COOLDOWNS.get(stack);
        if (current == null || current.isEmpty()) {
            return;
        }

        List<AbilityCooldown> updated = new ArrayList<>();
        for (AbilityCooldown cooldown : current) {
            int remaining = cooldown.getRemainingTicks() - 1;
            if (remaining > 0) {
                updated.add(new AbilityCooldown(cooldown.getAbilityId(), remaining, cooldown.isActive()));
                continue;
            }

            if (cooldown.isActive()) {
                Ability ability = Ability.byId(cooldown.getAbilityId());
                int followupTicks = cooldownTicks(stack, ability, false, 0);
                if (followupTicks > 0) {
                    updated.add(new AbilityCooldown(cooldown.getAbilityId(), followupTicks, false));
                    playCooldownTransitionSound(player, true);
                }
                clearCooldownData(stack, ability);
            } else {
                playCooldownTransitionSound(player, false);
            }
        }

        if (updated.isEmpty()) {
            COOLDOWNS.remove(stack);
        } else {
            COOLDOWNS.put(stack, updated);
        }
    }

    public static List<AbilityCooldown> getCooldowns(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Collections.emptyList();
        }
        List<AbilityCooldown> cooldowns = COOLDOWNS.get(stack);
        return cooldowns == null || cooldowns.isEmpty() ? Collections.emptyList() : new ArrayList<>(cooldowns);
    }

    public static void syncEquippedCooldowns(@Nullable EntityPlayer player, boolean force) {
        if (!(player instanceof EntityPlayerMP)) {
            return;
        }

        EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
        List<MessageSyncAbilityCooldowns.Entry> entries = new ArrayList<>();
        StringBuilder signature = new StringBuilder();
        for (EntityEquipmentSlot slot : HUD_SLOTS) {
            ItemStack stack = getEquippedStack(serverPlayer, slot);
            if (!getCooldowns(stack).isEmpty()) {
                signature.append(slot.ordinal()).append('@').append(System.identityHashCode(stack)).append('|');
            }
            for (AbilityCooldown cooldown : getCooldowns(stack)) {
                if (cooldown.getRemainingTicks() <= 0) {
                    continue;
                }
                entries.add(new MessageSyncAbilityCooldowns.Entry(slot, cooldown.getAbilityId(), cooldown.getRemainingTicks(), cooldown.isActive()));
                signature.append(slot.ordinal()).append(':')
                        .append(cooldown.getAbilityId()).append(':')
                        .append(cooldown.isActive()).append('|');
            }
        }

        UUID playerId = serverPlayer.getUniqueID();
        String newSignature = signature.toString();
        String oldSignature = LAST_HUD_SIGNATURES.get(playerId);
        if (!force && newSignature.equals(oldSignature)) {
            return;
        }
        if (newSignature.isEmpty() && oldSignature == null && !force) {
            return;
        }

        JDTNetwork.getChannel().sendTo(new MessageSyncAbilityCooldowns(entries), serverPlayer);
        if (newSignature.isEmpty()) {
            LAST_HUD_SIGNATURES.remove(playerId);
        } else {
            LAST_HUD_SIGNATURES.put(playerId, newSignature);
        }
    }

    public static void forgetPlayer(EntityPlayer player) {
        if (player != null) {
            LAST_HUD_SIGNATURES.remove(player.getUniqueID());
        }
    }

    private static ItemStack getEquippedStack(EntityPlayer player, EntityEquipmentSlot slot) {
        if (slot == EntityEquipmentSlot.MAINHAND) {
            return player.getHeldItemMainhand();
        }
        if (slot == EntityEquipmentSlot.OFFHAND) {
            return player.getHeldItemOffhand();
        }
        return player.getItemStackFromSlot(slot);
    }

    private static int cooldownTicks(ItemStack stack, Ability ability, boolean active, int fallbackTicks) {
        if (stack == null || stack.isEmpty() || ability == null || !(stack.getItem() instanceof ToggleableTool)) {
            return fallbackTicks;
        }
        ToggleableTool tool = (ToggleableTool) stack.getItem();
        AbilityParams params = tool.getAbilityParams(ability);
        int configured = active ? params.activeCooldown : params.cooldown;
        return configured > 0 ? configured : fallbackTicks;
    }

    private static void clearCooldownData(ItemStack stack, Ability ability) {
        if (ability == Ability.STUPEFY) {
            AbilityMethods.clearStupefyTargets(stack);
        }
    }

    private static void playCooldownTransitionSound(EntityPlayer player, boolean activeEnded) {
        if (player == null || player.world == null) {
            return;
        }
        player.world.playSound(
                null,
                player.posX,
                player.posY,
                player.posZ,
                activeEnded ? SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE : SoundEvents.ENTITY_ENDEREYE_DEATH,
                SoundCategory.PLAYERS,
                1.0F,
                1.0F
        );
    }
}
