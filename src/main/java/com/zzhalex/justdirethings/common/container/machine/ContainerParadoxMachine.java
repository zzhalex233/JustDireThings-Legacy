package com.zzhalex.justdirethings.common.container.machine;

import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import com.zzhalex.justdirethings.common.tile.machine.TileParadoxMachine;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;

public class ContainerParadoxMachine extends ContainerMachineBase {

    private final TileParadoxMachine tile;

    public ContainerParadoxMachine(InventoryPlayer playerInventory, TileParadoxMachine tile) {
        super(playerInventory, tile, new InventoryBasic("paradoxmachine", false, 0));
        this.tile = tile;
        addPlayerInventory(playerInventory, 8, 94);
    }

    public TileParadoxMachine getTile() {
        return tile;
    }
}
