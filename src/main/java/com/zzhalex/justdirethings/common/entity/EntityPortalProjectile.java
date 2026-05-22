package com.zzhalex.justdirethings.common.entity;

import com.zzhalex.justdirethings.common.portal.PortalLifecycleRules;
import com.zzhalex.justdirethings.common.portal.PortalLinkData;
import com.zzhalex.justdirethings.common.portal.PortalPlacementRules;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EntityPortalProjectile extends EntityThrowable {

    private UUID portalGunUuid;
    private PortalLinkData.PortalDestination destination = PortalLinkData.PortalDestination.EMPTY;
    private boolean classicMode;
    private boolean primaryPortal;
    private boolean hasSpawnedPortal;

    public EntityPortalProjectile(World worldIn) {
        super(worldIn);
    }

    public EntityPortalProjectile(World worldIn, EntityLivingBase throwerIn, UUID portalGunUuid, PortalLinkData.PortalDestination destination) {
        super(worldIn, throwerIn);
        this.portalGunUuid = portalGunUuid;
        this.destination = destination == null ? PortalLinkData.PortalDestination.EMPTY : destination;
    }

    public EntityPortalProjectile(World worldIn, EntityLivingBase throwerIn, UUID portalGunUuid, boolean primaryPortal) {
        super(worldIn, throwerIn);
        this.portalGunUuid = portalGunUuid;
        this.classicMode = true;
        this.primaryPortal = primaryPortal;
    }

    public UUID getPortalGunUuid() {
        return portalGunUuid;
    }

    public PortalLinkData.PortalDestination getDestination() {
        return destination;
    }

    public boolean isClassicMode() {
        return classicMode;
    }

    public boolean isPrimaryPortal() {
        return primaryPortal;
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (world.isRemote) {
            return;
        }
        if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
            if (!canImpactPortalSurface(result)) {
                return;
            }
            if (classicMode) {
                spawnClassicPortal(result);
            } else {
                spawnPortalPair(result);
            }
        } else {
            setDead();
        }
    }

    private boolean canImpactPortalSurface(RayTraceResult result) {
        if (result == null || result.getBlockPos() == null) {
            return false;
        }
        IBlockState state = world.getBlockState(result.getBlockPos());
        return PortalPlacementRules.hasProjectileCollision(state.getCollisionBoundingBox(world, result.getBlockPos()));
    }

    @Override
    protected float getGravityVelocity() {
        return 0.0F;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (world.isRemote || isDead) {
            return;
        }
        if (classicMode) {
            if (ticksExisted > 200) {
                setDead();
            }
            return;
        }
        if (ticksExisted > 5) {
            EnumFacing direction = primaryDirection().getOpposite();
            BlockPos blockPos = new BlockPos(posX, posY, posZ);
            if (!world.isAirBlock(blockPos) || !world.isAirBlock(blockPos.down())) {
                blockPos = blockPos.offset(direction);
            }
            Vec3d hitPos = new Vec3d(blockPos).add(0.5D, 0.5D, 0.5D);
            spawnPortalPair(hitPos.x, hitPos.y, hitPos.z, direction, blockPos);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (portalGunUuid != null) {
            compound.setString("PortalGunUuid", portalGunUuid.toString());
        }
        compound.setTag("Destination", destination.writeToNbt());
        compound.setBoolean("ClassicMode", classicMode);
        compound.setBoolean("PrimaryPortal", primaryPortal);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("PortalGunUuid")) {
            try {
                portalGunUuid = UUID.fromString(compound.getString("PortalGunUuid"));
            } catch (IllegalArgumentException ignored) {
                portalGunUuid = null;
            }
        }
        if (compound.hasKey("Destination")) {
            destination = PortalLinkData.PortalDestination.read(compound.getCompoundTag("Destination"));
        }
        classicMode = compound.getBoolean("ClassicMode");
        primaryPortal = compound.getBoolean("PrimaryPortal");
    }

    private void spawnClassicPortal(RayTraceResult result) {
        if (portalGunUuid == null || result.getBlockPos() == null) {
            return;
        }

        EntityLivingBase thrower = getThrower();
        UUID ownerUuid = thrower == null ? null : thrower.getUniqueID();
        EnumFacing impactFacing = result.sideHit == null ? EnumFacing.NORTH : result.sideHit;
        EnumFacing.Axis alignment = PortalLifecycleRules.axisFromMotion(motionX, motionZ);
        Vec3d hitPos = centeredImpact(result.getBlockPos(), impactFacing);
        PortalPlacementRules.PlacementResult placement = placementFor(hitPos.x, hitPos.y, hitPos.z, result.getBlockPos(), impactFacing, alignment);
        if (!placement.isValid()) {
            setDead();
            return;
        }

        EntityPortal portal = new EntityPortal(world, impactFacing, alignment, portalGunUuid, primaryPortal, ownerUuid);
        Vec3d sourcePos = placement.getPosition();
        portal.setPosition(sourcePos.x, sourcePos.y, sourcePos.z);
        if (hasPortalConflict(world, portal.getEntityBoundingBox())) {
            setDead();
            return;
        }

        clearExistingClassicPortal(primaryPortal);
        EntityPortal oppositePortal = findOppositeClassicPortal(primaryPortal);
        if (oppositePortal != null) {
            portal.setLinkedPortal(oppositePortal);
            oppositePortal.setLinkedPortal(portal);
        }
        world.spawnEntity(portal);
        setDead();
    }

    private void spawnPortalPair(RayTraceResult result) {
        if (result == null || result.getBlockPos() == null) {
            return;
        }
        EnumFacing impactFacing = result.sideHit == null ? EnumFacing.NORTH : result.sideHit;
        Vec3d hitPos = centeredImpact(result.getBlockPos(), impactFacing);
        spawnPortalPair(hitPos.x, hitPos.y, hitPos.z, impactFacing, result.getBlockPos());
    }

    private void spawnPortalPair(double x, double y, double z, EnumFacing impactFacing, BlockPos hitPos) {
        if (hasSpawnedPortal || portalGunUuid == null || destination == null || destination.isEmpty()) {
            return;
        }

        World destinationWorld = DimensionManager.getWorld(destination.getDimension());
        if (destinationWorld == null) {
            return;
        }

        EntityLivingBase thrower = getThrower();
        UUID ownerUuid = thrower == null ? null : thrower.getUniqueID();
        EnumFacing.Axis sourceAlignment = PortalLifecycleRules.axisFromMotion(motionX, motionZ);
        PortalPlacementRules.PlacementResult sourcePlacement = placementFor(x, y, z, hitPos, impactFacing, sourceAlignment);
        if (!sourcePlacement.isValid()) {
            setDead();
            return;
        }

        EntityPortal sourcePortal = new EntityPortal(world, impactFacing, sourceAlignment, portalGunUuid, true, true, ownerUuid);
        Vec3d sourcePos = sourcePlacement.getPosition();
        sourcePortal.setPosition(sourcePos.x, sourcePos.y, sourcePos.z);

        EnumFacing destinationFacing = destination.getFacing() == null ? EnumFacing.NORTH : destination.getFacing();
        EnumFacing.Axis destinationAlignment = destinationFacing.getAxis();
        EntityPortal destinationPortal = new EntityPortal(destinationWorld, destinationFacing, destinationAlignment, portalGunUuid, false, true, ownerUuid);
        destinationPortal.setPosition(destination.getX(), destination.getY(), destination.getZ());

        if (hasPortalConflict(world, sourcePortal.getEntityBoundingBox()) || hasPortalConflict(destinationWorld, destinationPortal.getEntityBoundingBox())) {
            setDead();
            return;
        }

        clearExistingPortals();
        world.spawnEntity(sourcePortal);
        destinationWorld.spawnEntity(destinationPortal);
        sourcePortal.setLinkedPortal(destinationPortal);
        destinationPortal.setLinkedPortal(sourcePortal);
        hasSpawnedPortal = true;
        setDead();
    }

    private PortalPlacementRules.PlacementResult placementFor(double x, double y, double z, BlockPos hitPos, EnumFacing impactFacing, EnumFacing.Axis alignment) {
        if (hitPos == null || impactFacing == null) {
            return PortalPlacementRules.PlacementResult.invalid();
        }

        if (impactFacing.getAxis() != EnumFacing.Axis.Y) {
            y -= 1.5D;
            BlockPos faceSpace = hitPos.offset(impactFacing);
            if (!world.isAirBlock(faceSpace.down())) {
                y += 1.0D;
                if (!world.isAirBlock(faceSpace.up())) {
                    return PortalPlacementRules.PlacementResult.invalid();
                }
            }
        } else if (alignment == EnumFacing.Axis.X) {
            x -= 0.5D;
        } else {
            z -= 0.5D;
        }
        return PortalPlacementRules.PlacementResult.valid(new Vec3d(x, y, z), alignment);
    }

    private Vec3d centeredImpact(BlockPos blockPos, EnumFacing impactFacing) {
        return new Vec3d(
                blockPos.getX() + 0.5D + impactFacing.getXOffset() * 0.501D,
                blockPos.getY() + 0.5D + impactFacing.getYOffset() * 0.501D,
                blockPos.getZ() + 0.5D + impactFacing.getZOffset() * 0.501D
        );
    }

    private EnumFacing primaryDirection() {
        double absX = Math.abs(motionX);
        double absY = Math.abs(motionY);
        double absZ = Math.abs(motionZ);
        if (absX > absY && absX > absZ) {
            return motionX > 0.0D ? EnumFacing.EAST : EnumFacing.WEST;
        } else if (absY > absX && absY > absZ) {
            return motionY > 0.0D ? EnumFacing.UP : EnumFacing.DOWN;
        }
        return motionZ > 0.0D ? EnumFacing.SOUTH : EnumFacing.NORTH;
    }

    private void clearExistingPortals() {
        for (World loadedWorld : DimensionManager.getWorlds()) {
            for (Entity entity : loadedWorld.loadedEntityList) {
                if (entity instanceof EntityPortal) {
                    EntityPortal portal = (EntityPortal) entity;
                    if (portalGunUuid.equals(portal.getPortalGunUuid())) {
                        portal.markDying();
                    }
                }
            }
        }
    }

    private void clearExistingClassicPortal(boolean primary) {
        for (World loadedWorld : DimensionManager.getWorlds()) {
            for (Entity entity : loadedWorld.loadedEntityList) {
                if (entity instanceof EntityPortal) {
                    EntityPortal portal = (EntityPortal) entity;
                    if (!portal.isDying() && portalGunUuid.equals(portal.getPortalGunUuid()) && portal.isPrimary() == primary) {
                        portal.markDying();
                    }
                }
            }
        }
    }

    private EntityPortal findOppositeClassicPortal(boolean primary) {
        for (World loadedWorld : DimensionManager.getWorlds()) {
            for (Entity entity : loadedWorld.loadedEntityList) {
                if (entity instanceof EntityPortal) {
                    EntityPortal portal = (EntityPortal) entity;
                    if (!portal.isDying() && portalGunUuid.equals(portal.getPortalGunUuid()) && portal.isPrimary() != primary) {
                        return portal;
                    }
                }
            }
        }
        return null;
    }

    private boolean hasPortalConflict(World targetWorld, AxisAlignedBB candidateBounds) {
        if (candidateBounds == null || targetWorld == null) {
            return false;
        }

        List<AxisAlignedBB> occupiedBoxes = new ArrayList<>();
        for (Entity entity : targetWorld.loadedEntityList) {
            if (entity instanceof EntityPortal) {
                EntityPortal portal = (EntityPortal) entity;
                if (!portal.isDying() && !portalGunUuid.equals(portal.getPortalGunUuid())) {
                    occupiedBoxes.add(portal.getEntityBoundingBox());
                }
            }
        }
        return PortalPlacementRules.conflicts(candidateBounds, occupiedBoxes);
    }
}
