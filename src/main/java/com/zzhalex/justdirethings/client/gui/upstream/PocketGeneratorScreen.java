package com.zzhalex.justdirethings.client.gui.upstream;

import com.zzhalex.justdirethings.client.gui.GuiPocketGenerator;
import com.zzhalex.justdirethings.common.container.ContainerPocketGenerator;
import net.minecraft.entity.player.InventoryPlayer;

public class PocketGeneratorScreen extends GuiPocketGenerator {

    public PocketGeneratorScreen(InventoryPlayer playerInventory, ContainerPocketGenerator container) {
        super(playerInventory, container);
    }
}
