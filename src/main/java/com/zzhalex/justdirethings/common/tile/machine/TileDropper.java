package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.common.tile.base.TileTimedMachineBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class TileDropper extends TileTimedMachineBase {

    private int pickupDelay;
    private int dropCount = 1;

    public TileDropper() {
        super(1);
    }

    public int getPickupDelay() {
        return pickupDelay;
    }

    public void setPickupDelay(int pickupDelay) {
        this.pickupDelay = Math.max(0, pickupDelay);
    }

    public int getDropCount() {
        return dropCount;
    }

    public void setDropCount(int dropCount) {
        this.dropCount = Math.max(1, Math.min(64, dropCount));
    }

    @Override
    protected boolean performWork() {
        int slot = MachineActionHelper.findFirstNonEmptySlot(getItemHandler());
        if (slot < 0) {
            return false;
        }
        ItemStack extracted = getItemHandler().extractItem(slot, dropCount, false);
        if (extracted.isEmpty()) {
            return false;
        }
        EnumFacing facing = MachineActionHelper.getFacing(this);
        MachineActionHelper.spawnStack(world, MachineActionHelper.targetPos(this), facing, extracted, pickupDelay);
        return true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("PickupDelay", pickupDelay);
        compound.setInteger("DropCount", dropCount);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        pickupDelay = compound.getInteger("PickupDelay");
        dropCount = compound.hasKey("DropCount") ? Math.max(1, compound.getInteger("DropCount")) : 1;
    }

    public static class T1 extends TileDropper {
    }

    public static class T2 extends TileDropper {
        // PARITY STUB: Upstream DropperT2BE adds powered area/filter dropping.
    }
}
