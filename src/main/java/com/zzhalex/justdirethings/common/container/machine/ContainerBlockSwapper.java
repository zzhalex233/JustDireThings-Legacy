package com.zzhalex.justdirethings.common.container.machine;

import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import com.zzhalex.justdirethings.common.tile.machine.TileBlockSwapper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;

public class ContainerBlockSwapper extends ContainerMachineBase {

    private final TileBlockSwapper tile;

    public ContainerBlockSwapper(InventoryPlayer playerInventory, TileBlockSwapper tile) {
        super(playerInventory, tile, new InventoryBasic("blockswappert1", false, 0));
        this.tile = tile;

        addPlayerInventory(playerInventory, 8, 84);
    }

    public TileBlockSwapper getTile() {
        return tile;
    }
}
