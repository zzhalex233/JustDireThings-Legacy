package com.zzhalex.justdirethings.registry;

import net.minecraft.init.Bootstrap;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModContentBlocksCatalogTest {

    @Test
    void coreContentBlockIdsStayStable() {
        Bootstrap.register();
        List<String> ids = ModContentBlocks.coreContentBlockIds();

        assertEquals(33, ids.size());
        assertEquals("ferricore_block", ids.get(0));
        assertTrue(ids.contains("gooblock_tier1"));
        assertTrue(ids.contains("goosoil_tier4"));
        assertTrue(ids.contains("eclipsegateblock"));
        assertTrue(ids.contains("raw_coal_t4_ore"));
        assertTrue(ids.contains("time_crystal_cluster_large"));
    }
}
