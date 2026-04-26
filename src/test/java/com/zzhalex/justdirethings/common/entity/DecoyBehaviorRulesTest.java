package com.zzhalex.justdirethings.common.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecoyBehaviorRulesTest {

    @Test
    void matchesOriginalLifetimeAndAggroCadence() {
        assertEquals(10, DecoyBehaviorRules.aggroRadiusBlocks());
        assertFalse(DecoyBehaviorRules.shouldAggro(9));
        assertTrue(DecoyBehaviorRules.shouldAggro(10));
        assertFalse(DecoyBehaviorRules.shouldExpire(199));
        assertTrue(DecoyBehaviorRules.shouldExpire(200));
    }

    @Test
    void formatsSummonerNameLikeOriginal() {
        assertEquals("zzhalex_Decoy", DecoyBehaviorRules.formatSummonerName("zzhalex", "Decoy"));
    }
}
