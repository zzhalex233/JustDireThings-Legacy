package com.zzhalex.justdirethings.common.container.base;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContainerMachineBaseShiftClickTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void shiftClickMovesPlayerStackIntoMachineSlot() {
        InventoryBasic machine = new InventoryBasic("machine", false, 1);
        InventoryBasic player = new InventoryBasic("player", false, 36);
        HarnessContainer container = new HarnessContainer(machine, player);
        player.setInventorySlotContents(0, new ItemStack(Items.STICK, 5));

        ItemStack moved = container.transferStackInSlot(null, 1);

        assertFalse(moved.isEmpty());
        assertEquals(5, machine.getStackInSlot(0).getCount());
        assertTrue(player.getStackInSlot(0).isEmpty());
    }

    @Test
    void shiftClickMovesMachineStackBackToPlayerInventory() {
        InventoryBasic machine = new InventoryBasic("machine", false, 1);
        InventoryBasic player = new InventoryBasic("player", false, 36);
        HarnessContainer container = new HarnessContainer(machine, player);
        machine.setInventorySlotContents(0, new ItemStack(Items.STICK, 5));

        ItemStack moved = container.transferStackInSlot(null, 0);

        assertFalse(moved.isEmpty());
        assertTrue(machine.getStackInSlot(0).isEmpty());
        assertEquals(5, countItems(player));
    }

    private static int countItems(IInventory inventory) {
        int count = 0;
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static final class HarnessContainer extends ContainerMachineBase {

        private HarnessContainer(IInventory machineInventory, IInventory playerInventory) {
            super(null, null, machineInventory);
            addSlotToContainer(new Slot(machineInventory, 0, 0, 0));
            for (int slot = 0; slot < playerInventory.getSizeInventory(); slot++) {
                addSlotToContainer(new Slot(playerInventory, slot, 0, 0));
            }
        }
    }
}
