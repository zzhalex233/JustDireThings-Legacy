package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.common.tile.base.TileTimedMachineBase;
import com.zzhalex.justdirethings.common.util.WorldInteractionRules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class TileFluidCollector extends TileTimedMachineBase {

    public TileFluidCollector() {
        super(1);
        getFluidState().setCapacity(8000);
    }

    @Override
    protected boolean performWork() {
        boolean changed = fillFluidContainer();
        BlockPos targetPos = MachineActionHelper.targetPos(this);
        IBlockState state = world.getBlockState(targetPos);
        Fluid fluid = resolveFluid(state, targetPos);
        if (fluid == null || getFluidState().getAmount() + 1000 > getFluidState().getCapacity()) {
            return changed;
        }
        if (!getFluidState().getFluidName().isEmpty() && !getFluidState().getFluidName().equals(fluid.getName())) {
            return changed;
        }

        if (isInfiniteWaterSource(targetPos, state)) {
            getFluidState().setFluidName(fluid.getName());
            getFluidState().setAmount(getFluidState().getAmount() + 1000);
            return true;
        }

        if (state.getBlock() instanceof IFluidBlock) {
            IFluidBlock fluidBlock = (IFluidBlock) state.getBlock();
            if (fluidBlock.drain(world, targetPos, true) == null) {
                return false;
            }
        } else {
            world.setBlockToAir(targetPos);
        }

        getFluidState().setFluidName(fluid.getName());
        getFluidState().setAmount(getFluidState().getAmount() + 1000);
        return true;
    }

    private boolean fillFluidContainer() {
        if (getFluidState().getAmount() <= 0 || getFluidState().getFluidName().isEmpty()) {
            return false;
        }

        Fluid fluid = FluidRegistry.getFluid(getFluidState().getFluidName());
        if (fluid == null) {
            return false;
        }

        ItemStack stack = getItemHandler().getStackInSlot(0);
        if (stack.isEmpty()) {
            return false;
        }

        IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack);
        if (handler == null) {
            return false;
        }

        FluidStack available = new FluidStack(fluid, getFluidState().getAmount());
        int filled = handler.fill(available, true);
        if (filled <= 0) {
            return false;
        }

        getFluidState().setAmount(Math.max(0, getFluidState().getAmount() - filled));
        if (getFluidState().getAmount() == 0) {
            getFluidState().setFluidName("");
        }
        getItemHandler().setStackInSlot(0, handler.getContainer());
        return true;
    }

    private boolean isInfiniteWaterSource(BlockPos targetPos, IBlockState state) {
        if ((state.getBlock() != Blocks.WATER && state.getBlock() != Blocks.FLOWING_WATER) || state.getValue(BlockLiquid.LEVEL) != 0) {
            return false;
        }

        int sourceNeighbors = 0;
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            IBlockState neighborState = world.getBlockState(targetPos.offset(facing));
            if ((neighborState.getBlock() == Blocks.WATER || neighborState.getBlock() == Blocks.FLOWING_WATER)
                    && neighborState.getValue(BlockLiquid.LEVEL) == 0) {
                sourceNeighbors++;
            }
        }
        return WorldInteractionRules.isInfiniteWaterSource(true, sourceNeighbors);
    }

    private Fluid resolveFluid(IBlockState state, BlockPos targetPos) {
        Block block = state.getBlock();
        if (block == Blocks.WATER && state.getValue(BlockLiquid.LEVEL) == 0) {
            return FluidRegistry.WATER;
        }
        if (block == Blocks.LAVA && state.getValue(BlockLiquid.LEVEL) == 0) {
            return FluidRegistry.LAVA;
        }
        if (block instanceof IFluidBlock) {
            IFluidBlock fluidBlock = (IFluidBlock) block;
            return fluidBlock.canDrain(world, targetPos) ? fluidBlock.getFluid() : null;
        }
        return null;
    }

    public static class T1 extends TileFluidCollector {
    }

    public static class T2 extends TileFluidCollector {
        // PARITY STUB: Upstream FluidCollectorT2BE adds powered area/filter collection.
    }
}
