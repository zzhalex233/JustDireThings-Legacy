package com.zzhalex.justdirethings.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TickAccelerationRulesTest {

    @Test
    void multiplierLevelMapsToExpectedRate() {
        assertEquals(2.0F, TickAccelerationRules.accelRateForLevel(1), 0.001F);
    }
}
