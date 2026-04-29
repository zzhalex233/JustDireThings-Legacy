package com.zzhalex.justdirethings.common.container.machine;

import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import com.zzhalex.justdirethings.common.tile.base.TileAdvancedMachine;
import com.zzhalex.justdirethings.common.tile.machine.TileFluidPlacer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerFluidPlacer extends ContainerMachineBase {

    private final TileFluidPlacer tile;

    public ContainerFluidPlacer(InventoryPlayer playerInventory, TileFluidPlacer tile) {
        super(playerInventory, tile, new InventoryBasic("fluidplacert1", false, tile.getItemHandler().getSlots()));
        this.tile = tile;

        addSlotToContainer(new SlotItemHandler(tile.getItemHandler(), 0, 80, machineSlotY(tile)));
        addAdvancedFilterSlots(tile instanceof TileAdvancedMachine ? (TileAdvancedMachine) tile : null);
        addPlayerInventory(playerInventory, 8, 84);
    }

    public TileFluidPlacer getTile() {
        return tile;
    }
}
