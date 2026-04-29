package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.capability.inventory.FilterItemHandler;
import com.zzhalex.justdirethings.common.tile.base.TileAdvancedMachine;
import com.zzhalex.justdirethings.common.tile.base.TileTimedMachineBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class TileFluidPlacer extends TileTimedMachineBase {

    public TileFluidPlacer() {
        super(1);
        getFluidState().setCapacity(8000);
    }

    @Override
    protected boolean performWork() {
        boolean changed = absorbFluidContainer();
        BlockPos targetPos = MachineActionHelper.targetPos(this);
        if (getFluidState().getAmount() < 1000 || !canPlaceFluidAt(targetPos)) {
            return changed;
        }

        Fluid fluid = resolveFluid();
        if (fluid == null) {
            return changed;
        }

        FluidStack toPlace = new FluidStack(fluid, 1000);
        if (!FluidUtil.tryPlaceFluid(null, world, targetPos, createPlacementFluidSource(), toPlace)) {
            return changed;
        }

        world.neighborChanged(targetPos, world.getBlockState(targetPos).getBlock(), targetPos);
        return true;
    }

    protected boolean absorbFluidContainer() {
        ItemStack stack = getItemHandler().getStackInSlot(0);
        if (stack.isEmpty()) {
            return false;
        }

        IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack);
        if (handler == null) {
            return false;
        }

        int room = getFluidState().getCapacity() - getFluidState().getAmount();
        if (room <= 0) {
            return false;
        }

        FluidStack contained = handler.drain(Math.min(1000, room), false);
        if (contained == null || contained.amount <= 0) {
            return false;
        }

        if (!getFluidState().getFluidName().isEmpty() && !getFluidState().getFluidName().equals(contained.getFluid().getName())) {
            return false;
        }

        int accepted = Math.min(contained.amount, room);
        FluidStack drained = handler.drain(accepted, true);
        if (drained == null || drained.amount <= 0) {
            return false;
        }

        getFluidState().setFluidName(drained.getFluid().getName());
        getFluidState().setAmount(getFluidState().getAmount() + drained.amount);
        getItemHandler().setStackInSlot(0, handler.getContainer());
        return true;
    }

    protected Fluid resolveFluid() {
        Fluid fluid = FluidRegistry.getFluid(getFluidState().getFluidName());
        if (fluid == FluidRegistry.WATER || fluid == FluidRegistry.LAVA) {
            return fluid;
        }
        Block fluidBlock = fluid == null ? null : fluid.getBlock();
        return fluidBlock == null ? null : fluid;
    }

    protected boolean canPlaceFluidAt(BlockPos targetPos) {
        return MachineActionHelper.canReplace(world, targetPos) && !isSourceFluidAt(targetPos);
    }

    private boolean isSourceFluidAt(BlockPos targetPos) {
        IBlockState state = world.getBlockState(targetPos);
        Block block = state.getBlock();
        if (block instanceof IFluidBlock) {
            return ((IFluidBlock) block).canDrain(world, targetPos);
        }
        return block instanceof BlockLiquid && state.getValue(BlockLiquid.LEVEL) == 0;
    }

    protected IFluidHandler createPlacementFluidSource() {
        return new IFluidHandler() {
            @Override
            public IFluidTankProperties[] getTankProperties() {
                return new IFluidTankProperties[] {
                        new FluidTankProperties(currentFluidStack(), getFluidState().getCapacity(), false, true)
                };
            }

            @Override
            public int fill(FluidStack resource, boolean doFill) {
                return 0;
            }

            @Override
            public FluidStack drain(FluidStack resource, boolean doDrain) {
                if (resource == null) {
                    return null;
                }
                FluidStack available = currentFluidStack();
                if (available == null || !available.isFluidEqual(resource)) {
                    return null;
                }
                return drain(Math.min(resource.amount, available.amount), doDrain);
            }

            @Override
            public FluidStack drain(int maxDrain, boolean doDrain) {
                FluidStack available = currentFluidStack();
                if (available == null || maxDrain <= 0) {
                    return null;
                }
                int drained = Math.min(maxDrain, available.amount);
                FluidStack result = new FluidStack(available.getFluid(), drained);
                if (doDrain) {
                    int newAmount = getFluidState().getAmount() - drained;
                    getFluidState().setAmount(newAmount);
                    if (newAmount <= 0) {
                        getFluidState().setFluidName("");
                    }
                }
                return result;
            }
        };
    }

    protected FluidStack currentFluidStack() {
        Fluid fluid = resolveFluid();
        int amount = getFluidState().getAmount();
        return fluid == null || amount <= 0 ? null : new FluidStack(fluid, amount);
    }

    public static class T1 extends TileFluidPlacer {
    }

    public static class T2 extends TileFluidPlacer implements TileAdvancedMachine {

        private final FilterItemHandler filterHandler = new FilterItemHandler(ADVANCED_FILTER_SLOTS);

        public T2() {
            configureAdvancedMachine();
            getFluidState().setCapacity(32000);
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
            boolean changed = absorbFluidContainer();
            if (getFluidState().getAmount() < 1000 || !hasEnoughEnergy(getStandardEnergyCost())) {
                return changed;
            }

            Fluid fluid = resolveFluid();
            if (fluid == null) {
                return changed;
            }

            EnumFacing facing = MachineActionHelper.getFacing(this);
            for (BlockPos targetPos : getAreaPositionsNearestFirst()) {
                if (!canPlaceFluidAt(targetPos)) {
                    continue;
                }
                BlockPos supportPos = targetPos.offset(facing);
                if (!matchesBlockFilter(world.getBlockState(supportPos), supportPos)) {
                    continue;
                }
                FluidStack toPlace = new FluidStack(fluid, 1000);
                if (FluidUtil.tryPlaceFluid(null, world, targetPos, createPlacementFluidSource(), toPlace)) {
                    consumeEnergy(getStandardEnergyCost(), false);
                    world.neighborChanged(targetPos, world.getBlockState(targetPos).getBlock(), targetPos);
                    return true;
                }
            }
            return changed;
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
