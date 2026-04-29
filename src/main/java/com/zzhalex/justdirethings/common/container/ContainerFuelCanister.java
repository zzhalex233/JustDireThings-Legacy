package com.zzhalex.justdirethings.common.container;

import com.zzhalex.justdirethings.common.container.handler.FuelCanisterHandler;
import com.zzhalex.justdirethings.common.item.misc.FuelCanisterItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerFuelCanister extends Container {

    private static final int SLOT_INPUT = 0;

    private final ItemStack boundStack;
    private final FuelCanisterHandler handler;

    public ContainerFuelCanister(InventoryPlayer playerInventory, ItemStack boundStack) {
        this.boundStack = boundStack;
        this.handler = new FuelCanisterHandler(1, boundStack);

        addSlotToContainer(new SlotItemHandler(handler, SLOT_INPUT, 80, 35));

        addPlayerInventory(playerInventory);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return !boundStack.isEmpty() && playerIn.isEntityAlive();
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);

        ItemStack input = handler.getStackInSlot(SLOT_INPUT);
        if (input.isEmpty()) {
            return;
        }

        handler.setStackInSlot(SLOT_INPUT, ItemStack.EMPTY);
        if (!input.isEmpty()) {
            playerIn.addItemStackToInventory(input);
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (!slot.getHasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack current = slot.getStack();
        itemStack = current.copy();
        if (index == SLOT_INPUT) {
            if (!mergeItemStack(current, 1, inventorySlots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!mergeItemStack(current, SLOT_INPUT, SLOT_INPUT + 1, false)) {
            return ItemStack.EMPTY;
        }

        if (current.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        return itemStack;
    }

    public int getFuelLevel() {
        return FuelCanisterItem.getFuelLevel(boundStack);
    }

    public int getFuelItemsEquivalent() {
        return getFuelLevel() / FuelCanisterItem.MINIMUM_TICKS_CONSUMED;
    }

    private void addPlayerInventory(InventoryPlayer playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlotToContainer(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            addSlotToContainer(new Slot(playerInventory, hotbarSlot, 8 + hotbarSlot * 18, 142));
        }
    }
}
