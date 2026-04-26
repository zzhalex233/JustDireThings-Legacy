package com.zzhalex.justdirethings.common.container.base;

import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerMachineBase extends Container {

    public static final int PLAYER_INV_START = 0;
    public static final int PLAYER_INV_ROWS = 3;
    public static final int PLAYER_INV_COLUMNS = 9;
    public static final int PLAYER_HOTBAR_SIZE = 9;

    protected final TileMachineBase machine;
    protected final IInventory machineInventory;

    public ContainerMachineBase(InventoryPlayer playerInventory, TileMachineBase machine, IInventory machineInventory) {
        this.machine = machine;
        this.machineInventory = machineInventory;
    }

    public TileMachineBase getMachine() {
        return machine;
    }

    protected void addPlayerInventory(InventoryPlayer playerInventory, int left, int top) {
        for (int row = 0; row < PLAYER_INV_ROWS; row++) {
            for (int column = 0; column < PLAYER_INV_COLUMNS; column++) {
                addSlotToContainer(new Slot(
                        playerInventory,
                        column + row * PLAYER_INV_COLUMNS + PLAYER_HOTBAR_SIZE,
                        left + column * 18,
                        top + row * 18
                ));
            }
        }

        int hotbarY = top + 58;
        for (int hotbarSlot = 0; hotbarSlot < PLAYER_HOTBAR_SIZE; hotbarSlot++) {
            addSlotToContainer(new Slot(playerInventory, hotbarSlot, left + hotbarSlot * 18, hotbarY));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return machine == null || !machine.isInvalid() && playerIn.isEntityAlive();
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        if (index < 0 || index >= inventorySlots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        int machineSlotCount = getMachineSlotCount();

        boolean merged;
        if (index < machineSlotCount) {
            merged = mergeItemStack(stack, machineSlotCount, inventorySlots.size(), true);
        } else if (machineSlotCount > 0) {
            merged = mergeItemStack(stack, 0, machineSlotCount, false);
        } else {
            return ItemStack.EMPTY;
        }

        if (!merged || stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }

        if (playerIn != null) {
            slot.onTake(playerIn, stack);
        }

        return original;
    }

    private int getMachineSlotCount() {
        if (machineInventory == null) {
            return 0;
        }
        return Math.min(machineInventory.getSizeInventory(), inventorySlots.size());
    }
}
