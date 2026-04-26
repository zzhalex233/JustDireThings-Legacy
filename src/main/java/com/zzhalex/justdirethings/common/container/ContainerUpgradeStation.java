package com.zzhalex.justdirethings.common.container;

import com.zzhalex.justdirethings.common.tile.TileUpgradeStation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerUpgradeStation extends Container {

    public static final int SLOT_COUNT = 4;

    private final TileUpgradeStation tile;

    public ContainerUpgradeStation(InventoryPlayer playerInventory, TileUpgradeStation tile) {
        this.tile = tile;

        addSlotToContainer(new SlotItemHandler(tile.getItemHandler(), TileUpgradeStation.SLOT_TEMPLATE, 44, 20));
        addSlotToContainer(new SlotItemHandler(tile.getItemHandler(), TileUpgradeStation.SLOT_BASE, 62, 20));
        addSlotToContainer(new SlotItemHandler(tile.getItemHandler(), TileUpgradeStation.SLOT_ADDITION, 80, 20));
        addSlotToContainer(new SlotItemHandler(tile.getItemHandler(), TileUpgradeStation.SLOT_OUTPUT, 134, 38) {
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
        return ItemStack.EMPTY;
    }
}
