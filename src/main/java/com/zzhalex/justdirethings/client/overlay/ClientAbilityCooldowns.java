package com.zzhalex.justdirethings.client.overlay;

import com.zzhalex.justdirethings.data.tool.AbilityCooldown;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public final class ClientAbilityCooldowns {

    private static final Map<EntityEquipmentSlot, List<AbilityCooldown>> COOLDOWNS = new EnumMap<>(EntityEquipmentSlot.class);
    private static int lastClientTick = -1;

    private ClientAbilityCooldowns() {
    }

    public static void replaceAll(Map<EntityEquipmentSlot, List<AbilityCooldown>> cooldowns) {
        COOLDOWNS.clear();
        lastClientTick = -1;
        if (cooldowns == null || cooldowns.isEmpty()) {
            return;
        }
        for (Map.Entry<EntityEquipmentSlot, List<AbilityCooldown>> entry : cooldowns.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && !entry.getValue().isEmpty()) {
                COOLDOWNS.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }
    }

    public static List<AbilityCooldown> getCooldowns(EntityEquipmentSlot slot) {
        List<AbilityCooldown> cooldowns = COOLDOWNS.get(slot);
        return cooldowns == null || cooldowns.isEmpty() ? Collections.emptyList() : cooldowns;
    }

    public static void updateForClientTick(int clientTick) {
        if (lastClientTick < 0) {
            lastClientTick = clientTick;
            return;
        }

        int elapsed = clientTick - lastClientTick;
        if (elapsed <= 0) {
            return;
        }
        lastClientTick = clientTick;

        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            List<AbilityCooldown> cooldowns = COOLDOWNS.get(slot);
            if (cooldowns == null || cooldowns.isEmpty()) {
                continue;
            }

            List<AbilityCooldown> updated = new ArrayList<>();
            for (AbilityCooldown cooldown : cooldowns) {
                int remaining = cooldown.getRemainingTicks() - elapsed;
                if (remaining > 0) {
                    updated.add(new AbilityCooldown(cooldown.getAbilityId(), remaining, cooldown.isActive()));
                }
            }

            if (updated.isEmpty()) {
                COOLDOWNS.remove(slot);
            } else {
                COOLDOWNS.put(slot, updated);
            }
        }
    }
}
