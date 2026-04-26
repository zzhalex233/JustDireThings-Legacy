package com.zzhalex.justdirethings.client.gui.upstream;

import com.zzhalex.justdirethings.client.gui.GuiFuelCanister;
import com.zzhalex.justdirethings.common.container.ContainerFuelCanister;
import net.minecraft.entity.player.InventoryPlayer;

public class FuelCanisterScreen extends GuiFuelCanister {

    public FuelCanisterScreen(InventoryPlayer playerInventory, ContainerFuelCanister container) {
        super(playerInventory, container);
    }
}
