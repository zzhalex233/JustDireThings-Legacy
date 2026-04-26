package com.zzhalex.justdirethings.common.tile.machine;

public final class GeneratorMath {

    private GeneratorMath() {
    }

    public static boolean canStartBurn(int fuelValue, int storedEnergy, int maxEnergy) {
        return fuelValue > 0 && storedEnergy < maxEnergy;
    }

    public static int burnTicksRemaining(int burnTime, int burnMultiplier) {
        if (burnTime <= 0) {
            return 0;
        }
        if (burnMultiplier <= 1) {
            return burnTime;
        }
        return Math.max(1, burnTime / burnMultiplier);
    }

    public static int energyPerTick(int fePerFuelTick, int burnMultiplier) {
        return Math.max(0, fePerFuelTick) * Math.max(1, burnMultiplier);
    }

    public static int energyToInsert(int storedEnergy, int maxEnergy, int generatedEnergy) {
        return Math.max(0, Math.min(Math.max(0, maxEnergy - storedEnergy), Math.max(0, generatedEnergy)));
    }
}
