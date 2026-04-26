package com.zzhalex.justdirethings.common.portal;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public final class PortalTransformRules {

    private static final double EPSILON = 1.0E-3D;

    private PortalTransformRules() {
    }

    public static Vec3d teleportPosition(
            AxisAlignedBB entityBounds,
            AxisAlignedBB entryPortalBounds,
            EnumFacing entryFacing,
            EnumFacing.Axis entryAlignment,
            AxisAlignedBB exitPortalBounds,
            EnumFacing exitFacing,
            EnumFacing.Axis exitAlignment
    ) {
        double entityFraction = entryFraction(entityBounds, entryPortalBounds, entryFacing, entryAlignment);
        Vec3d teleportTo;

        if (exitFacing.getAxis() == EnumFacing.Axis.Y) {
            if (exitAlignment == EnumFacing.Axis.X) {
                double offset = entityFraction * sizeX(exitPortalBounds);
                double buffer = sizeX(entityBounds) / 2.0D + EPSILON;
                offset = clamp(offset, buffer, sizeX(exitPortalBounds) - buffer);
                teleportTo = new Vec3d(exitPortalBounds.minX + offset, centerY(exitPortalBounds), centerZ(exitPortalBounds));
            } else {
                double offset = entityFraction * sizeZ(exitPortalBounds);
                double buffer = sizeZ(entityBounds) / 2.0D + EPSILON;
                offset = clamp(offset, buffer, sizeZ(exitPortalBounds) - buffer);
                teleportTo = new Vec3d(centerX(exitPortalBounds), centerY(exitPortalBounds), exitPortalBounds.minZ + offset);
            }
        } else {
            teleportTo = new Vec3d(
                    centerX(exitPortalBounds),
                    exitPortalBounds.minY + entityFraction * sizeY(exitPortalBounds),
                    centerZ(exitPortalBounds)
            );
        }

        if (exitFacing == EnumFacing.DOWN) {
            teleportTo = teleportTo.add(0.0D, -sizeY(entityBounds), 0.0D);
        } else if (exitFacing != EnumFacing.UP) {
            if (exitFacing.getAxis() == EnumFacing.Axis.X) {
                teleportTo = teleportTo.add(
                        exitFacing.getXOffset() * (sizeX(exitPortalBounds) / 2.0D + sizeX(entityBounds) / 2.0D),
                        0.0D,
                        0.0D
                );
            } else {
                teleportTo = teleportTo.add(
                        0.0D,
                        0.0D,
                        exitFacing.getZOffset() * (sizeZ(exitPortalBounds) / 2.0D + sizeZ(entityBounds) / 2.0D)
                );
            }
        }

        return teleportTo.add(
                exitFacing.getXOffset() * EPSILON,
                exitFacing.getYOffset() * EPSILON,
                exitFacing.getZOffset() * EPSILON
        );
    }

    public static Vec3d transformMotion(
            Vec3d motion,
            EnumFacing fromFacing,
            EnumFacing.Axis fromAlignment,
            EnumFacing toFacing,
            EnumFacing.Axis toAlignment
    ) {
        if (motion == null || motion.lengthSquared() <= 0.0D) {
            return Vec3d.ZERO;
        }

        Basis fromBasis = basis(fromFacing, fromAlignment);
        Basis toBasis = basis(toFacing, toAlignment);

        Vec3d localMotion = new Vec3d(
                motion.dotProduct(fromBasis.right),
                motion.dotProduct(fromBasis.up),
                motion.dotProduct(fromBasis.normal)
        );

        return toBasis.right.scale(localMotion.x)
                .add(toBasis.up.scale(localMotion.y))
                .add(toBasis.normal.scale(localMotion.z));
    }

    public static Rotation rotationFromVector(Vec3d vector) {
        double hyp = Math.sqrt(vector.x * vector.x + vector.z * vector.z);
        float pitch = wrapDegrees((float) (-(Math.atan2(vector.y, hyp) * 180.0D / Math.PI)));
        float yaw = wrapDegrees((float) (Math.atan2(vector.z, vector.x) * 180.0D / Math.PI) - 90.0F);
        return new Rotation(yaw, pitch);
    }

    private static double entryFraction(
            AxisAlignedBB entityBounds,
            AxisAlignedBB portalBounds,
            EnumFacing entryFacing,
            EnumFacing.Axis entryAlignment
    ) {
        double fraction;
        if (entryFacing.getAxis() == EnumFacing.Axis.Y) {
            if (entryAlignment == EnumFacing.Axis.X) {
                fraction = Math.abs((((entityBounds.maxX + entityBounds.minX) / 2.0D) - portalBounds.minX) / sizeX(portalBounds));
            } else {
                fraction = Math.abs((((entityBounds.maxZ + entityBounds.minZ) / 2.0D) - portalBounds.minZ) / sizeZ(portalBounds));
            }
        } else {
            fraction = (entityBounds.minY - portalBounds.minY) / sizeY(portalBounds);
        }
        return clamp(fraction, 0.0D, 1.0D);
    }

    private static Basis basis(EnumFacing facing, EnumFacing.Axis alignment) {
        Vec3d normal = new Vec3d(facing.getDirectionVec()).normalize();
        Vec3d up;
        if (facing.getAxis() == EnumFacing.Axis.Y) {
            up = alignment == EnumFacing.Axis.X ? new Vec3d(1.0D, 0.0D, 0.0D) : new Vec3d(0.0D, 0.0D, 1.0D);
        } else {
            up = new Vec3d(0.0D, 1.0D, 0.0D);
        }
        Vec3d right = normal.crossProduct(up).normalize();
        return new Basis(normal, up.normalize(), right);
    }

    private static double sizeX(AxisAlignedBB bounds) {
        return bounds.maxX - bounds.minX;
    }

    private static double sizeY(AxisAlignedBB bounds) {
        return bounds.maxY - bounds.minY;
    }

    private static double sizeZ(AxisAlignedBB bounds) {
        return bounds.maxZ - bounds.minZ;
    }

    private static double centerX(AxisAlignedBB bounds) {
        return (bounds.minX + bounds.maxX) / 2.0D;
    }

    private static double centerY(AxisAlignedBB bounds) {
        return (bounds.minY + bounds.maxY) / 2.0D;
    }

    private static double centerZ(AxisAlignedBB bounds) {
        return (bounds.minZ + bounds.maxZ) / 2.0D;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float wrapDegrees(float value) {
        float wrapped = value % 360.0F;
        if (wrapped >= 180.0F) {
            wrapped -= 360.0F;
        }
        if (wrapped < -180.0F) {
            wrapped += 360.0F;
        }
        return wrapped;
    }

    private static final class Basis {
        private final Vec3d normal;
        private final Vec3d up;
        private final Vec3d right;

        private Basis(Vec3d normal, Vec3d up, Vec3d right) {
            this.normal = normal;
            this.up = up;
            this.right = right;
        }
    }

    public static final class Rotation {
        private final float yaw;
        private final float pitch;

        public Rotation(float yaw, float pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public float getYaw() {
            return yaw;
        }

        public float getPitch() {
            return pitch;
        }
    }
}
