package com.zzhalex.justdirethings.capability.item;

import com.zzhalex.justdirethings.common.item.base.EnergyBackedItem;
import com.zzhalex.justdirethings.common.item.base.FluidBackedItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class StackItemCapabilityProvider implements ICapabilityProvider {

    private final IEnergyStorage energyStorage;
    private final IFluidHandlerItem fluidHandler;

    public StackItemCapabilityProvider(ItemStack stack, @Nullable EnergyBackedItem energyItem, @Nullable FluidBackedItem fluidItem) {
        this.energyStorage = energyItem == null ? null : new StackEnergyStorage(stack, energyItem);
        this.fluidHandler = fluidItem == null ? null : new StackFluidHandler(stack, fluidItem);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY && energyStorage != null
                || capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY && fluidHandler != null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return energyStorage == null ? null : (T) energyStorage;
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY) {
            return fluidHandler == null ? null : (T) fluidHandler;
        }
        return null;
    }

    private static final class StackEnergyStorage implements IEnergyStorage {

        private final ItemStack stack;
        private final EnergyBackedItem item;

        private StackEnergyStorage(ItemStack stack, EnergyBackedItem item) {
            this.stack = stack;
            this.item = item;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!canReceive() || maxReceive <= 0) {
                return 0;
            }

            int stored = item.getStoredEnergy(stack);
            int accepted = Math.min(item.getEnergyCapacity(stack) - stored, Math.min(item.getMaxReceive(stack), maxReceive));
            if (!simulate && accepted > 0) {
                item.setStoredEnergy(stack, stored + accepted);
            }
            return accepted;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (!canExtract() || maxExtract <= 0) {
                return 0;
            }

            int stored = item.getStoredEnergy(stack);
            int extracted = Math.min(stored, Math.min(item.getMaxExtract(stack), maxExtract));
            if (!simulate && extracted > 0) {
                item.setStoredEnergy(stack, stored - extracted);
            }
            return extracted;
        }

        @Override
        public int getEnergyStored() {
            return item.getStoredEnergy(stack);
        }

        @Override
        public int getMaxEnergyStored() {
            return item.getEnergyCapacity(stack);
        }

        @Override
        public boolean canExtract() {
            return item.getMaxExtract(stack) > 0;
        }

        @Override
        public boolean canReceive() {
            return item.getMaxReceive(stack) > 0;
        }
    }

    private static final class StackFluidHandler implements IFluidHandlerItem {

        private final ItemStack stack;
        private final FluidBackedItem item;

        private StackFluidHandler(ItemStack stack, FluidBackedItem item) {
            this.stack = stack;
            this.item = item;
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            Fluid fluid = item.getContainedFluid(stack);
            FluidStack fluidStack = fluid == null || item.getStoredFluid(stack) <= 0
                    ? null
                    : new FluidStack(fluid, item.getStoredFluid(stack));
            return new IFluidTankProperties[] {
                    new FluidTankProperties(fluidStack, item.getFluidCapacity(stack), true, true)
            };
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (resource == null || resource.amount <= 0 || !item.canFillFluid(stack, resource)) {
                return 0;
            }

            int stored = item.getStoredFluid(stack);
            int accepted = Math.min(item.getFluidCapacity(stack) - stored, resource.amount);
            if (doFill && accepted > 0) {
                item.applyFilledAmount(stack, resource, stored + accepted);
            }
            return accepted;
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || resource.amount <= 0) {
                return null;
            }

            Fluid contained = item.getContainedFluid(stack);
            if (contained == null || contained != resource.getFluid()) {
                return null;
            }
            return drain(resource.amount, doDrain);
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (maxDrain <= 0) {
                return null;
            }

            Fluid contained = item.getContainedFluid(stack);
            int stored = item.getStoredFluid(stack);
            if (contained == null || stored <= 0) {
                return null;
            }

            int drained = Math.min(maxDrain, stored);
            if (doDrain) {
                item.setStoredFluid(stack, stored - drained);
            }
            return new FluidStack(contained, drained);
        }

        @Override
        public ItemStack getContainer() {
            return stack;
        }
    }
}
