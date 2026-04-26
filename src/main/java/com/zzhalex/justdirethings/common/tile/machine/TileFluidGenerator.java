package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.capability.inventory.InternalItemHandler;
import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class TileFluidGenerator extends TileMachineBase implements ITickable {

    private static final int MAX_ENERGY = 100000;
    private static final int MAX_FLUID = 4000;

    private final InternalItemHandler itemHandler = new InternalItemHandler(1);

    public TileFluidGenerator() {
        getEnergyState().setCapacity(MAX_ENERGY);
        getEnergyState().setMaxExtract(1000);
        getFluidState().setCapacity(MAX_FLUID);
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }

        absorbFluidContainer();
        evaluateRedstoneControl();
        if (isRedstoneActive()) {
            generateFromFluid();
        }
    }

    public InternalItemHandler getItemHandler() {
        return itemHandler;
    }

    private void absorbFluidContainer() {
        ItemStack stack = itemHandler.getStackInSlot(0);
        if (stack.isEmpty()) {
            return;
        }

        FluidStack contained = FluidUtil.getFluidContained(stack);
        if (contained == null || contained.amount <= 0) {
            return;
        }
        if (!getFluidState().getFluidName().isEmpty() && !getFluidState().getFluidName().equals(contained.getFluid().getName())) {
            return;
        }
        if (getFluidState().getAmount() + contained.amount > getFluidState().getCapacity()) {
            return;
        }

        IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack);
        if (handler == null) {
            return;
        }

        FluidStack drained = handler.drain(contained.amount, true);
        if (drained == null || drained.amount <= 0) {
            return;
        }

        getFluidState().setFluidName(drained.getFluid().getName());
        getFluidState().setAmount(getFluidState().getAmount() + drained.amount);
        itemHandler.setStackInSlot(0, handler.getContainer());
        markDirtyClient();
    }

    private void generateFromFluid() {
        int fePerMb = getFePerMb();
        if (fePerMb <= 0 || getFluidState().getAmount() <= 0) {
            return;
        }

        int inserted = GeneratorMath.energyToInsert(getEnergyState().getStoredEnergy(), getEnergyState().getCapacity(), fePerMb);
        if (inserted <= 0) {
            return;
        }

        getFluidState().setAmount(getFluidState().getAmount() - 1);
        getEnergyState().setStoredEnergy(getEnergyState().getStoredEnergy() + inserted);
        markDirtyClient();
    }

    private int getFePerMb() {
        return FluidRegistry.LAVA != null && FluidRegistry.LAVA.getName().equals(getFluidState().getFluidName()) ? 40 : 0;
    }

    @Override
    public net.minecraft.nbt.NBTTagCompound writeToNBT(net.minecraft.nbt.NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("Items", itemHandler.serializeNBT());
        return compound;
    }

    @Override
    public void readFromNBT(net.minecraft.nbt.NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("Items")) {
            itemHandler.deserializeNBT(compound.getCompoundTag("Items"));
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) itemHandler;
        }
        return super.getCapability(capability, facing);
    }
}
