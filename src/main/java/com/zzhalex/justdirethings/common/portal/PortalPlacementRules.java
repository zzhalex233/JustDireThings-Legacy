package com.zzhalex.justdirethings.common.portal;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;

public final class PortalPlacementRules {

    private PortalPlacementRules() {
    }

    public static PlacementResult placementForImpact(
            BlockPos hitPos,
            EnumFacing impactFacing,
            EnumFacing.Axis alignment,
            boolean blockedBelow,
            boolean blockedAbove
    ) {
        if (hitPos == null || impactFacing == null) {
            return PlacementResult.invalid();
        }

        if (impactFacing.getAxis() == EnumFacing.Axis.Y) {
            double x = hitPos.getX() + 0.5D;
            double y = hitPos.getY() + (impactFacing == EnumFacing.UP ? 1.001D : -0.001D);
            double z = hitPos.getZ() + 0.5D;
            if (alignment == EnumFacing.Axis.X) {
                x -= 0.5D;
            } else {
                z -= 0.5D;
            }
            return PlacementResult.valid(new Vec3d(x, y, z), alignment);
        }

        double y = hitPos.getY() - 1.0D;
        if (blockedBelow) {
            y += 1.0D;
            if (blockedAbove) {
                return PlacementResult.invalid();
            }
        }

        return PlacementResult.valid(
                new Vec3d(
                        hitPos.getX() + 0.5D + impactFacing.getXOffset() * 0.501D,
                        y,
                        hitPos.getZ() + 0.5D + impactFacing.getZOffset() * 0.501D
                ),
                alignment
        );
    }

    public static boolean hasProjectileCollision(AxisAlignedBB collisionBox) {
        return collisionBox != null && collisionBox != net.minecraft.block.Block.NULL_AABB;
    }

    public static boolean conflicts(AxisAlignedBB candidate, Collection<? extends AxisAlignedBB> occupiedBoxes) {
        if (candidate == null || occupiedBoxes == null || occupiedBoxes.isEmpty()) {
            return false;
        }

        AxisAlignedBB shrunkCandidate = candidate.grow(-0.1D);
        for (AxisAlignedBB occupied : occupiedBoxes) {
            if (occupied != null && occupied.intersects(shrunkCandidate)) {
                return true;
            }
        }
        return false;
    }

    public static final class PlacementResult {

        private static final PlacementResult INVALID = new PlacementResult(null, EnumFacing.Axis.Z, false);

        private final Vec3d position;
        private final EnumFacing.Axis alignment;
        private final boolean valid;

        private PlacementResult(Vec3d position, EnumFacing.Axis alignment, boolean valid) {
            this.position = position;
            this.alignment = alignment == null ? EnumFacing.Axis.Z : alignment;
            this.valid = valid;
        }

        public static PlacementResult valid(Vec3d position, EnumFacing.Axis alignment) {
            return new PlacementResult(position, alignment, true);
        }

        public static PlacementResult invalid() {
            return INVALID;
        }

        public Vec3d getPosition() {
            return position;
        }

        public EnumFacing.Axis getAlignment() {
            return alignment;
        }

        public boolean isValid() {
            return valid && position != null;
        }
    }
}
