package com.zzhalex.justdirethings.common.container.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ContainerMachineBaseTest {

    @Test
    void machineContainerUsesStableSlotConstants() {
        assertTrue(ContainerMachineBase.PLAYER_INV_START >= 0);
    }
}
