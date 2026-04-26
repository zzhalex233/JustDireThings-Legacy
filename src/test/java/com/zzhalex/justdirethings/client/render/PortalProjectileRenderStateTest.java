package com.zzhalex.justdirethings.client.render;

import net.minecraft.util.math.AxisAlignedBB;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PortalProjectileRenderStateTest {

    @Test
    void projectileModelUsesThreeCrossedOriginalBoxes() {
        List<AxisAlignedBB> boxes = PortalProjectileRenderState.modelBoxes();

        assertEquals(3, boxes.size());
        assertEquals(-0.25D, boxes.get(0).minX, 0.001D);
        assertEquals(0.25D, boxes.get(0).maxX, 0.001D);
        assertEquals(-0.25D, boxes.get(0).minY, 0.001D);
        assertEquals(0.25D, boxes.get(0).maxY, 0.001D);
        assertEquals(-0.0625D, boxes.get(0).minZ, 0.001D);
        assertEquals(0.0625D, boxes.get(0).maxZ, 0.001D);

        assertEquals(-0.0625D, boxes.get(1).minX, 0.001D);
        assertEquals(0.0625D, boxes.get(1).maxX, 0.001D);
        assertEquals(-0.25D, boxes.get(1).minY, 0.001D);
        assertEquals(0.25D, boxes.get(1).maxY, 0.001D);
        assertEquals(-0.25D, boxes.get(1).minZ, 0.001D);
        assertEquals(0.25D, boxes.get(1).maxZ, 0.001D);

        assertEquals(-0.25D, boxes.get(2).minX, 0.001D);
        assertEquals(0.25D, boxes.get(2).maxX, 0.001D);
        assertEquals(-0.0625D, boxes.get(2).minY, 0.001D);
        assertEquals(0.0625D, boxes.get(2).maxY, 0.001D);
        assertEquals(-0.25D, boxes.get(2).minZ, 0.001D);
        assertEquals(0.25D, boxes.get(2).maxZ, 0.001D);
    }

    @Test
    void projectileRotationMatchesUpstreamSineMotion() {
        PortalProjectileRenderState.Rotation rotation = PortalProjectileRenderState.rotationForAge(10, 0.0F);

        assertEquals(Math.sin(1.0D) * 180.0D, rotation.getYawDegrees(), 0.001D);
        assertEquals(Math.cos(1.0D) * 180.0D, rotation.getPitchDegrees(), 0.001D);
        assertEquals(Math.sin(1.5D) * 360.0D, rotation.getRollDegrees(), 0.001D);
    }
}
