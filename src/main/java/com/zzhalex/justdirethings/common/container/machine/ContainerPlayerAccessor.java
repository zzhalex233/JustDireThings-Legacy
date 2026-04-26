package com.zzhalex.justdirethings.common.container.machine;

import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import com.zzhalex.justdirethings.common.container.slot.SlotReadOnlyItemHandler;
import com.zzhalex.justdirethings.common.tile.machine.TilePlayerAccessor;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;

public class ContainerPlayerAccessor extends ContainerMachineBase {

    private final TilePlayerAccessor tile;

    public ContainerPlayerAccessor(InventoryPlayer playerInventory, TilePlayerAccessor tile) {
        super(playerInventory, tile, new InventoryBasic("playeraccessor", false, 54));
        this.tile = tile;

        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 9; column++) {
                addSlotToContainer(new SlotReadOnlyItemHandler(tile.getItemHandler(), column + row * 9, 8 + column * 18, 18 + row * 18));
            }
        }
        addPlayerInventory(playerInventory, 8, 140);
    }

    public TilePlayerAccessor getTile() {
        return tile;
    }
}
