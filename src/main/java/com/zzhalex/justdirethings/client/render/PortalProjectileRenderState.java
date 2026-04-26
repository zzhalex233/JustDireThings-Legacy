package com.zzhalex.justdirethings.client.render;

import net.minecraft.util.math.AxisAlignedBB;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class PortalProjectileRenderState {

    private static final double MODEL_UNIT = 1.0D / 16.0D;
    private static final List<AxisAlignedBB> MODEL_BOXES = Collections.unmodifiableList(Arrays.asList(
            box(-4.0D, -4.0D, -1.0D, 8.0D, 8.0D, 2.0D),
            box(-1.0D, -4.0D, -4.0D, 2.0D, 8.0D, 8.0D),
            box(-4.0D, -1.0D, -4.0D, 8.0D, 2.0D, 8.0D)
    ));

    private PortalProjectileRenderState() {
    }

    public static List<AxisAlignedBB> modelBoxes() {
        return MODEL_BOXES;
    }

    public static Rotation rotationForAge(int ticksExisted, float partialTicks) {
        float age = ticksExisted + partialTicks;
        return new Rotation(
                Math.sin(age * 0.1D) * 180.0D,
                Math.cos(age * 0.1D) * 180.0D,
                Math.sin(age * 0.15D) * 360.0D
        );
    }

    private static AxisAlignedBB box(double x, double y, double z, double width, double height, double depth) {
        return new AxisAlignedBB(
                x * MODEL_UNIT,
                y * MODEL_UNIT,
                z * MODEL_UNIT,
                (x + width) * MODEL_UNIT,
                (y + height) * MODEL_UNIT,
                (z + depth) * MODEL_UNIT
        );
    }

    public static final class Rotation {
        private final double yawDegrees;
        private final double pitchDegrees;
        private final double rollDegrees;

        private Rotation(double yawDegrees, double pitchDegrees, double rollDegrees) {
            this.yawDegrees = yawDegrees;
            this.pitchDegrees = pitchDegrees;
            this.rollDegrees = rollDegrees;
        }

        public double getYawDegrees() {
            return yawDegrees;
        }

        public double getPitchDegrees() {
            return pitchDegrees;
        }

        public double getRollDegrees() {
            return rollDegrees;
        }
    }
}
