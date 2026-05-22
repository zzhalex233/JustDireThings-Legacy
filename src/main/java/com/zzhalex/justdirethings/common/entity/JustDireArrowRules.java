package com.zzhalex.justdirethings.common.entity;

public final class JustDireArrowRules {

    private static final int PHASE_LIFETIME_TICKS = 200;
    private static final int SLOW_DOWN_DURATION_TICKS = 4;
    private static final int STOP_DURATION_TICKS = 10;
    private static final int EPIC_PIERCE_LEVEL = 5;
    private static final double NORMAL_SEARCH_RADIUS = 10.0D;
    private static final double EPIC_SEARCH_RADIUS = 20.0D;

    private JustDireArrowRules() {
    }

    public static double searchRadius(boolean epic) {
        return epic ? EPIC_SEARCH_RADIUS : NORMAL_SEARCH_RADIUS;
    }

    public static boolean shouldDiscardPhaseArrow(int ticksExisted) {
        return ticksExisted >= PHASE_LIFETIME_TICKS;
    }

    public static int slowDownDurationTicks() {
        return SLOW_DOWN_DURATION_TICKS;
    }

    public static int stopDurationTicks() {
        return STOP_DURATION_TICKS;
    }

    public static int epicPierceLevel() {
        return EPIC_PIERCE_LEVEL;
    }

    public static int epicMaxPiercedTargets() {
        return EPIC_PIERCE_LEVEL + 1;
    }
}
