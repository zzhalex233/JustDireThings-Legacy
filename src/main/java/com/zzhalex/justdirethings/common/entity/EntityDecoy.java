package com.zzhalex.justdirethings.common.entity;

import com.google.common.base.Optional;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.UUID;

public class EntityDecoy extends EntityLiving {

    private static final String OWNER_UUID_KEY = "player_uuid";
    private static final DataParameter<Optional<UUID>> OWNER_UUID = EntityDataManager.createKey(EntityDecoy.class, DataSerializers.OPTIONAL_UNIQUE_ID);

    public EntityDecoy(World worldIn) {
        super(worldIn);
        setSize(0.6F, 1.8F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(OWNER_UUID, Optional.absent());
    }

    @Override
    protected void initEntityAI() {
        // Decoys stand still; their job is to periodically redirect nearby mob targets.
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        setBaseAttributeValue(SharedMonsterAttributes.MAX_HEALTH, 20.0D);
        setBaseAttributeValue(SharedMonsterAttributes.FOLLOW_RANGE, 35.0D);
        setBaseAttributeValue(SharedMonsterAttributes.MOVEMENT_SPEED, 0.0D);
        setBaseAttributeValue(SharedMonsterAttributes.ATTACK_DAMAGE, 0.0D);
        setBaseAttributeValue(SharedMonsterAttributes.ARMOR, 0.0D);
    }

    private void setBaseAttributeValue(IAttribute attribute, double value) {
        IAttributeInstance instance = getEntityAttribute(attribute);
        if (instance == null) {
            instance = getAttributeMap().registerAttribute(attribute);
        }
        instance.setBaseValue(value);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (world.isRemote) {
            return;
        }

        if (DecoyBehaviorRules.shouldAggro(ticksExisted)) {
            aggroMobs();
        }

        if (DecoyBehaviorRules.shouldExpire(ticksExisted)) {
            setDead();
        }
    }

    public void aggroMobs() {
        int radius = DecoyBehaviorRules.aggroRadiusBlocks();
        AxisAlignedBB bounds = new AxisAlignedBB(
                posX - radius,
                posY - radius,
                posZ - radius,
                posX + radius,
                posY + radius,
                posZ + radius
        );

        for (EntityLiving mob : world.getEntitiesWithinAABB(EntityLiving.class, bounds, this::canAggroMob)) {
            mob.setAttackTarget(this);
        }
    }

    public void setSummonerName(String playerName) {
        setCustomNameTag(DecoyBehaviorRules.formatSummonerName(playerName, "Decoy"));
    }

    public java.util.Optional<UUID> getOwnerUUID() {
        Optional<UUID> ownerUUID = dataManager.get(OWNER_UUID);
        if (ownerUUID.isPresent()) {
            return java.util.Optional.of(ownerUUID.get());
        }
        return java.util.Optional.empty();
    }

    public void setOwnerUUID(UUID uuid) {
        dataManager.set(OWNER_UUID, uuid == null ? Optional.absent() : Optional.of(uuid));
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return source == DamageSource.OUT_OF_WORLD && super.attackEntityFrom(source, amount);
    }

    @Override
    public Iterable<ItemStack> getArmorInventoryList() {
        return java.util.Collections.emptyList();
    }

    @Override
    public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {
    }

    @Override
    public EnumHandSide getPrimaryHand() {
        return EnumHandSide.RIGHT;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasUniqueId(OWNER_UUID_KEY)) {
            setOwnerUUID(compound.getUniqueId(OWNER_UUID_KEY));
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        java.util.Optional<UUID> ownerUUID = getOwnerUUID();
        if (ownerUUID.isPresent()) {
            compound.setUniqueId(OWNER_UUID_KEY, ownerUUID.get());
        }
    }

    private boolean canAggroMob(EntityLiving mob) {
        return mob != null && mob != this && mob.isEntityAlive();
    }
}
