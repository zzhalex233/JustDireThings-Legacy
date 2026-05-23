package com.zzhalex.justdirethings.common.entity;

import com.zzhalex.justdirethings.common.block.group.JDTBlockGroups;
import com.zzhalex.justdirethings.common.entity.group.JDTEntityGroups;
import com.zzhalex.justdirethings.common.item.group.JDTItemGroups;
import com.zzhalex.justdirethings.registry.ModContentItems;
import com.zzhalex.justdirethings.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EntityParadox extends Entity {

    private static final DataParameter<Integer> REQUIRED_CONSUMPTION = EntityDataManager.createKey(EntityParadox.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> CONSUMPTION = EntityDataManager.createKey(EntityParadox.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> RADIUS = EntityDataManager.createKey(EntityParadox.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> TARGET_RADIUS = EntityDataManager.createKey(EntityParadox.class, DataSerializers.VARINT);
    private static final DataParameter<Float> SHRINK_SCALE = EntityDataManager.createKey(EntityParadox.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> GROWTH_TICKS = EntityDataManager.createKey(EntityParadox.class, DataSerializers.VARINT);

    public int growthDuration = 50;
    public int radiusGrowthTime = 1200;
    public int radiusGrowthTimer;
    public int growthPerBlock = 10;
    public int growthPerItem = 10;

    private final Map<BlockPos, Integer> blocksToAbsorb = new HashMap<>();
    private int maxRadius = 12;
    private double itemSuckSpeed = 0.5D;
    private int maxBlocksForPerf = 40;
    private int maxRadiusGrowthTimer;
    private boolean collapsing;

    public EntityParadox(World worldIn) {
        super(worldIn);
        setSize(1.0F, 1.0F);
        noClip = true;
        ignoreFrustumCheck = true;
        maxRadiusGrowthTimer = radiusGrowthTime * maxRadius + radiusGrowthTime;
    }

    @Override
    protected void entityInit() {
        dataManager.register(REQUIRED_CONSUMPTION, 100);
        dataManager.register(CONSUMPTION, 0);
        dataManager.register(RADIUS, 0);
        dataManager.register(TARGET_RADIUS, 0);
        dataManager.register(SHRINK_SCALE, 1.0F);
        dataManager.register(GROWTH_TICKS, 0);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (world.isRemote) {
            return;
        }

        int currentRadius = getRadius();
        int targetRadius = getTargetRadius();
        incRadiusGrowthTimer(1);

        if (collapsing) {
            float nextScale = Math.max(0.0F, getShrinkScale() - 0.02F);
            setShrinkScale(nextScale);
            if (nextScale <= 0.01F) {
                setDead();
            }
            return;
        }

        if (ticksExisted == 1 || ticksExisted % 600 == 0) {
            world.playSound(null, posX, posY, posZ, ModSounds.PARADOX_AMBIENT, SoundCategory.HOSTILE, 1.0F, 1.0F);
        }

        int calculatedTargetRadius = Math.min(maxRadius, Math.max(0, radiusGrowthTimer / radiusGrowthTime));
        if (calculatedTargetRadius != targetRadius && getGrowthTicks() == 0) {
            setTargetRadius(calculatedTargetRadius);
        }

        if (currentRadius != getTargetRadius()) {
            int growthTicks = getGrowthTicks() + 1;
            setGrowthTicks(growthTicks);
            if (growthTicks >= growthDuration) {
                setRadius(targetRadius);
                setGrowthTicks(0);
            }
        }

        handleBlockAbsorption(currentRadius);
        handleItemAbsorption(currentRadius);
    }

    public void incRadiusGrowthTimer(int value) {
        radiusGrowthTimer = Math.min(maxRadiusGrowthTimer, radiusGrowthTimer + Math.max(0, value));
    }

    public void decRadiusGrowthTimer(int value) {
        radiusGrowthTimer = Math.max(0, radiusGrowthTimer - Math.max(0, value));
    }

    private void handleBlockAbsorption(int currentRadius) {
        BlockPos center = getPosition();
        for (BlockPos mutablePos : BlockPos.getAllInBox(center.add(-currentRadius, -currentRadius, -currentRadius), center.add(currentRadius, currentRadius, currentRadius))) {
            BlockPos blockPos = mutablePos.toImmutable();
            if (isBlockValid(blockPos) && rand.nextFloat() < 0.0125F) {
                blocksToAbsorb.put(blockPos, 40 + rand.nextInt(41));
            }
        }

        Iterator<Map.Entry<BlockPos, Integer>> iterator = blocksToAbsorb.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Integer> entry = iterator.next();
            BlockPos blockPos = entry.getKey();
            if (!isBlockWithinRadius(blockPos) || world.isAirBlock(blockPos)) {
                iterator.remove();
                continue;
            }

            int timeLeft = entry.getValue() - 1;
            spawnAbsorbParticle(blockPos, false);
            if (timeLeft <= 0) {
                int particles = blocksToAbsorb.size() > maxBlocksForPerf * 2 ? 10 : blocksToAbsorb.size() > maxBlocksForPerf ? 20 : 50;
                for (int i = 0; i < particles; i++) {
                    spawnAbsorbParticle(blockPos, true);
                }
                world.setBlockToAir(blockPos);
                incRadiusGrowthTimer(growthPerBlock);
                iterator.remove();
            } else {
                entry.setValue(timeLeft);
            }
        }
    }

    public boolean isBlockWithinRadius(BlockPos blockPos) {
        BlockPos center = getPosition();
        int radius = getTargetRadius();
        AxisAlignedBB box = new AxisAlignedBB(center.add(-radius, -radius, -radius), center.add(radius + 1, radius + 1, radius + 1));
        return box.contains(new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
    }

    private void handleItemsPostShrink(int targetRadius) {
        List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, getEntityBoundingBox().grow(getRadius() + 0.25D));
        List<EntityItem> newItems = world.getEntitiesWithinAABB(EntityItem.class, getEntityBoundingBox().grow(targetRadius + 0.25D));
        for (EntityItem item : items) {
            if (!newItems.contains(item)) {
                item.setNoGravity(false);
            }
        }

        List<EntityLivingBase> livingEntities = world.getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox().grow(getRadius() + 0.25D));
        List<EntityLivingBase> newLivingEntities = world.getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox().grow(targetRadius + 0.25D));
        for (EntityLivingBase livingEntity : livingEntities) {
            if (!newLivingEntities.contains(livingEntity)) {
                livingEntity.setNoGravity(false);
            }
        }
    }

    private boolean isValidItem(EntityItem entity) {
        return entity != null
                && !entity.isDead
                && !entity.getItem().isEmpty()
                && !JDTItemGroups.isParadoxAbsorbDenied(entity.getItem().getItem());
    }

    private boolean isValidEntity(Entity entity) {
        return entity instanceof EntityLivingBase
                && !(entity instanceof EntityPlayer)
                && !(entity instanceof MultiPartEntityPart)
                && entity.getParts() == null
                && !JDTEntityGroups.isParadoxAbsorbDenied(entity)
                && entity != this;
    }

    private void handleItemAbsorption(int currentRadius) {
        List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, getEntityBoundingBox().grow(currentRadius + 0.25D));
        for (EntityItem item : items) {
            if (!isValidItem(item)) {
                continue;
            }
            if (collapsing) {
                break;
            }
            Vec3d direction = currentPosition().subtract(new Vec3d(item.posX, item.posY, item.posZ)).normalize().scale(itemSuckSpeed);
            item.setNoGravity(true);
            item.motionX = direction.x;
            item.motionY = direction.y;
            item.motionZ = direction.z;

            if (currentPosition().squareDistanceTo(new Vec3d(item.posX, item.posY, item.posZ)) < 0.0625D) {
                ItemStack stack = item.getItem();
                Item timeCrystal = ModContentItems.getItem("time_crystal");
                if (timeCrystal != null && stack.getItem() == timeCrystal) {
                    collapse();
                } else {
                    incRadiusGrowthTimer(growthPerItem * stack.getCount());
                }
                item.setDead();
            }
        }
        if (collapsing) {
            return;
        }

        List<EntityLivingBase> livingEntities = world.getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox().grow(currentRadius + 0.25D));
        for (EntityLivingBase livingEntity : livingEntities) {
            if (!isValidEntity(livingEntity)) {
                continue;
            }
            Vec3d direction = currentPosition().subtract(new Vec3d(livingEntity.posX, livingEntity.posY, livingEntity.posZ)).normalize().scale(itemSuckSpeed);
            livingEntity.setNoGravity(true);
            livingEntity.motionX = direction.x;
            livingEntity.motionY = direction.y;
            livingEntity.motionZ = direction.z;

            if (currentPosition().squareDistanceTo(new Vec3d(livingEntity.posX, livingEntity.posY, livingEntity.posZ)) < 0.0625D) {
                livingEntity.setDead();
            }
        }
    }

    public void collapse() {
        if (collapsing) {
            return;
        }
        collapsing = true;
        handleItemsPostShrink(0);
        world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 1.0F, 0.25F);
    }

    public boolean isCollapsing() {
        return collapsing;
    }

    public boolean isBlockValid(BlockPos blockPos) {
        IBlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block == Blocks.AIR) {
            return false;
        }
        if (JDTBlockGroups.isParadoxAbsorbDenied(block) || blocksToAbsorb.containsKey(blockPos)) {
            return false;
        }
        if (blockState.getBlockHardness(world, blockPos) < 0.0F) {
            return false;
        }
        if (block instanceof BlockLiquid) {
            return blockState.getValue(BlockLiquid.LEVEL) == 0;
        }
        if (block instanceof IFluidBlock) {
            return ((IFluidBlock) block).canDrain(world, blockPos);
        }
        return true;
    }

    public int getRadius() {
        return dataManager.get(RADIUS);
    }

    public void setRadius(int radius) {
        dataManager.set(RADIUS, MathHelper.clamp(radius, 0, maxRadius));
    }

    public int getTargetRadius() {
        return dataManager.get(TARGET_RADIUS);
    }

    public void setTargetRadius(int targetRadius) {
        dataManager.set(TARGET_RADIUS, MathHelper.clamp(targetRadius, 0, maxRadius));
        setGrowthTicks(0);
    }

    public float getShrinkScale() {
        return dataManager.get(SHRINK_SCALE);
    }

    public void setShrinkScale(float shrinkScale) {
        dataManager.set(SHRINK_SCALE, MathHelper.clamp(shrinkScale, 0.0F, 1.0F));
    }

    public int getGrowthTicks() {
        return dataManager.get(GROWTH_TICKS);
    }

    public void setGrowthTicks(int growthTicks) {
        dataManager.set(GROWTH_TICKS, Math.max(0, growthTicks));
    }

    public int getRequiredConsumption() {
        return dataManager.get(REQUIRED_CONSUMPTION);
    }

    public void setRequiredConsumption(int totalRequired) {
        dataManager.set(REQUIRED_CONSUMPTION, Math.max(0, totalRequired));
    }

    public int getConsumed() {
        return dataManager.get(CONSUMPTION);
    }

    public void setConsumption(int consumed) {
        dataManager.set(CONSUMPTION, Math.max(0, consumed));
    }

    public void consume(int amount) {
        setConsumption(getConsumed() + Math.max(0, amount));
    }

    private Vec3d currentPosition() {
        return new Vec3d(posX, posY, posZ);
    }

    private void spawnAbsorbParticle(BlockPos blockPos, boolean burst) {
        if (!(world instanceof WorldServer)) {
            return;
        }
        double x = blockPos.getX() + 0.5D + (rand.nextDouble() - 0.5D);
        double y = blockPos.getY() + 0.5D + (rand.nextDouble() - 0.5D);
        double z = blockPos.getZ() + 0.5D + (rand.nextDouble() - 0.5D);
        ((WorldServer) world).spawnParticle(
                burst ? EnumParticleTypes.SMOKE_LARGE : EnumParticleTypes.PORTAL,
                x,
                y,
                z,
                1,
                0.0D,
                0.0D,
                0.0D,
                0.1D
        );
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        setRequiredConsumption(compound.getInteger("requiredConsumption"));
        setConsumption(compound.getInteger("consumed"));
        setRadius(compound.hasKey("radius") ? compound.getInteger("radius") : compound.getInteger("Radius"));
        setTargetRadius(compound.hasKey("targetRadius") ? compound.getInteger("targetRadius") : compound.getInteger("TargetRadius"));
        setShrinkScale(compound.hasKey("ShrinkScale") ? compound.getFloat("ShrinkScale") : 1.0F);
        setGrowthTicks(compound.hasKey("GrowthTicks") ? compound.getInteger("GrowthTicks") : 0);
        growthDuration = compound.hasKey("GrowthDuration") ? compound.getInteger("GrowthDuration") : 50;
        radiusGrowthTimer = compound.getInteger("radiusGrowthTimer");
        collapsing = compound.getBoolean("Collapsing");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("requiredConsumption", getRequiredConsumption());
        compound.setInteger("consumed", getConsumed());
        compound.setInteger("radius", getRadius());
        compound.setInteger("targetRadius", getTargetRadius());
        compound.setInteger("radiusGrowthTimer", radiusGrowthTimer);
        compound.setFloat("ShrinkScale", getShrinkScale());
        compound.setInteger("GrowthTicks", getGrowthTicks());
        compound.setInteger("GrowthDuration", growthDuration);
        compound.setBoolean("Collapsing", collapsing);
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }
}
