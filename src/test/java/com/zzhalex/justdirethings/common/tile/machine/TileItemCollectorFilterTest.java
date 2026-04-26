package com.zzhalex.justdirethings.common.tile.machine;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TileItemCollectorFilterTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void emptyDefaultDenylistCollectsEverything() {
        TileItemCollector collector = new TileItemCollector();

        assertTrue(collector.matchesFilter(new ItemStack(Items.STICK)));
    }

    @Test
    void allowlistOnlyCollectsMatchingFilters() {
        TileItemCollector collector = new TileItemCollector();
        collector.getFilterState().setAllowList(true);
        collector.getFilterHandler().setStackInSlot(0, new ItemStack(Items.STICK));

        assertTrue(collector.matchesFilter(new ItemStack(Items.STICK)));
        assertFalse(collector.matchesFilter(new ItemStack(Items.APPLE)));
    }

    @Test
    void denylistRejectsMatchingFilters() {
        TileItemCollector collector = new TileItemCollector();
        collector.getFilterHandler().setStackInSlot(0, new ItemStack(Items.STICK));

        assertFalse(collector.matchesFilter(new ItemStack(Items.STICK)));
        assertTrue(collector.matchesFilter(new ItemStack(Items.APPLE)));
    }
}
