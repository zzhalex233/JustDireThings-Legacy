package com.zzhalex.justdirethings.coremod.hooks;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.ability.AbilityMethods;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public final class NightVisionHooks {

    private NightVisionHooks() {
    }

    public static boolean hasNightVisionAbility(EntityLivingBase entity) {
        if (entity == null) {
            return false;
        }

        ItemStack helmet = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        return AbilityMethods.canUseAbilityAndDurability(helmet, Ability.NIGHTVISION);
    }
}
