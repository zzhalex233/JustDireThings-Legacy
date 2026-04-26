package com.zzhalex.justdirethings.capability.energy;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.energy.EnergyStorage;

public class ItemEnergyStorage extends EnergyStorage {

    private final boolean canReceive;
    private final boolean canExtract;

    public ItemEnergyStorage(int capacity) {
        this(capacity, capacity, capacity, 0, true, true);
    }

    public ItemEnergyStorage(int capacity, int maxTransfer, int energy) {
        this(capacity, maxTransfer, maxTransfer, energy, true, true);
    }

    public ItemEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy, boolean canReceive, boolean canExtract) {
        super(capacity, maxReceive, maxExtract, energy);
        this.canReceive = canReceive;
        this.canExtract = canExtract;
    }

    @Override
    public boolean canExtract() {
        return canExtract;
    }

    @Override
    public boolean canReceive() {
        return canReceive;
    }

    public void setEnergyStored(int energy) {
        this.energy = Math.max(0, Math.min(energy, capacity));
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Energy", energy);
        return tag;
    }

    public void deserializeNBT(NBTTagCompound tag) {
        setEnergyStored(tag.getInteger("Energy"));
    }
}
