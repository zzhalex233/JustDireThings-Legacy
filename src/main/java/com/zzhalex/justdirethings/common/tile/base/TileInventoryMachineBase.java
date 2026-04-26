package com.zzhalex.justdirethings.common.tile.base;

import com.zzhalex.justdirethings.capability.inventory.InternalItemHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

public abstract class TileInventoryMachineBase extends TileMachineBase {

    private final InternalItemHandler itemHandler;
    private final boolean exposesItemHandler;

    protected TileInventoryMachineBase(int slotCount) {
        this.itemHandler = new InternalItemHandler(Math.max(0, slotCount));
        this.exposesItemHandler = slotCount > 0;
    }

    public InternalItemHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("Items", itemHandler.serializeNBT());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("Items")) {
            itemHandler.deserializeNBT(compound.getCompoundTag("Items"));
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return exposesItemHandler && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (exposesItemHandler && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) itemHandler;
        }
        return super.getCapability(capability, facing);
    }
}
