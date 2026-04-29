package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.capability.inventory.FilterItemHandler;
import com.zzhalex.justdirethings.common.tile.base.TileAdvancedMachine;
import com.zzhalex.justdirethings.common.tile.base.TileTimedMachineBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class TileBlockPlacer extends TileTimedMachineBase {

    public TileBlockPlacer() {
        super(1);
    }

    @Override
    protected boolean performWork() {
        return MachineActionHelper.placeFirstBlock(getItemHandler(), world, MachineActionHelper.targetPos(this), MachineActionHelper.getFacing(this));
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
        public int getStandardEnergyCost() {
            return 500;
        }

        @Override
        protected boolean performWork() {
            if (!hasEnoughEnergy(getStandardEnergyCost())) {
                return false;
            }
            EnumFacing facing = MachineActionHelper.getFacing(this);
            for (BlockPos targetPos : getAreaPositionsNearestFirst()) {
                if (!MachineActionHelper.canReplace(world, targetPos)) {
                    continue;
                }
                BlockPos supportPos = targetPos.offset(facing);
                if (!matchesBlockFilter(world.getBlockState(supportPos), supportPos)) {
                    continue;
                }
                if (MachineActionHelper.placeFirstBlock(getItemHandler(), world, targetPos, facing)) {
                    consumeEnergy(getStandardEnergyCost(), false);
                    return true;
                }
            }
            return false;
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
