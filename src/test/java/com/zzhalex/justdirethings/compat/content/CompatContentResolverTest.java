package com.zzhalex.justdirethings.compat.content;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompatContentResolverTest {

    @Test
    void bambooFallsBackToReedsWhenFutureMcIsMissing() {
        assertEquals("minecraft:reeds", CompatContentResolver.fallbackIdString(CompatContentKey.BAMBOO));
    }

    @Test
    void futureMcProvidedMaterialsRecordFutureIdsAndFallbacks() {
        assertEquals("futuremc:blast_furnace", CompatContentResolver.futureMcId(CompatContentKey.BLAST_FURNACE).toString());
        assertEquals("minecraft:furnace", CompatContentResolver.fallbackIdString(CompatContentKey.BLAST_FURNACE));
        assertEquals("futuremc:smoker", CompatContentResolver.futureMcId(CompatContentKey.SMOKER).toString());
        assertEquals("minecraft:furnace", CompatContentResolver.fallbackIdString(CompatContentKey.SMOKER));
        assertEquals("futuremc:honey_bottle", CompatContentResolver.futureMcId(CompatContentKey.HONEY_BOTTLE).toString());
        assertEquals("minecraft:sugar", CompatContentResolver.fallbackIdString(CompatContentKey.HONEY_BOTTLE));
        assertEquals("futuremc:netherite_pickaxe", CompatContentResolver.futureMcId(CompatContentKey.NETHERITE_PICKAXE).toString());
        assertEquals("minecraft:diamond_pickaxe", CompatContentResolver.fallbackIdString(CompatContentKey.NETHERITE_PICKAXE));
        assertEquals("futuremc:netherite_scrap", CompatContentResolver.futureMcId(CompatContentKey.NETHERITE_SCRAP).toString());
        assertEquals("minecraft:obsidian", CompatContentResolver.fallbackIdString(CompatContentKey.NETHERITE_SCRAP));
    }

    @Test
    void unsupportedFutureMcMaterialsHaveVanillaFallbacksOnly() {
        assertEquals("minecraft:waterlily", CompatContentResolver.fallbackIdString(CompatContentKey.KELP));
        assertEquals("minecraft:wheat_seeds", CompatContentResolver.fallbackIdString(CompatContentKey.SHORT_GRASS));
        assertEquals("minecraft:quartz", CompatContentResolver.fallbackIdString(CompatContentKey.AMETHYST_SHARD));
        assertEquals("minecraft:ghast_tear", CompatContentResolver.fallbackIdString(CompatContentKey.PHANTOM_MEMBRANE));
        assertEquals("minecraft:observer", CompatContentResolver.fallbackIdString(CompatContentKey.CALIBRATED_SCULK_SENSOR));
    }
}
