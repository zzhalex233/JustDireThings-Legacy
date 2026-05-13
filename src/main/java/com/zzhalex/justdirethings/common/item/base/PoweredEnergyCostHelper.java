package com.zzhalex.justdirethings.common.item.base;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;

public final class PoweredEnergyCostHelper {

    private PoweredEnergyCostHelper() {
    }

    public static int afterUnbreakingDiscount(ItemStack stack, int amount) {
        if (stack == null || stack.isEmpty() || amount <= 0) {
            return 0;
        }
        int unbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack);
        float reductionFactor = Math.min(1.0F, unbreakingLevel * 0.1F);
        return Math.max(0, (int) (amount - amount * reductionFactor));
    }
}
