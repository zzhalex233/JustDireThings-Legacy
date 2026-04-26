package com.zzhalex.justdirethings.client.gui.upstream;

import com.zzhalex.justdirethings.client.gui.GuiPotionCanister;
import com.zzhalex.justdirethings.common.container.ContainerPotionCanister;
import net.minecraft.entity.player.InventoryPlayer;

public class PotionCanisterScreen extends GuiPotionCanister {

    public PotionCanisterScreen(InventoryPlayer playerInventory, ContainerPotionCanister container) {
        super(playerInventory, container);
    }
}
