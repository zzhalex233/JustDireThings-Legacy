package com.zzhalex.justdirethings.common.tile.base;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MachineAreaStateTest {

    @Test
    void createsOriginalOffsetAwareAreaBox() {
        MachineAreaState state = new MachineAreaState();
        state.setArea(2.0D, 1.0D, 0.5D);
        state.setOffset(3, -2, 1);

        AxisAlignedBB area = state.createArea(new BlockPos(10, 64, 10));

        assertEquals(11.0D, area.minX, 0.001D);
        assertEquals(16.0D, area.maxX, 0.001D);
        assertEquals(61.0D, area.minY, 0.001D);
        assertEquals(64.0D, area.maxY, 0.001D);
        assertEquals(11.0D, area.minZ, 0.001D);
        assertEquals(13.0D, area.maxZ, 0.001D);
    }

    @Test
    void createsOriginalOffsetOnlyBoxForAreaRenderer() {
        MachineAreaState state = new MachineAreaState();
        state.setArea(2.0D, 1.0D, 0.5D);
        state.setOffset(3, -2, 1);

        AxisAlignedBB area = state.createOffsetOnlyArea(new BlockPos(10, 64, 10));

        assertEquals(13.0D, area.minX, 0.001D);
        assertEquals(14.0D, area.maxX, 0.001D);
        assertEquals(62.0D, area.minY, 0.001D);
        assertEquals(63.0D, area.maxY, 0.001D);
        assertEquals(11.75D, area.minZ, 0.001D);
        assertEquals(12.25D, area.maxZ, 0.001D);
    }
}
