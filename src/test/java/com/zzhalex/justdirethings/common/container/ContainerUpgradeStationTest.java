package com.zzhalex.justdirethings.common.container;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContainerUpgradeStationTest {

    @Test
    void upgradeStationExposesThreeInputSlotsAndOneOutput() {
        assertEquals(4, ContainerUpgradeStation.SLOT_COUNT);
    }
}
