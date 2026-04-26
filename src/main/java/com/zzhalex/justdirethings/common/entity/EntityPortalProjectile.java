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
        if (!world.isRemote) {
            if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
                if (!canImpactPortalSurface(result)) {
                    return;
                }
                if (classicMode) {
                    spawnClassicPortal(result);
                } else {
                    spawnPortalPair(result);
                }
            }
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
        if (portalGunUuid == null || result.hitVec == null) {
            return;
        }

        EntityLivingBase thrower = getThrower();
        UUID ownerUuid = thrower == null ? null : thrower.getUniqueID();
        EnumFacing impactFacing = result.sideHit == null ? EnumFacing.NORTH : result.sideHit;
        EnumFacing.Axis alignment = PortalLifecycleRules.axisFromMotion(motionX, motionZ);
        PortalPlacementRules.PlacementResult placement = placementFor(result.getBlockPos(), impactFacing, alignment);
        if (!placement.isValid()) {
            return;
        }

        EntityPortal portal = new EntityPortal(world, impactFacing, alignment, portalGunUuid, primaryPortal, ownerUuid);
        Vec3d sourcePos = placement.getPosition();
        portal.setPosition(sourcePos.x, sourcePos.y, sourcePos.z);
        if (hasPortalConflict(world, portal.getEntityBoundingBox())) {
            return;
        }

        clearExistingClassicPortal(primaryPortal);
        EntityPortal oppositePortal = findOppositeClassicPortal(primaryPortal);
        if (oppositePortal != null) {
            portal.setLinkedPortal(oppositePortal);
            oppositePortal.setLinkedPortal(portal);
        }
        world.spawnEntity(portal);
    }

    private void spawnPortalPair(RayTraceResult result) {
        if (portalGunUuid == null || destination == null || destination.isEmpty() || !(result.hitVec != null)) {
            return;
        }

        World destinationWorld = DimensionManager.getWorld(destination.getDimension());
        if (destinationWorld == null) {
            return;
        }

        EntityLivingBase thrower = getThrower();
        UUID ownerUuid = thrower == null ? null : thrower.getUniqueID();
        EnumFacing impactFacing = result.sideHit == null ? EnumFacing.NORTH : result.sideHit;
        EnumFacing.Axis sourceAlignment = PortalLifecycleRules.axisFromMotion(motionX, motionZ);
        PortalPlacementRules.PlacementResult sourcePlacement = placementFor(result.getBlockPos(), impactFacing, sourceAlignment);
        if (!sourcePlacement.isValid()) {
            return;
        }

        EntityPortal sourcePortal = new EntityPortal(world, impactFacing, sourceAlignment, portalGunUuid, true, ownerUuid);
        Vec3d sourcePos = sourcePlacement.getPosition();
        sourcePortal.setPosition(sourcePos.x, sourcePos.y, sourcePos.z);

        EnumFacing destinationFacing = destination.getFacing() == null ? EnumFacing.NORTH : destination.getFacing();
        EnumFacing.Axis destinationAlignment = destinationFacing.getAxis() == EnumFacing.Axis.Y ? sourceAlignment : destinationFacing.getAxis();
        EntityPortal destinationPortal = new EntityPortal(destinationWorld, destinationFacing, destinationAlignment, portalGunUuid, false, ownerUuid);
        destinationPortal.setPosition(destination.getX(), destination.getY(), destination.getZ());

        if (hasPortalConflict(world, sourcePortal.getEntityBoundingBox()) || hasPortalConflict(destinationWorld, destinationPortal.getEntityBoundingBox())) {
            return;
        }

        clearExistingPortals();
        sourcePortal.setLinkedPortal(destinationPortal);
        destinationPortal.setLinkedPortal(sourcePortal);
        world.spawnEntity(sourcePortal);
        destinationWorld.spawnEntity(destinationPortal);
    }

    private PortalPlacementRules.PlacementResult placementFor(BlockPos hitPos, EnumFacing impactFacing, EnumFacing.Axis alignment) {
        if (hitPos == null || impactFacing == null) {
            return PortalPlacementRules.PlacementResult.invalid();
        }

        BlockPos faceSpace = hitPos.offset(impactFacing);
        return PortalPlacementRules.placementForImpact(
                hitPos,
                impactFacing,
                alignment,
                !world.isAirBlock(faceSpace.down()),
                !world.isAirBlock(faceSpace.up())
        );
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
                    if (portalGunUuid.equals(portal.getPortalGunUuid()) && portal.isPrimary() == primary) {
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
                if (!portalGunUuid.equals(portal.getPortalGunUuid())) {
                    occupiedBoxes.add(portal.getEntityBoundingBox());
                }
            }
        }
        return PortalPlacementRules.conflicts(candidateBounds, occupiedBoxes);
    }
}
