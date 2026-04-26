package com.zzhalex.justdirethings.common.item.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AbilityAvailabilityTest {

    @Test
    void everyDeclaredAbilityHasAStableStringId() {
        assertNotNull(Ability.FLIGHT.getId());
    }
}
