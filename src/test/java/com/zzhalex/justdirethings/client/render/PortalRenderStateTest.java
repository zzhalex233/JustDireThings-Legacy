package com.zzhalex.justdirethings.client.render;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PortalRenderStateTest {

    @Test
    void openingProgressClampsToOne() {
        assertEquals(1.0F, PortalRenderState.animationProgress(20, 0, false, 0.0F, 5), 0.001F);
    }

    @Test
    void dyingProgressRunsBackward() {
        assertEquals(0.5F, PortalRenderState.animationProgress(20, 2, true, 0.5F, 5), 0.001F);
    }

    @Test
    void horizontalFrameBoxesUseFlatPortalPlane() {
        AxisAlignedBB bounds = new AxisAlignedBB(10.0D, 65.0D, 10.0D, 12.0D, 65.2D, 11.0D);

        List<AxisAlignedBB> frame = PortalRenderState.frameBoxes(bounds, 0.025D, EnumFacing.Axis.Y);

        assertEquals(4, frame.size());
        assertEquals(65.0D, frame.get(0).minY, 0.001D);
        assertEquals(65.01D, frame.get(0).maxY, 0.001D);
        assertEquals(10.0125D, frame.get(0).minX, 0.001D);
        assertEquals(10.0375D, frame.get(0).maxX, 0.001D);
        assertEquals(10.0125D, frame.get(2).minZ, 0.001D);
        assertEquals(10.0375D, frame.get(2).maxZ, 0.001D);
    }

    @Test
    void verticalZFrameBoxesKeepPortalDepthSoTheyDoNotCollapseToALine() {
        AxisAlignedBB bounds = new AxisAlignedBB(10.0D, 65.0D, 9.9D, 11.0D, 67.0D, 10.1D);

        List<AxisAlignedBB> frame = PortalRenderState.frameBoxes(bounds, 0.025D, EnumFacing.Axis.Z);

        assertEquals(4, frame.size());
        assertEquals(9.995D, frame.get(0).minZ, 0.001D);
        assertEquals(10.005D, frame.get(0).maxZ, 0.001D);
        assertEquals(65.0125D, frame.get(0).minY, 0.001D);
        assertEquals(66.9875D, frame.get(0).maxY, 0.001D);
    }

    @Test
    void portalPlaneAxisComesFromFacingNotBoundingBoxGuesswork() {
        assertEquals(EnumFacing.Axis.Z, PortalRenderState.planeAxisForFacing(EnumFacing.NORTH));
        assertEquals(EnumFacing.Axis.X, PortalRenderState.planeAxisForFacing(EnumFacing.EAST));
        assertEquals(EnumFacing.Axis.Y, PortalRenderState.planeAxisForFacing(EnumFacing.UP));
    }
}
