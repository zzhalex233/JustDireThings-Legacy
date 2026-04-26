package com.zzhalex.justdirethings.common.portal;

import net.minecraft.util.EnumFacing;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class PortalLifecycleRules {

    private PortalLifecycleRules() {
    }

    public static void tickCooldowns(Map<UUID, Integer> cooldowns) {
        if (cooldowns == null || cooldowns.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<UUID, Integer>> iterator = cooldowns.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            int remaining = entry.getValue() == null ? 0 : entry.getValue();
            if (remaining <= 0) {
                iterator.remove();
            } else {
                entry.setValue(remaining - 1);
            }
        }
    }

    public static EnumFacing.Axis axisFromMotion(double motionX, double motionZ) {
        return Math.abs(motionX) > Math.abs(motionZ) ? EnumFacing.Axis.X : EnumFacing.Axis.Z;
    }
}
