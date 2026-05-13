package com.zzhalex.justdirethings.coremod.hooks;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.ability.AbilityMethods;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;

public final class ElytraHooks {

    private ElytraHooks() {
    }

    public static boolean canElytraFly(ItemStack stack, EntityLivingBase entity) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() == Items.ELYTRA) {
            return ItemElytra.isUsable(stack);
        }
        return AbilityMethods.canUseAbilityAndDurability(stack, Ability.ELYTRA);
    }

    public static void damageElytra(ItemStack stack, int amount, EntityLivingBase entity) {
        if (stack != null && !stack.isEmpty() && AbilityMethods.canUseAbility(stack, Ability.ELYTRA)) {
            AbilityMethods.damageTool(stack, entity, Ability.ELYTRA);
            return;
        }
        stack.damageItem(amount, entity);
    }
}
