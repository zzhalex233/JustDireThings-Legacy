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
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TileFluidPlacer extends TileTimedMachineBase implements ITickable {

    private final List<BlockPos> positionsToPlace = new ArrayList<>();

    public TileFluidPlacer() {
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

        boolean activeRedstone = isRedstoneActive();
        boolean changed = absorbFluidContainer();
        FakePlayer fakePlayer = MachineActionHelper.createFakePlayer((WorldServer) world, this);
        changed |= doFluidPlace(fakePlayer, activeRedstone);
        if (changed) {
            markDirtyClient();
        }
    }

    @Override
    protected boolean performWork() {
        if (world == null || world.isRemote) {
            return false;
        }
        boolean changed = absorbFluidContainer();
        changed |= doFluidPlace(MachineActionHelper.createFakePlayer((WorldServer) world, this), isRedstoneActive());
        return changed;
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

    protected boolean doFluidPlace(FakePlayer fakePlayer, boolean activeRedstone) {
        FluidStack placeStack = getPlaceStack();
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
            if (placeFluid(placeStack, blockPos)) {
                return true;
            }
        }
        return false;
    }

    protected FluidStack getPlaceStack() {
        return currentFluidStack();
    }

    protected boolean isStackValid(FluidStack fluidStack) {
        return fluidStack != null && fluidStack.amount >= 1000;
    }

    protected boolean canRun() {
        return getOperationTicks() == 0 || getRedstoneState().isPulseMode();
    }

    public boolean canPlace() {
        return true;
    }

    public boolean clearTrackerIfNeeded(FluidStack fluidStack) {
        return clearTrackerIfNeeded(fluidStack, isRedstoneActive());
    }

    protected boolean clearTrackerIfNeeded(FluidStack fluidStack, boolean activeRedstone) {
        if (positionsToPlace.isEmpty()) {
            return false;
        }
        if (!isStackValid(fluidStack)) {
            return true;
        }
        if (!canPlace()) {
            return true;
        }
        return !activeRedstone && !getRedstoneState().isPulseMode();
    }

    protected boolean placeFluid(FluidStack fluidStack, BlockPos blockPos) {
        return FluidUtil.tryPlaceFluid(null, world, blockPos, createPlacementFluidSource(), fluidStack);
    }

    protected List<BlockPos> findSpotsToPlace(FakePlayer fakePlayer) {
        List<BlockPos> returnList = new ArrayList<>();
        BlockPos blockPos = MachineActionHelper.targetPos(this);
        if (isBlockPosValid(blockPos, fakePlayer)) {
            returnList.add(blockPos);
        }
        return returnList;
    }

    public boolean isBlockPosValid(BlockPos blockPos, FakePlayer fakePlayer) {
        return world.isBlockModifiable(fakePlayer, blockPos) && canPlaceFluidAt(blockPos);
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
        public boolean canPlace() {
            return hasEnoughEnergy(getStandardEnergyCost());
        }

        @Override
        protected boolean placeFluid(FluidStack fluidStack, BlockPos blockPos) {
            if (!super.placeFluid(fluidStack, blockPos)) {
                return false;
            }
            consumeEnergy(getStandardEnergyCost(), false);
            world.neighborChanged(blockPos, world.getBlockState(blockPos).getBlock(), blockPos);
            return true;
        }

        @Override
        protected List<BlockPos> findSpotsToPlace(FakePlayer fakePlayer) {
            List<BlockPos> returnList = new ArrayList<>();
            Fluid fluid = resolveFluid();
            if (fluid == null || !matchesFluidFilter(fluid)) {
                return returnList;
            }
            if (getFluidState().getAmount() + 1000 > getFluidState().getCapacity()) {
                return returnList;
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
                if (isBlockPosValid(targetPos, fakePlayer)) {
                    returnList.add(targetPos);
                }
            }
            return returnList;
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
