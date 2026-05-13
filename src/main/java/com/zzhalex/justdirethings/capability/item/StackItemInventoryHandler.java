package com.zzhalex.justdirethings.capability.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

public class StackItemInventoryHandler extends ItemStackHandler {

    private final ItemStack containerStack;
    private final String nbtKey;
    private final int requestedSize;

    public StackItemInventoryHandler(ItemStack containerStack, String nbtKey, int size) {
        super(size);
        this.containerStack = containerStack;
        this.nbtKey = nbtKey;
        this.requestedSize = size;
        load();
    }

    @Override
    protected void onContentsChanged(int slot) {
        save();
    }

    private void load() {
        if (containerStack.hasTagCompound() && containerStack.getTagCompound().hasKey(nbtKey)) {
            deserializeNBT(containerStack.getTagCompound().getCompoundTag(nbtKey));
            resizeToRequestedSize();
        }
    }

    private void resizeToRequestedSize() {
        if (getSlots() == requestedSize) {
            return;
        }

        NonNullList<ItemStack> resized = NonNullList.withSize(requestedSize, ItemStack.EMPTY);
        for (int slot = 0; slot < Math.min(requestedSize, stacks.size()); slot++) {
            resized.set(slot, stacks.get(slot));
        }
        stacks = resized;
        save();
    }

    private void save() {
        NBTTagCompound root = containerStack.getTagCompound();
        if (root == null) {
            root = new NBTTagCompound();
            containerStack.setTagCompound(root);
        }
        root.setTag(nbtKey, serializeNBT());
    }
}
