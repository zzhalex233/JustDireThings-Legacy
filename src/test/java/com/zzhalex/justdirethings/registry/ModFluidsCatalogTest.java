package com.zzhalex.justdirethings.registry;

import net.minecraft.init.Bootstrap;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModFluidsCatalogTest {

    @Test
    void coreFluidIdsStayStable() {
        List<String> ids = ModFluids.coreFluidIds();

        assertEquals(11, ids.size());
        assertEquals("polymorphic_fluid", ids.get(0));
        assertEquals("portal_fluid", ids.get(1));
        assertEquals("time_fluid", ids.get(2));
        assertTrue(ids.contains("xp_fluid"));
        assertTrue(ids.contains("refined_t4_fluid"));
    }

    @Test
    void bootstrapCreatesFluidAndBlockEntriesForCoreCatalog() {
        Bootstrap.register();
        ModFluids.bootstrap();

        assertNotNull(ModFluids.getFluid("portal_fluid"));
        assertNotNull(ModFluids.getFluid("time_fluid"));
        assertNotNull(ModFluids.getFluidBlock("portal_fluid"));
        assertNotNull(ModFluids.getFluidBlock("xp_fluid"));
        assertEquals("portal_fluid_block", ModFluids.getFluidBlock("portal_fluid").getRegistryName().getPath());
    }
}
