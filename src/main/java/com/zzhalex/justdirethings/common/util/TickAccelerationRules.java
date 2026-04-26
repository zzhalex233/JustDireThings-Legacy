package com.zzhalex.justdirethings.common.util;

import com.zzhalex.justdirethings.config.JDTConfig;

public final class TickAccelerationRules {

    private TickAccelerationRules() {
    }

    public static float accelRateForLevel(int level) {
        if (level <= 0) {
            return 1.0F;
        }
        return (float) Math.pow(2.0D, level);
    }

    public static int extraTicksForLevel(int level) {
        return Math.max(0, Math.round(accelRateForLevel(level)) - 1);
    }

    public static int feCostForLevel(int level, boolean creative) {
        if (creative) {
            return 0;
        }
        return Math.round(accelRateForLevel(level) * Math.max(0, JDTConfig.timeWandRfCost));
    }

    public static int fluidCostForLevel(int level, boolean creative) {
        if (creative) {
            return 0;
        }
        return Math.round((float) (accelRateForLevel(level) * Math.max(0.0D, JDTConfig.timeWandFluidCost)));
    }

    public static int maxLevel() {
        return maxLevelForMultiplier(JDTConfig.timeWandMaxMultiplier);
    }

    public static int maxLevelForMultiplier(int maxMultiplier) {
        int sanitized = Math.max(1, maxMultiplier);
        int level = 0;
        while (sanitized > 1) {
            sanitized >>= 1;
            level++;
        }
        return level;
    }

    public static int nextLevel(int currentLevel) {
        return nextLevel(currentLevel, JDTConfig.timeWandMaxMultiplier);
    }

    public static int nextLevel(int currentLevel, int maxMultiplier) {
        int next = Math.max(1, currentLevel + 1);
        return next > maxLevelForMultiplier(maxMultiplier) ? -1 : next;
    }

    public static int initialDurationTicks() {
        return 20 * 30;
    }

    public static int bonusDurationTicks(int totalTime, int remainingTime) {
        int elapsed = Math.max(0, totalTime - remainingTime);
        return elapsed / 2;
    }

    public static float pitchForLevel(int level) {
        switch (level) {
            case 1:
                return 0.707107F;
            case 2:
                return 0.793701F;
            case 3:
                return 0.890899F;
            case 4:
                return 0.943874F;
            case 5:
                return 1.059463F;
            case 6:
                return 1.189207F;
            case 7:
                return 1.334840F;
            case 8:
                return 1.414214F;
            case 9:
                return 1.587401F;
            case 10:
                return 1.781797F;
            case 11:
                return 1.887749F;
            default:
                return 1.0F;
        }
    }
}
