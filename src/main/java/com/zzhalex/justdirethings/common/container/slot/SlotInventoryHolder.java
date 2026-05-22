package com.zzhalex.justdirethings.common.container.slot;

import com.zzhalex.justdirethings.common.tile.machine.TileInventoryHolder;
import com.zzhalex.justdirethings.common.util.ItemStackKey;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotInventoryHolder extends SlotItemHandler {

    private final TileInventoryHolder inventoryHolder;

    public SlotInventoryHolder(IItemHandler itemHandler, int index, int xPosition, int yPosition, TileInventoryHolder inventoryHolder) {
        super(itemHandler, index, xPosition, yPosition);
        this.inventoryHolder = inventoryHolder;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return mayPlaceFiltered(stack);
    }

    @Override
    public int getSlotStackLimit() {
        if (inventoryHolder != null && inventoryHolder.isCompareCounts()) {
            return getFilterStackSize();
        }
        return super.getSlotStackLimit();
    }

    @Override
    public int getItemStackLimit(@Nonnull ItemStack stack) {
        if (inventoryHolder != null && inventoryHolder.isCompareCounts()) {
            return getFilterStackSize(stack);
        }
        return super.getItemStackLimit(stack);
    }

    public boolean mayPlaceFiltered(ItemStack currentStack) {
        if (inventoryHolder == null || currentStack.isEmpty()) {
            return false;
        }
        ItemStack filterStack = inventoryHolder.getFilterHandler().getStackInSlot(getSlotIndex());
        if (filterStack.isEmpty()) {
            return !inventoryHolder.isFiltersOnly();
        }
        return new ItemStackKey(currentStack, inventoryHolder.isCompareNbt()).equals(new ItemStackKey(filterStack, inventoryHolder.isCompareNbt()));
    }

    public int getFilterStackSize() {
        if (inventoryHolder == null) {
            return 0;
        }
        ItemStack filterStack = inventoryHolder.getFilterHandler().getStackInSlot(getSlotIndex());
        return filterStack.isEmpty() ? super.getSlotStackLimit() : filterStack.getCount();
    }

    public int getFilterStackSize(ItemStack stack) {
        if (inventoryHolder == null) {
            return 0;
        }
        ItemStack filterStack = inventoryHolder.getFilterHandler().getStackInSlot(getSlotIndex());
        return filterStack.isEmpty() ? super.getItemStackLimit(stack) : filterStack.getCount();
    }
}
