package com.zzhalex.justdirethings.common.tile.base;

import net.minecraft.nbt.NBTTagCompound;

public class MachineEnergyState {

    private int capacity;
    private int storedEnergy;
    private int maxReceive;
    private int maxExtract;

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = Math.max(0, capacity);
        storedEnergy = Math.min(storedEnergy, this.capacity);
    }

    public int getStoredEnergy() {
        return storedEnergy;
    }

    public void setStoredEnergy(int storedEnergy) {
        this.storedEnergy = Math.max(0, Math.min(capacity, storedEnergy));
    }

    public int getMaxReceive() {
        return maxReceive;
    }

    public void setMaxReceive(int maxReceive) {
        this.maxReceive = Math.max(0, maxReceive);
    }

    public int getMaxExtract() {
        return maxExtract;
    }

    public void setMaxExtract(int maxExtract) {
        this.maxExtract = Math.max(0, maxExtract);
    }

    public NBTTagCompound writeToNbt(NBTTagCompound tag) {
        tag.setInteger("Capacity", capacity);
        tag.setInteger("StoredEnergy", storedEnergy);
        tag.setInteger("MaxReceive", maxReceive);
        tag.setInteger("MaxExtract", maxExtract);
        return tag;
    }

    public void readFromNbt(NBTTagCompound tag) {
        setCapacity(tag.getInteger("Capacity"));
        setStoredEnergy(tag.getInteger("StoredEnergy"));
        setMaxReceive(tag.getInteger("MaxReceive"));
        setMaxExtract(tag.getInteger("MaxExtract"));
    }
}
