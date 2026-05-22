package com.zzhalex.justdirethings.common.container.machine;

import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import com.zzhalex.justdirethings.common.tile.machine.TilePlayerAccessor;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;

public class ContainerPlayerAccessor extends ContainerMachineBase {

    private final TilePlayerAccessor tile;

    public ContainerPlayerAccessor(InventoryPlayer playerInventory, TilePlayerAccessor tile) {
        super(playerInventory, tile, new InventoryBasic("playeraccessor", false, 0));
        this.tile = tile;

        addPlayerInventory(playerInventory, 8, 84);
    }

    public TilePlayerAccessor getTile() {
        return tile;
    }
}
