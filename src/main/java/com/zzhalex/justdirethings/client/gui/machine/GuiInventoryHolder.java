package com.zzhalex.justdirethings.client.gui.machine;

import com.zzhalex.justdirethings.client.gui.base.GuiMachineBase;
import com.zzhalex.justdirethings.client.gui.button.MachineButtonFactory;
import com.zzhalex.justdirethings.common.container.machine.ContainerInventoryHolder;
import com.zzhalex.justdirethings.common.tile.base.MachineSettingKeys;
import com.zzhalex.justdirethings.common.tile.machine.TileInventoryHolder;

public class GuiInventoryHolder extends GuiMachineBase {

    private static final String FILTER_ONLY = MachineSettingKeys.FILTER_ONLY;
    private static final String COMPARE_COUNTS = MachineSettingKeys.COMPARE_COUNTS;
    private static final String SHOW_FAKE_PLAYER = MachineSettingKeys.SHOW_FAKE_PLAYER;

    private final ContainerInventoryHolder container;

    public GuiInventoryHolder(ContainerInventoryHolder container) {
        super(container);
        this.container = container;
        this.baseYSize = 208;
    }

    @Override
    protected void setTopSection() {
        extraWidth = 0;
        extraHeight = 24;
    }

    @Override
    protected void addMachineButtons() {
        TileInventoryHolder tile = container.getTile();
        addMachineButton(MachineButtonFactory.filterOnlyButton(134, 22, MachineSettingKeys.FILTER_ONLY, tile.isFiltersOnly()));
        addMachineButton(MachineButtonFactory.compareCountsButton(134, 4, MachineSettingKeys.COMPARE_COUNTS, tile.isCompareCounts()));
        addMachineButton(MachineButtonFactory.filterOnlyButton(26, 22, MachineSettingKeys.AUTOMATED_FILTER_ONLY, tile.isAutomatedFiltersOnly()));
        addMachineButton(MachineButtonFactory.compareCountsButton(26, 4, MachineSettingKeys.AUTOMATED_COMPARE_COUNTS, tile.isAutomatedCompareCounts()));
        addMachineButton(MachineButtonFactory.renderPlayerButton(8, 4, tile.isRenderPlayer()));
        addMachineButton(MachineButtonFactory.sendInventoryButton(134, 132));
        addMachineButton(MachineButtonFactory.pullInventoryButton(26, 132));
        addMachineButton(MachineButtonFactory.swapInventoryButton(152, 132));
    }
}
