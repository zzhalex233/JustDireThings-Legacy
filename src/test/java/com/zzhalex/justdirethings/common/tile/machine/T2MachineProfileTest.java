package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.common.tile.base.TileAdvancedMachine;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.energy.IEnergyStorage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class T2MachineProfileTest {

    @Test
    void t2AutomationMachinesExposeSourceDerivedAdvancedProfiles() {
        assertAdvanced(new TileBlockBreaker.T2(), 500, 9);
        assertAdvanced(new TileBlockPlacer.T2(), 500, 9);
        assertAdvanced(new TileClicker.T2(), 250, 9);
        assertAdvanced(new TileDropper.T2(), 25, 9);
        assertAdvanced(new TileFluidCollector.T2(), 500, 9);
        assertAdvanced(new TileFluidPlacer.T2(), 500, 9);
        assertAdvanced(new TileSensor.T2(), 2, 9);
        assertAdvanced(new TileBlockSwapper.T2(), 50, 9);
    }

    @Test
    void t2DropperUsesOriginalNineSlotMachineInventory() {
        assertEquals(1, new TileDropper.T1().getItemHandler().getSlots());
        assertEquals(9, new TileDropper.T2().getItemHandler().getSlots());
    }

    @Test
    void t2MachineEnergyStateUsesForgeEnergySemantics() {
        TileBlockBreaker.T2 breaker = new TileBlockBreaker.T2();

        IEnergyStorage energy = breaker.getEnergyState();

        assertNotNull(energy);
        assertEquals(100000, energy.getMaxEnergyStored());
        assertEquals(100000, energy.receiveEnergy(100000, false));
        assertEquals(500, ((TileAdvancedMachine) breaker).consumeEnergy(500, false));
        assertEquals(99500, energy.getEnergyStored());
    }

    @Test
    void t2FilterInventoryPersistsWithMachineNbt() {
        TileBlockBreaker.T2 original = new TileBlockBreaker.T2();
        original.getFilterState().setAllowList(true);

        NBTTagCompound tag = original.writeMachineStateToNbt(new NBTTagCompound());
        ((TileAdvancedMachine) original).writeAdvancedMachineToNbt(tag);
        TileBlockBreaker.T2 restored = new TileBlockBreaker.T2();
        restored.readMachineStateFromNbt(tag);
        ((TileAdvancedMachine) restored).readAdvancedMachineFromNbt(tag);

        assertTrue(tag.hasKey("AdvancedFilters"));
        assertEquals(9, ((TileAdvancedMachine) restored).getFilterHandler().getSlots());
        assertTrue(restored.getFilterState().isAllowList());
    }

    private static void assertAdvanced(Object machine, int energyCost, int filterSlots) {
        assertTrue(machine instanceof TileAdvancedMachine, machine.getClass().getSimpleName() + " should use the shared T2 advanced-machine contract");
        TileAdvancedMachine advanced = (TileAdvancedMachine) machine;
        assertEquals(100000, advanced.getMachine().getEnergyState().getCapacity());
        assertEquals(100000, advanced.getMachine().getEnergyState().getMaxReceive());
        assertEquals(energyCost, advanced.getStandardEnergyCost());
        if (filterSlots > 0) {
            assertNotNull(advanced.getFilterHandler());
            assertEquals(filterSlots, advanced.getFilterHandler().getSlots());
        }
    }
}
