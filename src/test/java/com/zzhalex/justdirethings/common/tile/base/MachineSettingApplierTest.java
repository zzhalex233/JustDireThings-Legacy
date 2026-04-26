package com.zzhalex.justdirethings.common.tile.base;

import com.zzhalex.justdirethings.common.tile.machine.TileItemCollector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineSettingApplierTest {

    @Test
    void appliesBaseMachineSettingsFromGuiPacket() {
        TileMachineBase tile = new TileMachineBase();

        MachineSettingApplier.apply(tile, MachineSettingKeys.TICK_SPEED, 7);
        MachineSettingApplier.apply(tile, MachineSettingKeys.REDSTONE_MODE, MachineRedstoneState.RedstoneMode.HIGH.ordinal());
        MachineSettingApplier.apply(tile, MachineSettingKeys.DIRECTION, 5);

        assertEquals(7, tile.getTickSpeed());
        assertEquals(MachineRedstoneState.RedstoneMode.HIGH, tile.getRedstoneState().getMode());
        assertEquals(5, tile.getDirection());
    }

    @Test
    void appliesAreaFilterAndItemCollectorSettingsFromGuiPacket() {
        TileItemCollector tile = new TileItemCollector();

        MachineSettingApplier.apply(tile, MachineSettingKeys.RENDER_AREA, 0);
        MachineSettingApplier.apply(tile, MachineSettingKeys.X_RADIUS_TENTHS, 35);
        MachineSettingApplier.apply(tile, MachineSettingKeys.Y_OFFSET, 4);
        MachineSettingApplier.apply(tile, MachineSettingKeys.FILTER_ALLOWLIST, 0);
        MachineSettingApplier.apply(tile, MachineSettingKeys.FILTER_COMPARE_NBT, 1);
        MachineSettingApplier.apply(tile, MachineSettingKeys.FILTER_BLOCK_ITEM, 1);
        MachineSettingApplier.apply(tile, MachineSettingKeys.RESPECT_PICKUP_DELAY, 1);

        assertFalse(tile.getAreaState().isRenderArea());
        assertEquals(3.5D, tile.getAreaState().getXRadius(), 0.001D);
        assertEquals(4, tile.getAreaState().getYOffset());
        assertFalse(tile.getFilterState().isAllowList());
        assertTrue(tile.getFilterState().isCompareNbt());
        assertEquals(1, tile.getFilterState().getBlockItemFilter());
        assertTrue(tile.isRespectPickupDelay());
    }
}
