package com.zzhalex.justdirethings.common.container.machine;

import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import com.zzhalex.justdirethings.common.container.slot.SlotFuel;
import com.zzhalex.justdirethings.common.tile.machine.TileGenerator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;

public class ContainerGenerator extends ContainerMachineBase {

    private final TileGenerator tile;

    public ContainerGenerator(InventoryPlayer playerInventory, TileGenerator tile) {
        super(playerInventory, tile, new InventoryBasic("generatort1", false, 1));
        this.tile = tile;

        addSlotToContainer(new SlotFuel(tile.getItemHandler(), 0, 80, 13));
        addPlayerInventory(playerInventory, 8, 84);
    }

    public TileGenerator getTile() {
        return tile;
    }
}
