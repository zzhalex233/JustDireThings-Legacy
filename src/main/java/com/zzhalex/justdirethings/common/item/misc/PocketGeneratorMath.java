package com.zzhalex.justdirethings.common.item.misc;

public final class PocketGeneratorMath {

    private PocketGeneratorMath() {
    }

    public static int fePerTick(int fePerFuelTick, int burnMultiplier) {
        return Math.max(0, fePerFuelTick) * Math.max(0, burnMultiplier);
    }

    public static int burnTicksRemaining(int vanillaBurnTicks, int burnMultiplier) {
        if (vanillaBurnTicks <= 0) {
            return 0;
        }
        if (burnMultiplier <= 1) {
            return vanillaBurnTicks;
        }
        return Math.max(1, vanillaBurnTicks / burnMultiplier);
    }

    public static double weightedBurnMultiplier(int existingFuel, double existingMultiplier, int addedFuel, double addedMultiplier) {
        int totalFuel = Math.max(0, existingFuel) + Math.max(0, addedFuel);
        if (totalFuel <= 0) {
            return 1.0D;
        }
        return ((Math.max(0, existingFuel) * existingMultiplier) + (Math.max(0, addedFuel) * addedMultiplier)) / totalFuel;
    }
}
