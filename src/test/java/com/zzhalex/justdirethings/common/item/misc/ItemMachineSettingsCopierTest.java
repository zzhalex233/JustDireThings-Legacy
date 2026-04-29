package com.zzhalex.justdirethings.common.item.misc;

import com.zzhalex.justdirethings.common.tile.base.MachineRedstoneState;
import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import com.zzhalex.justdirethings.common.tile.machine.TileEnergyTransmitter;
import com.zzhalex.justdirethings.common.tile.machine.TileItemCollector;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemMachineSettingsCopierTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void copySettingTogglesDefaultToEnabledAndPersistToStack() {
        ItemStack stack = new ItemStack(new ItemMachineSettingsCopier());

        assertTrue(ItemMachineSettingsCopier.getCopyArea(stack));
        assertTrue(ItemMachineSettingsCopier.getCopyOffset(stack));
        assertTrue(ItemMachineSettingsCopier.getCopyFilter(stack));
        assertTrue(ItemMachineSettingsCopier.getCopyRedstone(stack));

        ItemMachineSettingsCopier.setSettings(stack, false, true, false, true);

        assertFalse(ItemMachineSettingsCopier.getCopyArea(stack));
        assertTrue(ItemMachineSettingsCopier.getCopyOffset(stack));
        assertFalse(ItemMachineSettingsCopier.getCopyFilter(stack));
        assertTrue(ItemMachineSettingsCopier.getCopyRedstone(stack));
    }

    @Test
    void savesCopiedMachineDataUsingOriginalSettingKeys() {
        ItemMachineSettingsCopier item = new ItemMachineSettingsCopier();
        ItemStack stack = new ItemStack(item);
        TileMachineBase source = new TileMachineBase();
        source.getAreaState().setArea(2.5D, 1.0D, 4.0D);
        source.getAreaState().setOffset(2, -1, 3);
        source.getFilterState().setAllowList(false);
        source.getFilterState().setCompareNbt(true);
        source.getFilterState().setBlockItemFilter(1);
        source.getRedstoneState().setMode(MachineRedstoneState.RedstoneMode.HIGH);

        assertTrue(item.saveSettings(source, stack));

        NBTTagCompound copiedData = ItemMachineSettingsCopier.getCopiedMachineData(stack);
        assertEquals(2.5D, copiedData.getDouble("xRadiusDouble"), 0.001D);
        assertEquals(2, copiedData.getInteger("xOffset"));
        assertFalse(copiedData.getBoolean("allowlist"));
        assertTrue(copiedData.getBoolean("compareNBT"));
        assertEquals(1, copiedData.getInteger("blockitemfilter"));
        assertEquals(MachineRedstoneState.RedstoneMode.HIGH.ordinal(), copiedData.getInteger("redstoneMode"));
    }

    @Test
    void copiesSelectedMachineSettingsOntoAnotherMachine() {
        ItemMachineSettingsCopier item = new ItemMachineSettingsCopier();
        ItemStack stack = new ItemStack(item);
        TileMachineBase source = new TileMachineBase();
        source.getAreaState().setArea(5.0D, 2.0D, 3.5D);
        source.getAreaState().setOffset(9, -4, 1);
        source.getFilterState().setAllowList(false);
        source.getFilterState().setCompareNbt(true);
        source.getFilterState().setBlockItemFilter(1);
        source.getRedstoneState().setMode(MachineRedstoneState.RedstoneMode.PULSE);
        source.getRedstoneState().setPulsed(true);
        source.getRedstoneState().setReceivingRedstone(true);

        TileMachineBase target = new TileMachineBase();
        target.getAreaState().setArea(1.0D, 1.0D, 1.0D);
        target.getAreaState().setOffset(0, 0, 0);

        item.saveSettings(source, stack);
        assertTrue(item.loadSettings(target, stack));

        assertEquals(5.0D, target.getAreaState().getXRadius(), 0.001D);
        assertEquals(2.0D, target.getAreaState().getYRadius(), 0.001D);
        assertEquals(3.5D, target.getAreaState().getZRadius(), 0.001D);
        assertEquals(9, target.getAreaState().getXOffset());
        assertEquals(-4, target.getAreaState().getYOffset());
        assertEquals(1, target.getAreaState().getZOffset());
        assertFalse(target.getFilterState().isAllowList());
        assertTrue(target.getFilterState().isCompareNbt());
        assertEquals(1, target.getFilterState().getBlockItemFilter());
        assertEquals(MachineRedstoneState.RedstoneMode.PULSE, target.getRedstoneState().getMode());
        assertTrue(target.getRedstoneState().isPulsed());
        assertTrue(target.getRedstoneState().isReceivingRedstone());
    }

    @Test
    void respectsDisabledSettingGroupsWhenPasting() {
        ItemMachineSettingsCopier item = new ItemMachineSettingsCopier();
        ItemStack stack = new ItemStack(item);
        TileMachineBase source = new TileMachineBase();
        source.getAreaState().setArea(4.0D, 4.0D, 4.0D);
        source.getAreaState().setOffset(3, 3, 3);
        source.getRedstoneState().setMode(MachineRedstoneState.RedstoneMode.HIGH);

        item.saveSettings(source, stack);
        ItemMachineSettingsCopier.setSettings(stack, false, true, false, false);

        TileMachineBase target = new TileMachineBase();
        target.getAreaState().setArea(1.0D, 1.0D, 1.0D);
        target.getAreaState().setOffset(0, 0, 0);
        target.getRedstoneState().setMode(MachineRedstoneState.RedstoneMode.LOW);

        assertTrue(item.loadSettings(target, stack));

        assertEquals(1.0D, target.getAreaState().getXRadius(), 0.001D);
        assertEquals(3, target.getAreaState().getXOffset());
        assertEquals(MachineRedstoneState.RedstoneMode.LOW, target.getRedstoneState().getMode());
    }

    @Test
    void copiesItemCollectorFilterSlotsWhenAvailable() {
        ItemMachineSettingsCopier item = new ItemMachineSettingsCopier();
        ItemStack stack = new ItemStack(item);
        TileItemCollector source = new TileItemCollector();
        source.getFilterHandler().setStackInSlot(0, new ItemStack(Items.APPLE, 2));

        TileItemCollector target = new TileItemCollector();

        item.saveSettings(source, stack);
        item.loadSettings(target, stack);

        assertEquals(Items.APPLE, target.getFilterHandler().getStackInSlot(0).getItem());
        assertEquals(2, target.getFilterHandler().getStackInSlot(0).getCount());
    }

    @Test
    void copiesEnergyTransmitterFilterSlotsLikeOriginalFilterableMachines() {
        ItemMachineSettingsCopier item = new ItemMachineSettingsCopier();
        ItemStack stack = new ItemStack(item);
        TileEnergyTransmitter source = new TileEnergyTransmitter();
        source.getFilterHandler().setStackInSlot(0, new ItemStack(Items.REDSTONE, 3));

        TileEnergyTransmitter target = new TileEnergyTransmitter();

        item.saveSettings(source, stack);
        item.loadSettings(target, stack);

        assertEquals(Items.REDSTONE, target.getFilterHandler().getStackInSlot(0).getItem());
        assertEquals(3, target.getFilterHandler().getStackInSlot(0).getCount());
    }
}
