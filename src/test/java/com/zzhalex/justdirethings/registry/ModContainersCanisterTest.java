package com.zzhalex.justdirethings.registry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModContainersCanisterTest {

    @Test
    void canisterGuiIdsStayRegistered() {
        assertEquals(17, ModContainers.GUI_FUEL_CANISTER);
        assertEquals(18, ModContainers.GUI_POTION_CANISTER);
        assertTrue(ModContainers.GUI_POTION_CANISTER > ModContainers.GUI_PLAYER_ACCESSOR);
    }
}
