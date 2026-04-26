package com.zzhalex.justdirethings.common.item.misc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PocketGeneratorFuelMathTest {

    @Test
    void burnMultiplierScalesOutputLinearly() {
        assertEquals(40, PocketGeneratorMath.fePerTick(10, 4));
    }
}
