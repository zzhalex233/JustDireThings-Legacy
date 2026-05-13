package com.zzhalex.justdirethings.common.container;

import com.zzhalex.justdirethings.common.container.handler.PotionCanisterHandler;
import com.zzhalex.justdirethings.common.item.misc.PotionCanisterItem;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerPotionCanister extends Container {

    private static final int SLOT_INPUT = 0;
    private static final int FIELD_POTION_AMOUNT = 0;
    private static final int FIELD_POTION_TYPE = 1;

    private final ItemStack boundStack;
    private final PotionCanisterHandler handler;
    private int syncedPotionAmount;
    private int syncedPotionTypeId;

    public ContainerPotionCanister(InventoryPlayer playerInventory, ItemStack boundStack) {
        this.boundStack = boundStack;
        this.handler = new PotionCanisterHandler(boundStack, JDTDataKeys.TOOL_CONTENTS, 1);
        refreshSyncedFieldsFromStack();

        addSlotToContainer(new SlotItemHandler(handler, SLOT_INPUT, 80, 35) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return !stack.isEmpty() && stack.getItem() instanceof ItemPotion;
            }
        });

        addPlayerInventory(playerInventory);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return !boundStack.isEmpty() && playerIn.isEntityAlive();
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

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        refreshSyncedFieldsFromStack();
        listener.sendWindowProperty(this, FIELD_POTION_AMOUNT, syncedPotionAmount);
        listener.sendWindowProperty(this, FIELD_POTION_TYPE, syncedPotionTypeId);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        int potionAmount = PotionCanisterItem.getPotionAmount(boundStack);
        int potionTypeId = getPotionTypeId(boundStack);

        for (IContainerListener listener : listeners) {
            if (potionAmount != syncedPotionAmount) {
                listener.sendWindowProperty(this, FIELD_POTION_AMOUNT, potionAmount);
            }
            if (potionTypeId != syncedPotionTypeId) {
                listener.sendWindowProperty(this, FIELD_POTION_TYPE, potionTypeId);
            }
        }

        syncedPotionAmount = potionAmount;
        syncedPotionTypeId = potionTypeId;
    }

    @Override
    public void updateProgressBar(int id, int data) {
        switch (id) {
            case FIELD_POTION_AMOUNT:
                syncedPotionAmount = Math.max(0, Math.min(PotionCanisterItem.MAX_MB, data));
                break;
            case FIELD_POTION_TYPE:
                syncedPotionTypeId = data;
                break;
            default:
                super.updateProgressBar(id, data);
        }
    }

    public ItemStack getBoundStack() {
        return boundStack;
    }

    public int getPotionAmount() {
        return syncedPotionAmount;
    }

    public PotionType getPotionType() {
        PotionType potionType = PotionType.REGISTRY.getObjectById(syncedPotionTypeId);
        return potionType == null ? PotionTypes.EMPTY : potionType;
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

    private void refreshSyncedFieldsFromStack() {
        syncedPotionAmount = PotionCanisterItem.getPotionAmount(boundStack);
        syncedPotionTypeId = getPotionTypeId(boundStack);
    }

    private static int getPotionTypeId(ItemStack stack) {
        PotionType potionType = PotionCanisterItem.getPotionType(stack);
        int id = PotionType.REGISTRY.getIDForObject(potionType);
        if (id < 0) {
            return PotionType.REGISTRY.getIDForObject(PotionTypes.EMPTY);
        }
        return id;
    }
}
