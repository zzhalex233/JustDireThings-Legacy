package com.zzhalex.justdirethings.registry;

import net.minecraft.init.Bootstrap;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModContentItemsCatalogTest {

    @Test
    void resourceAndUpgradeItemCatalogsStayStable() {
        Bootstrap.register();
        List<String> resourceIds = ModContentItems.resourceItemIds();
        List<String> templateIds = ModContentItems.templateItemIds();
        List<String> upgradeIds = ModContentItems.upgradeItemIds();

        assertEquals(14, resourceIds.size());
        assertTrue(resourceIds.contains("portal_fluid_catalyst"));
        assertTrue(resourceIds.contains("time_crystal"));

        assertEquals(4, templateIds.size());
        assertEquals("template_ferricore", templateIds.get(0));

        assertEquals(44, upgradeIds.size());
        assertEquals("upgrade_blank", upgradeIds.get(0));
        assertTrue(upgradeIds.contains("upgrade_mobscanner"));
        assertTrue(upgradeIds.contains("upgrade_time_protection"));
    }
}
