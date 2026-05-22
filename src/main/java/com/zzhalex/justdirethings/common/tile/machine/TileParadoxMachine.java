package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.common.entity.EntityParadox;
import com.zzhalex.justdirethings.common.entity.group.JDTEntityGroups;
import com.zzhalex.justdirethings.common.paradox.ParadoxRuntimePlan;
import com.zzhalex.justdirethings.common.paradox.ParadoxSanitizer;
import com.zzhalex.justdirethings.common.paradox.ParadoxSnapshot;
import com.zzhalex.justdirethings.common.paradox.ParadoxSnapshotCapture;
import com.zzhalex.justdirethings.common.recipe.custom.JDTBlockStateSpec;
import com.zzhalex.justdirethings.common.tile.base.MachineRedstoneState;
import com.zzhalex.justdirethings.common.tile.base.TileAdvancedMachine;
import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import com.zzhalex.justdirethings.common.util.OreDetection;
import com.zzhalex.justdirethings.config.JDTConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TileParadoxMachine extends TileMachineBase implements ITickable, TileAdvancedMachine {

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
    private final Map<BlockPos, IBlockState> restoringBlockStates = new LinkedHashMap<>();

    public TileParadoxMachine() {
        getEnergyState().setCapacity(JDTConfig.paradoxRfCapacity);
        getEnergyState().setMaxReceive(JDTConfig.paradoxRfCapacity);
        getEnergyState().setMaxExtract(JDTConfig.paradoxRfCapacity);
        getFluidState().setCapacity(JDTConfig.paradoxFluidCapacity);
        getRedstoneState().setMode(MachineRedstoneState.RedstoneMode.PULSE);
    }

    @Override
    public int getStandardEnergyCost() {
        return JDTConfig.paradoxRfPerBlock;
    }

    @Override
    public void update() {
        if (world == null || pos == null) {
            return;
        }
        if (world.isRemote) {
            if (running) {
                timeRunning++;
                spawnRestoreParticles();
            }
            return;
        }

        handleTicks();
        doParadox();
        if (paradoxEnergy >= getMaxParadoxEnergy()) {
            spawnParadox();
        }
    }

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
        restoringBlockStates.clear();
        markDirtyClient();
    }

    public void setPreviewState(boolean renderParadox, int targetType) {
        this.renderParadox = renderParadox;
        this.targetType = Math.max(0, Math.min(2, targetType));
        if (!running && hasSnapshot()) {
            primeRestoreQueues();
        } else {
            markDirtyClient();
        }
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

    public float getParadoxEnergyPerBlock() {
        return (float) JDTConfig.paradoxEnergyPerBlock;
    }

    public float getParadoxEnergyPerEntity() {
        return (float) JDTConfig.paradoxEnergyPerEntity;
    }

    public float getMaxParadoxEnergy() {
        return (float) JDTConfig.paradoxEnergyMax;
    }

    public void setParadoxEnergy(float paradoxEnergy) {
        this.paradoxEnergy = Math.max(0.0F, Math.min(getMaxParadoxEnergy(), paradoxEnergy));
        markDirtyClient();
    }

    public void addParadoxEnergy(float amount) {
        setParadoxEnergy(paradoxEnergy + Math.max(0.0F, amount));
    }

    public void resetParadoxEnergy() {
        setParadoxEnergy(0.0F);
    }

    public List<BlockPos> getRestoringBlocks() {
        return Collections.unmodifiableList(restoringBlocks);
    }

    public List<Vec3d> getRestoringEntities() {
        return Collections.unmodifiableList(restoringEntities);
    }

    public Map<BlockPos, IBlockState> getRestoringBlockStates() {
        return Collections.unmodifiableMap(restoringBlockStates);
    }

    public ParadoxRuntimePlan buildRuntimePlan() {
        return ParadoxRuntimePlan.fromSnapshot(getPos(), snapshot);
    }

    public int getPreviewBlockCount() {
        return testRestoreBlocks(null).size();
    }

    public int getPreviewEntityCount() {
        return getEntitiesFromSnapshot(true).size();
    }

    public Map<BlockPos, IBlockState> getPreviewBlockStates() {
        if (running) {
            return getRestoringBlockStates();
        }
        return Collections.unmodifiableMap(testRestoreBlocks(null));
    }

    public List<Vec3d> getPreviewEntityPositions() {
        if (running) {
            return getRestoringEntities();
        }
        return Collections.unmodifiableList(new ArrayList<>(getEntitiesFromSnapshot(true).keySet()));
    }

    public Map<Vec3d, EntityLivingBase> getPreviewEntities() {
        Map<Vec3d, EntityLivingBase> entities = getEntitiesFromSnapshot(true);
        if (!running) {
            return Collections.unmodifiableMap(entities);
        }

        Map<Vec3d, EntityLivingBase> runningEntities = new LinkedHashMap<>();
        for (Vec3d restoringEntity : restoringEntities) {
            EntityLivingBase entity = entities.get(restoringEntity);
            if (entity != null) {
                runningEntities.put(restoringEntity, entity);
            }
        }
        return Collections.unmodifiableMap(runningEntities);
    }

    public void primeRestoreQueues() {
        restoringBlocks.clear();
        restoringEntities.clear();
        restoringBlockStates.clear();
        restoringBlockStates.putAll(testRestoreBlocks(null));
        restoringBlocks.addAll(restoringBlockStates.keySet());
        restoringEntities.addAll(getEntitiesFromSnapshot(true).keySet());
    }

    public int getRunTime() {
        if (!running) {
            return 300;
        }
        return ParadoxRuntimePlan.runtimeTicksFor(restoringBlocks.size(), restoringEntities.size());
    }

    public int getEnergyCost(int blocks, int entities) {
        return Math.max(0, blocks) * JDTConfig.paradoxRfPerBlock
                + Math.max(0, entities) * JDTConfig.paradoxRfPerEntity;
    }

    public int getFluidCost(int blocks, int entities) {
        return Math.max(0, blocks) * JDTConfig.paradoxFluidPerBlock
                + Math.max(0, entities) * JDTConfig.paradoxFluidPerEntity;
    }

    public int getEnergyCostPerTick(int totalEnergyCost) {
        return getCostPerTick(totalEnergyCost);
    }

    public int getFluidCostPerTick(int totalFluidCost) {
        return getCostPerTick(totalFluidCost);
    }

    private int getCostPerTick(int totalCost) {
        int runTime = getRunTime();
        return runTime <= 0 ? 0 : (int) Math.floor((double) totalCost / runTime);
    }

    public void snapshotArea() {
        if (world == null || world.isRemote || pos == null) {
            return;
        }

        ParadoxSnapshot nextSnapshot = new ParadoxSnapshot();
        AxisAlignedBB area = getAreaState().createArea(pos);
        for (int x = (int) Math.floor(area.minX); x <= (int) Math.floor(area.maxX - 0.0001D); x++) {
            for (int y = (int) Math.floor(area.minY); y <= (int) Math.floor(area.maxY - 0.0001D); y++) {
                for (int z = (int) Math.floor(area.minZ); z <= (int) Math.floor(area.maxZ - 0.0001D); z++) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    if (isBlockPosValid(blockPos)) {
                        IBlockState state = world.getBlockState(blockPos);
                        ParadoxSnapshotCapture.captureBlock(nextSnapshot, pos, blockPos, JDTBlockStateSpec.fromState(state).writeToNbt());
                    }
                }
            }
        }

        for (EntityLivingBase entity : world.getEntitiesWithinAABB(EntityLivingBase.class, area)) {
            if (isValidEntity(entity)) {
                NBTTagCompound entityData = new NBTTagCompound();
                if (entity.writeToNBTOptional(entityData)) {
                    ParadoxSnapshotCapture.captureEntity(nextSnapshot, pos, new Vec3d(entity.posX, entity.posY, entity.posZ), entityData);
                }
            }
        }

        setSnapshot(nextSnapshot);
    }

    public boolean canConsumeRuntimeTick() {
        return running
                && getEnergyState().extractEnergy(fePerTick, true) == fePerTick
                && getFluidState().getAmount() >= fluidPerTick;
    }

    public boolean consumeRuntimeTick() {
        if (!running) {
            return false;
        }
        if (!canConsumeRuntimeTick()) {
            stopRunning(false);
            return false;
        }

        getEnergyState().extractEnergy(fePerTick, false);
        getFluidState().setAmount(getFluidState().getAmount() - fluidPerTick);
        timeRunning++;
        markDirtyClient();
        return true;
    }

    public void stopRunning(boolean success) {
        if (success && consumeRemainingCosts()) {
            restoreBlocks();
            restoreEntities();
            addParadoxEnergy(getParadoxEnergyPerBlock() * restoringBlocks.size());
            addParadoxEnergy(getParadoxEnergyPerEntity() * restoringEntities.size());
            playSuccessSound();
        } else if (running) {
            playFailureSound();
        }
        running = false;
        timeRunning = 0;
        fePerTick = 0;
        fluidPerTick = 0;
        restoringBlocks.clear();
        restoringEntities.clear();
        restoringBlockStates.clear();
        markDirtyClient();
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
        rebuildRestoringBlockStates();
        if ((restoringBlocks.isEmpty() && restoringEntities.isEmpty()) && hasSnapshot()) {
            primeRestoreQueues();
        }
    }

    private void doParadox() {
        startParadox();
        if (!running) {
            return;
        }

        if (timeRunning < getRunTime() && timeRunning % 100 == 0) {
            playAmbientSound();
        }

        if (!consumeRuntimeTick()) {
            return;
        }

        if (timeRunning >= getRunTime()) {
            stopRunning(true);
        }
    }

    private void startParadox() {
        evaluateRedstoneControl();
        if (!isRedstoneActive() || !canRun() || !canParadox() || paradoxExists()) {
            return;
        }
        if (running) {
            return;
        }

        FakePlayer fakePlayer = world instanceof WorldServer ? getUsefulFakePlayer((WorldServer) world) : null;
        restoringBlockStates.clear();
        restoringBlockStates.putAll(testRestoreBlocks(fakePlayer));
        restoringBlocks.clear();
        restoringBlocks.addAll(restoringBlockStates.keySet());
        restoringEntities.clear();
        restoringEntities.addAll(getEntitiesFromSnapshot(true).keySet());
        if (restoringBlocks.isEmpty() && restoringEntities.isEmpty()) {
            return;
        }

        running = true;
        timeRunning = 0;
        fePerTick = getEnergyCostPerTick(getEnergyCost(restoringBlocks.size(), restoringEntities.size()));
        fluidPerTick = getFluidCostPerTick(getFluidCost(restoringBlocks.size(), restoringEntities.size()));
        playAmbientSound();
        markDirtyClient();
    }

    private boolean canRun() {
        return getOperationTicks() == 0 || getRedstoneState().isPulseMode();
    }

    private boolean canParadox() {
        return hasSnapshot() && getFluidState().getAmount() > 0;
    }

    private boolean consumeRemainingCosts() {
        int runTime = getRunTime();
        int remainingEnergy = getEnergyCost(restoringBlocks.size(), restoringEntities.size()) - fePerTick * runTime;
        int remainingFluid = getFluidCost(restoringBlocks.size(), restoringEntities.size()) - fluidPerTick * runTime;
        if (getEnergyState().extractEnergy(remainingEnergy, true) != remainingEnergy || getFluidState().getAmount() < remainingFluid) {
            playFailureSound();
            return false;
        }
        getEnergyState().extractEnergy(remainingEnergy, false);
        getFluidState().setAmount(getFluidState().getAmount() - remainingFluid);
        return true;
    }

    private void restoreBlocks() {
        if (world == null || world.isRemote) {
            return;
        }
        FakePlayer fakePlayer = world instanceof WorldServer ? getUsefulFakePlayer((WorldServer) world) : null;
        for (Map.Entry<BlockPos, IBlockState> entry : restoringBlockStates.entrySet()) {
            if (canPlace(fakePlayer, entry.getKey())) {
                world.setBlockState(entry.getKey(), entry.getValue(), 3);
            }
        }
    }

    private void restoreEntities() {
        if (world == null || world.isRemote) {
            return;
        }
        Map<Vec3d, EntityLivingBase> entities = getEntitiesFromSnapshot(true);
        for (Vec3d targetPos : restoringEntities) {
            EntityLivingBase entity = entities.get(targetPos);
            if (entity != null) {
                entity.setPositionAndRotation(targetPos.x, targetPos.y, targetPos.z, entity.rotationYaw, entity.rotationPitch);
                if (entity instanceof EntityLiving && world instanceof WorldServer) {
                    ((EntityLiving) entity).onInitialSpawn(world.getDifficultyForLocation(entity.getPosition()), null);
                }
                world.spawnEntity(entity);
            }
        }
    }

    private Map<BlockPos, IBlockState> testRestoreBlocks(FakePlayer fakePlayer) {
        Map<BlockPos, IBlockState> blocks = new LinkedHashMap<>();
        for (Map.Entry<BlockPos, IBlockState> entry : getBlocksFromSnapshot(true).entrySet()) {
            if (canPlace(fakePlayer, entry.getKey())) {
                blocks.put(entry.getKey(), entry.getValue());
            }
        }
        return blocks;
    }

    private Map<BlockPos, IBlockState> getBlocksFromSnapshot(boolean applyTargetType) {
        Map<BlockPos, IBlockState> blocks = new LinkedHashMap<>();
        if ((applyTargetType && targetType == 2) || !hasSnapshot()) {
            return blocks;
        }

        for (ParadoxRuntimePlan.BlockTarget block : buildRuntimePlan().getBlocksToRestore()) {
            IBlockState state = JDTBlockStateSpec.readFromNbt(block.getStateTag()).toBlockState();
            if (state.getBlock() != Blocks.AIR) {
                blocks.put(block.getAbsolutePos(), state);
            }
        }
        return blocks;
    }

    private Map<Vec3d, EntityLivingBase> getEntitiesFromSnapshot(boolean applyTargetType) {
        Map<Vec3d, EntityLivingBase> entities = new LinkedHashMap<>();
        if ((applyTargetType && targetType == 1) || !hasSnapshot() || world == null) {
            return entities;
        }

        for (ParadoxRuntimePlan.EntityTarget target : buildRuntimePlan().getEntitiesToRestore()) {
            Entity entity = createRestoredEntity(target.getEntityData());
            if (!(entity instanceof EntityLivingBase) || isDuplicateEntity(entity)) {
                continue;
            }
            Vec3d absolute = target.getAbsolutePos();
            entity.setPositionAndRotation(absolute.x, absolute.y, absolute.z, entity.rotationYaw, entity.rotationPitch);
            entities.put(absolute, (EntityLivingBase) entity);
        }
        return entities;
    }

    private Entity createRestoredEntity(NBTTagCompound entityData) {
        if (entityData == null || !entityData.hasKey("id")) {
            return null;
        }
        Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(entityData.getString("id")), world);
        if (entity == null) {
            return null;
        }
        entity.readFromNBT(sanitizeEntityData(entityData));
        return entity;
    }

    private NBTTagCompound sanitizeEntityData(NBTTagCompound entityData) {
        if (JDTConfig.paradoxRestrictedMobs) {
            return ParadoxSanitizer.restrictive(entityData);
        }
        return ParadoxSanitizer.denyInventory(entityData);
    }

    private boolean canPlace(FakePlayer fakePlayer, BlockPos blockPos) {
        if (world == null || blockPos == null || !MachineActionHelper.canReplace(world, blockPos)) {
            return false;
        }
        if (fakePlayer == null) {
            return true;
        }
        return world.isBlockModifiable(fakePlayer, blockPos) && MachineActionHelper.canPlaceAt(world, blockPos, fakePlayer);
    }

    private boolean isBlockPosValid(BlockPos blockPos) {
        IBlockState state = world.getBlockState(blockPos);
        return state.getBlock() != Blocks.AIR && OreDetection.isOreBlock(state);
    }

    private boolean isValidEntity(Entity entity) {
        return entity instanceof EntityLivingBase
                && !(entity instanceof EntityPlayer)
                && !(entity instanceof MultiPartEntityPart)
                && !JDTEntityGroups.isTeleportingNotSupported(entity)
                && entity.getParts() == null;
    }

    private boolean isDuplicateEntity(Entity entity) {
        UUID uuid = entity == null ? null : entity.getUniqueID();
        if (uuid == null || world == null || world.isRemote) {
            return false;
        }
        for (Entity existing : world.loadedEntityList) {
            if (existing != null && existing != entity && uuid.equals(existing.getUniqueID())) {
                return true;
            }
        }
        return false;
    }

    private boolean paradoxExists() {
        AxisAlignedBB box = new AxisAlignedBB(getStartingPoint()).grow(1.0D);
        return !world.getEntitiesWithinAABB(EntityParadox.class, box).isEmpty();
    }

    private void spawnParadox() {
        if (world == null || world.isRemote || paradoxExists()) {
            resetParadoxEnergy();
            return;
        }
        BlockPos startingPoint = getStartingPoint();
        EntityParadox paradox = new EntityParadox(world);
        paradox.setPosition(startingPoint.getX() + 0.5D, startingPoint.getY() + 0.5D, startingPoint.getZ() + 0.5D);
        world.spawnEntity(paradox);
        resetParadoxEnergy();
    }

    private BlockPos getStartingPoint() {
        return pos.add(getAreaState().getXOffset(), getAreaState().getYOffset(), getAreaState().getZOffset());
    }

    private void rebuildRestoringBlockStates() {
        restoringBlockStates.clear();
        Map<BlockPos, IBlockState> blocks = getBlocksFromSnapshot(true);
        for (BlockPos restoringBlock : restoringBlocks) {
            IBlockState state = blocks.get(restoringBlock);
            if (state != null) {
                restoringBlockStates.put(restoringBlock, state);
            }
        }
    }

    private void spawnRestoreParticles() {
        for (BlockPos blockPos : restoringBlocks) {
            spawnParticle(blockPos.getX() + world.rand.nextDouble(), blockPos.getY() + world.rand.nextDouble(), blockPos.getZ() + world.rand.nextDouble());
        }
        for (Vec3d entityPos : restoringEntities) {
            spawnParticle(entityPos.x + (world.rand.nextDouble() - 0.5D), entityPos.y + world.rand.nextDouble(), entityPos.z + (world.rand.nextDouble() - 0.5D));
        }
    }

    private void spawnParticle(double x, double y, double z) {
        world.spawnParticle(net.minecraft.util.EnumParticleTypes.VILLAGER_HAPPY, x, y, z, 0.0D, 0.01D, 0.0D);
    }

    private void playAmbientSound() {
        if (world != null) {
            world.playSound(null, pos, SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS, 0.5F, 0.25F);
        }
    }

    private void playSuccessSound() {
        if (world != null) {
            world.playSound(null, pos, SoundEvents.EVOCATION_ILLAGER_PREPARE_SUMMON, SoundCategory.BLOCKS, 0.5F, 0.25F);
        }
    }

    private void playFailureSound() {
        if (world != null) {
            world.playSound(null, pos, SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.BLOCKS, 0.5F, 0.25F);
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
