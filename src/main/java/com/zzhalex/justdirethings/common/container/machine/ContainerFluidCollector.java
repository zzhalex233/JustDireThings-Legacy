package com.zzhalex.justdirethings.common.container.machine;

import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import com.zzhalex.justdirethings.common.tile.base.TileAdvancedMachine;
import com.zzhalex.justdirethings.common.tile.machine.TileFluidCollector;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerFluidCollector extends ContainerMachineBase {

    private final TileFluidCollector tile;

    public ContainerFluidCollector(InventoryPlayer playerInventory, TileFluidCollector tile) {
        super(playerInventory, tile, new InventoryBasic("fluidcollectort1", false, tile.getItemHandler().getSlots()));
        this.tile = tile;
        addSlotToContainer(new SlotItemHandler(tile.getItemHandler(), 0, 80, machineSlotY(tile)));
        addAdvancedFilterSlots(tile instanceof TileAdvancedMachine ? (TileAdvancedMachine) tile : null);
        addPlayerInventory(playerInventory, 8, 84);
    }

    public TileFluidCollector getTile() {
        return tile;
    }
}
