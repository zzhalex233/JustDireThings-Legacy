package com.zzhalex.justdirethings.common.portal;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalPlacementRulesTest {

    @Test
    void wallPlacementStartsOneBlockLowerWhenSpaceBelowIsClear() {
        PortalPlacementRules.PlacementResult result = PortalPlacementRules.placementForImpact(
                new BlockPos(10, 64, 10),
                EnumFacing.NORTH,
                EnumFacing.Axis.Z,
                false,
                false
        );

        assertTrue(result.isValid());
        assertEquals(10.5D, result.getPosition().x, 0.001D);
        assertEquals(63.0D, result.getPosition().y, 0.001D);
        assertEquals(9.999D, result.getPosition().z, 0.001D);
    }

    @Test
    void floorPlacementUsesWholeTwoBlockFootprintForXAlignment() {
        PortalPlacementRules.PlacementResult result = PortalPlacementRules.placementForImpact(
                new BlockPos(10, 64, 10),
                EnumFacing.UP,
                EnumFacing.Axis.X,
                false,
                false
        );

        assertTrue(result.isValid());
        assertEquals(10.0D, result.getPosition().x, 0.001D);
        assertEquals(65.001D, result.getPosition().y, 0.001D);
        assertEquals(10.5D, result.getPosition().z, 0.001D);
    }

    @Test
    void floorPlacementUsesWholeTwoBlockFootprintForZAlignment() {
        PortalPlacementRules.PlacementResult result = PortalPlacementRules.placementForImpact(
                new BlockPos(10, 64, 10),
                EnumFacing.UP,
                EnumFacing.Axis.Z,
                false,
                false
        );

        assertTrue(result.isValid());
        assertEquals(10.5D, result.getPosition().x, 0.001D);
        assertEquals(65.001D, result.getPosition().y, 0.001D);
        assertEquals(10.0D, result.getPosition().z, 0.001D);
    }

    @Test
    void ceilingPlacementSitsJustBelowHitFace() {
        PortalPlacementRules.PlacementResult result = PortalPlacementRules.placementForImpact(
                new BlockPos(10, 64, 10),
                EnumFacing.DOWN,
                EnumFacing.Axis.X,
                false,
                false
        );

        assertTrue(result.isValid());
        assertEquals(10.0D, result.getPosition().x, 0.001D);
        assertEquals(63.999D, result.getPosition().y, 0.001D);
        assertEquals(10.5D, result.getPosition().z, 0.001D);
    }

    @Test
    void wallPlacementMovesUpWhenSupportSpaceBelowIsBlocked() {
        PortalPlacementRules.PlacementResult result = PortalPlacementRules.placementForImpact(
                new BlockPos(10, 64, 10),
                EnumFacing.SOUTH,
                EnumFacing.Axis.Z,
                true,
                false
        );

        assertTrue(result.isValid());
        assertEquals(64.0D, result.getPosition().y, 0.001D);
    }

    @Test
    void wallPlacementFailsWhenBothSupportSpacesAreBlocked() {
        PortalPlacementRules.PlacementResult result = PortalPlacementRules.placementForImpact(
                new BlockPos(10, 64, 10),
                EnumFacing.EAST,
                EnumFacing.Axis.X,
                true,
                true
        );

        assertFalse(result.isValid());
    }

    @Test
    void conflictCheckFlagsIntersectingPortals() {
        AxisAlignedBB candidate = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 2.0D, 0.125D);
        AxisAlignedBB occupied = new AxisAlignedBB(0.0D, 0.0D, 0.05D, 1.0D, 2.0D, 0.175D);

        assertTrue(PortalPlacementRules.conflicts(candidate, Collections.singleton(occupied)));
        assertFalse(PortalPlacementRules.conflicts(candidate, Collections.emptyList()));
    }

    @Test
    void projectileIgnoresBlocksWithoutCollisionShape() {
        assertFalse(PortalPlacementRules.hasProjectileCollision(null));
        assertFalse(PortalPlacementRules.hasProjectileCollision(Block.NULL_AABB));
        assertTrue(PortalPlacementRules.hasProjectileCollision(new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D)));
    }
}
