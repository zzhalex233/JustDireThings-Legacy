package com.zzhalex.justdirethings.common.portal;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

public final class PortalVelocityRules {

    private static final double VELOCITY_THRESHOLD = 0.2D;

    private PortalVelocityRules() {
    }

    public static Vec3d sampledVelocity(Vec3d currentPosition, Vec3d lastPosition, Vec3d lastLastPosition) {
        if (lastPosition == null) {
            return Vec3d.ZERO;
        }
        if (lastLastPosition != null) {
            return lastPosition.subtract(lastLastPosition);
        }
        if (currentPosition == null) {
            return Vec3d.ZERO;
        }
        return currentPosition.subtract(lastPosition);
    }

    public static Vec3d inheritedVelocity(
            Vec3d velocity,
            EnumFacing entryFacing,
            EnumFacing.Axis entryAlignment,
            EnumFacing exitFacing,
            EnumFacing.Axis exitAlignment
    ) {
        if (!shouldInherit(velocity)) {
            return Vec3d.ZERO;
        }
        return PortalTransformRules.transformMotion(velocity, entryFacing, entryAlignment, exitFacing, exitAlignment);
    }

    public static boolean shouldInherit(Vec3d velocity) {
        if (velocity == null || velocity.lengthSquared() <= 0.0D) {
            return false;
        }
        return Math.abs(velocity.x) > VELOCITY_THRESHOLD
                || Math.abs(velocity.y) > VELOCITY_THRESHOLD
                || Math.abs(velocity.z) > VELOCITY_THRESHOLD
                || velocity.y > 0.0D;
    }
}
