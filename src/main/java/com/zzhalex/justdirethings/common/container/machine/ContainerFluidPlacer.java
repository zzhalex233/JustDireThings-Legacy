package com.zzhalex.justdirethings.common.container.machine;

import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import com.zzhalex.justdirethings.common.tile.machine.TileFluidPlacer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerFluidPlacer extends ContainerMachineBase {

    private final TileFluidPlacer tile;

    public ContainerFluidPlacer(InventoryPlayer playerInventory, TileFluidPlacer tile) {
        super(playerInventory, tile, new InventoryBasic("fluidplacert1", false, 1));
        this.tile = tile;

        addSlotToContainer(new SlotItemHandler(tile.getItemHandler(), 0, 80, 13));
        addPlayerInventory(playerInventory, 8, 84);
    }

    public TileFluidPlacer getTile() {
        return tile;
    }
}
