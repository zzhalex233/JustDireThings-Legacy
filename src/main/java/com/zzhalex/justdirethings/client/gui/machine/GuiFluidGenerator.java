package com.zzhalex.justdirethings.client.gui.machine;

import com.zzhalex.justdirethings.client.gui.base.GuiMachineBase;
import com.zzhalex.justdirethings.client.gui.widget.WidgetEnergyBar;
import com.zzhalex.justdirethings.client.gui.widget.WidgetFluidBar;
import com.zzhalex.justdirethings.common.container.machine.ContainerFluidGenerator;
import com.zzhalex.justdirethings.common.item.tooltip.TooltipHelper;
import net.minecraft.client.resources.I18n;

import java.util.Arrays;

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

    @Override
    protected boolean drawEnergyBarTooltip(int mouseX, int mouseY) {
        if (!energyBar.contains(topSectionLeft, topSectionTop, mouseX, mouseY)) {
            return false;
        }

        drawHoveringText(Arrays.asList(
                I18n.format(
                        "justdirethings.screen.energy",
                        TooltipHelper.formatTooltipValue(container.getTile().getEnergyState().getStoredEnergy()),
                        TooltipHelper.formatTooltipValue(container.getTile().getEnergyState().getCapacity())
                ),
                I18n.format("justdirethings.screen.fepertick", TooltipHelper.formatNumber(container.getTile().getCurrentFePerFuelTick()))
        ), mouseX, mouseY, fontRenderer);
        return true;
    }
}
