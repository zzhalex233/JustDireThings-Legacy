package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.common.paradox.ParadoxRuntimePlan;
import com.zzhalex.justdirethings.common.paradox.ParadoxSnapshot;
import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TileParadoxMachine extends TileMachineBase {

    private static final String KEY_SNAPSHOT = "ParadoxSnapshot";
    private static final String KEY_RENDER = "RenderParadox";
    private static final String KEY_TARGET_TYPE = "TargetType";
    private static final String KEY_RUNNING = "Running";
    private static final String KEY_TIME_RUNNING = "TimeRunning";
    private static final String KEY_FE_PER_TICK = "FePerTick";
    private static final String KEY_FLUID_PER_TICK = "FluidPerTick";
    private static final String KEY_PARADOX_ENERGY = "ParadoxEnergy";
    private static final String KEY_RESTORING_BLOCKS = "RestoringBlocks";
    private static final String KEY_RESTORING_ENTITIES = "RestoringEntities";
    private static final String KEY_X = "X";
    private static final String KEY_Y = "Y";
    private static final String KEY_Z = "Z";

    private ParadoxSnapshot snapshot = new ParadoxSnapshot();
    private boolean renderParadox;
    private int targetType;
    private boolean running;
    private int timeRunning;
    private int fePerTick;
    private int fluidPerTick;
    private float paradoxEnergy;
    private final List<BlockPos> restoringBlocks = new ArrayList<>();
    private final List<Vec3d> restoringEntities = new ArrayList<>();

    public ParadoxSnapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(ParadoxSnapshot snapshot) {
        this.snapshot = snapshot == null ? new ParadoxSnapshot() : snapshot;
        primeRestoreQueues();
        markDirtyClient();
    }

    public boolean hasSnapshot() {
        return snapshot != null && !snapshot.isEmpty();
    }

    public void clearSnapshot() {
        snapshot = new ParadoxSnapshot();
        restoringBlocks.clear();
        restoringEntities.clear();
        markDirtyClient();
    }

    public void setPreviewState(boolean renderParadox, int targetType) {
        this.renderParadox = renderParadox;
        this.targetType = Math.max(0, targetType);
        markDirtyClient();
    }

    public boolean shouldRenderParadox() {
        return renderParadox;
    }

    public int getTargetType() {
        return targetType;
    }

    public void setRunningState(boolean running, int timeRunning, int fePerTick, int fluidPerTick) {
        this.running = running;
        this.timeRunning = Math.max(0, timeRunning);
        this.fePerTick = Math.max(0, fePerTick);
        this.fluidPerTick = Math.max(0, fluidPerTick);
        markDirtyClient();
    }

    public boolean isRunning() {
        return running;
    }

    public int getTimeRunning() {
        return timeRunning;
    }

    public int getFePerTick() {
        return fePerTick;
    }

    public int getFluidPerTick() {
        return fluidPerTick;
    }

    public float getParadoxEnergy() {
        return paradoxEnergy;
    }

    public void setParadoxEnergy(float paradoxEnergy) {
        this.paradoxEnergy = Math.max(0.0F, paradoxEnergy);
        markDirtyClient();
    }

    public List<BlockPos> getRestoringBlocks() {
        return Collections.unmodifiableList(restoringBlocks);
    }

    public List<Vec3d> getRestoringEntities() {
        return Collections.unmodifiableList(restoringEntities);
    }

    public ParadoxRuntimePlan buildRuntimePlan() {
        return ParadoxRuntimePlan.fromSnapshot(getPos(), snapshot);
    }

    public void primeRestoreQueues() {
        ParadoxRuntimePlan plan = buildRuntimePlan();
        restoringBlocks.clear();
        restoringEntities.clear();
        for (ParadoxRuntimePlan.BlockTarget block : plan.getBlocksToRestore()) {
            restoringBlocks.add(block.getAbsolutePos());
        }
        for (ParadoxRuntimePlan.EntityTarget entity : plan.getEntitiesToRestore()) {
            restoringEntities.add(entity.getAbsolutePos());
        }
    }

    public int getRunTime() {
        return ParadoxRuntimePlan.runtimeTicksFor(restoringBlocks.size(), restoringEntities.size());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag(KEY_SNAPSHOT, snapshot.writeToNbt());
        compound.setBoolean(KEY_RENDER, renderParadox);
        compound.setInteger(KEY_TARGET_TYPE, targetType);
        compound.setBoolean(KEY_RUNNING, running);
        compound.setInteger(KEY_TIME_RUNNING, timeRunning);
        compound.setInteger(KEY_FE_PER_TICK, fePerTick);
        compound.setInteger(KEY_FLUID_PER_TICK, fluidPerTick);
        compound.setFloat(KEY_PARADOX_ENERGY, paradoxEnergy);
        compound.setTag(KEY_RESTORING_BLOCKS, writeBlockPositions(restoringBlocks));
        compound.setTag(KEY_RESTORING_ENTITIES, writeVectors(restoringEntities));
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        snapshot = ParadoxSnapshot.read(compound.getCompoundTag(KEY_SNAPSHOT));
        renderParadox = compound.getBoolean(KEY_RENDER);
        targetType = compound.getInteger(KEY_TARGET_TYPE);
        running = compound.getBoolean(KEY_RUNNING);
        timeRunning = compound.getInteger(KEY_TIME_RUNNING);
        fePerTick = compound.getInteger(KEY_FE_PER_TICK);
        fluidPerTick = compound.getInteger(KEY_FLUID_PER_TICK);
        paradoxEnergy = compound.getFloat(KEY_PARADOX_ENERGY);
        readBlockPositions(compound.getTagList(KEY_RESTORING_BLOCKS, Constants.NBT.TAG_COMPOUND), restoringBlocks);
        readVectors(compound.getTagList(KEY_RESTORING_ENTITIES, Constants.NBT.TAG_COMPOUND), restoringEntities);
        if ((restoringBlocks.isEmpty() && restoringEntities.isEmpty()) && hasSnapshot()) {
            primeRestoreQueues();
        }
    }

    private static NBTTagList writeBlockPositions(List<BlockPos> positions) {
        NBTTagList list = new NBTTagList();
        for (BlockPos pos : positions) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger(KEY_X, pos.getX());
            tag.setInteger(KEY_Y, pos.getY());
            tag.setInteger(KEY_Z, pos.getZ());
            list.appendTag(tag);
        }
        return list;
    }

    private static void readBlockPositions(NBTTagList list, List<BlockPos> output) {
        output.clear();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            output.add(new BlockPos(tag.getInteger(KEY_X), tag.getInteger(KEY_Y), tag.getInteger(KEY_Z)));
        }
    }

    private static NBTTagList writeVectors(List<Vec3d> vectors) {
        NBTTagList list = new NBTTagList();
        for (Vec3d vec : vectors) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setDouble(KEY_X, vec.x);
            tag.setDouble(KEY_Y, vec.y);
            tag.setDouble(KEY_Z, vec.z);
            list.appendTag(tag);
        }
        return list;
    }

    private static void readVectors(NBTTagList list, List<Vec3d> output) {
        output.clear();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            output.add(new Vec3d(tag.getDouble(KEY_X), tag.getDouble(KEY_Y), tag.getDouble(KEY_Z)));
        }
    }
}
