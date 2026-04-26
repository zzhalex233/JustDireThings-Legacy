package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.capability.inventory.InternalItemHandler;
import com.zzhalex.justdirethings.common.item.misc.FuelCanisterItem;
import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class TileGenerator extends TileMachineBase implements ITickable {

    private static final int MAX_ENERGY = 100000;
    private static final int FE_PER_FUEL_TICK = 10;

    private final InternalItemHandler itemHandler = new InternalItemHandler(1);
    private int burnRemaining;
    private int maxBurn;
    private int fuelBurnMultiplier = 1;

    public TileGenerator() {
        getEnergyState().setCapacity(MAX_ENERGY);
        getEnergyState().setMaxExtract(1000);
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
            int produced = GeneratorMath.energyPerTick(FE_PER_FUEL_TICK, fuelBurnMultiplier);
            int inserted = GeneratorMath.energyToInsert(getEnergyState().getStoredEnergy(), getEnergyState().getCapacity(), produced);
            getEnergyState().setStoredEnergy(getEnergyState().getStoredEnergy() + inserted);
            burnRemaining--;
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

    private void tryStartBurn() {
        ItemStack fuelStack = itemHandler.getStackInSlot(0);
        int burnTime = TileEntityFurnace.getItemBurnTime(fuelStack);
        if (!GeneratorMath.canStartBurn(burnTime, getEnergyState().getStoredEnergy(), getEnergyState().getCapacity())) {
            return;
        }

        fuelBurnMultiplier = fuelStack.getItem() instanceof FuelCanisterItem
                ? FuelCanisterItem.getBurnSpeedMultiplier(fuelStack)
                : 1;
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
