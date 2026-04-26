package com.zzhalex.justdirethings.common.entity;

import com.zzhalex.justdirethings.common.entity.group.JDTEntityGroups;
import com.zzhalex.justdirethings.common.item.misc.ItemCreatureCatcher;
import com.zzhalex.justdirethings.registry.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityCreatureCatcher extends EntityThrowable {

    private static final DataParameter<Boolean> HAS_HIT = EntityDataManager.createKey(EntityCreatureCatcher.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CAPTURING = EntityDataManager.createKey(EntityCreatureCatcher.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> SHRINKING_TIME = EntityDataManager.createKey(EntityCreatureCatcher.class, DataSerializers.VARINT);
    private static final DataParameter<ItemStack> RETURN_STACK = EntityDataManager.createKey(EntityCreatureCatcher.class, DataSerializers.ITEM_STACK);
    private static final DataParameter<Float> ENTITY_POS_X = EntityDataManager.createKey(EntityCreatureCatcher.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> ENTITY_POS_Y = EntityDataManager.createKey(EntityCreatureCatcher.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> ENTITY_POS_Z = EntityDataManager.createKey(EntityCreatureCatcher.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> ENTITY_BODY_ROT = EntityDataManager.createKey(EntityCreatureCatcher.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> ENTITY_HEAD_ROT = EntityDataManager.createKey(EntityCreatureCatcher.class, DataSerializers.FLOAT);
    private static final int ANIMATION_TICKS = 20;

    public EntityCreatureCatcher(World worldIn) {
        super(worldIn);
        setSize(0.25F, 0.25F);
    }

    public EntityCreatureCatcher(World worldIn, EntityLivingBase throwerIn) {
        super(worldIn, throwerIn);
        setSize(0.25F, 0.25F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(HAS_HIT, false);
        dataManager.register(CAPTURING, false);
        dataManager.register(SHRINKING_TIME, 0);
        dataManager.register(RETURN_STACK, ItemStack.EMPTY);
        dataManager.register(ENTITY_POS_X, 0.0F);
        dataManager.register(ENTITY_POS_Y, 0.0F);
        dataManager.register(ENTITY_POS_Z, 0.0F);
        dataManager.register(ENTITY_BODY_ROT, 0.0F);
        dataManager.register(ENTITY_HEAD_ROT, 0.0F);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!hasHit()) {
            return;
        }

        motionX = 0.0D;
        motionY = 0.0D;
        motionZ = 0.0D;
        setNoGravity(true);

        if (world.isRemote) {
            return;
        }

        dataManager.set(SHRINKING_TIME, getShrinkingTime() + 1);
        if (getShrinkingTime() <= ANIMATION_TICKS) {
            return;
        }

        if (isCapturing()) {
            spawnItemEntity(getReturnStack());
        } else {
            releaseCapturedEntity();
            spawnItemEntity(new ItemStack(ModItems.CREATURE_CATCHER));
        }
        setDead();
    }

    public ItemStack getReturnStack() {
        return dataManager.get(RETURN_STACK);
    }

    public void setReturnStack(ItemStack returnStack) {
        dataManager.set(RETURN_STACK, returnStack == null ? ItemStack.EMPTY : returnStack.copy());
    }

    public boolean hasHit() {
        return dataManager.get(HAS_HIT);
    }

    public boolean isCapturing() {
        return dataManager.get(CAPTURING);
    }

    public int getShrinkingTime() {
        return dataManager.get(SHRINKING_TIME);
    }

    public int getAnimationTicks() {
        return ANIMATION_TICKS;
    }

    public float getCapturedEntityX() {
        return dataManager.get(ENTITY_POS_X);
    }

    public float getCapturedEntityY() {
        return dataManager.get(ENTITY_POS_Y);
    }

    public float getCapturedEntityZ() {
        return dataManager.get(ENTITY_POS_Z);
    }

    public float getCapturedEntityBodyRot() {
        return dataManager.get(ENTITY_BODY_ROT);
    }

    public float getCapturedEntityHeadRot() {
        return dataManager.get(ENTITY_HEAD_ROT);
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (world.isRemote || hasHit()) {
            return;
        }
        snapToImpact(result);

        if (result.typeOfHit == RayTraceResult.Type.ENTITY && result.entityHit instanceof EntityLiving) {
            EntityLiving hitEntity = (EntityLiving) result.entityHit;
            if (!ItemCreatureCatcher.hasEntity(getReturnStack()) && canCapture(hitEntity)) {
                beginCapture(hitEntity);
                return;
            }
        }

        if (ItemCreatureCatcher.hasEntity(getReturnStack())) {
            beginRelease();
        } else {
            spawnItemEntity(new ItemStack(ModItems.CREATURE_CATCHER));
            setDead();
        }
    }

    private void beginCapture(EntityLiving entity) {
        ResourceLocation entityId = EntityList.getKey(entity);
        if (entityId == null) {
            spawnItemEntity(new ItemStack(ModItems.CREATURE_CATCHER));
            setDead();
            return;
        }

        NBTTagCompound entityData = new NBTTagCompound();
        entity.writeToNBT(entityData);
        sanitizeCapturedEntityData(entityData);
        setReturnStack(ItemCreatureCatcher.createCapturedStack(entityId.toString(), entityData));
        dataManager.set(ENTITY_POS_X, (float) entity.posX);
        dataManager.set(ENTITY_POS_Y, (float) entity.posY);
        dataManager.set(ENTITY_POS_Z, (float) entity.posZ);
        dataManager.set(ENTITY_BODY_ROT, entity.renderYawOffset);
        dataManager.set(ENTITY_HEAD_ROT, entity.rotationYawHead);
        dataManager.set(CAPTURING, true);
        beginAnimation();
        entity.setDead();
    }

    private void beginRelease() {
        dataManager.set(CAPTURING, false);
        beginAnimation();
    }

    private void beginAnimation() {
        dataManager.set(HAS_HIT, true);
        dataManager.set(SHRINKING_TIME, 0);
        motionX = 0.0D;
        motionY = 0.0D;
        motionZ = 0.0D;
        setNoGravity(true);
    }

    private void snapToImpact(RayTraceResult result) {
        if (result != null && result.hitVec != null) {
            setPosition(result.hitVec.x, result.hitVec.y, result.hitVec.z);
            prevPosX = lastTickPosX = posX;
            prevPosY = lastTickPosY = posY;
            prevPosZ = lastTickPosZ = posZ;
        }
    }

    private void releaseCapturedEntity() {
        ItemStack stack = getReturnStack();
        if (!ItemCreatureCatcher.hasEntity(stack)) {
            return;
        }

        Entity entity = createCapturedEntity(stack);
        if (entity == null) {
            return;
        }

        Vec3d location = getPositionVector();
        entity.setPositionAndRotation(location.x, location.y, location.z, entity.rotationYaw, entity.rotationPitch);
        world.spawnEntity(entity);
    }

    public Entity createCapturedEntity(ItemStack stack) {
        return createCapturedEntity(stack, world);
    }

    public static Entity createCapturedEntity(ItemStack stack, World world) {
        if (world == null) {
            return null;
        }
        String entityId = ItemCreatureCatcher.getCapturedEntityId(stack);
        if (entityId.isEmpty()) {
            return null;
        }

        Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(entityId), world);
        if (entity == null) {
            return null;
        }

        NBTTagCompound entityData = ItemCreatureCatcher.getCapturedEntityData(stack);
        entityData.setString("id", entityId);
        sanitizeCapturedEntityData(entityData);
        entity.readFromNBT(entityData);
        resetCapturedEntityVisualState(entity);
        return entity;
    }

    public static void sanitizeCapturedEntityData(NBTTagCompound entityData) {
        if (entityData == null) {
            return;
        }
        entityData.removeTag("HurtTime");
        entityData.removeTag("DeathTime");
        entityData.removeTag("HurtByTimestamp");
    }

    public static void resetCapturedEntityVisualState(Entity entity) {
        if (entity == null) {
            return;
        }
        entity.hurtResistantTime = 0;
        entity.ticksExisted = 0;
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) entity;
            living.hurtTime = 0;
            living.maxHurtTime = 0;
            living.deathTime = 0;
            living.attackedAtYaw = 0.0F;
        }
    }

    private boolean canCapture(Entity entity) {
        if (!(entity instanceof EntityLiving)) {
            return false;
        }
        if (!entity.isEntityAlive() || !entity.isNonBoss()) {
            return false;
        }
        ResourceLocation entityId = EntityList.getKey(entity);
        if (entityId == null || JDTEntityGroups.isCreatureCatcherDenied(entityId.toString())) {
            return false;
        }
        return entity.writeToNBTOptional(new NBTTagCompound());
    }

    private void spawnItemEntity(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        EntityItem itemEntity = new EntityItem(world, posX, posY, posZ, stack.copy());
        itemEntity.setPickupDelay(10);
        world.spawnEntity(itemEntity);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        dataManager.set(HAS_HIT, compound.getBoolean("HasHitEntity"));
        dataManager.set(CAPTURING, compound.getBoolean("Capturing"));
        dataManager.set(SHRINKING_TIME, compound.getInteger("ShrinkingTime"));
        if (compound.hasKey("ReturnStack")) {
            setReturnStack(new ItemStack(compound.getCompoundTag("ReturnStack")));
        }
        dataManager.set(ENTITY_POS_X, compound.getFloat("EntityPosX"));
        dataManager.set(ENTITY_POS_Y, compound.getFloat("EntityPosY"));
        dataManager.set(ENTITY_POS_Z, compound.getFloat("EntityPosZ"));
        dataManager.set(ENTITY_BODY_ROT, compound.getFloat("EntityBodyRot"));
        dataManager.set(ENTITY_HEAD_ROT, compound.getFloat("EntityHeadRot"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("HasHitEntity", hasHit());
        compound.setBoolean("Capturing", isCapturing());
        compound.setInteger("ShrinkingTime", getShrinkingTime());
        compound.setFloat("EntityPosX", getCapturedEntityX());
        compound.setFloat("EntityPosY", getCapturedEntityY());
        compound.setFloat("EntityPosZ", getCapturedEntityZ());
        compound.setFloat("EntityBodyRot", getCapturedEntityBodyRot());
        compound.setFloat("EntityHeadRot", getCapturedEntityHeadRot());
        if (!getReturnStack().isEmpty()) {
            compound.setTag("ReturnStack", getReturnStack().writeToNBT(new NBTTagCompound()));
        }
    }
}
