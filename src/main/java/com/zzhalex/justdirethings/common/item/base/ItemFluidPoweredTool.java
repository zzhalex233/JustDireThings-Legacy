package com.zzhalex.justdirethings.common.item.base;

import com.zzhalex.justdirethings.capability.item.StackItemCapabilityProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public abstract class ItemFluidPoweredTool extends ItemPoweredTool implements FluidBackedItem {

    private final int fluidCapacity;

    protected ItemFluidPoweredTool() {
        this(100000, 100000, 100000, 50, 4000);
    }

    protected ItemFluidPoweredTool(int energyCapacity, int maxReceive, int maxExtract, int blockBreakFeCost, int fluidCapacity) {
        super(energyCapacity, maxReceive, maxExtract, blockBreakFeCost);
        this.fluidCapacity = fluidCapacity;
    }

    public int getFluidCapacity() {
        return fluidCapacity;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new StackItemCapabilityProvider(stack, this, this);
    }

    @Override
    public int getFluidCapacity(ItemStack stack) {
        return fluidCapacity;
    }
}
