package com.zzhalex.justdirethings.client.gui.machine;

import com.zzhalex.justdirethings.client.gui.base.GuiMachineBase;
import com.zzhalex.justdirethings.client.gui.widget.WidgetEnergyBar;
import com.zzhalex.justdirethings.common.container.machine.ContainerGenerator;

public class GuiGenerator extends GuiMachineBase {

    private final ContainerGenerator container;
    private final WidgetEnergyBar energyBar = new WidgetEnergyBar(5, 5, 18, 72);

    public GuiGenerator(ContainerGenerator container) {
        super(container);
        this.container = container;
        addEnergyBar(energyBar);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        drawBurnProgress();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        energyBar.setValue(container.getTile().getEnergyState().getStoredEnergy(), container.getTile().getEnergyState().getCapacity());
    }

    private void drawBurnProgress() {
        int maxBurn = container.getTile().getMaxBurn();
        int remaining = container.getTile().getBurnRemaining();
        if (maxBurn <= 0 || remaining <= 0) {
            return;
        }
        int height = Math.max(1, (int) (13.0F * remaining / maxBurn));
        int left = getBaseGuiLeft() + 80;
        int top = getBaseGuiTop() + 30 + (13 - height);
        drawRect(left, getBaseGuiTop() + 30, left + 14, getBaseGuiTop() + 43, 0xFF5A3518);
        drawRect(left, top, left + 14, getBaseGuiTop() + 43, 0xFFFFA000);
    }
}
