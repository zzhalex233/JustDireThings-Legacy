package com.zzhalex.justdirethings.common.tile.base;

import net.minecraft.nbt.NBTTagCompound;

public class MachineFluidState {

    private String fluidName = "";
    private int amount;
    private int capacity;

    public String getFluidName() {
        return fluidName;
    }

    public void setFluidName(String fluidName) {
        this.fluidName = fluidName == null ? "" : fluidName;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = Math.max(0, Math.min(capacity, amount));
        if (this.amount == 0) {
            fluidName = "";
        }
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = Math.max(0, capacity);
        amount = Math.min(amount, this.capacity);
    }

    public NBTTagCompound writeToNbt(NBTTagCompound tag) {
        tag.setString("FluidName", fluidName);
        tag.setInteger("Amount", amount);
        tag.setInteger("Capacity", capacity);
        return tag;
    }

    public void readFromNbt(NBTTagCompound tag) {
        setCapacity(tag.getInteger("Capacity"));
        setFluidName(tag.getString("FluidName"));
        setAmount(tag.getInteger("Amount"));
    }
}
