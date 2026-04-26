package com.zzhalex.justdirethings.common.item.base;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public interface FluidBackedItem {

    int getStoredFluid(ItemStack stack);

    void setStoredFluid(ItemStack stack, int storedFluid);

    int getFluidCapacity(ItemStack stack);

    @Nullable
    Fluid getContainedFluid(ItemStack stack);

    boolean canFillFluid(ItemStack stack, FluidStack resource);

    default void applyFilledAmount(ItemStack stack, FluidStack resource, int storedFluid) {
        setStoredFluid(stack, storedFluid);
    }
}
