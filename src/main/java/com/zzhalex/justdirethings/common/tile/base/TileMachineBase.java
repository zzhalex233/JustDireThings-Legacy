package com.zzhalex.justdirethings.common.tile.base;

import com.mojang.authlib.GameProfile;
import com.zzhalex.justdirethings.common.util.UsefulFakePlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.UUID;

public class TileMachineBase extends TileEntity {

    private int direction;
    private int tickSpeed = 20;
    private int operationTicks = -1;
    private UUID ownerUuid;
    private UsefulFakePlayer usefulFakePlayer;

    private final MachineRedstoneState redstoneState = new MachineRedstoneState();
    private final MachineAreaState areaState = new MachineAreaState();
    private final MachineFilterState filterState = new MachineFilterState();
    private final MachineEnergyState energyState = new MachineEnergyState();
    private final IEnergyStorage energyCapability = new SyncingEnergyStorage();
    private final MachineFluidState fluidState = new MachineFluidState();
    private final IFluidHandler fluidCapability = new SyncingFluidHandler();

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        int oldDirection = this.direction;
        this.direction = Math.max(0, direction);
        rotateDefaultAreaWithFacing(oldDirection, this.direction);
    }

    public int getTickSpeed() {
        return tickSpeed;
    }

    public void setTickSpeed(int tickSpeed) {
        this.tickSpeed = Math.max(1, tickSpeed);
        if (operationTicks > this.tickSpeed) {
            operationTicks = this.tickSpeed;
        }
    }

    public int getOperationTicks() {
        return operationTicks;
    }

    public void setOperationTicks(int operationTicks) {
        this.operationTicks = operationTicks;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        if (this.ownerUuid == null ? ownerUuid != null : !this.ownerUuid.equals(ownerUuid)) {
            usefulFakePlayer = null;
        }
        this.ownerUuid = ownerUuid;
    }

    public UsefulFakePlayer getUsefulFakePlayer(WorldServer worldServer) {
        if (usefulFakePlayer == null || usefulFakePlayer.getEntityWorld() != worldServer) {
            usefulFakePlayer = UsefulFakePlayer.createPlayer(worldServer, getFakePlayerProfile());
        }
        return usefulFakePlayer;
    }

    protected GameProfile getFakePlayerProfile() {
        UUID profileUuid = ownerUuid == null ? UUID.fromString("127c8dd2-b17e-4a95-82af-7dcbcafc3987") : ownerUuid;
        return new GameProfile(profileUuid, "[JDTMachine]");
    }

    public MachineRedstoneState getRedstoneState() {
        return redstoneState;
    }

    public MachineAreaState getAreaState() {
        return areaState;
    }

    public MachineFilterState getFilterState() {
        return filterState;
    }

    public MachineEnergyState getEnergyState() {
        return energyState;
    }

    public MachineFluidState getFluidState() {
        return fluidState;
    }

    protected boolean isRedstoneActive() {
        return redstoneState.consumeActiveSignal();
    }

    protected void evaluateRedstoneControl() {
        if (world != null && pos != null) {
            redstoneState.evaluateSignal(world.isBlockPowered(pos));
        }
    }

    protected boolean shouldRunTimedMachine() {
        evaluateRedstoneControl();
        if (!redstoneState.isPulseMode() && world.getTotalWorldTime() % Math.max(1, getTickSpeed()) != 0L) {
            return false;
        }
        return isRedstoneActive();
    }

    public void handleTicks() {
        if (operationTicks <= 0) {
            operationTicks = tickSpeed;
        }
        operationTicks--;
    }

    public void markDirtyClient() {
        markDirty();
        if (world != null) {
            World currentWorld = world;
            BlockPos currentPos = pos;
            currentWorld.notifyBlockUpdate(currentPos, currentWorld.getBlockState(currentPos), currentWorld.getBlockState(currentPos), 3);
        }
    }

    public NBTTagCompound writeMachineStateToNbt(NBTTagCompound compound) {
        compound.setInteger("Direction", direction);
        compound.setInteger("TickSpeed", tickSpeed);
        compound.setInteger("OperationTicks", operationTicks);
        if (ownerUuid != null) {
            compound.setString("OwnerUuid", ownerUuid.toString());
        }
        compound.setTag("RedstoneState", redstoneState.writeToNbt(new NBTTagCompound()));
        compound.setTag("AreaState", areaState.writeToNbt(new NBTTagCompound()));
        compound.setTag("FilterState", filterState.writeToNbt(new NBTTagCompound()));
        compound.setTag("EnergyState", energyState.writeToNbt(new NBTTagCompound()));
        compound.setTag("FluidState", fluidState.writeToNbt(new NBTTagCompound()));
        return compound;
    }

    public void readMachineStateFromNbt(NBTTagCompound compound) {
        setDirection(compound.getInteger("Direction"));
        tickSpeed = compound.hasKey("TickSpeed") ? Math.max(1, compound.getInteger("TickSpeed")) : 20;
        operationTicks = compound.hasKey("OperationTicks") ? compound.getInteger("OperationTicks") : -1;
        ownerUuid = compound.hasKey("OwnerUuid") ? UUID.fromString(compound.getString("OwnerUuid")) : null;
        if (compound.hasKey("RedstoneState")) {
            redstoneState.readFromNbt(compound.getCompoundTag("RedstoneState"));
        }
        if (compound.hasKey("AreaState")) {
            areaState.readFromNbt(compound.getCompoundTag("AreaState"));
        }
        if (compound.hasKey("FilterState")) {
            filterState.readFromNbt(compound.getCompoundTag("FilterState"));
        }
        if (compound.hasKey("EnergyState")) {
            energyState.readFromNbt(compound.getCompoundTag("EnergyState"));
        }
        if (compound.hasKey("FluidState")) {
            fluidState.readFromNbt(compound.getCompoundTag("FluidState"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        return writeMachineStateToNbt(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        readMachineStateFromNbt(compound);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable net.minecraft.util.EnumFacing facing) {
        return (capability != null && capability == CapabilityEnergy.ENERGY && energyState.getCapacity() > 0)
                || (capability != null && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && fluidState.getCapacity() > 0)
                || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, @Nullable net.minecraft.util.EnumFacing facing) {
        if (capability != null && capability == CapabilityEnergy.ENERGY && energyState.getCapacity() > 0) {
            return (T) energyCapability;
        }
        if (capability != null && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && fluidState.getCapacity() > 0) {
            return (T) fluidCapability;
        }
        return super.getCapability(capability, facing);
    }

    private final class SyncingEnergyStorage implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = energyState.receiveEnergy(maxReceive, simulate);
            if (received > 0 && !simulate) {
                markDirtyClient();
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = energyState.extractEnergy(maxExtract, simulate);
            if (extracted > 0 && !simulate) {
                markDirtyClient();
            }
            return extracted;
        }

        @Override
        public int getEnergyStored() {
            return energyState.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return energyState.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return energyState.canExtract();
        }

        @Override
        public boolean canReceive() {
            return energyState.canReceive();
        }
    }

    private final class SyncingFluidHandler implements IFluidHandler {
        @Override
        public IFluidTankProperties[] getTankProperties() {
            return new IFluidTankProperties[] {
                    new FluidTankProperties(currentFluidStack(), fluidState.getCapacity(), true, true)
            };
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (resource == null || resource.amount <= 0 || fluidState.getCapacity() <= 0 || resource.getFluid() == null) {
                return 0;
            }
            if (!fluidState.getFluidName().isEmpty() && !fluidState.getFluidName().equals(resource.getFluid().getName())) {
                return 0;
            }

            int accepted = Math.min(fluidState.getCapacity() - fluidState.getAmount(), resource.amount);
            if (accepted <= 0) {
                return 0;
            }

            if (doFill) {
                fluidState.setFluidName(resource.getFluid().getName());
                fluidState.setAmount(fluidState.getAmount() + accepted);
                markDirtyClient();
            }
            return accepted;
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || resource.amount <= 0) {
                return null;
            }
            FluidStack current = currentFluidStack();
            if (current == null || !current.isFluidEqual(resource)) {
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
            FluidStack current = currentFluidStack();
            if (current == null || current.amount <= 0) {
                return null;
            }

            int drained = Math.min(maxDrain, current.amount);
            FluidStack result = new FluidStack(current.getFluid(), drained);
            if (doDrain) {
                int remaining = fluidState.getAmount() - drained;
                fluidState.setAmount(remaining);
                if (remaining <= 0) {
                    fluidState.setFluidName("");
                }
                markDirtyClient();
            }
            return result;
        }
    }

    @Nullable
    private FluidStack currentFluidStack() {
        if (fluidState.getAmount() <= 0 || fluidState.getFluidName().isEmpty()) {
            return null;
        }
        Fluid fluid = FluidRegistry.getFluid(fluidState.getFluidName());
        return fluid == null ? null : new FluidStack(fluid, fluidState.getAmount());
    }

    private void rotateDefaultAreaWithFacing(int oldDirection, int newDirection) {
        if (areaState.getXRadius() != 0.0D || areaState.getYRadius() != 0.0D || areaState.getZRadius() != 0.0D) {
            return;
        }
        net.minecraft.util.EnumFacing oldFacing = net.minecraft.util.EnumFacing.byIndex(oldDirection);
        net.minecraft.util.EnumFacing newFacing = net.minecraft.util.EnumFacing.byIndex(newDirection);
        if (oldFacing == null || newFacing == null) {
            return;
        }
        boolean followsOldFacing = areaState.getXOffset() == oldFacing.getXOffset()
                && areaState.getYOffset() == oldFacing.getYOffset()
                && areaState.getZOffset() == oldFacing.getZOffset();
        boolean unset = areaState.getXOffset() == 0 && areaState.getYOffset() == 0 && areaState.getZOffset() == 0;
        if (followsOldFacing || unset) {
            areaState.setOffset(newFacing.getXOffset(), newFacing.getYOffset(), newFacing.getZOffset());
        }
    }
}
