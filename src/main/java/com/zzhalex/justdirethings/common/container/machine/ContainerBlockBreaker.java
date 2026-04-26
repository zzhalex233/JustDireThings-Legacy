package com.zzhalex.justdirethings.common.container.machine;

import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import com.zzhalex.justdirethings.common.tile.machine.TileBlockBreaker;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerBlockBreaker extends ContainerMachineBase {

    private final TileBlockBreaker tile;

    public ContainerBlockBreaker(InventoryPlayer playerInventory, TileBlockBreaker tile) {
        super(playerInventory, tile, new InventoryBasic("blockbreakert1", false, 1));
        this.tile = tile;

        addSlotToContainer(new SlotItemHandler(tile.getItemHandler(), 0, 80, 13));
        addPlayerInventory(playerInventory, 8, 84);
    }

    public TileBlockBreaker getTile() {
        return tile;
    }
}
