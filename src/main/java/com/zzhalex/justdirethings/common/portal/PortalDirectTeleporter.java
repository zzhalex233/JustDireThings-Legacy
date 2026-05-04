package com.zzhalex.justdirethings.common.portal;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

public final class PortalDirectTeleporter implements ITeleporter {

    private final Vec3d targetPosition;

    public PortalDirectTeleporter() {
        this(null);
    }

    public PortalDirectTeleporter(Vec3d targetPosition) {
        this.targetPosition = targetPosition;
    }

    @Override
    public void placeEntity(World world, Entity entity, float yaw) {
        // Do not ask vanilla to search/create Nether portals; callers may provide the exact exit point.
        if (targetPosition != null) {
            entity.setLocationAndAngles(targetPosition.x, targetPosition.y, targetPosition.z, entity.rotationYaw, entity.rotationPitch);
            entity.fallDistance = 0.0F;
        }
    }

    @Override
    public boolean isVanilla() {
        return false;
    }
}
