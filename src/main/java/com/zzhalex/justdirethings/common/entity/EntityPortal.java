package com.zzhalex.justdirethings.common.entity;

import com.zzhalex.justdirethings.common.portal.PortalLifecycleRules;
import com.zzhalex.justdirethings.common.portal.PortalDimensionTransferRules;
import com.zzhalex.justdirethings.common.portal.PortalDirectTeleporter;
import com.zzhalex.justdirethings.common.portal.PortalTransformRules;
import com.zzhalex.justdirethings.common.portal.PortalVelocityRules;
import com.zzhalex.justdirethings.common.world.PortalChunkKeeper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityPortal extends Entity {

    public static final int TELEPORT_COOLDOWN = 10;
    public static final int ANIMATION_COOLDOWN = 5;

    private static final DataParameter<Integer> FACING = EntityDataManager.createKey(EntityPortal.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> ALIGNMENT = EntityDataManager.createKey(EntityPortal.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> PRIMARY = EntityDataManager.createKey(EntityPortal.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> DYING = EntityDataManager.createKey(EntityPortal.class, DataSerializers.BOOLEAN);

    private final Map<UUID, Integer> entityCooldowns = new HashMap<>();
    private final Map<UUID, Integer> entityVelocityCooldowns = new HashMap<>();
    private final Map<UUID, Vec3d> entityLastPosition = new HashMap<>();
    private final Map<UUID, Vec3d> entityLastLastPosition = new HashMap<>();
    private UUID portalGunUuid;
    private UUID linkedPortalUuid;
    private UUID ownerUuid;
    private int deathCounter;
    private boolean chunkTracked;
    private boolean portalDataReady;

    public EntityPortal(World worldIn) {
        super(worldIn);
        setSize(1.0F, 2.0F);
        noClip = true;
        ignoreFrustumCheck = true;
    }

    public EntityPortal(World worldIn, EnumFacing facing, EnumFacing.Axis alignment, UUID portalGunUuid, boolean primary, UUID ownerUuid) {
        this(worldIn);
        dataManager.set(FACING, facing.ordinal());
        dataManager.set(ALIGNMENT, alignment.ordinal());
        dataManager.set(PRIMARY, primary);
        this.portalGunUuid = portalGunUuid;
        this.ownerUuid = ownerUuid;
        updatePortalBounds();
    }

    @Override
    protected void entityInit() {
        dataManager.register(FACING, EnumFacing.NORTH.ordinal());
        dataManager.register(ALIGNMENT, EnumFacing.Axis.Z.ordinal());
        dataManager.register(PRIMARY, true);
        dataManager.register(DYING, false);
        portalDataReady = true;
    }

    public UUID getPortalGunUuid() {
        return portalGunUuid;
    }

    public boolean isPrimary() {
        if (!isPortalDataReady()) {
            return true;
        }
        return dataManager.get(PRIMARY);
    }

    public boolean isDying() {
        if (!isPortalDataReady()) {
            return false;
        }
        return dataManager.get(DYING);
    }

    public int getDeathCounter() {
        return deathCounter;
    }

    public EnumFacing getFacing() {
        if (!isPortalDataReady()) {
            return EnumFacing.NORTH;
        }
        int ordinal = MathHelper.clamp(dataManager.get(FACING), 0, EnumFacing.values().length - 1);
        return EnumFacing.values()[ordinal];
    }

    public EnumFacing.Axis getAlignmentAxis() {
        if (!isPortalDataReady()) {
            return EnumFacing.Axis.Z;
        }
        int ordinal = MathHelper.clamp(dataManager.get(ALIGNMENT), 0, EnumFacing.Axis.values().length - 1);
        return EnumFacing.Axis.values()[ordinal];
    }

    public UUID getLinkedPortalUuid() {
        return linkedPortalUuid;
    }

    public void setLinkedPortal(EntityPortal linkedPortal) {
        this.linkedPortalUuid = linkedPortal == null ? null : linkedPortal.getUniqueID();
    }

    public void markDying() {
        if (!isPortalDataReady()) {
            return;
        }
        dataManager.set(DYING, true);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        updatePortalBounds();

        if (world.isRemote) {
            return;
        }

        PortalChunkKeeper.track(getUniqueID(), world, getPosition());
        chunkTracked = true;

        PortalLifecycleRules.tickCooldowns(entityCooldowns);
        tickVelocityCooldowns();

        if (isDying()) {
            deathCounter++;
            if (deathCounter > ANIMATION_COOLDOWN) {
                setDead();
            }
            return;
        }

        EntityPortal linkedPortal = getLinkedPortal();
        if (linkedPortal == null || linkedPortal.isDying()) {
            return;
        }

        for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox())) {
            if (canTeleport(entity)) {
                teleport(entity, linkedPortal);
            }
        }
        captureVelocity();
    }

    private boolean canTeleport(Entity entity) {
        if (entity == null || entity == this || entity instanceof EntityPortal) {
            return false;
        }
        return !entityCooldowns.containsKey(entity.getUniqueID());
    }

    private void teleport(Entity entity, EntityPortal linkedPortal) {
        Vec3d exit = PortalTransformRules.teleportPosition(
                entity.getEntityBoundingBox(),
                getEntityBoundingBox(),
                getFacing(),
                getAlignmentAxis(),
                linkedPortal.getEntityBoundingBox(),
                linkedPortal.getFacing(),
                linkedPortal.getAlignmentAxis()
        );
        Vec3d transformedLook = PortalTransformRules.transformMotion(
                Vec3d.fromPitchYaw(entity.rotationPitch, entity.rotationYaw),
                getFacing(),
                getAlignmentAxis(),
                linkedPortal.getFacing().getOpposite(),
                linkedPortal.getAlignmentAxis()
        );
        PortalTransformRules.Rotation rotation = PortalTransformRules.rotationFromVector(transformedLook);
        Vec3d inheritedMotion = calculateVelocity(entity, linkedPortal);
        boolean hasInheritedMotion = inheritedMotion.lengthSquared() > 0.0D;

        Entity teleportedEntity = moveAcrossDimensions(entity, linkedPortal.world.provider.getDimension());
        if (teleportedEntity == null) {
            return;
        }

        if (teleportedEntity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) teleportedEntity;
            player.connection.setPlayerLocation(exit.x, exit.y, exit.z, rotation.getYaw(), rotation.getPitch());
        } else {
            teleportedEntity.setLocationAndAngles(exit.x, exit.y, exit.z, rotation.getYaw(), rotation.getPitch());
        }
        if (hasInheritedMotion) {
            teleportedEntity.motionX = inheritedMotion.x;
            teleportedEntity.motionY = inheritedMotion.y;
            teleportedEntity.motionZ = inheritedMotion.z;
            if (teleportedEntity instanceof EntityPlayerMP) {
                ((EntityPlayerMP) teleportedEntity).connection.sendPacket(new SPacketEntityVelocity(teleportedEntity));
            }
        }
        teleportedEntity.setRotationYawHead(rotation.getYaw());
        teleportedEntity.fallDistance = 0.0F;
        teleportedEntity.velocityChanged = true;
        entityCooldowns.put(teleportedEntity.getUniqueID(), TELEPORT_COOLDOWN);
        linkedPortal.entityCooldowns.put(teleportedEntity.getUniqueID(), TELEPORT_COOLDOWN);
    }

    private Entity moveAcrossDimensions(Entity entity, int targetDimension) {
        if (entity == null) {
            return null;
        }
        if (entity.world.provider.getDimension() == targetDimension) {
            return entity;
        }

        Entity moved = PortalDimensionTransferRules.requiresDirectTeleporter(entity.world.provider.getDimension(), targetDimension)
                ? entity.changeDimension(targetDimension, new PortalDirectTeleporter())
                : entity.changeDimension(targetDimension);
        return moved == null ? entity : moved;
    }

    public AxisAlignedBB getVelocityBoundingBox() {
        EnumFacing facing = getFacing();
        return getEntityBoundingBox().expand(
                facing.getXOffset() * 2.5D,
                facing.getYOffset() * 2.5D,
                facing.getZOffset() * 2.5D
        );
    }

    private void captureVelocity() {
        AxisAlignedBB velocityBounds = getVelocityBoundingBox();
        for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(this, velocityBounds)) {
            if (canTeleport(entity)) {
                UUID entityId = entity.getUniqueID();
                Vec3d currentPosition = entity.getPositionVector();
                if (entityLastPosition.containsKey(entityId)) {
                    entityLastLastPosition.put(entityId, entityLastPosition.get(entityId));
                }
                entityLastPosition.put(entityId, currentPosition);
                entityVelocityCooldowns.put(entityId, TELEPORT_COOLDOWN);
            }
        }
    }

    private Vec3d calculateVelocity(Entity entity, EntityPortal linkedPortal) {
        UUID entityId = entity.getUniqueID();
        Vec3d velocity = PortalVelocityRules.sampledVelocity(
                entity.getPositionVector(),
                entityLastPosition.get(entityId),
                entityLastLastPosition.get(entityId)
        );
        entityLastPosition.remove(entityId);
        entityLastLastPosition.remove(entityId);
        entityVelocityCooldowns.remove(entityId);
        return PortalVelocityRules.inheritedVelocity(
                velocity,
                getFacing(),
                getAlignmentAxis(),
                linkedPortal.getFacing().getOpposite(),
                linkedPortal.getAlignmentAxis()
        );
    }

    private void tickVelocityCooldowns() {
        entityVelocityCooldowns.entrySet().removeIf(entry -> {
            if (entry.getValue() <= 0) {
                entityLastPosition.remove(entry.getKey());
                entityLastLastPosition.remove(entry.getKey());
                return true;
            }
            entry.setValue(entry.getValue() - 1);
            return false;
        });
    }

    public EntityPortal getLinkedPortal() {
        if (linkedPortalUuid == null) {
            return null;
        }
        MinecraftServer server = world.getMinecraftServer();
        if (server == null) {
            return null;
        }
        for (World serverWorld : DimensionManager.getWorlds()) {
            for (Entity entity : serverWorld.loadedEntityList) {
                if (entity instanceof EntityPortal && linkedPortalUuid.equals(entity.getUniqueID())) {
                    return (EntityPortal) entity;
                }
            }
        }
        return null;
    }

    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition(x, y, z);
        updatePortalBounds();
    }

    private void updatePortalBounds() {
        if (!isPortalDataReady()) {
            return;
        }
        EnumFacing facing = getFacing();
        EnumFacing.Axis alignment = getAlignmentAxis();
        AxisAlignedBB bounds;
        if (facing.getAxis() == EnumFacing.Axis.Y) {
            if (alignment == EnumFacing.Axis.X) {
                bounds = new AxisAlignedBB(posX, posY, posZ - 0.5D, posX + 2.0D, posY + 0.2D, posZ + 0.5D);
            } else {
                bounds = new AxisAlignedBB(posX - 0.5D, posY, posZ, posX + 0.5D, posY + 0.2D, posZ + 2.0D);
            }
        } else if (facing.getAxis() == EnumFacing.Axis.X) {
            bounds = new AxisAlignedBB(posX - 0.1D, posY, posZ - 0.5D, posX + 0.1D, posY + 2.0D, posZ + 0.5D);
        } else {
            bounds = new AxisAlignedBB(posX - 0.5D, posY, posZ - 0.1D, posX + 0.5D, posY + 2.0D, posZ + 0.1D);
        }
        setEntityBoundingBox(bounds);
    }

    @Override
    public void setDead() {
        if (!world.isRemote) {
            PortalChunkKeeper.clear(getUniqueID());
        }
        super.setDead();
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        dataManager.set(FACING, compound.getInteger("Facing"));
        dataManager.set(ALIGNMENT, compound.getInteger("Alignment"));
        dataManager.set(PRIMARY, compound.getBoolean("Primary"));
        dataManager.set(DYING, compound.getBoolean("Dying"));
        deathCounter = compound.getInteger("DeathCounter");
        if (compound.hasKey("PortalGunUuid")) {
            portalGunUuid = UUID.fromString(compound.getString("PortalGunUuid"));
        }
        if (compound.hasKey("LinkedPortalUuid")) {
            linkedPortalUuid = UUID.fromString(compound.getString("LinkedPortalUuid"));
        }
        if (compound.hasKey("OwnerUuid")) {
            ownerUuid = UUID.fromString(compound.getString("OwnerUuid"));
        }
        updatePortalBounds();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("Facing", getFacing().ordinal());
        compound.setInteger("Alignment", getAlignmentAxis().ordinal());
        compound.setBoolean("Primary", isPrimary());
        compound.setBoolean("Dying", isDying());
        compound.setInteger("DeathCounter", deathCounter);
        if (portalGunUuid != null) {
            compound.setString("PortalGunUuid", portalGunUuid.toString());
        }
        if (linkedPortalUuid != null) {
            compound.setString("LinkedPortalUuid", linkedPortalUuid.toString());
        }
        if (ownerUuid != null) {
            compound.setString("OwnerUuid", ownerUuid.toString());
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0;
    }

    private boolean isPortalDataReady() {
        return portalDataReady && dataManager != null;
    }
}
