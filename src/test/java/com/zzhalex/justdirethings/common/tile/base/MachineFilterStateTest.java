package com.zzhalex.justdirethings.common.tile.base;

import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineFilterStateTest {

    @Test
    void defaultsToDenylistLikeUpstreamFilterData() {
        MachineFilterState state = new MachineFilterState();

        assertFalse(state.isAllowList());
    }

    @Test
    void missingAllowListTagReadsAsDenylistForOldMachineData() {
        MachineFilterState state = new MachineFilterState();
        state.setAllowList(true);

        state.readFromNbt(new NBTTagCompound());

        assertFalse(state.isAllowList());
    }

    @Test
    void explicitAllowListTagStillRoundTrips() {
        MachineFilterState state = new MachineFilterState();
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("AllowList", true);

        state.readFromNbt(tag);

        assertTrue(state.isAllowList());
    }
}
