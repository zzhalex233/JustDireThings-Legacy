package com.zzhalex.justdirethings.common.container.machine;

import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import com.zzhalex.justdirethings.common.container.slot.SlotFilterItemHandler;
import com.zzhalex.justdirethings.common.tile.machine.TileItemCollector;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerItemCollector extends ContainerMachineBase {

    public static final int FILTER_SLOT_COUNT = 9;

    private final TileItemCollector tile;

    public ContainerItemCollector(InventoryPlayer playerInventory, TileItemCollector tile) {
        super(playerInventory, tile, new InventoryBasic("itemcollector", false, 0));
        this.tile = tile;
        for (int slot = 0; slot < FILTER_SLOT_COUNT; slot++) {
            addSlotToContainer(new SlotFilterItemHandler(tile.getFilterHandler(), slot, 8 + slot * 18, 54));
        }
        addPlayerInventory(playerInventory, 8, 84);
    }

    public TileItemCollector getTile() {
        return tile;
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        if (isFilterSlot(slotId)) {
            if (clickTypeIn == ClickType.PICKUP) {
                ItemStack held = player.inventory.getItemStack();
                if (dragType == 1 || held.isEmpty()) {
                    tile.getFilterHandler().setStackInSlot(slotId, ItemStack.EMPTY);
                } else {
                    ItemStack filterStack = held.copy();
                    filterStack.setCount(1);
                    tile.getFilterHandler().setStackInSlot(slotId, filterStack);
                }
                tile.markDirtyClient();
                detectAndSendChanges();
                return held;
            }
            return ItemStack.EMPTY;
        }
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        if (index < FILTER_SLOT_COUNT || index >= inventorySlots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack filterStack = slot.getStack().copy();
        filterStack.setCount(1);
        return addFilterCopy(filterStack) ? filterStack : ItemStack.EMPTY;
    }

    private boolean addFilterCopy(ItemStack filterStack) {
        for (int slot = 0; slot < FILTER_SLOT_COUNT; slot++) {
            ItemStack existing = tile.getFilterHandler().getStackInSlot(slot);
            if (!existing.isEmpty() && ItemStack.areItemStacksEqual(existing, filterStack)) {
                return false;
            }
        }

        for (int slot = 0; slot < FILTER_SLOT_COUNT; slot++) {
            if (tile.getFilterHandler().getStackInSlot(slot).isEmpty()) {
                tile.getFilterHandler().setStackInSlot(slot, filterStack);
                tile.markDirtyClient();
                detectAndSendChanges();
                return true;
            }
        }
        return false;
    }

    private boolean isFilterSlot(int slotId) {
        return slotId >= 0 && slotId < FILTER_SLOT_COUNT;
    }
}
