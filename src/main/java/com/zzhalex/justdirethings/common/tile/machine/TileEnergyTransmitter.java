package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.capability.inventory.FilterItemHandler;
import com.zzhalex.justdirethings.common.tile.base.EnergyTransferHelper;
import com.zzhalex.justdirethings.common.tile.base.MachineFilterHelper;
import com.zzhalex.justdirethings.common.tile.base.TileFilteredMachine;
import com.zzhalex.justdirethings.common.tile.base.TileInventoryMachineBase;
import com.zzhalex.justdirethings.common.item.misc.PocketGeneratorItem;
import com.zzhalex.justdirethings.config.JDTConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TileEnergyTransmitter extends TileInventoryMachineBase implements ITickable, TileFilteredMachine {

    private int lastTransferAmount;
    private boolean showParticles = true;
    private final IEnergyStorage networkEnergyStorage = new NetworkEnergyStorage();
    private final FilterItemHandler filterHandler = new FilterItemHandler(9);

    public TileEnergyTransmitter() {
        super(1);
        getEnergyState().setCapacity(JDTConfig.energyTransmitterT1MaxRf);
        getEnergyState().setMaxReceive(JDTConfig.energyTransmitterT1MaxRf);
        getEnergyState().setMaxExtract(JDTConfig.energyTransmitterT1MaxRf);
        getFilterState().setAllowList(false);
        setTickSpeed(50);
        getAreaState().setOffset(0, 1, 0);
    }

    public int getLastTransferAmount() {
        return lastTransferAmount;
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }
        evaluateRedstoneControl();
        if (!isRedstoneActive()) {
            if (lastTransferAmount != 0) {
                lastTransferAmount = 0;
                markDirtyClient();
            }
            return;
        }

        List<TileEnergyTransmitter> transmitters = getVisibleTransmitters();
        int transferred = drainFromSlot();
        transferred += providePower(getReceiversToCharge(transmitters));
        balanceEnergy(transmitters);

        if (transferred != lastTransferAmount) {
            lastTransferAmount = transferred;
            markDirtyClient();
        }
    }

    public boolean isShowParticles() {
        return showParticles;
    }

    public void setShowParticles(boolean showParticles) {
        this.showParticles = showParticles;
    }

    public FilterItemHandler getFilterHandler() {
        return filterHandler;
    }

    public boolean matchesFilter(ItemStack stack) {
        return MachineFilterHelper.matchesFilter(filterHandler, getFilterState(), stack);
    }

    int drainFromSlot() {
        ItemStack stack = getItemHandler().getStackInSlot(0);
        if (stack.isEmpty() || !stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            return 0;
        }
        IEnergyStorage storage = stack.getCapability(CapabilityEnergy.ENERGY, null);
        if (storage == null || !storage.canExtract()) {
            return 0;
        }
        if (stack.getItem() instanceof PocketGeneratorItem) {
            ((PocketGeneratorItem) stack.getItem()).tryBurn(stack);
        }
        IEnergyStorage networkEnergy = getNetworkEnergyStorage();
        if (networkEnergy.getEnergyStored() >= networkEnergy.getMaxEnergyStored()) {
            return 0;
        }
        return EnergyTransferHelper.transmitPower(storage, networkEnergy, JDTConfig.energyTransmitterT1RfPerTick);
    }

    private List<TileEnergyTransmitter> getVisibleTransmitters() {
        List<TileEnergyTransmitter> transmitters = new ArrayList<>();
        transmitters.add(this);
        if (world == null || pos == null) {
            return transmitters;
        }
        for (BlockPos candidate : getAreaPositionsNearestFirst()) {
            if (candidate.equals(pos)) {
                continue;
            }
            TileEntity tileEntity = world.getTileEntity(candidate);
            if (tileEntity instanceof TileEnergyTransmitter && matchesFilter(candidate)) {
                transmitters.add((TileEnergyTransmitter) tileEntity);
            }
        }
        return transmitters;
    }

    private List<EnergyReceiver> getReceiversToCharge(List<TileEnergyTransmitter> transmitters) {
        List<EnergyReceiver> receivers = new ArrayList<>();
        for (BlockPos candidate : getAreaPositionsNearestFirst()) {
            if (candidate.equals(pos) || isKnownTransmitter(candidate, transmitters)) {
                continue;
            }
            if (!matchesFilter(candidate)) {
                continue;
            }
            IEnergyStorage receiver = getEnergyReceiver(candidate);
            if (receiver != null && receiver.canReceive()) {
                receivers.add(new EnergyReceiver(candidate, receiver));
            }
        }
        return receivers;
    }

    private int providePower(List<EnergyReceiver> receivers) {
        int sent = 0;
        IEnergyStorage networkEnergy = getNetworkEnergyStorage();
        for (EnergyReceiver receiver : receivers) {
            if (networkEnergy.getEnergyStored() <= 0) {
                break;
            }
            sent += EnergyTransferHelper.transmitPowerWithLoss(
                    networkEnergy,
                    receiver.storage,
                    JDTConfig.energyTransmitterT1RfPerTick,
                    manhattanDistance(receiver.pos),
                    JDTConfig.energyTransmitterT1LossPerBlock
            );
        }
        return sent;
    }

    private int manhattanDistance(BlockPos targetPos) {
        return Math.abs(pos.getX() - targetPos.getX())
                + Math.abs(pos.getY() - targetPos.getY())
                + Math.abs(pos.getZ() - targetPos.getZ());
    }

    private void balanceEnergy(List<TileEnergyTransmitter> transmitters) {
        if (transmitters.size() <= 1) {
            return;
        }

        int totalEnergy = 0;
        for (TileEnergyTransmitter transmitter : transmitters) {
            totalEnergy += transmitter.getEnergyState().getEnergyStored();
        }

        int average = totalEnergy / transmitters.size();
        int remainder = totalEnergy % transmitters.size();
        for (int i = 0; i < transmitters.size(); i++) {
            TileEnergyTransmitter transmitter = transmitters.get(i);
            int targetEnergy = average + (i < remainder ? 1 : 0);
            if (transmitter.getEnergyState().getEnergyStored() != targetEnergy) {
                transmitter.getEnergyState().setStoredEnergy(targetEnergy);
                transmitter.markDirtyClient();
            }
        }
    }

    private boolean isKnownTransmitter(BlockPos candidate, List<TileEnergyTransmitter> transmitters) {
        for (TileEnergyTransmitter transmitter : transmitters) {
            if (candidate.equals(transmitter.getPos())) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesFilter(BlockPos candidate) {
        IBlockState blockState = world.getBlockState(candidate);
        ItemStack blockStack = new ItemStack(blockState.getBlock(), 1, blockState.getBlock().getMetaFromState(blockState));
        return matchesFilter(blockStack);
    }

    IEnergyStorage getNetworkEnergyStorage() {
        return networkEnergyStorage;
    }

    int getTotalEnergyStored(List<TileEnergyTransmitter> transmitters) {
        long totalEnergy = 0L;
        for (TileEnergyTransmitter transmitter : transmitters) {
            totalEnergy += transmitter.getEnergyState().getEnergyStored();
            if (totalEnergy >= Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
        }
        return (int) totalEnergy;
    }

    int getTotalMaxEnergyStored(List<TileEnergyTransmitter> transmitters) {
        long totalEnergy = 0L;
        for (TileEnergyTransmitter transmitter : transmitters) {
            totalEnergy += transmitter.getEnergyState().getMaxEnergyStored();
            if (totalEnergy >= Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
        }
        return (int) totalEnergy;
    }

    int distributeEnergyToNetwork(List<TileEnergyTransmitter> transmitters, int maxReceive, boolean simulate) {
        int cappedReceive = Math.min(maxReceive, getEnergyState().getMaxReceive());
        if (cappedReceive <= 0) {
            return 0;
        }
        int received = 0;
        for (TileEnergyTransmitter transmitter : transmitters) {
            if (received >= cappedReceive) {
                break;
            }
            int accepted = transmitter.getEnergyState().receiveEnergy(cappedReceive - received, simulate);
            if (accepted > 0) {
                received += accepted;
                if (!simulate) {
                    transmitter.markDirtyClient();
                }
            }
        }
        return received;
    }

    int extractEnergyFromNetwork(List<TileEnergyTransmitter> transmitters, int maxExtract, boolean simulate) {
        int cappedExtract = Math.min(maxExtract, getEnergyState().getMaxExtract());
        if (cappedExtract <= 0) {
            return 0;
        }
        int extracted = 0;
        for (TileEnergyTransmitter transmitter : transmitters) {
            if (extracted >= cappedExtract) {
                break;
            }
            int removed = transmitter.getEnergyState().extractEnergy(cappedExtract - extracted, simulate);
            if (removed > 0) {
                extracted += removed;
                if (!simulate) {
                    transmitter.markDirtyClient();
                }
            }
        }
        return extracted;
    }

    @Nullable
    private IEnergyStorage getEnergyReceiver(BlockPos targetPos) {
        TileEntity tileEntity = world.getTileEntity(targetPos);
        if (tileEntity == null) {
            return null;
        }
        for (EnumFacing side : EnumFacing.VALUES) {
            if (tileEntity.hasCapability(CapabilityEnergy.ENERGY, side)) {
                IEnergyStorage storage = tileEntity.getCapability(CapabilityEnergy.ENERGY, side);
                if (storage != null && storage.canReceive()) {
                    return storage;
                }
            }
        }
        return null;
    }

    private List<BlockPos> getAreaPositionsNearestFirst() {
        if (pos == null) {
            return new ArrayList<>();
        }
        AxisAlignedBB area = getAreaState().createArea(pos);
        List<BlockPos> positions = new ArrayList<>();
        for (int x = (int) Math.floor(area.minX); x <= (int) Math.floor(area.maxX - 0.0001D); x++) {
            for (int y = (int) Math.floor(area.minY); y <= (int) Math.floor(area.maxY - 0.0001D); y++) {
                for (int z = (int) Math.floor(area.minZ); z <= (int) Math.floor(area.maxZ - 0.0001D); z++) {
                    positions.add(new BlockPos(x, y, z));
                }
            }
        }
        positions.sort(Comparator.comparingDouble(candidate -> candidate.distanceSq(pos)));
        return positions;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("LastTransferAmount", lastTransferAmount);
        compound.setBoolean("ShowParticles", showParticles);
        compound.setTag("Filters", filterHandler.serializeNBT());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        lastTransferAmount = compound.getInteger("LastTransferAmount");
        showParticles = !compound.hasKey("ShowParticles") || compound.getBoolean("ShowParticles");
        if (compound.hasKey("Filters")) {
            filterHandler.deserializeNBT(compound.getCompoundTag("Filters"));
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return (T) getNetworkEnergyStorage();
        }
        return super.getCapability(capability, facing);
    }

    private final class NetworkEnergyStorage implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return distributeEnergyToNetwork(getVisibleTransmitters(), maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return extractEnergyFromNetwork(getVisibleTransmitters(), maxExtract, simulate);
        }

        @Override
        public int getEnergyStored() {
            return getTotalEnergyStored(getVisibleTransmitters());
        }

        @Override
        public int getMaxEnergyStored() {
            return getTotalMaxEnergyStored(getVisibleTransmitters());
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }

    private static final class EnergyReceiver {
        private final BlockPos pos;
        private final IEnergyStorage storage;

        private EnergyReceiver(BlockPos pos, IEnergyStorage storage) {
            this.pos = pos;
            this.storage = storage;
        }
    }
}
