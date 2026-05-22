package com.zzhalex.justdirethings.common.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Objects;

public final class ItemStackKey {

    private final ItemStack stack;
    private final boolean compareNbt;
    private final int hash;

    public ItemStackKey(ItemStack stack, boolean compareNbt) {
        this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
        this.stack.setCount(1);
        this.compareNbt = compareNbt;
        NBTTagCompound tag = compareNbt ? this.stack.getTagCompound() : null;
        this.hash = Objects.hash(this.stack.getItem(), this.stack.getMetadata(), tag);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ItemStackKey)) {
            return false;
        }
        ItemStackKey other = (ItemStackKey) obj;
        if (!ItemStack.areItemsEqual(stack, other.stack)) {
            return false;
        }
        return !compareNbt || ItemStack.areItemStackTagsEqual(stack, other.stack);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
