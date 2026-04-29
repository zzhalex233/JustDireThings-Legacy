package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.common.tile.base.EnergyTransferHelper;
import com.zzhalex.justdirethings.common.tile.base.MachineEnergyState;
import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import com.zzhalex.justdirethings.common.item.misc.PocketGeneratorItem;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineEnergyParityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void sourceEnergyEndpointDefaultsMatchUpstreamBuffers() {
        assertEnergyState(new TileGenerator().getEnergyState(), 1_000_000, false, true);
        assertEnergyState(new TileFluidGenerator().getEnergyState(), 5_000_000, false, true);
        assertEnergyState(new TileEnergyTransmitter().getEnergyState(), 1_000_000, true, true);
        assertEnergyState(new TileParadoxMachine().getEnergyState(), 10_000_000, true, true);
        assertEquals(16_000, new TileParadoxMachine().getFluidState().getCapacity());
    }

    @Test
    void energyTransferMovesOnlyWhatReceiverCanAccept() {
        MachineEnergyState sender = new MachineEnergyState();
        sender.setCapacity(10_000);
        sender.setMaxExtract(10_000);
        sender.forceReceiveEnergy(5_000, false);
        EnergyStorage receiver = new EnergyStorage(1_000, 250, 0, 900);

        int received = EnergyTransferHelper.transmitPower(sender, receiver, 1_000);

        assertEquals(100, received);
        assertEquals(4_900, sender.getEnergyStored());
        assertEquals(1_000, receiver.getEnergyStored());
    }

    @Test
    void transmitterLossUsesSourceDistanceFormula() {
        assertEquals(990, EnergyTransferHelper.applyDistanceLoss(1_000, 1, 1.0D));
        assertEquals(950, EnergyTransferHelper.applyDistanceLoss(1_000, 5, 1.0D));
        assertEquals(1_000, EnergyTransferHelper.applyDistanceLoss(1_000, 5, 0.0D));
    }

    @Test
    void fluidGeneratorUsesOnlyRefinedFuelTiers() {
        TileFluidGenerator generator = new TileFluidGenerator();

        assertEquals(450, generator.getFePerFuelTick("refined_t2_fluid"));
        assertEquals(1_300, generator.getFePerFuelTick("refined_t3_fluid"));
        assertEquals(4_000, generator.getFePerFuelTick("refined_t4_fluid"));
        assertEquals(0, generator.getFePerFuelTick("lava"));
        assertEquals(0, generator.getFePerFuelTick("unrefined_t2_fluid"));
    }

    @Test
    void fluidGeneratorDoesNotConsumeFluidUnlessFullTickEnergyFits() {
        TileFluidGenerator generator = new TileFluidGenerator();
        generator.getFluidState().setFluidName("refined_t4_fluid");
        generator.getFluidState().setAmount(2);
        generator.getEnergyState().setStoredEnergy(generator.getEnergyState().getMaxEnergyStored() - 3_999);

        assertFalse(generator.generateOneFluidTick());
        assertEquals(2, generator.getFluidState().getAmount());
        assertEquals(generator.getEnergyState().getMaxEnergyStored() - 3_999, generator.getEnergyState().getEnergyStored());

        generator.getEnergyState().setStoredEnergy(generator.getEnergyState().getMaxEnergyStored() - 4_000);

        assertTrue(generator.generateOneFluidTick());
        assertEquals(1, generator.getFluidState().getAmount());
        assertEquals(generator.getEnergyState().getMaxEnergyStored(), generator.getEnergyState().getEnergyStored());
    }

    @Test
    void energyTransmitterLetsPocketGeneratorBurnBeforeDrainingSlot() {
        PocketGeneratorItem pocketGenerator = new PocketGeneratorItem();
        ItemStack stack = new ItemStack(pocketGenerator);
        pocketGenerator.setFuelStack(stack, new ItemStack(Items.COAL));

        TileEnergyTransmitter transmitter = new TileEnergyTransmitter();
        transmitter.getItemHandler().setStackInSlot(0, stack);

        assertEquals(60, transmitter.drainFromSlot());
        assertEquals(60, transmitter.getEnergyState().getEnergyStored());
        assertEquals(0, pocketGenerator.getStoredEnergy(stack));
        assertEquals(399, pocketGenerator.getCounter(stack));
    }

    @Test
    void energyTransmitterFacingCapabilityExposesNetworkView() {
        TileEnergyTransmitter transmitter = new TileEnergyTransmitter();
        transmitter.setDirection(EnumFacing.EAST.getIndex());

        assertSame(transmitter.getNetworkEnergyStorage(), transmitter.getCapability(CapabilityEnergy.ENERGY, EnumFacing.EAST));
    }

    @Test
    void energyTransmitterAcceptsGenericForgeEnergyPipeQueriesOnEverySide() {
        TileEnergyTransmitter transmitter = new TileEnergyTransmitter();
        transmitter.setDirection(EnumFacing.EAST.getIndex());

        assertSame(transmitter.getNetworkEnergyStorage(), transmitter.getCapability(CapabilityEnergy.ENERGY, null));
        for (EnumFacing side : EnumFacing.VALUES) {
            assertTrue(transmitter.hasCapability(CapabilityEnergy.ENERGY, side), side + " should expose FE in the 1.12 capability layer");
            assertSame(transmitter.getNetworkEnergyStorage(), transmitter.getCapability(CapabilityEnergy.ENERGY, side));
        }
    }

    @Test
    void exposedForgeEnergyCapabilitySyncsExternalPipeMutations() throws ReflectiveOperationException {
        TrackingMachine machine = new TrackingMachine();
        machine.getEnergyState().setCapacity(1_000);
        machine.getEnergyState().setMaxReceive(1_000);
        machine.getEnergyState().setMaxExtract(1_000);

        net.minecraftforge.energy.IEnergyStorage storage = getExternalEnergyCapability(machine);

        assertEquals(250, storage.receiveEnergy(250, false));
        assertEquals(250, machine.getEnergyState().getEnergyStored());
        assertEquals(1, machine.clientSyncs);

        assertEquals(100, storage.extractEnergy(100, false));
        assertEquals(150, machine.getEnergyState().getEnergyStored());
        assertEquals(2, machine.clientSyncs);

        storage.receiveEnergy(10, true);

        assertEquals(150, machine.getEnergyState().getEnergyStored());
        assertEquals(2, machine.clientSyncs, "Simulated FE probes should not spam block updates");
    }

    @Test
    void energyTransmitterNetworkViewReceivesAndExtractsAcrossVisibleTransmitters() {
        TileEnergyTransmitter first = new TileEnergyTransmitter();
        TileEnergyTransmitter second = new TileEnergyTransmitter();

        assertEquals(2_000_000, first.getTotalMaxEnergyStored(Arrays.asList(first, second)));
        assertEquals(1_000_000, first.distributeEnergyToNetwork(Arrays.asList(first, second), 1_500_000, false));
        assertEquals(1_000_000, first.getEnergyState().getEnergyStored());
        assertEquals(0, second.getEnergyState().getEnergyStored());

        assertEquals(500_000, first.distributeEnergyToNetwork(Arrays.asList(first, second), 500_000, false));
        assertEquals(1_000_000, first.getEnergyState().getEnergyStored());
        assertEquals(500_000, second.getEnergyState().getEnergyStored());
        assertEquals(1_500_000, first.getTotalEnergyStored(Arrays.asList(first, second)));

        assertEquals(600_000, first.extractEnergyFromNetwork(Arrays.asList(first, second), 600_000, false));
        assertEquals(400_000, first.getEnergyState().getEnergyStored());
        assertEquals(500_000, second.getEnergyState().getEnergyStored());
    }

    @Test
    void energyTransmitterUsesOriginalNineSlotDenylistFilter() {
        TileEnergyTransmitter transmitter = new TileEnergyTransmitter();
        ItemStack stone = new ItemStack(net.minecraft.init.Blocks.STONE);

        assertEquals(9, transmitter.getFilterHandler().getSlots());
        assertFalse(transmitter.getFilterState().isAllowList(),
                "Energy Transmitter should default to denylist mode so an empty filter charges everything like upstream");
        assertTrue(transmitter.matchesFilter(stone));

        transmitter.getFilterState().setAllowList(true);

        assertFalse(transmitter.matchesFilter(stone));
    }

    @Test
    void pocketGeneratorUsesUpstreamEnergyDefaults() {
        PocketGeneratorItem pocketGenerator = new PocketGeneratorItem();
        ItemStack stack = new ItemStack(pocketGenerator);

        assertEquals(1_000_000, pocketGenerator.getMaxEnergy());
        assertEquals(5_000, pocketGenerator.getMaxExtract(stack));
        assertEquals(0, pocketGenerator.getMaxReceive(stack));
        assertEquals(60, pocketGenerator.getFePerTick(stack));
    }

    @Test
    void paradoxMachineConsumesEnergyAndFluidAtomicallyPerRuntimeTick() {
        TileParadoxMachine paradox = new TileParadoxMachine();
        paradox.getEnergyState().forceReceiveEnergy(500, false);
        paradox.getFluidState().setFluidName("time_fluid");
        paradox.getFluidState().setAmount(10);
        paradox.setRunningState(true, 0, 250, 5);

        assertTrue(paradox.consumeRuntimeTick());
        assertEquals(250, paradox.getEnergyState().getEnergyStored());
        assertEquals(5, paradox.getFluidState().getAmount());
        assertEquals(1, paradox.getTimeRunning());

        paradox.setRunningState(true, paradox.getTimeRunning(), 251, 5);

        assertFalse(paradox.consumeRuntimeTick());
        assertEquals(250, paradox.getEnergyState().getEnergyStored());
        assertEquals(5, paradox.getFluidState().getAmount());
        assertFalse(paradox.isRunning());
    }

    @Test
    void paradoxMachineUsesUpstreamRestoreCostDefaults() {
        TileParadoxMachine paradox = new TileParadoxMachine();

        assertEquals(250_000, paradox.getEnergyCost(1, 0));
        assertEquals(250_000, paradox.getEnergyCost(0, 1));
        assertEquals(50, paradox.getFluidCost(1, 0));
        assertEquals(50, paradox.getFluidCost(0, 1));
        assertEquals(0.25F, paradox.getParadoxEnergyPerBlock(), 0.0001F);
        assertEquals(0.25F, paradox.getParadoxEnergyPerEntity(), 0.0001F);
        assertEquals(100.0F, paradox.getMaxParadoxEnergy(), 0.0001F);
    }

    private static void assertEnergyState(MachineEnergyState state, int capacity, boolean canReceive, boolean canExtract) {
        assertEquals(capacity, state.getMaxEnergyStored());
        assertEquals(canReceive, state.canReceive());
        assertEquals(canExtract, state.canExtract());
        if (canReceive) {
            assertTrue(state.receiveEnergy(1, true) > 0);
        } else {
            assertFalse(state.receiveEnergy(1, true) > 0);
        }
    }

    private static final class TrackingMachine extends TileMachineBase {
        private int clientSyncs;

        @Override
        public void markDirtyClient() {
            clientSyncs++;
        }
    }

    private static net.minecraftforge.energy.IEnergyStorage getExternalEnergyCapability(TileMachineBase machine) throws ReflectiveOperationException {
        Field field = TileMachineBase.class.getDeclaredField("energyCapability");
        field.setAccessible(true);
        return (net.minecraftforge.energy.IEnergyStorage) field.get(machine);
    }
}
