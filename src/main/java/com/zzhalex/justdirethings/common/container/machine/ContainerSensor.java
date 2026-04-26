package com.zzhalex.justdirethings.common.container.machine;

import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import com.zzhalex.justdirethings.common.tile.machine.TileSensor;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerSensor extends ContainerMachineBase {

    private final TileSensor tile;

    public ContainerSensor(InventoryPlayer playerInventory, TileSensor tile) {
        super(playerInventory, tile, new InventoryBasic("sensort1", false, 1));
        this.tile = tile;
        addSlotToContainer(new SlotItemHandler(tile.getItemHandler(), 0, 80, 13));
        addPlayerInventory(playerInventory, 8, 84);
    }

    public TileSensor getTile() {
        return tile;
    }
}
