package com.zzhalex.justdirethings.common.container;

import com.zzhalex.justdirethings.common.tile.TileUpgradeStation;
import com.zzhalex.justdirethings.registry.ModRecipes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerUpgradeStation extends Container {

    public static final int SLOT_COUNT = 4;
    public static final int TEMPLATE_SLOT_X = 8;
    public static final int BASE_SLOT_X = 26;
    public static final int ADDITION_SLOT_X = 44;
    public static final int OUTPUT_SLOT_X = 98;
    public static final int SMITHING_SLOT_Y = 48;
    public static final int ARROW_X = 67;
    public static final int ARROW_Y = 51;

    private final TileUpgradeStation tile;

    public ContainerUpgradeStation(InventoryPlayer playerInventory, TileUpgradeStation tile) {
        this.tile = tile;

        addSlotToContainer(new SlotItemHandler(tile.getItemHandler(), TileUpgradeStation.SLOT_TEMPLATE, TEMPLATE_SLOT_X, SMITHING_SLOT_Y) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return ModRecipes.isUpgradeStationTemplate(stack);
            }
        });
        addSlotToContainer(new SlotItemHandler(tile.getItemHandler(), TileUpgradeStation.SLOT_BASE, BASE_SLOT_X, SMITHING_SLOT_Y) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return ModRecipes.isUpgradeStationBase(stack);
            }
        });
        addSlotToContainer(new SlotItemHandler(tile.getItemHandler(), TileUpgradeStation.SLOT_ADDITION, ADDITION_SLOT_X, SMITHING_SLOT_Y) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return ModRecipes.isUpgradeStationAddition(stack);
            }
        });
        addSlotToContainer(new SlotItemHandler(tile.getItemHandler(), TileUpgradeStation.SLOT_OUTPUT, OUTPUT_SLOT_X, SMITHING_SLOT_Y) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            @Override
            public ItemStack onTake(EntityPlayer player, ItemStack stack) {
                tile.consumeInputsForOutput(stack);
                return super.onTake(player, stack);
            }
        });

        addPlayerInventory(playerInventory);
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

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return tile.getWorld() != null
                && tile.getWorld().getTileEntity(tile.getPos()) == tile
                && playerIn.getDistanceSq(tile.getPos()) <= 64.0D;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack current = slot.getStack();
        ItemStack original = current.copy();
        if (index < SLOT_COUNT) {
            if (!mergeItemStack(current, SLOT_COUNT, inventorySlots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!mergeItemStack(current, TileUpgradeStation.SLOT_TEMPLATE, TileUpgradeStation.SLOT_OUTPUT, false)) {
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
}
