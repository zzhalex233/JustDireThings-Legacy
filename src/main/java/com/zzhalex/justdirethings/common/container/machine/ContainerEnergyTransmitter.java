package com.zzhalex.justdirethings.common.container.machine;

import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import com.zzhalex.justdirethings.common.tile.machine.TileEnergyTransmitter;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerEnergyTransmitter extends ContainerMachineBase {

    private final TileEnergyTransmitter tile;

    public ContainerEnergyTransmitter(InventoryPlayer playerInventory, TileEnergyTransmitter tile) {
        super(playerInventory, tile, new InventoryBasic("energytransmitter", false, 1));
        this.tile = tile;
        addSlotToContainer(new SlotItemHandler(tile.getItemHandler(), 0, 80, 35));
        addPlayerInventory(playerInventory, 8, 84);
    }

    public TileEnergyTransmitter getTile() {
        return tile;
    }
}
