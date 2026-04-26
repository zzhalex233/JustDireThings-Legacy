package com.zzhalex.justdirethings.common.tile.machine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratorMathTest {

    @Test
    void generatorConsumesFuelToFillEnergyBuffer() {
        assertTrue(GeneratorMath.canStartBurn(1600, 0, 10000));
    }
}
