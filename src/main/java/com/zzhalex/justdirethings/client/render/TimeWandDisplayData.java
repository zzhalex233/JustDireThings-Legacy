package com.zzhalex.justdirethings.client.render;

import com.zzhalex.justdirethings.common.util.TickAccelerationRules;

import java.util.Locale;

public final class TimeWandDisplayData {

    private TimeWandDisplayData() {
    }

    public static String accelerationLabel(int tickLevel) {
        return "x" + Math.round(TickAccelerationRules.accelRateForLevel(tickLevel));
    }

    public static String remainingTimeLabel(int remainingTicks) {
        float seconds = Math.max(0, remainingTicks) / 20.0F;
        return String.format(Locale.ROOT, "%.2fs", seconds);
    }

    public static float tickRateProgress(int tickLevel, int maxMultiplier) {
        int maxLevel = TickAccelerationRules.maxLevelForMultiplier(maxMultiplier);
        if (maxLevel <= 0) {
            return 1.0F;
        }
        return clamp(tickLevel / (float) maxLevel);
    }

    public static float remainingTimeProgress(int remainingTicks, int totalTicks) {
        if (totalTicks <= 0) {
            return remainingTicks > 0 ? 1.0F : 0.0F;
        }
        return clamp(remainingTicks / (float) totalTicks);
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
