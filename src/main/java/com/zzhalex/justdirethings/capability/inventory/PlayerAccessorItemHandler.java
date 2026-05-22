package com.zzhalex.justdirethings.capability.inventory;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class PlayerAccessorItemHandler extends ItemStackHandler {

    public enum InventoryType {
        INVENTORY,
        ARMOR,
        OFFHAND
    }

    private static final EntityEquipmentSlot[] ARMOR_SLOTS = new EntityEquipmentSlot[] {
            EntityEquipmentSlot.FEET,
            EntityEquipmentSlot.LEGS,
            EntityEquipmentSlot.CHEST,
            EntityEquipmentSlot.HEAD
    };

    private final EntityPlayer player;
    private final InventoryType inventoryType;

    public PlayerAccessorItemHandler(EntityPlayer player, InventoryType inventoryType) {
        super(getStacks(player, inventoryType));
        this.player = player;
        this.inventoryType = inventoryType;
    }

    public boolean isPlayerInvalid() {
        return player == null || player.isDead;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (isPlayerInvalid()) {
            return;
        }
        super.setStackInSlot(slot, stack);
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        if (isPlayerInvalid()) {
            return ItemStack.EMPTY;
        }
        return super.getStackInSlot(slot);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (isPlayerInvalid() || !isItemValid(slot, stack)) {
            return stack;
        }
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (isPlayerInvalid()) {
            return ItemStack.EMPTY;
        }
        if (inventoryType == InventoryType.ARMOR && EnchantmentHelper.hasBindingCurse(super.getStackInSlot(slot))) {
            return ItemStack.EMPTY;
        }
        return super.extractItem(slot, amount, simulate);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (isPlayerInvalid()) {
            return false;
        }
        if (inventoryType == InventoryType.INVENTORY || inventoryType == InventoryType.OFFHAND) {
            return true;
        }
        return slot >= 0
                && slot < ARMOR_SLOTS.length
                && stack.getItem().isValidArmor(stack, ARMOR_SLOTS[slot], player);
    }

    @Override
    public int getSlotLimit(int slot) {
        return inventoryType == InventoryType.ARMOR ? 1 : super.getSlotLimit(slot);
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (isPlayerInvalid()) {
            return;
        }
        player.inventory.markDirty();
        if (player instanceof EntityPlayerMP) {
            player.openContainer.detectAndSendChanges();
        }
    }

    private static NonNullList<ItemStack> getStacks(EntityPlayer player, InventoryType inventoryType) {
        if (player == null) {
            return NonNullList.withSize(0, ItemStack.EMPTY);
        }
        if (inventoryType == InventoryType.INVENTORY) {
            return player.inventory.mainInventory;
        }
        if (inventoryType == InventoryType.ARMOR) {
            return player.inventory.armorInventory;
        }
        return player.inventory.offHandInventory;
    }
}
