package com.zzhalex.justdirethings.common.tile.base;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public class TileMachineBase extends TileEntity {

    private int direction;
    private int tickSpeed = 20;
    private int operationTicks = -1;
    private UUID ownerUuid;

    private final MachineRedstoneState redstoneState = new MachineRedstoneState();
    private final MachineAreaState areaState = new MachineAreaState();
    private final MachineFilterState filterState = new MachineFilterState();
    private final MachineEnergyState energyState = new MachineEnergyState();
    private final MachineFluidState fluidState = new MachineFluidState();

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = Math.max(0, direction);
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
        this.ownerUuid = ownerUuid;
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
        direction = compound.getInteger("Direction");
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
}
