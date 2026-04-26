package com.zzhalex.justdirethings.capability.fluid;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class ItemFluidTank extends FluidTank {

    public ItemFluidTank(int capacity) {
        super(capacity);
    }

    public ItemFluidTank(FluidStack stack, int capacity) {
        super(stack, capacity);
    }

    public NBTTagCompound serializeNBT() {
        return writeToNBT(new NBTTagCompound());
    }

    public void deserializeNBT(NBTTagCompound tag) {
        readFromNBT(tag);
    }
}
