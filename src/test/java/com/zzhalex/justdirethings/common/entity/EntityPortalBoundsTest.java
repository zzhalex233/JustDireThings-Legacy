package com.zzhalex.justdirethings.common.entity;

import net.minecraft.init.Bootstrap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntityPortalBoundsTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void floorPortalWithXAlignmentCoversTwoWholeBlocks() {
        EntityPortal portal = new EntityPortal(null, EnumFacing.UP, EnumFacing.Axis.X, UUID.randomUUID(), true, UUID.randomUUID());
        portal.setPosition(10.0D, 65.001D, 10.5D);

        AxisAlignedBB bounds = portal.getEntityBoundingBox();

        assertEquals(10.0D, bounds.minX, 0.001D);
        assertEquals(12.0D, bounds.maxX, 0.001D);
        assertEquals(10.0D, bounds.minZ, 0.001D);
        assertEquals(11.0D, bounds.maxZ, 0.001D);
        assertEquals(65.001D, bounds.minY, 0.001D);
        assertEquals(65.201D, bounds.maxY, 0.001D);
    }

    @Test
    void floorPortalWithZAlignmentCoversTwoWholeBlocks() {
        EntityPortal portal = new EntityPortal(null, EnumFacing.UP, EnumFacing.Axis.Z, UUID.randomUUID(), true, UUID.randomUUID());
        portal.setPosition(10.5D, 65.001D, 10.0D);

        AxisAlignedBB bounds = portal.getEntityBoundingBox();

        assertEquals(10.0D, bounds.minX, 0.001D);
        assertEquals(11.0D, bounds.maxX, 0.001D);
        assertEquals(10.0D, bounds.minZ, 0.001D);
        assertEquals(12.0D, bounds.maxZ, 0.001D);
        assertEquals(65.001D, bounds.minY, 0.001D);
        assertEquals(65.201D, bounds.maxY, 0.001D);
    }
}
