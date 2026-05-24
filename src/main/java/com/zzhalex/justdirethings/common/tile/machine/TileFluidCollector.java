package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.capability.inventory.FilterItemHandler;
import com.zzhalex.justdirethings.common.tile.base.TileAdvancedMachine;
import com.zzhalex.justdirethings.common.tile.base.TileTimedMachineBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.ArrayList;
import java.util.List;

public class TileFluidCollector extends TileTimedMachineBase {

    protected final List<BlockPos> positionsToCollect = new ArrayList<>();

    public TileFluidCollector() {
        super(1);
        getFluidState().setCapacity(8000);
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }
        handleTicks();
        evaluateRedstoneControl();
        boolean changed = fillFluidContainer();
        FakePlayer fakePlayer = MachineActionHelper.createFakePlayer((WorldServer) world, this);
        changed |= doFluidCollect(fakePlayer);
        if (changed) {
            markDirtyClient();
        }
    }

    @Override
    protected boolean performWork() {
        if (world == null || world.isRemote) {
            return false;
        }
        boolean changed = fillFluidContainer();
        changed |= doFluidCollect(MachineActionHelper.createFakePlayer((WorldServer) world, this));
        return changed;
    }

    protected boolean doFluidCollect(FakePlayer fakePlayer) {
        if (clearTrackerIfNeeded()) {
            positionsToCollect.clear();
            return false;
        }
        if (!canCollect()) {
            return false;
        }
        if (isRedstoneActive() && canRun() && positionsToCollect.isEmpty()) {
            positionsToCollect.addAll(findSpotsToCollect(fakePlayer));
        }
        if (positionsToCollect.isEmpty()) {
            return false;
        }
        if (canRun()) {
            BlockPos blockPos = positionsToCollect.remove(0);
            return collectFluid(blockPos);
        }
        return false;
    }

    protected boolean fillFluidContainer() {
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

        FluidStack available = new FluidStack(fluid, Math.min(1000, getFluidState().getAmount()));
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

    protected boolean clearTrackerIfNeeded() {
        if (positionsToCollect.isEmpty()) {
            return false;
        }
        if (!canCollect()) {
            return true;
        }
        return !getRedstoneState().isPulseMode() && !isRedstoneCurrentlyActive();
    }

    protected boolean canCollect() {
        return true;
    }

    protected boolean canRun() {
        return getOperationTicks() == 0 || getRedstoneState().isPulseMode();
    }

    protected Fluid resolveFluid(IBlockState state, BlockPos targetPos) {
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

    protected boolean collectFluid(BlockPos blockPos) {
        IBlockState state = world.getBlockState(blockPos);
        Fluid fluid = resolveFluid(state, blockPos);
        if (fluid == null || !isBlockValidForTank(fluid)) {
            return false;
        }
        if (getFluidState().getAmount() + 1000 > getFluidState().getCapacity()) {
            return false;
        }

        if (state.getBlock() instanceof IFluidBlock) {
            IFluidBlock fluidBlock = (IFluidBlock) state.getBlock();
            if (fluidBlock.drain(world, blockPos, true) == null) {
                return false;
            }
        } else if (!world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 3)) {
            return false;
        }

        getFluidState().setFluidName(fluid.getName());
        getFluidState().setAmount(getFluidState().getAmount() + 1000);
        world.playSound(null, blockPos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
        afterCollectFluid();
        return true;
    }

    protected void afterCollectFluid() {
    }

    protected boolean isBlockValidForTank(Fluid fluid) {
        return getFluidState().getFluidName().isEmpty() || getFluidState().getFluidName().equals(fluid.getName());
    }

    protected boolean isBlockPosValid(BlockPos blockPos, FakePlayer fakePlayer) {
        if (!world.isBlockModifiable(fakePlayer, blockPos)) {
            return false;
        }
        IBlockState state = world.getBlockState(blockPos);
        Fluid fluid = resolveFluid(state, blockPos);
        if (fluid == null || !isBlockValidForTank(fluid)) {
            return false;
        }
        if ((state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.FLOWING_WATER
                || state.getBlock() == Blocks.LAVA || state.getBlock() == Blocks.FLOWING_LAVA)
                && state.getValue(BlockLiquid.LEVEL) != 0) {
            return false;
        }
        return getFluidState().getAmount() + 1000 <= getFluidState().getCapacity()
                && MachineActionHelper.canPlaceAt(world, blockPos, fakePlayer);
    }

    protected List<BlockPos> findSpotsToCollect(FakePlayer fakePlayer) {
        List<BlockPos> returnList = new ArrayList<>();
        BlockPos blockPos = MachineActionHelper.targetPos(this);
        if (isBlockPosValid(blockPos, fakePlayer)) {
            returnList.add(blockPos);
        }
        return returnList;
    }

    protected boolean isRedstoneCurrentlyActive() {
        switch (getRedstoneState().getMode()) {
            case LOW:
                return !getRedstoneState().isReceivingRedstone();
            case HIGH:
                return getRedstoneState().isReceivingRedstone();
            case PULSE:
                return getRedstoneState().isPulsed();
            case IGNORED:
            default:
                return true;
        }
    }

    public static class T1 extends TileFluidCollector {
    }

    public static class T2 extends TileFluidCollector implements TileAdvancedMachine {

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
        protected boolean canCollect() {
            return hasEnoughEnergy(getStandardEnergyCost());
        }

        @Override
        protected List<BlockPos> findSpotsToCollect(FakePlayer fakePlayer) {
            List<BlockPos> returnList = new ArrayList<>();
            for (BlockPos targetPos : getAreaPositionsNearestFirst()) {
                if (isBlockPosValid(targetPos, fakePlayer)) {
                    returnList.add(targetPos);
                }
            }
            return returnList;
        }

        @Override
        protected boolean isBlockPosValid(BlockPos blockPos, FakePlayer fakePlayer) {
            if (!super.isBlockPosValid(blockPos, fakePlayer)) {
                return false;
            }
            Fluid fluid = resolveFluid(world.getBlockState(blockPos), blockPos);
            return fluid != null && matchesFluidFilter(fluid);
        }

        @Override
        protected void afterCollectFluid() {
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
