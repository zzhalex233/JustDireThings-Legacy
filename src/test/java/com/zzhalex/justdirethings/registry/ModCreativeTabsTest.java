package com.zzhalex.justdirethings.registry;

import net.minecraft.init.Bootstrap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModCreativeTabsTest {

    @Test
    void portalGunAndUpgradeStationUseTheJdtCreativeTab() {
        Bootstrap.register();
        assertEquals(ModCreativeTabs.JUST_DIRE_THINGS, ModItems.PORTAL_GUN_V2.getCreativeTab());
        assertEquals(ModCreativeTabs.JUST_DIRE_THINGS, ModBlocks.UPGRADE_STATION.getCreativeTab());
    }
}
