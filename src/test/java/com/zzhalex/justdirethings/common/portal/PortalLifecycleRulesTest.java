package com.zzhalex.justdirethings.common.portal;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalLifecycleRulesTest {

    @Test
    void tickCooldownsDecrementsAndRemovesExpiredEntries() {
        UUID active = UUID.randomUUID();
        UUID expired = UUID.randomUUID();
        Map<UUID, Integer> cooldowns = new LinkedHashMap<>();
        cooldowns.put(active, 2);
        cooldowns.put(expired, 0);

        PortalLifecycleRules.tickCooldowns(cooldowns);

        assertEquals(1, cooldowns.get(active));
        assertFalse(cooldowns.containsKey(expired));
        assertTrue(cooldowns.containsKey(active));
    }
}
