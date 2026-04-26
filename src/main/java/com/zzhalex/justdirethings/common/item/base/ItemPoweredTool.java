package com.zzhalex.justdirethings.common.item.base;

import com.zzhalex.justdirethings.capability.item.StackItemCapabilityProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public abstract class ItemPoweredTool extends ItemToggleableTool implements EnergyBackedItem {

    private final int energyCapacity;
    private final int maxReceive;
    private final int maxExtract;
    private final int blockBreakFeCost;

    protected ItemPoweredTool() {
        this(100000, 1000, 1000, 50);
    }

    protected ItemPoweredTool(int energyCapacity, int maxReceive, int maxExtract, int blockBreakFeCost) {
        this.energyCapacity = energyCapacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.blockBreakFeCost = blockBreakFeCost;
    }

    public int getEnergyCapacity() {
        return energyCapacity;
    }

    public int getMaxReceive() {
        return maxReceive;
    }

    public int getMaxExtract() {
        return maxExtract;
    }

    public int getBlockBreakFeCost() {
        return blockBreakFeCost;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new StackItemCapabilityProvider(stack, this, null);
    }

    public int getEnergyCapacity(ItemStack stack) {
        return energyCapacity;
    }

    public int getMaxReceive(ItemStack stack) {
        return maxReceive;
    }

    public int getMaxExtract(ItemStack stack) {
        return maxExtract;
    }

    public boolean hasSufficientPower(int storedEnergy) {
        return storedEnergy >= blockBreakFeCost;
    }
}
