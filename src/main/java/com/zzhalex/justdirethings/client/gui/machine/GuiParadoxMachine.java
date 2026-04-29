package com.zzhalex.justdirethings.client.gui.machine;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.client.gui.base.GuiMachineBase;
import com.zzhalex.justdirethings.common.container.machine.ContainerParadoxMachine;
import com.zzhalex.justdirethings.common.tile.machine.TileParadoxMachine;
import net.minecraft.util.ResourceLocation;

public class GuiParadoxMachine extends GuiMachineBase {

    private static final ResourceLocation PARADOX_BAR = new ResourceLocation(Reference.MOD_ID, "textures/gui/paradoxbar.png");
    private final ContainerParadoxMachine container;

    public GuiParadoxMachine(ContainerParadoxMachine container) {
        super(container);
        this.container = container;
        baseYSize = 176;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        TileParadoxMachine tile = container.getTile();
        if (tile != null) {
            drawMachineGuiText("justdirethings.gui.paradox", 8, 62, 4210752, (int) tile.getParadoxEnergy());
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        TileParadoxMachine tile = container.getTile();
        if (tile == null || tile.getParadoxEnergy() <= 0.0F) {
            return;
        }

        mc.getTextureManager().bindTexture(PARADOX_BAR);
        int height = Math.min(72, (int) (tile.getParadoxEnergy() / 100.0F * 72.0F));
        drawTexturedModalRect(getBaseGuiLeft() + 152, getBaseGuiTop() + 13 + 72 - height, 0, 72 - height, 18, height);
    }
}
