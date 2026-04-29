package com.zzhalex.justdirethings.common.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class ContainerToolSettings extends Container {

    public ContainerToolSettings(InventoryPlayer playerInventory) {
        addPlayerInventory(playerInventory);
        addArmorAndOffhandSlots(playerInventory);
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
        return ItemStack.EMPTY;
    }
}
