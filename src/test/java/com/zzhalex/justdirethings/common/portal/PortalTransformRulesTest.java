package com.zzhalex.justdirethings.common.portal;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalTransformRulesTest {

    @Test
    void transformMotionRotatesForwardBetweenWallPortals() {
        Vec3d transformed = PortalTransformRules.transformMotion(
                new Vec3d(0.0D, 0.0D, -1.0D),
                EnumFacing.NORTH,
                EnumFacing.Axis.Z,
                EnumFacing.WEST,
                EnumFacing.Axis.X
        );

        assertEquals(-1.0D, transformed.x, 0.001D);
        assertEquals(0.0D, transformed.y, 0.001D);
        assertEquals(0.0D, transformed.z, 0.001D);
    }

    @Test
    void teleportPositionPreservesWallPortalHeightFraction() {
        AxisAlignedBB entityBounds = new AxisAlignedBB(4.7D, 65.0D, 4.7D, 5.3D, 66.8D, 5.3D);
        AxisAlignedBB entryPortalBounds = new AxisAlignedBB(4.5D, 64.0D, 4.9375D, 5.5D, 66.0D, 5.0625D);
        AxisAlignedBB exitPortalBounds = new AxisAlignedBB(9.5D, 30.0D, 19.9375D, 10.5D, 32.0D, 20.0625D);

        Vec3d teleportTo = PortalTransformRules.teleportPosition(
                entityBounds,
                entryPortalBounds,
                EnumFacing.NORTH,
                EnumFacing.Axis.Z,
                exitPortalBounds,
                EnumFacing.SOUTH,
                EnumFacing.Axis.Z
        );

        assertEquals(10.0D, teleportTo.x, 0.001D);
        assertEquals(31.0D, teleportTo.y, 0.001D);
        assertTrue(teleportTo.z > 20.3D);
    }

    @Test
    void teleportPositionUsesHorizontalFractionForFloorPortals() {
        AxisAlignedBB entityBounds = new AxisAlignedBB(0.2D, 63.5D, 0.7D, 0.8D, 64.1D, 1.3D);
        AxisAlignedBB entryPortalBounds = new AxisAlignedBB(0.0D, 63.9375D, 0.5D, 2.0D, 64.0625D, 1.5D);
        AxisAlignedBB exitPortalBounds = new AxisAlignedBB(9.9375D, 30.0D, 19.5D, 10.0625D, 32.0D, 20.5D);

        Vec3d teleportTo = PortalTransformRules.teleportPosition(
                entityBounds,
                entryPortalBounds,
                EnumFacing.UP,
                EnumFacing.Axis.X,
                exitPortalBounds,
                EnumFacing.EAST,
                EnumFacing.Axis.X
        );

        assertTrue(teleportTo.x > 10.3D);
        assertEquals(30.5D, teleportTo.y, 0.001D);
        assertEquals(20.0D, teleportTo.z, 0.001D);
    }
}
