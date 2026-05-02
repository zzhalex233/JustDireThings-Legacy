package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.capability.inventory.FilterItemHandler;
import com.zzhalex.justdirethings.common.tile.base.TileAdvancedMachine;
import com.zzhalex.justdirethings.common.tile.base.TileTimedMachineBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ITickable;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

import java.util.ArrayList;
import java.util.List;

public class TileBlockPlacer extends TileTimedMachineBase implements ITickable {

    private final List<BlockPos> positionsToPlace = new ArrayList<>();

    public TileBlockPlacer() {
        super(1);
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }

        handleTicks();
        evaluateRedstoneControl();

        boolean activeRedstone = isRedstoneActive();
        FakePlayer fakePlayer = MachineActionHelper.createFakePlayer((WorldServer) world, this);
        boolean changed = doBlockPlace(fakePlayer, activeRedstone);
        if (changed) {
            markDirtyClient();
        }
    }

    @Override
    protected boolean performWork() {
        if (world == null || world.isRemote) {
            return false;
        }
        return doBlockPlace(MachineActionHelper.createFakePlayer((WorldServer) world, this), isRedstoneActive());
    }

    protected boolean doBlockPlace(FakePlayer fakePlayer, boolean activeRedstone) {
        ItemStack placeStack = getPlaceStack();
        if (!isStackValid(placeStack)) {
            getRedstoneState().setPulsed(false);
            return false;
        }
        if (clearTrackerIfNeeded(placeStack, activeRedstone)) {
            positionsToPlace.clear();
            return false;
        }
        if (!canPlace()) {
            return false;
        }

        if (activeRedstone && canRun() && positionsToPlace.isEmpty()) {
            positionsToPlace.addAll(findSpotsToPlace(fakePlayer));
        }
        if (positionsToPlace.isEmpty()) {
            return false;
        }

        if (canRun()) {
            BlockPos blockPos = positionsToPlace.remove(0);
            if (placeBlock(placeStack, fakePlayer, blockPos)) {
                return true;
            }
        }
        return false;
    }

    protected ItemStack getPlaceStack() {
        return getItemHandler().getStackInSlot(0);
    }

    protected boolean isStackValid(ItemStack itemStack) {
        return !itemStack.isEmpty() && itemStack.getItem() instanceof ItemBlock;
    }

    protected boolean canRun() {
        return getOperationTicks() == 0 || getRedstoneState().isPulseMode();
    }

    protected boolean clearTrackerIfNeeded(ItemStack itemStack, boolean activeRedstone) {
        if (positionsToPlace.isEmpty()) {
            return false;
        }
        if (!isStackValid(itemStack)) {
            return true;
        }
        if (!canPlace()) {
            return true;
        }
        return !activeRedstone && !getRedstoneState().isPulseMode();
    }

    protected boolean canPlace() {
        return true;
    }

    protected boolean placeBlock(ItemStack itemStack, FakePlayer fakePlayer, BlockPos blockPos) {
        return MachineActionHelper.useHeldItemOnTarget((WorldServer) world, this, getItemHandler(), 0, blockPos, MachineActionHelper.getFacing(this), MachineActionHelper.canReplace(world, blockPos));
    }

    protected boolean isBlockPosValid(FakePlayer fakePlayer, BlockPos blockPos) {
        if (!world.isBlockModifiable(fakePlayer, blockPos)) {
            return false;
        }
        if (!MachineActionHelper.canReplace(world, blockPos)) {
            return false;
        }
        return true;
    }

    protected List<BlockPos> findSpotsToPlace(FakePlayer fakePlayer) {
        List<BlockPos> returnList = new ArrayList<>();
        BlockPos blockPos = MachineActionHelper.targetPos(this);
        if (isBlockPosValid(fakePlayer, blockPos)) {
            returnList.add(blockPos);
        }
        return returnList;
    }

    public static class T1 extends TileBlockPlacer {
    }

    public static class T2 extends TileBlockPlacer implements TileAdvancedMachine {

        private final FilterItemHandler filterHandler = new FilterItemHandler(ADVANCED_FILTER_SLOTS);

        public T2() {
            configureAdvancedMachine();
        }

        @Override
        public FilterItemHandler getFilterHandler() {
            return filterHandler;
        }

        @Override
        protected boolean canPlace() {
            return hasEnoughEnergy(getStandardEnergyCost());
        }

        @Override
        protected boolean placeBlock(ItemStack itemStack, FakePlayer fakePlayer, BlockPos blockPos) {
            if (!super.placeBlock(itemStack, fakePlayer, blockPos)) {
                return false;
            }
            consumeEnergy(getStandardEnergyCost(), false);
            return true;
        }

        @Override
        protected List<BlockPos> findSpotsToPlace(FakePlayer fakePlayer) {
            List<BlockPos> returnList = new ArrayList<>();
            EnumFacing facing = MachineActionHelper.getFacing(this);
            for (BlockPos targetPos : getAreaPositionsNearestFirst()) {
                if (!MachineActionHelper.canReplace(world, targetPos)) {
                    continue;
                }
                BlockPos supportPos = targetPos.offset(facing);
                if (!matchesBlockFilter(world.getBlockState(supportPos), supportPos)) {
                    continue;
                }
                if (isBlockPosValid(fakePlayer, targetPos)) {
                    returnList.add(targetPos);
                }
            }
            return returnList;
        }

        @Override
        public int getStandardEnergyCost() {
            return 500;
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
