package com.zzhalex.justdirethings.common.tile.base;

import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MachineStateSerializationTest {

    @Test
    void machineTickSpeedPersistsToNbt() {
        TestMachineTile original = new TestMachineTile();
        original.setTickSpeed(40);

        NBTTagCompound tag = original.writeMachineStateToNbt(new NBTTagCompound());
        TestMachineTile restored = new TestMachineTile();
        restored.readMachineStateFromNbt(tag);

        assertEquals(40, restored.getTickSpeed());
    }

    @Test
    void redstoneModePersistsToNbt() {
        MachineRedstoneState original = new MachineRedstoneState();
        original.setMode(MachineRedstoneState.RedstoneMode.HIGH);

        NBTTagCompound tag = original.writeToNbt(new NBTTagCompound());
        MachineRedstoneState restored = new MachineRedstoneState();
        restored.readFromNbt(tag);

        assertEquals(MachineRedstoneState.RedstoneMode.HIGH, restored.getMode());
    }

    private static final class TestMachineTile extends TileMachineBase {
    }
}
