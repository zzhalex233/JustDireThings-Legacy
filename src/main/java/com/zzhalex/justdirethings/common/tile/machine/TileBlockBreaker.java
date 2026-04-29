package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.capability.inventory.FilterItemHandler;
import com.zzhalex.justdirethings.common.tile.base.TileAdvancedMachine;
import com.zzhalex.justdirethings.common.tile.base.TileTimedMachineBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
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

    public static class T2 extends TileBlockBreaker implements TileAdvancedMachine {

        private final FilterItemHandler filterHandler = new FilterItemHandler(ADVANCED_FILTER_SLOTS);

        public T2() {
            configureAdvancedMachine();
            getFilterState().setBlockItemFilter(0);
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
        protected void onServerTick() {
            chargeItemStack(getItemHandler().getStackInSlot(0));
        }

        @Override
        protected boolean performWork() {
            if (!hasEnoughEnergy(getStandardEnergyCost())) {
                return false;
            }

            EnumFacing facing = MachineActionHelper.getFacing(this);
            for (BlockPos targetPos : getAreaPositionsNearestFirst()) {
                IBlockState state = world.getBlockState(targetPos);
                if (state.getBlock() == Blocks.AIR || state.getBlockHardness(world, targetPos) < 0.0F) {
                    continue;
                }
                boolean filterMatches = getFilterState().getBlockItemFilter() == 0
                        ? matchesBlockFilter(state, targetPos)
                        : matchesDropFilter(state, targetPos, 0);
                if (!filterMatches || !hasEnoughEnergy(getStandardEnergyCost())) {
                    continue;
                }
                if (MachineActionHelper.breakBlockIntoInventory(getItemHandler(), world, pos, targetPos, facing)) {
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
