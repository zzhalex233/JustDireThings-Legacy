package com.zzhalex.justdirethings.capability.inventory;

import com.zzhalex.justdirethings.common.tile.machine.TileInventoryHolder;
import com.zzhalex.justdirethings.common.util.ItemStackKey;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class InventoryHolderItemHandler extends ItemStackHandler {

    private final TileInventoryHolder inventoryHolder;
    private final IItemHandlerModifiable sourceHandler;

    public InventoryHolderItemHandler(TileInventoryHolder inventoryHolder, IItemHandlerModifiable sourceHandler) {
        super(sourceHandler.getSlots());
        this.inventoryHolder = inventoryHolder;
        this.sourceHandler = sourceHandler;
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (inventoryHolder == null || amount <= 0) {
            return ItemStack.EMPTY;
        }
        int allowedAmount = inventoryHolder.allowedExtractAmount(slot, amount);
        return sourceHandler.extractItem(slot, allowedAmount, simulate);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (inventoryHolder == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStackKey key = new ItemStackKey(stack, inventoryHolder.isCompareNbt());
        List<Integer> filteredSlots = inventoryHolder.getFilteredSlots(key);
        if (filteredSlots != null) {
            if (filteredSlots.contains(slot)) {
                return insertItemProxy(slot, stack, simulate);
            }
            for (Integer filteredSlot : filteredSlots) {
                stack = insertItemProxy(filteredSlot, stack, simulate);
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return insertItemProxy(slot, stack, simulate);
    }

    @Nonnull
    public ItemStack insertItemProxy(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!isItemValid(slot, stack)) {
            return stack;
        }

        ItemStack existing = sourceHandler.getStackInSlot(slot);
        int limit = getStackLimit(slot, stack);
        if (!existing.isEmpty()) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) {
                return stack;
            }
            limit -= existing.getCount();
        }
        if (limit <= 0) {
            return stack;
        }

        boolean reachedLimit = stack.getCount() > limit;
        if (!simulate) {
            if (existing.isEmpty()) {
                sourceHandler.setStackInSlot(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack.copy());
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
                sourceHandler.setStackInSlot(slot, existing);
            }
            onContentsChanged(slot);
        }
        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (inventoryHolder == null) {
            return false;
        }
        if (inventoryHolder.isAutomatedFiltersOnly()) {
            return inventoryHolder.isStackValidFilter(stack, slot);
        }
        return sourceHandler.isItemValid(slot, stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        if (inventoryHolder == null) {
            return 0;
        }
        int allowedAmount = inventoryHolder.getAutomatedSlotLimit(slot);
        return allowedAmount == -1 ? 64 : allowedAmount;
    }

    @Override
    protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
        if (inventoryHolder == null) {
            return 0;
        }
        int allowedAmount = inventoryHolder.getAutomatedSlotLimit(slot);
        return Math.min(allowedAmount == -1 ? 64 : allowedAmount, stack.getMaxStackSize());
    }

    @Override
    public int getSlots() {
        return sourceHandler.getSlots();
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        return sourceHandler.getStackInSlot(slot);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        sourceHandler.setStackInSlot(slot, stack);
    }
}
