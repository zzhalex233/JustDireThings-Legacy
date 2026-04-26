package com.zzhalex.justdirethings.common.container;

import com.zzhalex.justdirethings.common.item.misc.PotionCanisterItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;

public class ContainerPotionCanister extends Container {

    private static final int SLOT_INPUT = 0;

    private final ItemStack boundStack;
    private final IInventory inputInventory;

    public ContainerPotionCanister(InventoryPlayer playerInventory, ItemStack boundStack) {
        this.boundStack = boundStack;
        this.inputInventory = new InventoryBasic("potion_canister", false, 1);

        addSlotToContainer(new Slot(inputInventory, SLOT_INPUT, 80, 35) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return !stack.isEmpty() && (stack.getItem() instanceof ItemPotion || stack.getItem() == Items.GLASS_BOTTLE);
            }
        });

        addPlayerInventory(playerInventory);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return !boundStack.isEmpty() && playerIn.isEntityAlive();
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);

        ItemStack input = inputInventory.getStackInSlot(SLOT_INPUT);
        inputInventory.setInventorySlotContents(SLOT_INPUT, ItemStack.EMPTY);
        if (input.isEmpty()) {
            return;
        }

        if (input.getItem() instanceof ItemPotion && PotionCanisterItem.tryFillFromPotionItem(boundStack, input)) {
            playerIn.addItemStackToInventory(new ItemStack(Items.GLASS_BOTTLE));
            if (!input.isEmpty()) {
                playerIn.addItemStackToInventory(input);
            }
            return;
        }

        playerIn.addItemStackToInventory(input);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (!slot.getHasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack current = slot.getStack();
        itemStack = current.copy();
        if (index == SLOT_INPUT) {
            if (!mergeItemStack(current, 1, inventorySlots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!mergeItemStack(current, SLOT_INPUT, SLOT_INPUT + 1, false)) {
            return ItemStack.EMPTY;
        }

        if (current.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        return itemStack;
    }

    public ItemStack getBoundStack() {
        return boundStack;
    }

    private void addPlayerInventory(InventoryPlayer playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlotToContainer(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            addSlotToContainer(new Slot(playerInventory, hotbarSlot, 8 + hotbarSlot * 18, 142));
        }
    }
}
