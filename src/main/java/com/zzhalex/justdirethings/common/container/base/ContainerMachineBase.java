package com.zzhalex.justdirethings.common.container.base;

import com.zzhalex.justdirethings.common.container.slot.SlotFilterItemHandler;
import com.zzhalex.justdirethings.common.tile.base.TileAdvancedMachine;
import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMachineBase extends Container {

    public static final int PLAYER_INV_START = 0;
    public static final int PLAYER_INV_ROWS = 3;
    public static final int PLAYER_INV_COLUMNS = 9;
    public static final int PLAYER_HOTBAR_SIZE = 9;

    protected final TileMachineBase machine;
    protected final IInventory machineInventory;
    private ItemStackHandler filterHandler;
    private int filterSlotStart = -1;
    private int filterSlotCount;

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

    protected int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int slot = 0; slot < amount; slot++) {
            addSlotToContainer(new SlotItemHandler(handler, index + slot, x + slot * dx, y));
        }
        return index + amount;
    }

    protected int machineSlotY(TileMachineBase tile) {
        return tile instanceof TileAdvancedMachine ? 35 : 13;
    }

    protected int addSlotBox(IItemHandler handler, int index, int x, int y, int columns, int dx, int rows, int dy) {
        int current = index;
        for (int row = 0; row < rows; row++) {
            current = addSlotRange(handler, current, x, y + row * dy, columns, dx);
        }
        return current;
    }

    protected void addAdvancedFilterSlots(TileAdvancedMachine advancedMachine) {
        if (advancedMachine == null || advancedMachine.getFilterHandler() == null) {
            return;
        }
        addFilterSlots(advancedMachine.getFilterHandler());
    }

    protected void addFilterSlots(ItemStackHandler filterHandler) {
        if (filterHandler == null) {
            return;
        }
        this.filterHandler = filterHandler;
        this.filterSlotStart = inventorySlots.size();
        this.filterSlotCount = filterHandler.getSlots();
        addFilterSlots(filterHandler, 8, 54, 9);
    }

    protected void addFilterSlots(ItemStackHandler filterHandler, int left, int top, int columns) {
        if (filterHandler == null) {
            return;
        }
        this.filterHandler = filterHandler;
        this.filterSlotStart = inventorySlots.size();
        this.filterSlotCount = filterHandler.getSlots();
        int slotColumns = Math.max(1, columns);
        for (int slot = 0; slot < filterHandler.getSlots(); slot++) {
            addSlotToContainer(new SlotFilterItemHandler(
                    filterHandler,
                    slot,
                    left + (slot % slotColumns) * 18,
                    top + (slot / slotColumns) * 18
            ));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return machine == null || !machine.isInvalid() && playerIn.isEntityAlive();
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        if (isFilterSlot(slotId)) {
            return handleFilterSlotClick(slotId, dragType, clickTypeIn, player);
        }
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    protected ItemStack handleFilterSlotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        if (filterHandler == null || clickTypeIn != ClickType.PICKUP || player == null) {
            return ItemStack.EMPTY;
        }

        ItemStack held = player.inventory.getItemStack();
        int filterIndex = slotId - filterSlotStart;
        if (dragType == 1 || held.isEmpty()) {
            filterHandler.setStackInSlot(filterIndex, ItemStack.EMPTY);
        } else {
            ItemStack filterStack = held.copy();
            filterStack.setCount(1);
            filterHandler.setStackInSlot(filterIndex, filterStack);
        }
        markFilterChanged();
        return held;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        if (index < 0 || index >= inventorySlots.size()) {
            return ItemStack.EMPTY;
        }
        if (isFilterSlot(index)) {
            return ItemStack.EMPTY;
        }

        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        int machineSlotCount = getMachineSlotCount();
        int playerSlotStart = getPlayerSlotStart(machineSlotCount);

        if (index < machineSlotCount) {
            if (!mergeItemStack(stack, playerSlotStart, inventorySlots.size(), true) || stack.getCount() == original.getCount()) {
                return ItemStack.EMPTY;
            }
            updateTransferredSlot(playerIn, slot, stack);
            return original;
        }

        if (index >= playerSlotStart) {
            if (machineSlotCount > 0 && mergeItemStack(stack, 0, machineSlotCount, false) && stack.getCount() != original.getCount()) {
                updateTransferredSlot(playerIn, slot, stack);
                return original;
            }
            ItemStack filterStack = original.copy();
            filterStack.setCount(1);
            return addFilterCopy(filterStack) ? filterStack : ItemStack.EMPTY;
        }

        return ItemStack.EMPTY;
    }

    protected boolean addFilterCopy(ItemStack filterStack) {
        if (filterHandler == null || filterStack.isEmpty()) {
            return false;
        }
        for (int slot = 0; slot < filterSlotCount; slot++) {
            ItemStack existing = filterHandler.getStackInSlot(slot);
            if (!existing.isEmpty() && ItemStack.areItemStacksEqual(existing, filterStack)) {
                return false;
            }
        }
        for (int slot = 0; slot < filterSlotCount; slot++) {
            if (filterHandler.getStackInSlot(slot).isEmpty()) {
                filterHandler.setStackInSlot(slot, filterStack);
                markFilterChanged();
                return true;
            }
        }
        return false;
    }

    public boolean applyGhostSlot(int slotId, ItemStack ghostStack) {
        if (!isFilterSlot(slotId) || filterHandler == null) {
            return false;
        }
        int filterIndex = slotId - filterSlotStart;
        if (ghostStack == null || ghostStack.isEmpty()) {
            filterHandler.setStackInSlot(filterIndex, ItemStack.EMPTY);
        } else {
            ItemStack filterStack = ghostStack.copy();
            filterStack.setCount(1);
            filterHandler.setStackInSlot(filterIndex, filterStack);
        }
        markFilterChanged();
        return true;
    }

    private void updateTransferredSlot(EntityPlayer playerIn, Slot slot, ItemStack stack) {
        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }

        if (playerIn != null) {
            slot.onTake(playerIn, stack);
        }
    }

    private int getMachineSlotCount() {
        if (machineInventory == null) {
            return 0;
        }
        return Math.min(machineInventory.getSizeInventory(), inventorySlots.size());
    }

    private int getPlayerSlotStart(int machineSlotCount) {
        if (filterSlotStart >= 0) {
            return filterSlotStart + filterSlotCount;
        }
        return machineSlotCount;
    }

    private boolean isFilterSlot(int slotId) {
        return filterSlotStart >= 0 && slotId >= filterSlotStart && slotId < filterSlotStart + filterSlotCount;
    }

    private void markFilterChanged() {
        if (machine != null) {
            machine.markDirtyClient();
        }
        detectAndSendChanges();
    }
}
