package com.zzhalex.justdirethings.common.container.machine;

import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import com.zzhalex.justdirethings.common.tile.machine.TileDropper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerDropper extends ContainerMachineBase {

    private final TileDropper tile;

    public ContainerDropper(InventoryPlayer playerInventory, TileDropper tile) {
        super(playerInventory, tile, new InventoryBasic("droppert1", false, 1));
        this.tile = tile;

        addSlotToContainer(new SlotItemHandler(tile.getItemHandler(), 0, 80, 13));
        addPlayerInventory(playerInventory, 8, 84);
    }

    public TileDropper getTile() {
        return tile;
    }
}
