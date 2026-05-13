package com.zzhalex.justdirethings.common.item.base;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public interface EnergyBackedItem {

    int getStoredEnergy(ItemStack stack);

    void setStoredEnergy(ItemStack stack, int storedEnergy);

    int getEnergyCapacity(ItemStack stack);

    int getMaxReceive(ItemStack stack);

    int getMaxExtract(ItemStack stack);

    default boolean isEnergyBarVisible(ItemStack stack) {
        int capacity = getEnergyCapacity(stack);
        return capacity > 0 && getStoredEnergy(stack) < capacity;
    }

    default double getEnergyDurabilityForDisplay(ItemStack stack) {
        int capacity = getEnergyCapacity(stack);
        if (capacity <= 0) {
            return 0.0D;
        }
        int stored = Math.max(0, Math.min(capacity, getStoredEnergy(stack)));
        return 1.0D - (stored / (double) capacity);
    }

    default int getEnergyBarColor(ItemStack stack) {
        int capacity = getEnergyCapacity(stack);
        if (capacity <= 0) {
            return 0xFFFFFF;
        }
        float filled = Math.max(0.0F, Math.min(1.0F, getStoredEnergy(stack) / (float) capacity));
        return MathHelper.hsvToRGB(filled / 3.0F, 1.0F, 1.0F);
    }
}
