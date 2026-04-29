package com.zzhalex.justdirethings.client.gui.machine;

import com.zzhalex.justdirethings.client.gui.base.GuiMachineBase;
import com.zzhalex.justdirethings.client.gui.widget.WidgetFluidBar;
import com.zzhalex.justdirethings.common.container.machine.ContainerFluidCollector;

public class GuiFluidCollector extends GuiMachineBase {

    private final ContainerFluidCollector container;
    private final WidgetFluidBar fluidBar;

    public GuiFluidCollector(ContainerFluidCollector container) {
        super(container);
        this.container = container;
        this.fluidBar = new WidgetFluidBar(getFluidBarOffset(), 5, 18, 72);
        addFluidBar(fluidBar);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        fluidBar.setValue(
                container.getTile().getFluidState().getAmount(),
                container.getTile().getFluidState().getCapacity(),
                container.getTile().getFluidState().getFluidName()
        );
    }
}
