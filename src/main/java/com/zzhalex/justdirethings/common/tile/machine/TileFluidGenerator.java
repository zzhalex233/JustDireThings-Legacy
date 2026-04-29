package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.capability.inventory.InternalItemHandler;
import com.zzhalex.justdirethings.common.tile.base.EnergyTransferHelper;
import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import com.zzhalex.justdirethings.config.JDTConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class TileFluidGenerator extends TileMachineBase implements ITickable {

    private static final int MAX_FLUID = 4000;

    private final InternalItemHandler itemHandler = new InternalItemHandler(1);

    public TileFluidGenerator() {
        getEnergyState().setCapacity(JDTConfig.generatorFluidT1MaxFe);
        getEnergyState().setMaxExtract(JDTConfig.generatorFluidT1MaxFe);
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
        if (providePowerAdjacent() > 0) {
            markDirtyClient();
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
        if (getFePerFuelTick(contained.getFluid().getName()) <= 0) {
            return;
        }
        if (!getFluidState().getFluidName().isEmpty() && !getFluidState().getFluidName().equals(contained.getFluid().getName())) {
            return;
        }
        int room = getFluidState().getCapacity() - getFluidState().getAmount();
        if (room <= 0) {
            return;
        }

        IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack);
        if (handler == null) {
            return;
        }

        FluidStack drained = handler.drain(Math.min(contained.amount, room), true);
        if (drained == null || drained.amount <= 0) {
            return;
        }

        getFluidState().setFluidName(drained.getFluid().getName());
        getFluidState().setAmount(getFluidState().getAmount() + drained.amount);
        itemHandler.setStackInSlot(0, handler.getContainer());
        markDirtyClient();
    }

    private void generateFromFluid() {
        if (generateOneFluidTick()) {
            markDirtyClient();
        }
    }

    boolean generateOneFluidTick() {
        int fePerMb = getFePerFuelTick(getFluidState().getFluidName());
        if (fePerMb <= 0 || getFluidState().getAmount() <= 0) {
            return false;
        }

        if (getEnergyState().forceReceiveEnergy(fePerMb, true) != fePerMb) {
            return false;
        }

        getEnergyState().forceReceiveEnergy(fePerMb, false);
        getFluidState().setAmount(getFluidState().getAmount() - 1);
        return true;
    }

    int getFePerFuelTick(String fluidName) {
        if ("refined_t2_fluid".equals(fluidName)) {
            return JDTConfig.fuelTier2FePerMb;
        }
        if ("refined_t3_fluid".equals(fluidName)) {
            return JDTConfig.fuelTier3FePerMb;
        }
        if ("refined_t4_fluid".equals(fluidName)) {
            return JDTConfig.fuelTier4FePerMb;
        }
        return 0;
    }

    private int providePowerAdjacent() {
        int sent = 0;
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (getEnergyState().getEnergyStored() <= 0) {
                break;
            }
            IEnergyStorage receiver = getEnergyReceiver(pos.offset(facing), facing.getOpposite());
            if (receiver == null || !receiver.canReceive()) {
                continue;
            }
            sent += EnergyTransferHelper.transmitPower(getEnergyState(), receiver, JDTConfig.generatorFluidT1FePerTick * 10);
        }
        return sent;
    }

    @Nullable
    private IEnergyStorage getEnergyReceiver(BlockPos targetPos, EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(targetPos);
        if (tileEntity == null || !tileEntity.hasCapability(CapabilityEnergy.ENERGY, side)) {
            return null;
        }
        return tileEntity.getCapability(CapabilityEnergy.ENERGY, side);
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
