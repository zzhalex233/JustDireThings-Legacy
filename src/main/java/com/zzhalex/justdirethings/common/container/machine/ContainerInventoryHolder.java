package com.zzhalex.justdirethings.common.container.machine;

import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import com.zzhalex.justdirethings.common.container.slot.SlotInventoryHolder;
import com.zzhalex.justdirethings.common.tile.machine.TileInventoryHolder;
import com.zzhalex.justdirethings.common.util.ItemStackKey;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ContainerInventoryHolder extends ContainerMachineBase {

    public static final int SLOT_COUNT = 41;
    public static final int PLAYER_SLOT_COUNT = 41;
    private static final EntityEquipmentSlot[] ARMOR_SLOTS = new EntityEquipmentSlot[] {
            EntityEquipmentSlot.HEAD,
            EntityEquipmentSlot.CHEST,
            EntityEquipmentSlot.LEGS,
            EntityEquipmentSlot.FEET
    };

    private final TileInventoryHolder tile;
    private final InventoryPlayer playerInventory;

    public ContainerInventoryHolder(InventoryPlayer playerInventory, TileInventoryHolder tile) {
        super(playerInventory, tile, new InventoryBasic("inventory_holder", false, SLOT_COUNT));
        this.tile = tile;
        this.playerInventory = playerInventory;

        addMachineSlotBox(tile.getItemHandler(), 0, 8, -8, 9, 18, 3, 18);
        addMachineSlotRange(tile.getItemHandler(), 27, 8, 50, 9, 18);
        addMachineArmorSlots(tile.getItemHandler(), 36, 44, -28);
        addPlayerInventory(playerInventory, 8, 102);
        addPlayerArmorSlots(playerInventory);
    }

    public TileInventoryHolder getTile() {
        return tile;
    }

    public void sendAllItemsToMachine() {
        for (int i = SLOT_COUNT; i < SLOT_COUNT + PLAYER_SLOT_COUNT; i++) {
            transferStackInSlot(playerInventory.player, i);
        }
    }

    public void sendAllItemsToPlayer() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            transferStackInSlot(playerInventory.player, i);
        }
    }

    public void swapItems() {
        for (int playerSlot = SLOT_COUNT; playerSlot < SLOT_COUNT + PLAYER_SLOT_COUNT; playerSlot++) {
            Slot playerSlotObject = inventorySlots.get(playerSlot);
            ItemStack playerStack = playerSlotObject.getStack();
            int machineSlot = playerSlot - SLOT_COUNT;
            if (machineSlot >= SLOT_COUNT) {
                continue;
            }

            Slot machineSlotObject = inventorySlots.get(machineSlot);
            ItemStack machineStack = machineSlotObject.getStack();
            if (playerStack.isEmpty() && machineStack.isEmpty()) {
                continue;
            }
            if (isPlayerArmorSlot(playerSlot) && EnchantmentHelper.hasBindingCurse(playerStack)) {
                continue;
            }

            ItemStack machineStackCopy = machineStack.copy();
            ItemStack playerStackCopy = playerStack.copy();
            if (!playerStack.isEmpty()) {
                machineSlotObject.putStack(ItemStack.EMPTY);
                if (mergeItemStack(playerStack, machineSlot, machineSlot + 1, false) && playerStack.isEmpty()) {
                    playerSlotObject.putStack(machineStackCopy);
                    machineSlotObject.onSlotChanged();
                    playerSlotObject.onSlotChanged();
                } else {
                    machineSlotObject.putStack(machineStackCopy);
                    playerSlotObject.putStack(playerStackCopy);
                }
            } else if (!machineStack.isEmpty()) {
                mergeItemStack(machineStack, playerSlot, playerSlot + 1, false);
            }
        }
    }

    @Override
    public ItemStack transferStackInSlot(net.minecraft.entity.player.EntityPlayer playerIn, int index) {
        if (index < 0 || index >= inventorySlots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack currentStack = slot.getStack();
        ItemStack original = currentStack.copy();
        if (isPlayerArmorSlot(index) && EnchantmentHelper.hasBindingCurse(currentStack)) {
            return ItemStack.EMPTY;
        }

        if (index < SLOT_COUNT) {
            if (mergeItemStack(currentStack, index + SLOT_COUNT, index + SLOT_COUNT + 1, false)) {
                // Exact matching player slot accepted it.
            } else if (!mergeItemStack(currentStack, SLOT_COUNT, SLOT_COUNT + 36 + 4, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (moveToFilteredSlot(currentStack)) {
                // Filtered slot accepted it.
            } else if (tile.isFiltersOnly()) {
                return ItemStack.EMPTY;
            } else if (mergeItemStack(currentStack, index - SLOT_COUNT, index - SLOT_COUNT + 1, false)) {
                // Exact matching machine slot accepted it.
            } else if (!mergeItemStack(currentStack, 0, SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (currentStack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        if (currentStack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(playerIn, currentStack);
        return original;
    }

    public boolean moveToFilteredSlot(ItemStack currentStack) {
        if (tile == null || currentStack.isEmpty()) {
            return false;
        }
        ItemStackKey key = new ItemStackKey(currentStack, tile.isCompareNbt());
        for (int i = 0; i < tile.getFilterHandler().getSlots(); i++) {
            ItemStack filterStack = tile.getFilterHandler().getStackInSlot(i);
            if (filterStack.isEmpty()) {
                continue;
            }
            if (key.equals(new ItemStackKey(filterStack, tile.isCompareNbt()))) {
                if (mergeItemStack(currentStack, i, i + 1, false) && currentStack.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private int addMachineSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int slot = 0; slot < amount; slot++) {
            addSlotToContainer(new SlotInventoryHolder(handler, index + slot, x + slot * dx, y, tile));
        }
        return index + amount;
    }

    private int addMachineSlotBox(IItemHandler handler, int index, int x, int y, int columns, int dx, int rows, int dy) {
        int current = index;
        for (int row = 0; row < rows; row++) {
            current = addMachineSlotRange(handler, current, x, y + row * dy, columns, dx);
        }
        return current;
    }

    private void addMachineArmorSlots(IItemHandler handler, int index, int x, int y) {
        for (int slot = 0; slot < 4; slot++) {
            final EntityEquipmentSlot equipmentSlot = ARMOR_SLOTS[slot];
            addSlotToContainer(new SlotInventoryHolder(handler, index + slot, x + slot * 18, y, tile) {
                @Override
                public int getSlotStackLimit() {
                    return 1;
                }

                @Override
                public boolean isItemValid(ItemStack stack) {
                    return stack.getItem().isValidArmor(stack, equipmentSlot, playerInventory.player);
                }

                @Override
                public String getSlotTexture() {
                    return ItemArmor.EMPTY_SLOT_NAMES[equipmentSlot.getIndex()];
                }
            });
        }
        addSlotToContainer(new SlotInventoryHolder(handler, index + 4, x + 4 * 18, y, tile) {
            @Override
            public String getSlotTexture() {
                return "minecraft:items/empty_armor_slot_shield";
            }
        });
    }

    private void addPlayerArmorSlots(InventoryPlayer inventory) {
        for (int slot = 0; slot < 4; slot++) {
            final EntityEquipmentSlot equipmentSlot = ARMOR_SLOTS[slot];
            addSlotToContainer(new Slot(inventory, 39 - slot, 44 + slot * 18, 82) {
                @Override
                public int getSlotStackLimit() {
                    return 1;
                }

                @Override
                public boolean isItemValid(ItemStack stack) {
                    return stack.getItem().isValidArmor(stack, equipmentSlot, playerInventory.player);
                }

                @Override
                public boolean canTakeStack(net.minecraft.entity.player.EntityPlayer playerIn) {
                    ItemStack stack = getStack();
                    return (stack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(stack)) && super.canTakeStack(playerIn);
                }

                @Override
                public String getSlotTexture() {
                    return ItemArmor.EMPTY_SLOT_NAMES[equipmentSlot.getIndex()];
                }
            });
        }
        addSlotToContainer(new Slot(inventory, 40, 44 + 4 * 18, 82) {
            @Override
            public String getSlotTexture() {
                return "minecraft:items/empty_armor_slot_shield";
            }
        });
    }

    private static boolean isPlayerArmorSlot(int index) {
        return index >= 77 && index <= 80;
    }
}
