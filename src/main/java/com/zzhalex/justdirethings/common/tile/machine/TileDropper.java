package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.capability.inventory.FilterItemHandler;
import com.zzhalex.justdirethings.common.tile.base.TileAdvancedMachine;
import com.zzhalex.justdirethings.common.tile.base.MachineRedstoneState;
import com.zzhalex.justdirethings.common.tile.base.TileTimedMachineBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class TileDropper extends TileTimedMachineBase {

    private int pickupDelay;
    private int dropCount = 1;
    protected final List<Integer> slotsToDropList = new ArrayList<>();

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
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }
        handleTicks();
        evaluateRedstoneControl();
        if (doDrop()) {
            markDirtyClient();
        }
    }

    protected boolean doDrop() {
        if (clearTrackerIfNeeded()) {
            slotsToDropList.clear();
            return false;
        }
        if (!canDrop()) {
            return false;
        }
        if (isRedstoneActive() && canRun() && slotsToDropList.isEmpty()) {
            populateDropSlots();
        }
        if (slotsToDropList.isEmpty()) {
            return false;
        }
        if (getOperationTicks() != 0) {
            return false;
        }

        int slot = slotsToDropList.remove(0);
        ItemStack dropStack = getItemHandler().getStackInSlot(slot);
        if (dropStack.isEmpty()) {
            slotsToDropList.clear();
            return false;
        }

        BlockPos dropPos = getDropPos();
        if (dropPos == null) {
            return false;
        }

        ItemStack extracted = getItemHandler().extractItem(slot, dropCount, false);
        if (extracted.isEmpty()) {
            slotsToDropList.clear();
            return false;
        }
        spawnItem(extracted, dropPos);
        afterSpawn();
        return true;
    }

    @Override
    protected boolean performWork() {
        return doDrop();
    }

    @Override
    protected boolean canRun() {
        return getOperationTicks() == 0 || getRedstoneState().isPulseMode();
    }

    @Override
    protected boolean isRedstoneActive() {
        boolean active = super.isRedstoneActive();
        if (active && getRedstoneState().isPulseMode() && slotsToDropList.isEmpty()) {
            setOperationTicks(0);
        }
        return active;
    }

    protected boolean clearTrackerIfNeeded() {
        if (slotsToDropList.isEmpty()) {
            return false;
        }
        if (!canDrop()) {
            return true;
        }
        return !getRedstoneState().isPulseMode() && !isRedstoneCurrentlyActive();
    }

    protected void populateDropSlots() {
        for (int slot = 0; slot < getItemHandler().getSlots(); slot++) {
            ItemStack stack = getItemHandler().getStackInSlot(slot);
            if (!stack.isEmpty()) {
                slotsToDropList.add(slot);
                return;
            }
        }
    }

    protected boolean canDrop() {
        return true;
    }

    protected BlockPos getDropPos() {
        return MachineActionHelper.targetPos(this);
    }

    protected EnumFacing getDropDirection() {
        return MachineActionHelper.getFacing(this);
    }

    protected void spawnItem(ItemStack stack, BlockPos dropPos) {
        if (stack.isEmpty() || world == null) {
            return;
        }
        EnumFacing direction = getDropDirection();
        double speed = 0.3D;
        EntityItem entityItem = new EntityItem(
                world,
                dropPos.getX() + 0.5D,
                dropPos.getY() + 0.5D,
                dropPos.getZ() + 0.5D,
                stack.copy()
        );
        entityItem.motionX = direction.getXOffset() * speed;
        entityItem.motionY = direction.getYOffset() * speed;
        entityItem.motionZ = direction.getZOffset() * speed;
        entityItem.setPickupDelay(pickupDelay);
        world.spawnEntity(entityItem);
    }

    protected void afterSpawn() {
    }

    private boolean isRedstoneCurrentlyActive() {
        MachineRedstoneState redstone = getRedstoneState();
        switch (redstone.getMode()) {
            case LOW:
                return !redstone.isReceivingRedstone();
            case HIGH:
                return redstone.isReceivingRedstone();
            case PULSE:
                return redstone.isPulsed();
            case IGNORED:
            default:
                return true;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("pickupDelay", pickupDelay);
        compound.setInteger("dropCount", dropCount);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        setPickupDelay(compound.hasKey("pickupDelay") ? compound.getInteger("pickupDelay") : compound.getInteger("PickupDelay"));
        setDropCount(compound.hasKey("dropCount") ? compound.getInteger("dropCount")
                : compound.hasKey("DropCount") ? compound.getInteger("DropCount") : 1);
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
            return super.performWork();
        }

        @Override
        protected boolean canDrop() {
            return hasEnoughEnergy(getStandardEnergyCost());
        }

        @Override
        protected BlockPos getDropPos() {
            return getOffsetTargetPos();
        }

        @Override
        protected void populateDropSlots() {
            boolean hasFilterStack = false;
            for (int filterSlot = 0; filterSlot < filterHandler.getSlots(); filterSlot++) {
                if (!filterHandler.getStackInSlot(filterSlot).isEmpty()) {
                    hasFilterStack = true;
                    break;
                }
            }
            if (hasFilterStack) {
                populateFilteredDropSlots();
                return;
            }
            super.populateDropSlots();
        }

        protected void populateFilteredDropSlots() {
            for (int filterSlot = 0; filterSlot < filterHandler.getSlots(); filterSlot++) {
                ItemStack filterStack = filterHandler.getStackInSlot(filterSlot);
                if (filterStack.isEmpty()) {
                    continue;
                }
                int matchingMachineSlot = -1;
                for (int machineSlot = 0; machineSlot < getItemHandler().getSlots(); machineSlot++) {
                    ItemStack stack = getItemHandler().getStackInSlot(machineSlot);
                    if (!stack.isEmpty() && matchesFilterStack(filterStack, stack)) {
                        matchingMachineSlot = machineSlot;
                        break;
                    }
                }
                if (matchingMachineSlot < 0) {
                    slotsToDropList.clear();
                    return;
                }
                slotsToDropList.add(matchingMachineSlot);
            }
        }

        @Override
        protected void afterSpawn() {
            consumeEnergy(getStandardEnergyCost(), false);
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
