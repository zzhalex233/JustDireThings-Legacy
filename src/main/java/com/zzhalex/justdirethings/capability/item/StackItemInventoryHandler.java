package com.zzhalex.justdirethings.capability.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;

public class StackItemInventoryHandler extends ItemStackHandler {

    private final ItemStack containerStack;
    private final String nbtKey;

    public StackItemInventoryHandler(ItemStack containerStack, String nbtKey, int size) {
        super(size);
        this.containerStack = containerStack;
        this.nbtKey = nbtKey;
        load();
    }

    @Override
    protected void onContentsChanged(int slot) {
        save();
    }

    private void load() {
        if (containerStack.hasTagCompound() && containerStack.getTagCompound().hasKey(nbtKey)) {
            deserializeNBT(containerStack.getTagCompound().getCompoundTag(nbtKey));
        }
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
