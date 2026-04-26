package com.zzhalex.justdirethings.common.container.machine;

import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import com.zzhalex.justdirethings.common.tile.machine.TileBlockPlacer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerBlockPlacer extends ContainerMachineBase {

    private final TileBlockPlacer tile;

    public ContainerBlockPlacer(InventoryPlayer playerInventory, TileBlockPlacer tile) {
        super(playerInventory, tile, new InventoryBasic("blockplacert1", false, 1));
        this.tile = tile;

        addSlotToContainer(new SlotItemHandler(tile.getItemHandler(), 0, 80, 13));
        addPlayerInventory(playerInventory, 8, 84);
    }

    public TileBlockPlacer getTile() {
        return tile;
    }
}
