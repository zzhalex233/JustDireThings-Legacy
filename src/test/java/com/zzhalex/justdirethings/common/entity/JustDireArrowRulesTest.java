package com.zzhalex.justdirethings.common.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JustDireArrowRulesTest {

    @Test
    void matchesOriginalSearchRadius() {
        assertEquals(10.0D, JustDireArrowRules.searchRadius(false));
        assertEquals(20.0D, JustDireArrowRules.searchRadius(true));
    }

    @Test
    void phaseArrowsExpireAfterOriginalLifetime() {
        assertFalse(JustDireArrowRules.shouldDiscardPhaseArrow(199));
        assertTrue(JustDireArrowRules.shouldDiscardPhaseArrow(200));
    }

    @Test
    void homingStateDurationsMatchOriginal() {
        assertEquals(4, JustDireArrowRules.slowDownDurationTicks());
        assertEquals(10, JustDireArrowRules.stopDurationTicks());
    }
}
