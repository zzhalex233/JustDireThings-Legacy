package com.zzhalex.justdirethings.client.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeWandDisplayDataTest {

    @Test
    void accelerationLabelUsesTheTickMultiplier() {
        assertEquals("x8", TimeWandDisplayData.accelerationLabel(3));
    }

    @Test
    void remainingTimeLabelFormatsSecondsToTwoDecimals() {
        assertEquals("2.25s", TimeWandDisplayData.remainingTimeLabel(45));
    }

    @Test
    void tickRateProgressUsesConfiguredMaximumMultiplier() {
        assertEquals(0.5F, TimeWandDisplayData.tickRateProgress(2, 16), 0.001F);
    }

    @Test
    void remainingTimeProgressClampsToTheUnitInterval() {
        assertEquals(1.0F, TimeWandDisplayData.remainingTimeProgress(500, 400), 0.001F);
        assertEquals(0.0F, TimeWandDisplayData.remainingTimeProgress(-1, 0), 0.001F);
    }
}
