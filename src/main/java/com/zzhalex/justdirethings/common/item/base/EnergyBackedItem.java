package com.zzhalex.justdirethings.common.item.base;

import net.minecraft.item.ItemStack;

public interface EnergyBackedItem {

    int getStoredEnergy(ItemStack stack);

    void setStoredEnergy(ItemStack stack, int storedEnergy);

    int getEnergyCapacity(ItemStack stack);

    int getMaxReceive(ItemStack stack);

    int getMaxExtract(ItemStack stack);
}
