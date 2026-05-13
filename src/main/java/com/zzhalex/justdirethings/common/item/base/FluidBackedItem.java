package com.zzhalex.justdirethings.common.item.base;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
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

    default boolean isFluidBarVisible(ItemStack stack) {
        int capacity = getFluidCapacity(stack);
        return capacity > 0 && getStoredFluid(stack) < capacity;
    }

    default int getFluidBarWidth(ItemStack stack) {
        int capacity = getFluidCapacity(stack);
        if (capacity <= 0) {
            return 13;
        }
        return Math.min(Math.round(getStoredFluid(stack) * 13.0F / capacity), 13);
    }

    default int getFluidBarColor(ItemStack stack) {
        return MathHelper.hsvToRGB(0.55F, 1.0F, 1.0F);
    }

    default void applyFilledAmount(ItemStack stack, FluidStack resource, int storedFluid) {
        setStoredFluid(stack, storedFluid);
    }
}
