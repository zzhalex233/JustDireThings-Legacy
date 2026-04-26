package com.zzhalex.justdirethings.common.portal;

public final class PortalDimensionTransferRules {

    private PortalDimensionTransferRules() {
    }

    public static boolean requiresDirectTeleporter(int currentDimension, int targetDimension) {
        return currentDimension != targetDimension;
    }
}
