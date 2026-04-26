package com.zzhalex.justdirethings.registry;

import net.minecraft.init.Bootstrap;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModEquipmentItemsCatalogTest {

    @Test
    void equipmentCatalogsStayStable() {
        Bootstrap.register();

        List<String> toolIds = ModEquipmentItems.toolItemIds();
        List<String> bowIds = ModEquipmentItems.bowItemIds();
        List<String> armorIds = ModEquipmentItems.armorItemIds();

        assertEquals(23, toolIds.size());
        assertEquals("ferricore_sword", toolIds.get(0));
        assertTrue(toolIds.contains("eclipsealloy_paxel"));
        assertTrue(toolIds.contains("ferricore_wrench"));

        assertEquals(4, bowIds.size());
        assertEquals("bow_ferricore", bowIds.get(0));
        assertTrue(bowIds.contains("bow_eclipsealloy"));

        assertEquals(16, armorIds.size());
        assertEquals("ferricore_boots", armorIds.get(0));
        assertTrue(armorIds.contains("eclipsealloy_chestplate"));
    }
}
