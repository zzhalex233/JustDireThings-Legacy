package com.zzhalex.justdirethings.compat.accessory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

public interface AccessoryInventoryBridge {

    AccessoryInventoryBridge EMPTY = new AccessoryInventoryBridge() {
        @Override
        public int getSlotCount() {
            return 0;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }
    };

    int getSlotCount();

    ItemStack getStackInSlot(int slot);

    default List<ItemStack> copyStacks() {
        List<ItemStack> copiedStacks = new ArrayList<>();
        for (int slot = 0; slot < getSlotCount(); slot++) {
            ItemStack stack = getStackInSlot(slot);
            if (!stack.isEmpty()) {
                copiedStacks.add(stack.copy());
            }
        }
        return copiedStacks;
    }

    static AccessoryInventoryBridge empty() {
        return EMPTY;
    }

    static AccessoryInventoryBridge none() {
        return EMPTY;
    }

    static AccessoryInventoryBridge fromItemHandler(IItemHandler itemHandler) {
        if (itemHandler == null) {
            return EMPTY;
        }
        return new AccessoryInventoryBridge() {
            @Override
            public int getSlotCount() {
                return itemHandler.getSlots();
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                if (slot < 0 || slot >= itemHandler.getSlots()) {
                    return ItemStack.EMPTY;
                }
                ItemStack stack = itemHandler.getStackInSlot(slot);
                return stack == null ? ItemStack.EMPTY : stack;
            }
        };
    }

    static AccessoryInventoryBridge forPlayer(EntityPlayer player) {
        if (BubblesCompatBridge.isLoaded()) {
            return BubblesCompatBridge.create(player);
        }
        if (BaublesCompatBridge.isLoaded()) {
            return BaublesCompatBridge.create(player);
        }
        return EMPTY;
    }
}
