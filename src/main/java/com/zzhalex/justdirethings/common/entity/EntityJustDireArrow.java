package com.zzhalex.justdirethings.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class EntityJustDireArrow extends EntityTippedArrow {

    private static final DataParameter<Boolean> POTION_ARROW = EntityDataManager.createKey(EntityJustDireArrow.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SPLASH = EntityDataManager.createKey(EntityJustDireArrow.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LINGERING = EntityDataManager.createKey(EntityJustDireArrow.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HOMING = EntityDataManager.createKey(EntityJustDireArrow.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HOSTILE_ONLY = EntityDataManager.createKey(EntityJustDireArrow.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> ARROW_STATE = EntityDataManager.createKey(EntityJustDireArrow.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> STATE_TICK_COUNTER = EntityDataManager.createKey(EntityJustDireArrow.class, DataSerializers.VARINT);
    private static final DataParameter<Float> ORIGINAL_VELOCITY = EntityDataManager.createKey(EntityJustDireArrow.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> EPIC_ARROW = EntityDataManager.createKey(EntityJustDireArrow.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> PHASE = EntityDataManager.createKey(EntityJustDireArrow.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> TARGET_ANGRY = EntityDataManager.createKey(EntityJustDireArrow.class, DataSerializers.BOOLEAN);

    private EntityLivingBase targetEntity;

    private enum ArrowState {
        NORMAL,
        SLOWING_DOWN,
        STOPPED_AND_ROTATING,
        RESUMING_FLIGHT
    }

    public EntityJustDireArrow(World worldIn) {
        super(worldIn);
    }

    public EntityJustDireArrow(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    public EntityJustDireArrow(World worldIn, EntityLivingBase shooter) {
        super(worldIn, shooter);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(POTION_ARROW, false);
        dataManager.register(SPLASH, false);
        dataManager.register(LINGERING, false);
        dataManager.register(HOMING, false);
        dataManager.register(HOSTILE_ONLY, true);
        dataManager.register(ARROW_STATE, ArrowState.NORMAL.ordinal());
        dataManager.register(STATE_TICK_COUNTER, 0);
        dataManager.register(ORIGINAL_VELOCITY, 0.0F);
        dataManager.register(EPIC_ARROW, false);
        dataManager.register(PHASE, false);
        dataManager.register(TARGET_ANGRY, false);
    }

    @Override
    public void onUpdate() {
        noClip = isPhase();

        if (!world.isRemote && isPhase() && JustDireArrowRules.shouldDiscardPhaseArrow(ticksExisted)) {
            setDead();
            return;
        }

        super.onUpdate();

        if (!world.isRemote && getOriginalVelocity() == 0.0F) {
            setOriginalVelocity((float) getMotionLength());
        }

        if (!world.isRemote && isHoming() && !inGround) {
            updateHomingTarget();
            tickHomingState();
        }
    }

    @Override
    protected void onHit(RayTraceResult raytraceResultIn) {
        if (isPhase() && raytraceResultIn != null && raytraceResultIn.typeOfHit == RayTraceResult.Type.BLOCK) {
            return;
        }
        super.onHit(raytraceResultIn);
    }

    @Override
    protected void arrowHit(EntityLivingBase living) {
        super.arrowHit(living);
        if (isEpic()) {
            setArrowState(ArrowState.STOPPED_AND_ROTATING);
            dataManager.set(STATE_TICK_COUNTER, 0);
            targetEntity = findNearestEntity();
        }
    }

    public void setPotionArrow(boolean potionArrow) {
        dataManager.set(POTION_ARROW, potionArrow);
    }

    public boolean isPotionArrow() {
        return dataManager.get(POTION_ARROW);
    }

    public void setSplash(boolean splash) {
        dataManager.set(SPLASH, splash);
    }

    public boolean isSplash() {
        return dataManager.get(SPLASH);
    }

    public void setLingering(boolean lingering) {
        dataManager.set(LINGERING, lingering);
    }

    public boolean isLingering() {
        return dataManager.get(LINGERING);
    }

    public void setHoming(boolean homing) {
        dataManager.set(HOMING, homing);
    }

    public boolean isHoming() {
        return dataManager.get(HOMING);
    }

    public void setPhase(boolean phase) {
        dataManager.set(PHASE, phase);
    }

    public boolean isPhase() {
        return dataManager.get(PHASE);
    }

    public void setEpicArrow(boolean epicArrow) {
        dataManager.set(EPIC_ARROW, epicArrow);
        if (epicArrow) {
            setIsCritical(true);
        }
    }

    public boolean isEpic() {
        return dataManager.get(EPIC_ARROW);
    }

    public void setHostileOnly(boolean hostileOnly) {
        dataManager.set(HOSTILE_ONLY, hostileOnly);
    }

    public boolean getHostileOnly() {
        return dataManager.get(HOSTILE_ONLY);
    }

    public void setTargetAngry(boolean angry) {
        dataManager.set(TARGET_ANGRY, angry);
    }

    public boolean getTargetAngry() {
        return dataManager.get(TARGET_ANGRY);
    }

    public float getOriginalVelocity() {
        return dataManager.get(ORIGINAL_VELOCITY);
    }

    public void setTargetEntity(EntityLivingBase targetEntity) {
        this.targetEntity = targetEntity;
    }

    public double searchRadius() {
        return JustDireArrowRules.searchRadius(isEpic());
    }

    public boolean isHostileEntity(EntityLivingBase entity) {
        if (getTargetAngry()) {
            return true;
        }
        if (entity instanceof IMob) {
            return true;
        }
        if (entity instanceof EntityLiving && ((EntityLiving) entity).getAttackTarget() != null) {
            setTargetAngry(true);
            return true;
        }
        return false;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("is_potionarrow", isPotionArrow());
        compound.setBoolean("is_splash", isSplash());
        compound.setBoolean("is_lingering", isLingering());
        compound.setBoolean("is_homing", isHoming());
        compound.setInteger("arrow_state", dataManager.get(ARROW_STATE));
        compound.setInteger("state_tick_counter", dataManager.get(STATE_TICK_COUNTER));
        compound.setFloat("original_velocity", getOriginalVelocity());
        compound.setBoolean("is_epic_arrow", isEpic());
        compound.setBoolean("is_phase", isPhase());
        compound.setBoolean("hostile_only", getHostileOnly());
        compound.setBoolean("is_target_angry", getTargetAngry());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        setPotionArrow(compound.getBoolean("is_potionarrow"));
        setSplash(compound.getBoolean("is_splash"));
        setLingering(compound.getBoolean("is_lingering"));
        setHoming(compound.getBoolean("is_homing"));
        dataManager.set(ARROW_STATE, compound.getInteger("arrow_state"));
        dataManager.set(STATE_TICK_COUNTER, compound.getInteger("state_tick_counter"));
        setOriginalVelocity(compound.getFloat("original_velocity"));
        setEpicArrow(compound.getBoolean("is_epic_arrow"));
        setPhase(compound.getBoolean("is_phase"));
        if (compound.hasKey("hostile_only")) {
            setHostileOnly(compound.getBoolean("hostile_only"));
        }
        setTargetAngry(compound.getBoolean("is_target_angry"));
    }

    private void updateHomingTarget() {
        if (targetEntity != null && targetEntity.isEntityAlive() && getDistanceSq(targetEntity) <= 400.0D) {
            return;
        }
        targetEntity = findNearestEntity();
    }

    private void tickHomingState() {
        if (targetEntity == null) {
            if (dataManager.get(ARROW_STATE) != ArrowState.NORMAL.ordinal() && ticksExisted > 5) {
                setDead();
            }
            return;
        }

        ArrowState currentState = ArrowState.values()[MathHelper.clamp(dataManager.get(ARROW_STATE), 0, ArrowState.values().length - 1)];
        int stateTickCounter = dataManager.get(STATE_TICK_COUNTER);

        switch (currentState) {
            case NORMAL:
                handleNormalState();
                break;
            case SLOWING_DOWN:
                handleSlowingDownState(stateTickCounter);
                break;
            case STOPPED_AND_ROTATING:
                handleStoppedAndRotatingState(stateTickCounter);
                break;
            case RESUMING_FLIGHT:
                handleResumingFlightState();
                break;
            default:
                break;
        }

        dataManager.set(STATE_TICK_COUNTER, stateTickCounter + 1);
    }

    private void handleNormalState() {
        Vec3d arrowPosition = getPositionVector();
        Vec3d targetPosition = getTargetCenter(targetEntity);
        Vec3d directionToTarget = targetPosition.subtract(arrowPosition).normalize();
        Vec3d arrowDirection = new Vec3d(motionX, motionY, motionZ).normalize();
        double dotProduct = arrowDirection.dotProduct(directionToTarget);
        double distanceToTarget = arrowPosition.distanceTo(targetPosition);

        if (dotProduct >= 0.85D || distanceToTarget < 1.0D) {
            adjustCourseTowards(targetEntity);
        } else {
            setArrowState(ArrowState.SLOWING_DOWN);
        }
    }

    private void handleSlowingDownState(int stateTickCounter) {
        if (stateTickCounter < JustDireArrowRules.slowDownDurationTicks()) {
            motionX *= 0.5D;
            motionY *= 0.5D;
            motionZ *= 0.5D;
        } else {
            setArrowState(ArrowState.STOPPED_AND_ROTATING);
        }
    }

    private void handleStoppedAndRotatingState(int stateTickCounter) {
        motionX = 0.0D;
        motionY = 0.0D;
        motionZ = 0.0D;
        rotateTowards(targetEntity, 0.3F);

        if (stateTickCounter >= JustDireArrowRules.stopDurationTicks()) {
            setArrowState(ArrowState.RESUMING_FLIGHT);
            adjustCourseTowards(targetEntity);
            if (shootingEntity instanceof EntityLivingBase) {
                world.playSound(null, posX, posY, posZ, net.minecraft.init.SoundEvents.ENTITY_ENDERDRAGON_FLAP, SoundCategory.PLAYERS, 1.0F, 0.5F);
            }
            motionX *= 0.25D;
            motionY *= 0.25D;
            motionZ *= 0.25D;
        }
    }

    private void handleResumingFlightState() {
        double originalVelocity = getOriginalVelocity();
        if (originalVelocity > 0.0D && getMotionLength() < originalVelocity) {
            motionX *= 1.5D;
            motionY *= 1.5D;
            motionZ *= 1.5D;
        }
        if (originalVelocity > 0.0D && getMotionLength() > originalVelocity) {
            Vec3d scaled = new Vec3d(motionX, motionY, motionZ).normalize().scale(originalVelocity);
            motionX = scaled.x;
            motionY = scaled.y;
            motionZ = scaled.z;
        }
        adjustCourseTowards(targetEntity);
    }

    private EntityLivingBase findNearestEntity() {
        double radius = searchRadius();
        AxisAlignedBB searchArea = getEntityBoundingBox().grow(radius, radius / 2.0D, radius);
        List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, searchArea, this::canTarget);
        EntityLivingBase nearestEntity = null;
        double nearestDistance = Double.MAX_VALUE;

        for (EntityLivingBase entity : entities) {
            double distance = getDistanceSq(entity);
            if (distance < nearestDistance) {
                nearestEntity = entity;
                nearestDistance = distance;
            }
        }

        return nearestEntity;
    }

    private boolean canTarget(EntityLivingBase entity) {
        if (entity == null || entity == this.shootingEntity || !entity.isEntityAlive()) {
            return false;
        }
        return !getHostileOnly() || isHostileEntity(entity);
    }

    private void adjustCourseTowards(EntityLivingBase target) {
        if (target == null) {
            return;
        }
        Vec3d direction = getTargetCenter(target).subtract(getPositionVector()).normalize();
        double speed = Math.max(getMotionLength(), 0.1D);
        motionX = direction.x * speed;
        motionY = direction.y * speed;
        motionZ = direction.z * speed;
        rotateTowards(target, 1.0F);
    }

    private void rotateTowards(EntityLivingBase target, float smoothing) {
        Vec3d direction = getTargetCenter(target).subtract(getPositionVector()).normalize();
        double horizontalDistance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float targetYaw = (float) (MathHelper.atan2(direction.x, direction.z) * (180D / Math.PI));
        float targetPitch = (float) (MathHelper.atan2(direction.y, horizontalDistance) * (180D / Math.PI));
        rotationYaw += MathHelper.wrapDegrees(targetYaw - rotationYaw) * smoothing;
        rotationPitch += MathHelper.wrapDegrees(targetPitch - rotationPitch) * smoothing;
        prevRotationYaw = rotationYaw;
        prevRotationPitch = rotationPitch;
    }

    private Vec3d getTargetCenter(Entity target) {
        AxisAlignedBB bounds = target.getEntityBoundingBox();
        return new Vec3d(
                (bounds.minX + bounds.maxX) * 0.5D,
                (bounds.minY + bounds.maxY) * 0.5D,
                (bounds.minZ + bounds.maxZ) * 0.5D
        );
    }

    private void setArrowState(ArrowState state) {
        dataManager.set(ARROW_STATE, state.ordinal());
        dataManager.set(STATE_TICK_COUNTER, 0);
    }

    private void setOriginalVelocity(float originalVelocity) {
        dataManager.set(ORIGINAL_VELOCITY, originalVelocity);
    }

    private double getMotionLength() {
        return Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
    }
}
