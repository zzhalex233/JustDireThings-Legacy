package com.zzhalex.justdirethings.common.entity;

public final class DecoyBehaviorRules {

    private static final int AGGRO_INTERVAL_TICKS = 10;
    private static final int LIFETIME_TICKS = 200;
    private static final int AGGRO_RADIUS_BLOCKS = 10;

    private DecoyBehaviorRules() {
    }

    public static int aggroRadiusBlocks() {
        return AGGRO_RADIUS_BLOCKS;
    }

    public static boolean shouldAggro(int ticksExisted) {
        return ticksExisted > 0 && ticksExisted % AGGRO_INTERVAL_TICKS == 0;
    }

    public static boolean shouldExpire(int ticksExisted) {
        return ticksExisted >= LIFETIME_TICKS;
    }

    public static String formatSummonerName(String playerName, String decoyName) {
        return playerName + "_" + decoyName;
    }
}
