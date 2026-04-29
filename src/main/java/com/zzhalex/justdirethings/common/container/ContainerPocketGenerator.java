package com.zzhalex.justdirethings.common.container;

import com.zzhalex.justdirethings.common.container.handler.PocketGeneratorFuelHandler;
import com.zzhalex.justdirethings.common.item.misc.PocketGeneratorMath;
import com.zzhalex.justdirethings.common.item.misc.PocketGeneratorItem;
import com.zzhalex.justdirethings.config.JDTConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerPocketGenerator extends Container {

    public static final int SLOT_FUEL = 0;
    public static final int SLOT_COUNT = 1;
    public static final int PLAYER_INV_START = 1;
    private static final int FIELD_ENERGY = 0;
    private static final int FIELD_COUNTER = 1;
    private static final int FIELD_MAX_BURN = 2;
    private static final int FIELD_FUEL_MULTIPLIER = 3;

    private final PocketGeneratorItem pocketGeneratorItem;
    private final ItemStack boundStack;
    private final PocketGeneratorFuelHandler handler;
    private int syncedEnergy;
    private int syncedCounter;
    private int syncedMaxBurn;
    private int syncedFuelMultiplier;

    public ContainerPocketGenerator(InventoryPlayer playerInventory, ItemStack boundStack) {
        this.pocketGeneratorItem = (PocketGeneratorItem) boundStack.getItem();
        this.boundStack = boundStack;
        this.handler = new PocketGeneratorFuelHandler(boundStack, pocketGeneratorItem);
        refreshSyncedFieldsFromStack();

        addSlotToContainer(new SlotItemHandler(handler, SLOT_FUEL, 80, 35));

        addPlayerInventory(playerInventory);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return !boundStack.isEmpty() && playerIn.isEntityAlive();
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        pocketGeneratorItem.setFuelStack(boundStack, handler.getStackInSlot(SLOT_FUEL));
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack current = slot.getStack();
        itemStack = current.copy();
        if (index < SLOT_COUNT) {
            if (!mergeItemStack(current, SLOT_COUNT, inventorySlots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!mergeItemStack(current, 0, SLOT_COUNT, false)) {
            return ItemStack.EMPTY;
        }

        if (current.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }

        if (current.getCount() == itemStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(playerIn, current);
        return itemStack;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        int energy = pocketGeneratorItem.getStoredEnergy(boundStack);
        int counter = pocketGeneratorItem.getCounter(boundStack);
        int maxBurn = pocketGeneratorItem.getMaxBurn(boundStack);
        int fuelMultiplier = pocketGeneratorItem.getFuelMultiplier(boundStack);

        for (IContainerListener listener : listeners) {
            if (energy != syncedEnergy) {
                listener.sendWindowProperty(this, FIELD_ENERGY, energy);
            }
            if (counter != syncedCounter) {
                listener.sendWindowProperty(this, FIELD_COUNTER, counter);
            }
            if (maxBurn != syncedMaxBurn) {
                listener.sendWindowProperty(this, FIELD_MAX_BURN, maxBurn);
            }
            if (fuelMultiplier != syncedFuelMultiplier) {
                listener.sendWindowProperty(this, FIELD_FUEL_MULTIPLIER, fuelMultiplier);
            }
        }

        syncedEnergy = energy;
        syncedCounter = counter;
        syncedMaxBurn = maxBurn;
        syncedFuelMultiplier = fuelMultiplier;
    }

    @Override
    public void updateProgressBar(int id, int data) {
        switch (id) {
            case FIELD_ENERGY:
                syncedEnergy = data;
                break;
            case FIELD_COUNTER:
                syncedCounter = data;
                break;
            case FIELD_MAX_BURN:
                syncedMaxBurn = data;
                break;
            case FIELD_FUEL_MULTIPLIER:
                syncedFuelMultiplier = Math.max(1, data);
                break;
            default:
                super.updateProgressBar(id, data);
        }
    }

    public int getStoredEnergy() {
        return syncedEnergy;
    }

    public int getMaxEnergy() {
        return pocketGeneratorItem.getMaxEnergy();
    }

    public int getFePerTick() {
        return PocketGeneratorMath.fePerTick(JDTConfig.pocketGeneratorFePerFuelTick, getBurnSpeedMultiplier());
    }

    public int getBurnSpeedMultiplier() {
        return Math.max(1, JDTConfig.pocketGeneratorBurnSpeedMultiplier) * syncedFuelMultiplier;
    }

    public int getRemainingBurnTicks() {
        return syncedCounter;
    }

    public int getMaxBurnTicks() {
        return syncedMaxBurn;
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

    private void refreshSyncedFieldsFromStack() {
        syncedEnergy = pocketGeneratorItem.getStoredEnergy(boundStack);
        syncedCounter = pocketGeneratorItem.getCounter(boundStack);
        syncedMaxBurn = pocketGeneratorItem.getMaxBurn(boundStack);
        syncedFuelMultiplier = pocketGeneratorItem.getFuelMultiplier(boundStack);
    }
}
