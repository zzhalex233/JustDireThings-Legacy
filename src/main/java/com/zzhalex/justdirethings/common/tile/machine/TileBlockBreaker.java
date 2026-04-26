package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.common.tile.base.TileTimedMachineBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class TileBlockBreaker extends TileTimedMachineBase {

    private boolean sneaking;

    public TileBlockBreaker() {
        super(1);
    }

    public boolean isSneaking() {
        return sneaking;
    }

    public void setSneaking(boolean sneaking) {
        this.sneaking = sneaking;
    }

    @Override
    protected boolean performWork() {
        BlockPos targetPos = MachineActionHelper.targetPos(this);
        EnumFacing facing = MachineActionHelper.getFacing(this);
        return MachineActionHelper.breakBlockIntoInventory(getItemHandler(), world, pos, targetPos, facing);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("Sneaking", sneaking);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        sneaking = compound.getBoolean("Sneaking");
    }

    public static class T1 extends TileBlockBreaker {
    }

    public static class T2 extends TileBlockBreaker {
        // PARITY STUB: Upstream BlockBreakerT2BE adds energy, area scanning, and filtering.
    }
}
