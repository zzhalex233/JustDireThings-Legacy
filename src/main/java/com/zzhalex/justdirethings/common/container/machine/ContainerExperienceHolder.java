package com.zzhalex.justdirethings.common.container.machine;

import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import com.zzhalex.justdirethings.common.tile.machine.TileExperienceHolder;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;

public class ContainerExperienceHolder extends ContainerMachineBase {

    private final TileExperienceHolder tile;

    public ContainerExperienceHolder(InventoryPlayer playerInventory, TileExperienceHolder tile) {
        super(playerInventory, tile, new InventoryBasic("experienceholder", false, 0));
        this.tile = tile;

        addPlayerInventory(playerInventory, 8, 84);
    }

    public TileExperienceHolder getTile() {
        return tile;
    }
}
