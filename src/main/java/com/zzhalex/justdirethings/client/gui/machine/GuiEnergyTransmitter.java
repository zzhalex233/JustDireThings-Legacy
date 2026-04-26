package com.zzhalex.justdirethings.client.gui.machine;

import com.zzhalex.justdirethings.client.gui.base.GuiMachineBase;
import com.zzhalex.justdirethings.client.gui.button.MachineButtonFactory;
import com.zzhalex.justdirethings.client.gui.widget.WidgetEnergyBar;
import com.zzhalex.justdirethings.common.container.machine.ContainerEnergyTransmitter;
import com.zzhalex.justdirethings.common.tile.base.MachineSettingKeys;
import com.zzhalex.justdirethings.common.tile.machine.TileEnergyTransmitter;

public class GuiEnergyTransmitter extends GuiMachineBase {

    private static final String SHOW_PARTICLES = MachineSettingKeys.SHOW_PARTICLES;

    private final ContainerEnergyTransmitter container;
    private final WidgetEnergyBar energyBar = new WidgetEnergyBar(5, 5, 18, 72);

    public GuiEnergyTransmitter(ContainerEnergyTransmitter container) {
        super(container);
        this.container = container;
        addEnergyBar(energyBar);
    }

    @Override
    protected void addMachineButtons() {
        TileEnergyTransmitter tile = container.getTile();
        addMachineButton(MachineButtonFactory.redstoneButton(134, 62, tile.getRedstoneState().getMode().ordinal()));
        addMachineButton(MachineButtonFactory.showParticlesButton(116, 62, tile.isShowParticles()));
        addMachineButtons(MachineButtonFactory.filterButtons(
                tile.getFilterState().isAllowList(),
                tile.getFilterState().isCompareNbt(),
                tile.getFilterState().getBlockItemFilter()
        ));
        addMachineButtons(MachineButtonFactory.areaButtons(
                tile.getAreaState().isRenderArea(),
                tile.getAreaState().getXRadius(),
                tile.getAreaState().getYRadius(),
                tile.getAreaState().getZRadius(),
                tile.getAreaState().getXOffset(),
                tile.getAreaState().getYOffset(),
                tile.getAreaState().getZOffset()
        ));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        energyBar.setValue(container.getTile().getEnergyState().getStoredEnergy(), container.getTile().getEnergyState().getCapacity());
    }
}
