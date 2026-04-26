package com.zzhalex.justdirethings.client.gui.machine;

import com.zzhalex.justdirethings.client.gui.base.GuiMachineBase;
import com.zzhalex.justdirethings.client.gui.widget.WidgetFluidBar;
import com.zzhalex.justdirethings.common.container.machine.ContainerFluidPlacer;

public class GuiFluidPlacer extends GuiMachineBase {

    private final ContainerFluidPlacer container;
    private final WidgetFluidBar fluidBar = new WidgetFluidBar(5, 5, 18, 72);

    public GuiFluidPlacer(ContainerFluidPlacer container) {
        super(container);
        this.container = container;
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
