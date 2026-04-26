package com.zzhalex.justdirethings.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

public class EntityParadox extends Entity {

    private static final DataParameter<Integer> RADIUS = EntityDataManager.createKey(EntityParadox.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> TARGET_RADIUS = EntityDataManager.createKey(EntityParadox.class, DataSerializers.VARINT);
    private static final DataParameter<Float> SHRINK_SCALE = EntityDataManager.createKey(EntityParadox.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> GROWTH_TICKS = EntityDataManager.createKey(EntityParadox.class, DataSerializers.VARINT);

    private int growthDuration = 50;
    private boolean collapsing;

    public EntityParadox(World worldIn) {
        super(worldIn);
        setSize(1.0F, 1.0F);
        noClip = true;
        ignoreFrustumCheck = true;
    }

    @Override
    protected void entityInit() {
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

        if (collapsing) {
            float nextScale = Math.max(0.0F, getShrinkScale() - 0.02F);
            setShrinkScale(nextScale);
            if (nextScale <= 0.01F) {
                setDead();
            }
            return;
        }

        if (getRadius() != getTargetRadius()) {
            int nextGrowthTick = getGrowthTicks() + 1;
            setGrowthTicks(nextGrowthTick);
            if (nextGrowthTick >= growthDuration) {
                setRadius(getTargetRadius());
                setGrowthTicks(0);
            }
        }
    }

    public void collapse() {
        collapsing = true;
    }

    public boolean isCollapsing() {
        return collapsing;
    }

    public int getRadius() {
        return dataManager.get(RADIUS);
    }

    public void setRadius(int radius) {
        dataManager.set(RADIUS, Math.max(0, radius));
    }

    public int getTargetRadius() {
        return dataManager.get(TARGET_RADIUS);
    }

    public void setTargetRadius(int targetRadius) {
        dataManager.set(TARGET_RADIUS, Math.max(0, targetRadius));
    }

    public float getShrinkScale() {
        return dataManager.get(SHRINK_SCALE);
    }

    public void setShrinkScale(float shrinkScale) {
        dataManager.set(SHRINK_SCALE, Math.max(0.0F, shrinkScale));
    }

    public int getGrowthTicks() {
        return dataManager.get(GROWTH_TICKS);
    }

    public void setGrowthTicks(int growthTicks) {
        dataManager.set(GROWTH_TICKS, Math.max(0, growthTicks));
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        setRadius(compound.getInteger("Radius"));
        setTargetRadius(compound.getInteger("TargetRadius"));
        setShrinkScale(compound.hasKey("ShrinkScale") ? compound.getFloat("ShrinkScale") : 1.0F);
        setGrowthTicks(compound.getInteger("GrowthTicks"));
        growthDuration = compound.hasKey("GrowthDuration") ? compound.getInteger("GrowthDuration") : 50;
        collapsing = compound.getBoolean("Collapsing");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("Radius", getRadius());
        compound.setInteger("TargetRadius", getTargetRadius());
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
