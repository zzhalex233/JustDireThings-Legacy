package com.zzhalex.justdirethings.common.tile.base;

import net.minecraft.nbt.NBTTagCompound;

public class MachineRedstoneState {

    public enum RedstoneMode {
        IGNORED,
        LOW,
        HIGH,
        PULSE
    }

    private RedstoneMode mode = RedstoneMode.IGNORED;
    private boolean pulsed;
    private boolean receivingRedstone;
    private boolean checkedRedstone;

    public RedstoneMode getMode() {
        return mode;
    }

    public void setMode(RedstoneMode mode) {
        this.mode = mode == null ? RedstoneMode.IGNORED : mode;
    }

    public boolean isPulsed() {
        return pulsed;
    }

    public void setPulsed(boolean pulsed) {
        this.pulsed = pulsed;
    }

    public boolean isReceivingRedstone() {
        return receivingRedstone;
    }

    public void setReceivingRedstone(boolean receivingRedstone) {
        this.receivingRedstone = receivingRedstone;
    }

    public boolean isCheckedRedstone() {
        return checkedRedstone;
    }

    public void setCheckedRedstone(boolean checkedRedstone) {
        this.checkedRedstone = checkedRedstone;
    }

    public void evaluateSignal(boolean newRedstoneSignal) {
        if (mode == RedstoneMode.PULSE && !receivingRedstone && newRedstoneSignal) {
            pulsed = true;
        }
        receivingRedstone = newRedstoneSignal;
        checkedRedstone = true;
    }

    public boolean consumeActiveSignal() {
        switch (mode) {
            case LOW:
                return !receivingRedstone;
            case HIGH:
                return receivingRedstone;
            case PULSE:
                if (pulsed) {
                    pulsed = false;
                    return true;
                }
                return false;
            case IGNORED:
            default:
                return true;
        }
    }

    public boolean isPulseMode() {
        return mode == RedstoneMode.PULSE;
    }

    public NBTTagCompound writeToNbt(NBTTagCompound tag) {
        tag.setInteger("Mode", mode.ordinal());
        tag.setBoolean("Pulsed", pulsed);
        tag.setBoolean("ReceivingRedstone", receivingRedstone);
        tag.setBoolean("CheckedRedstone", checkedRedstone);
        return tag;
    }

    public void readFromNbt(NBTTagCompound tag) {
        int ordinal = tag.getInteger("Mode");
        RedstoneMode[] values = RedstoneMode.values();
        if (ordinal < 0 || ordinal >= values.length) {
            ordinal = 0;
        }
        mode = values[ordinal];
        pulsed = tag.getBoolean("Pulsed");
        receivingRedstone = tag.getBoolean("ReceivingRedstone");
        checkedRedstone = tag.getBoolean("CheckedRedstone");
    }
}
