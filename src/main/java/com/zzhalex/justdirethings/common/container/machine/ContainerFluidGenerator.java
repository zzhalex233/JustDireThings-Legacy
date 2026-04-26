package com.zzhalex.justdirethings.common.container.machine;

import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import com.zzhalex.justdirethings.common.container.slot.SlotFluidFuel;
import com.zzhalex.justdirethings.common.tile.machine.TileFluidGenerator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;

public class ContainerFluidGenerator extends ContainerMachineBase {

    private final TileFluidGenerator tile;

    public ContainerFluidGenerator(InventoryPlayer playerInventory, TileFluidGenerator tile) {
        super(playerInventory, tile, new InventoryBasic("generatorfluidt1", false, 1));
        this.tile = tile;

        addSlotToContainer(new SlotFluidFuel(tile.getItemHandler(), 0, 80, 13));
        addPlayerInventory(playerInventory, 8, 84);
    }

    public TileFluidGenerator getTile() {
        return tile;
    }
}
