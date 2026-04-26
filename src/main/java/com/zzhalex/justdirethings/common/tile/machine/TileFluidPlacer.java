package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.common.tile.base.TileTimedMachineBase;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
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
        if (getFluidState().getAmount() < 1000 || !MachineActionHelper.canReplace(world, targetPos)) {
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

    private boolean absorbFluidContainer() {
        ItemStack stack = getItemHandler().getStackInSlot(0);
        if (stack.isEmpty()) {
            return false;
        }

        FluidStack contained = FluidUtil.getFluidContained(stack);
        if (contained == null || contained.amount <= 0) {
            return false;
        }
        if (!getFluidState().getFluidName().isEmpty() && !getFluidState().getFluidName().equals(contained.getFluid().getName())) {
            return false;
        }
        if (getFluidState().getAmount() + contained.amount > getFluidState().getCapacity()) {
            return false;
        }

        IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack);
        if (handler == null) {
            return false;
        }

        FluidStack drained = handler.drain(contained.amount, true);
        if (drained == null || drained.amount <= 0) {
            return false;
        }

        getFluidState().setFluidName(drained.getFluid().getName());
        getFluidState().setAmount(getFluidState().getAmount() + drained.amount);
        getItemHandler().setStackInSlot(0, handler.getContainer());
        return true;
    }

    private Fluid resolveFluid() {
        Fluid fluid = FluidRegistry.getFluid(getFluidState().getFluidName());
        if (fluid == FluidRegistry.WATER || fluid == FluidRegistry.LAVA) {
            return fluid;
        }
        Block fluidBlock = fluid == null ? null : fluid.getBlock();
        return fluidBlock == null ? null : fluid;
    }

    private IFluidHandler createPlacementFluidSource() {
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
                    getFluidState().setAmount(getFluidState().getAmount() - drained);
                }
                return result;
            }
        };
    }

    private FluidStack currentFluidStack() {
        Fluid fluid = resolveFluid();
        int amount = getFluidState().getAmount();
        return fluid == null || amount <= 0 ? null : new FluidStack(fluid, amount);
    }

    public static class T1 extends TileFluidPlacer {
    }

    public static class T2 extends TileFluidPlacer {
        // PARITY STUB: Upstream FluidPlacerT2BE adds powered area/filter placement.
    }
}
