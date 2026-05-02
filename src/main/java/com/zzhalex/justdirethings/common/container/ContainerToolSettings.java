package com.zzhalex.justdirethings.common.container;

import com.zzhalex.justdirethings.common.item.misc.PotionCanisterItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContainerToolSettings extends Container {

    private final List<Slot> dynamicSlots = new ArrayList<>();
    private IItemHandler selectedItemHandler;

    public ContainerToolSettings(InventoryPlayer playerInventory) {
        addPlayerInventory(playerInventory);
        addArmorAndOffhandSlots(playerInventory);
        refreshSlots(playerInventory.getCurrentItem());
    }

    private void addPlayerInventory(InventoryPlayer playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    private void addArmorAndOffhandSlots(InventoryPlayer playerInventory) {
        for (int index = 0; index < 4; index++) {
            final EntityEquipmentSlot equipmentSlot = EntityEquipmentSlot.values()[2 + index];
            addSlotToContainer(new Slot(playerInventory, 39 - index, 44 + index * 18, 66) {
                @Override
                public int getSlotStackLimit() {
                    return 1;
                }

                @Override
                public boolean isItemValid(ItemStack stack) {
                    return stack.getItem().isValidArmor(stack, equipmentSlot, playerInventory.player)
                            || stack.getItem() instanceof ItemArmor && ((ItemArmor) stack.getItem()).armorType == equipmentSlot;
                }
            });
        }

        addSlotToContainer(new Slot(playerInventory, 40, 116, 66));
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        if (index < 0 || index >= inventorySlots.size() || dynamicSlots.isEmpty()) {
            return ItemStack.EMPTY;
        }

        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack original = ItemStack.EMPTY;
        ItemStack current = slot.getStack();
        original = current.copy();
        int dynamicStart = inventorySlots.size() - dynamicSlots.size();
        if (index >= dynamicStart) {
            if (!mergeItemStack(current, 0, dynamicStart, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!mergeItemStack(current, dynamicStart, inventorySlots.size(), false)) {
            return ItemStack.EMPTY;
        }

        if (current.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }

        if (current.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(playerIn, current);
        return original;
    }

    public List<Slot> getDynamicSlots() {
        return Collections.unmodifiableList(dynamicSlots);
    }

    public void refreshSlots(ItemStack selectedStack) {
        clearDynamicSlots();
        selectedItemHandler = getItemSlots(selectedStack);
        if (selectedItemHandler != null) {
            addSelectedItemSlots();
        }
    }

    private void addSelectedItemSlots() {
        for (int i = 0; i < selectedItemHandler.getSlots(); i++) {
            int x = 134 + (i % 2) * 18;
            int y = 66 - (i / 2) * 18;
            Slot slot = new SlotItemHandler(selectedItemHandler, i, x, y) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return stack.getItem() instanceof PotionCanisterItem;
                }
            };
            addSlotToContainer(slot);
            dynamicSlots.add(slot);
        }
    }

    private void clearDynamicSlots() {
        for (int i = dynamicSlots.size() - 1; i >= 0; i--) {
            Slot slot = dynamicSlots.get(i);
            int index = inventorySlots.indexOf(slot);
            if (index >= 0) {
                inventorySlots.remove(index);
                inventoryItemStacks.remove(index);
            }
        }
        dynamicSlots.clear();
        selectedItemHandler = null;
    }

    private IItemHandler getItemSlots(ItemStack selectedStack) {
        if (selectedStack.isEmpty()) {
            return null;
        }
        return selectedStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    }
}
