package com.zzhalex.justdirethings.common.tile.machine;

import net.minecraft.util.EnumFacing;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FakePlayerMathTest {

    @Test
    void fakePlayerFacingProducesDeterministicRotation() {
        assertEquals(90.0F, FakePlayerMath.pitchForFacing(EnumFacing.DOWN), 0.01F);
    }
}
