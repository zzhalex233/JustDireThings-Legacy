package com.zzhalex.justdirethings.common.tile.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineRedstoneStateTest {

    @Test
    void ignoredModeAlwaysAllowsWork() {
        MachineRedstoneState state = new MachineRedstoneState();
        state.setMode(MachineRedstoneState.RedstoneMode.IGNORED);

        state.evaluateSignal(false);
        assertTrue(state.consumeActiveSignal());
        state.evaluateSignal(true);
        assertTrue(state.consumeActiveSignal());
    }

    @Test
    void lowModeOnlyAllowsWorkWithoutSignal() {
        MachineRedstoneState state = new MachineRedstoneState();
        state.setMode(MachineRedstoneState.RedstoneMode.LOW);

        state.evaluateSignal(false);
        assertTrue(state.consumeActiveSignal());
        state.evaluateSignal(true);
        assertFalse(state.consumeActiveSignal());
    }

    @Test
    void highModeOnlyAllowsWorkWithSignal() {
        MachineRedstoneState state = new MachineRedstoneState();
        state.setMode(MachineRedstoneState.RedstoneMode.HIGH);

        state.evaluateSignal(false);
        assertFalse(state.consumeActiveSignal());
        state.evaluateSignal(true);
        assertTrue(state.consumeActiveSignal());
    }

    @Test
    void pulseModeOnlyAllowsOneWorkCyclePerRisingEdge() {
        MachineRedstoneState state = new MachineRedstoneState();
        state.setMode(MachineRedstoneState.RedstoneMode.PULSE);

        state.evaluateSignal(false);
        assertFalse(state.consumeActiveSignal());
        state.evaluateSignal(true);
        assertTrue(state.consumeActiveSignal());
        assertFalse(state.consumeActiveSignal());
        state.evaluateSignal(true);
        assertFalse(state.consumeActiveSignal());
        state.evaluateSignal(false);
        state.evaluateSignal(true);
        assertTrue(state.consumeActiveSignal());
    }
}
