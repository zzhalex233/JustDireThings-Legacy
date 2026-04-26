package com.zzhalex.justdirethings.client.gui.machine;

import com.zzhalex.justdirethings.client.gui.base.GuiMachineBase;
import com.zzhalex.justdirethings.client.gui.widget.WidgetEnergyBar;
import com.zzhalex.justdirethings.client.gui.widget.WidgetFluidBar;
import com.zzhalex.justdirethings.common.container.machine.ContainerFluidGenerator;

public class GuiFluidGenerator extends GuiMachineBase {

    private final ContainerFluidGenerator container;
    private final WidgetEnergyBar energyBar = new WidgetEnergyBar(5, 5, 18, 72);
    private final WidgetFluidBar fluidBar = new WidgetFluidBar(24, 5, 18, 72);

    public GuiFluidGenerator(ContainerFluidGenerator container) {
        super(container);
        this.container = container;
        addEnergyBar(energyBar);
        addFluidBar(fluidBar);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        energyBar.setValue(container.getTile().getEnergyState().getStoredEnergy(), container.getTile().getEnergyState().getCapacity());
        fluidBar.setValue(container.getTile().getFluidState().getAmount(), container.getTile().getFluidState().getCapacity(), container.getTile().getFluidState().getFluidName());
    }
}
