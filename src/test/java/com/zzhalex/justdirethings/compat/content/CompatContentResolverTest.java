package com.zzhalex.justdirethings.compat.content;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompatContentResolverTest {

    @Test
    void bambooFallsBackToReedsWhenFutureMcIsMissing() {
        assertEquals("minecraft:reeds", CompatContentResolver.fallbackIdString(CompatContentKey.BAMBOO));
    }
}
