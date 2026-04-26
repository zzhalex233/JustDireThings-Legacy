package com.zzhalex.justdirethings.common.container.machine;

import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import com.zzhalex.justdirethings.common.tile.machine.TileInventoryHolder;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerInventoryHolder extends ContainerMachineBase {

    public static final int SLOT_COUNT = 41;

    private final TileInventoryHolder tile;

    public ContainerInventoryHolder(InventoryPlayer playerInventory, TileInventoryHolder tile) {
        super(playerInventory, tile, new InventoryBasic("inventory_holder", false, SLOT_COUNT));
        this.tile = tile;

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlotToContainer(new SlotItemHandler(tile.getItemHandler(), column + row * 9, 8 + column * 18, 18 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlotToContainer(new SlotItemHandler(tile.getItemHandler(), 27 + column, 8 + column * 18, 76));
        }
        for (int column = 0; column < 5; column++) {
            addSlotToContainer(new SlotItemHandler(tile.getItemHandler(), 36 + column, 44 + column * 18, 0));
        }
        addPlayerInventory(playerInventory, 8, 126);
    }

    public TileInventoryHolder getTile() {
        return tile;
    }
}
