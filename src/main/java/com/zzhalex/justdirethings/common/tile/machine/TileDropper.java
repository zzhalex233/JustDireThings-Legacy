package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.capability.inventory.FilterItemHandler;
import com.zzhalex.justdirethings.common.tile.base.TileAdvancedMachine;
import com.zzhalex.justdirethings.common.tile.base.TileTimedMachineBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class TileDropper extends TileTimedMachineBase {

    private int pickupDelay;
    private int dropCount = 1;

    public TileDropper() {
        this(1);
    }

    protected TileDropper(int slotCount) {
        super(slotCount);
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

    public static class T2 extends TileDropper implements TileAdvancedMachine {

        private final FilterItemHandler filterHandler = new FilterItemHandler(ADVANCED_FILTER_SLOTS);

        public T2() {
            super(9);
            configureAdvancedMachine();
        }

        @Override
        public FilterItemHandler getFilterHandler() {
            return filterHandler;
        }

        @Override
        public int getStandardEnergyCost() {
            return 25;
        }

        @Override
        protected boolean performWork() {
            if (!hasEnoughEnergy(getStandardEnergyCost())) {
                return false;
            }
            int slot = findDropSlot();
            if (slot < 0) {
                return false;
            }
            ItemStack extracted = getItemHandler().extractItem(slot, getDropCount(), false);
            if (extracted.isEmpty()) {
                return false;
            }
            EnumFacing facing = MachineActionHelper.getFacing(this);
            BlockPos dropPos = getOffsetTargetPos();
            MachineActionHelper.spawnStack(world, dropPos, facing, extracted, getPickupDelay());
            consumeEnergy(getStandardEnergyCost(), false);
            return true;
        }

        private int findDropSlot() {
            for (int slot = 0; slot < getItemHandler().getSlots(); slot++) {
                ItemStack stack = getItemHandler().getStackInSlot(slot);
                if (!stack.isEmpty() && matchesFilter(stack)) {
                    return slot;
                }
            }
            return -1;
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound compound) {
            super.writeToNBT(compound);
            return writeAdvancedMachineToNbt(compound);
        }

        @Override
        public void readFromNBT(NBTTagCompound compound) {
            super.readFromNBT(compound);
            readAdvancedMachineFromNbt(compound);
        }
    }
}
