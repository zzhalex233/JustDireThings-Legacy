package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.common.tile.base.TileInventoryMachineBase;
import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class TileEnergyTransmitter extends TileInventoryMachineBase implements ITickable {

    private static final int MAX_TRANSFER_PER_TICK = 200;

    private int lastTransferAmount;
    private boolean showParticles = true;

    public TileEnergyTransmitter() {
        super(1);
        getEnergyState().setCapacity(MAX_TRANSFER_PER_TICK);
        getEnergyState().setMaxReceive(MAX_TRANSFER_PER_TICK);
        getEnergyState().setMaxExtract(MAX_TRANSFER_PER_TICK);
        getAreaState().setArea(2.0D, 2.0D, 2.0D);
    }

    public int getLastTransferAmount() {
        return lastTransferAmount;
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }
        if (!shouldRunTimedMachine()) {
            if (lastTransferAmount != 0) {
                lastTransferAmount = 0;
                getEnergyState().setStoredEnergy(0);
                markDirtyClient();
            }
            return;
        }

        EnumFacing facing = MachineActionHelper.getFacing(this);
        TileMachineBase source = getMachine(pos.offset(facing.getOpposite()));
        TileMachineBase target = getMachine(pos.offset(facing));
        int transferred = 0;
        if (source != null && target != null && source != target) {
            int available = source.getEnergyState().getStoredEnergy();
            int space = target.getEnergyState().getCapacity() - target.getEnergyState().getStoredEnergy();
            transferred = Math.min(MAX_TRANSFER_PER_TICK, Math.min(available, Math.max(0, space)));
            if (transferred > 0) {
                source.getEnergyState().setStoredEnergy(source.getEnergyState().getStoredEnergy() - transferred);
                target.getEnergyState().setStoredEnergy(target.getEnergyState().getStoredEnergy() + transferred);
            }
        }
        transferred += chargeHeldItem(Math.max(0, MAX_TRANSFER_PER_TICK - transferred));

        if (transferred != lastTransferAmount) {
            lastTransferAmount = transferred;
            getEnergyState().setStoredEnergy(transferred);
            markDirtyClient();
        }
    }

    public boolean isShowParticles() {
        return showParticles;
    }

    public void setShowParticles(boolean showParticles) {
        this.showParticles = showParticles;
    }

    private int chargeHeldItem(int budget) {
        if (budget <= 0) {
            return 0;
        }
        ItemStack stack = getItemHandler().getStackInSlot(0);
        if (stack.isEmpty() || !stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            return 0;
        }
        IEnergyStorage storage = stack.getCapability(CapabilityEnergy.ENERGY, null);
        if (storage == null || !storage.canReceive()) {
            return 0;
        }
        return storage.receiveEnergy(budget, false);
    }

    private TileMachineBase getMachine(BlockPos targetPos) {
        TileEntity tileEntity = world.getTileEntity(targetPos);
        return tileEntity instanceof TileMachineBase ? (TileMachineBase) tileEntity : null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("LastTransferAmount", lastTransferAmount);
        compound.setBoolean("ShowParticles", showParticles);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        lastTransferAmount = compound.getInteger("LastTransferAmount");
        showParticles = !compound.hasKey("ShowParticles") || compound.getBoolean("ShowParticles");
        getEnergyState().setStoredEnergy(lastTransferAmount);
    }
}
