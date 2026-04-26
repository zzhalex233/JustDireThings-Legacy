package com.zzhalex.justdirethings.common.tile.base;

import net.minecraft.util.ITickable;

public abstract class TileTimedMachineBase extends TileInventoryMachineBase implements ITickable {

    protected TileTimedMachineBase(int slotCount) {
        super(slotCount);
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }
        if (shouldRunTimedMachine() && performWork()) {
            markDirtyClient();
        }
    }

    protected abstract boolean performWork();
}
