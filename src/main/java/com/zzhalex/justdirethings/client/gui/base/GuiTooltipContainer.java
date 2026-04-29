package com.zzhalex.justdirethings.client.gui.base;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

public abstract class GuiTooltipContainer extends GuiContainer {

    protected GuiTooltipContainer(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawAfterContainerBeforeTooltips(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }

    protected void drawAfterContainerBeforeTooltips(int mouseX, int mouseY, float partialTicks) {
    }
}
