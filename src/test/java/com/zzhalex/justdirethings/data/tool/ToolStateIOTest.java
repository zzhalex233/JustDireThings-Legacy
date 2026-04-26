package com.zzhalex.justdirethings.data.tool;

import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ToolStateIOTest {

    @Test
    void toolStateRoundTripsThroughNbt() {
        ToolState original = new ToolState();
        original.setEnabled(false);
        NBTTagCompound tag = ToolStateIO.write(original);
        ToolState restored = ToolStateIO.read(tag);
        assertFalse(restored.isEnabled());
    }
}
