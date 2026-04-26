package com.zzhalex.justdirethings.common.portal;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PortalVelocityRulesTest {

    @Test
    void prefersLastCompletedTickVelocityWhenTwoSamplesExist() {
        Vec3d current = new Vec3d(0.3D, 64.0D, 0.0D);
        Vec3d last = new Vec3d(0.0D, 64.0D, 0.0D);
        Vec3d lastLast = new Vec3d(-0.5D, 64.0D, 0.0D);

        Vec3d velocity = PortalVelocityRules.sampledVelocity(current, last, lastLast);

        assertEquals(0.5D, velocity.x, 0.001D);
        assertEquals(0.0D, velocity.y, 0.001D);
        assertEquals(0.0D, velocity.z, 0.001D);
    }

    @Test
    void ignoresSmallHorizontalVelocityLikeUpstreamThreshold() {
        Vec3d velocity = new Vec3d(0.1D, 0.0D, 0.1D);

        assertEquals(Vec3d.ZERO, PortalVelocityRules.inheritedVelocity(
                velocity,
                EnumFacing.NORTH,
                EnumFacing.Axis.Z,
                EnumFacing.SOUTH,
                EnumFacing.Axis.Z
        ));
    }

    @Test
    void keepsUpwardVelocityEvenBelowHorizontalThreshold() {
        Vec3d velocity = new Vec3d(0.0D, 0.05D, 0.0D);

        Vec3d inherited = PortalVelocityRules.inheritedVelocity(
                velocity,
                EnumFacing.NORTH,
                EnumFacing.Axis.Z,
                EnumFacing.SOUTH,
                EnumFacing.Axis.Z
        );

        assertEquals(0.0D, inherited.x, 0.001D);
        assertEquals(0.05D, inherited.y, 0.001D);
        assertEquals(0.0D, inherited.z, 0.001D);
    }
}
