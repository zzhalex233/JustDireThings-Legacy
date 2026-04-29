package com.zzhalex.justdirethings.common.tile.base;

import com.zzhalex.justdirethings.capability.inventory.FilterItemHandler;
import net.minecraft.item.ItemStack;

public final class MachineFilterHelper {

    private MachineFilterHelper() {
    }

    public static boolean matchesFilter(FilterItemHandler filterHandler, MachineFilterState filterState, ItemStack stack) {
        if (filterHandler == null || filterState == null) {
            return true;
        }
        boolean allowList = filterState.isAllowList();
        for (int slot = 0; slot < filterHandler.getSlots(); slot++) {
            ItemStack filter = filterHandler.getStackInSlot(slot);
            if (!filter.isEmpty() && matchesFilterStack(filterState, filter, stack)) {
                return allowList;
            }
        }
        return !allowList;
    }

    public static boolean matchesFilterStack(MachineFilterState filterState, ItemStack filter, ItemStack stack) {
        if (filterState == null || filter.isEmpty() || stack.isEmpty()) {
            return false;
        }
        if (filter.getItem() != stack.getItem()) {
            return false;
        }
        if (filter.getMetadata() != stack.getMetadata()) {
            return false;
        }
        return !filterState.isCompareNbt() || ItemStack.areItemStackTagsEqual(filter, stack);
    }
}
