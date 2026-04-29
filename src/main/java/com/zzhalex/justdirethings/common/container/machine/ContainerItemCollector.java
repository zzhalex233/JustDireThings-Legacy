package com.zzhalex.justdirethings.common.container.machine;

import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import com.zzhalex.justdirethings.common.tile.machine.TileItemCollector;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;

public class ContainerItemCollector extends ContainerMachineBase {

    public static final int FILTER_SLOT_COUNT = 9;

    private final TileItemCollector tile;

    public ContainerItemCollector(InventoryPlayer playerInventory, TileItemCollector tile) {
        super(playerInventory, tile, new InventoryBasic("itemcollector", false, 0));
        this.tile = tile;
        addFilterSlots(tile.getFilterHandler());
        addPlayerInventory(playerInventory, 8, 84);
    }

    public TileItemCollector getTile() {
        return tile;
    }
}
