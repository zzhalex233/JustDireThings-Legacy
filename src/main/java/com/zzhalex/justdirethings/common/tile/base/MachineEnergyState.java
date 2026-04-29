package com.zzhalex.justdirethings.common.tile.base;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.energy.IEnergyStorage;

public class MachineEnergyState implements IEnergyStorage {

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

    public int forceReceiveEnergy(int maxReceive, boolean simulate) {
        if (maxReceive <= 0) {
            return 0;
        }
        int received = Math.min(capacity - storedEnergy, maxReceive);
        if (!simulate) {
            storedEnergy += received;
        }
        return received;
    }

    public void readFromNbt(NBTTagCompound tag) {
        setCapacity(tag.getInteger("Capacity"));
        setStoredEnergy(tag.getInteger("StoredEnergy"));
        setMaxReceive(tag.getInteger("MaxReceive"));
        setMaxExtract(tag.getInteger("MaxExtract"));
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive() || maxReceive <= 0) {
            return 0;
        }
        int received = Math.min(capacity - storedEnergy, Math.min(this.maxReceive, maxReceive));
        if (!simulate) {
            storedEnergy += received;
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract() || maxExtract <= 0) {
            return 0;
        }
        int extracted = Math.min(storedEnergy, Math.min(this.maxExtract, maxExtract));
        if (!simulate) {
            storedEnergy -= extracted;
        }
        return extracted;
    }

    @Override
    public int getEnergyStored() {
        return storedEnergy;
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return maxExtract > 0;
    }

    @Override
    public boolean canReceive() {
        return maxReceive > 0;
    }
}
