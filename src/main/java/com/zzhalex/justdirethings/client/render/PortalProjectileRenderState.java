package com.zzhalex.justdirethings.client.render;

public final class PortalProjectileRenderState {

    private PortalProjectileRenderState() {
    }

    public static Rotation rotationForAge(int ticksExisted, float partialTicks) {
        float age = ticksExisted + partialTicks;
        return new Rotation(
                Math.sin(age * 0.1D) * 180.0D,
                Math.cos(age * 0.1D) * 180.0D,
                Math.sin(age * 0.15D) * 360.0D
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
