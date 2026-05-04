package com.zzhalex.justdirethings.common.util;

import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;

public final class DimensionDisplayHelper {

    private DimensionDisplayHelper() {
    }

    public static String getDimensionName(int dimension) {
        try {
            WorldProvider provider = DimensionManager.getProvider(dimension);
            if (provider != null && provider.getDimensionType() != null) {
                return provider.getDimensionType().getName();
            }
        } catch (RuntimeException ignored) {
            // Keep stale/offline bindings readable even when the dimension is not loaded.
        }
        return Integer.toString(dimension);
    }
}
