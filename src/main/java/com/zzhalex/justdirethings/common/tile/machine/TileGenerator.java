package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.capability.inventory.InternalItemHandler;
import com.zzhalex.justdirethings.common.item.fuel.FuelBurnHelper;
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
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class TileGenerator extends TileMachineBase implements ITickable {

    private final InternalItemHandler itemHandler = new InternalItemHandler(1);
    private int burnRemaining;
    private int maxBurn;
    private int fuelBurnMultiplier = 1;

    public TileGenerator() {
        getEnergyState().setCapacity(JDTConfig.generatorT1MaxFe);
        getEnergyState().setMaxExtract(JDTConfig.generatorT1MaxFe);
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }
        evaluateRedstoneControl();

        boolean redstoneActive = isRedstoneActive();
        if (burnRemaining <= 0 && redstoneActive) {
            tryStartBurn();
        }

        if (burnRemaining > 0) {
            int produced = GeneratorMath.energyPerTick(JDTConfig.generatorT1FePerFuelTick, fuelBurnMultiplier);
            getEnergyState().forceReceiveEnergy(produced, false);
            burnRemaining--;
            markDirtyClient();
        }

        if (providePowerAdjacent() > 0) {
            markDirtyClient();
        }
    }

    public InternalItemHandler getItemHandler() {
        return itemHandler;
    }

    public int getBurnRemaining() {
        return burnRemaining;
    }

    public int getMaxBurn() {
        return maxBurn;
    }

    public int getFePerTick() {
        return GeneratorMath.energyPerTick(JDTConfig.generatorT1FePerFuelTick, fuelBurnMultiplier);
    }

    private void tryStartBurn() {
        ItemStack fuelStack = itemHandler.getStackInSlot(0);
        int burnTime = FuelBurnHelper.getBurnTime(fuelStack);
        if (!GeneratorMath.canStartBurn(burnTime, getEnergyState().getStoredEnergy(), getEnergyState().getCapacity())) {
            return;
        }

        fuelBurnMultiplier = Math.max(1, JDTConfig.generatorT1BurnSpeedMultiplier) * FuelBurnHelper.getBurnSpeedMultiplier(fuelStack);
        burnRemaining = GeneratorMath.burnTicksRemaining(burnTime, fuelBurnMultiplier);
        maxBurn = burnRemaining;

        if (fuelStack.getItem().hasContainerItem(fuelStack)) {
            itemHandler.setStackInSlot(0, fuelStack.getItem().getContainerItem(fuelStack));
        } else {
            fuelStack.shrink(1);
            itemHandler.setStackInSlot(0, fuelStack);
        }

        markDirtyClient();
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
            sent += EnergyTransferHelper.transmitPower(getEnergyState(), receiver, JDTConfig.generatorT1FePerTick * 10);
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
        compound.setInteger("BurnRemaining", burnRemaining);
        compound.setInteger("MaxBurn", maxBurn);
        compound.setInteger("FuelBurnMultiplier", fuelBurnMultiplier);
        return compound;
    }

    @Override
    public void readFromNBT(net.minecraft.nbt.NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("Items")) {
            itemHandler.deserializeNBT(compound.getCompoundTag("Items"));
        }
        burnRemaining = compound.getInteger("BurnRemaining");
        maxBurn = compound.getInteger("MaxBurn");
        fuelBurnMultiplier = Math.max(1, compound.getInteger("FuelBurnMultiplier"));
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
