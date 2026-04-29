package com.zzhalex.justdirethings.client.gui.machine;

import com.zzhalex.justdirethings.client.gui.base.GuiMachineBase;
import com.zzhalex.justdirethings.client.gui.button.MachineButtonFactory;
import com.zzhalex.justdirethings.common.container.machine.ContainerSensor;
import com.zzhalex.justdirethings.common.tile.base.MachineSettingKeys;
import com.zzhalex.justdirethings.common.tile.base.TileAdvancedMachine;
import com.zzhalex.justdirethings.common.tile.machine.TileSensor;

public class GuiSensor extends GuiMachineBase {

    private static final String SENSOR_TARGET = MachineSettingKeys.SENSOR_TARGET;
    private static final String STRONG_WEAK_REDSTONE = MachineSettingKeys.STRONG_WEAK_REDSTONE;

    private final ContainerSensor container;

    public GuiSensor(ContainerSensor container) {
        super(container);
        this.container = container;
    }

    @Override
    protected void addMachineButtons() {
        TileSensor tile = container.getTile();
        if (tile instanceof TileAdvancedMachine) {
            addMachineButton(MachineButtonFactory.tickSpeedButton(tile.getTickSpeed()));
            addAdvancedMachineButtons((TileAdvancedMachine) tile);
            addMachineButton(MachineButtonFactory.sensorTargetButton(26, 62, tile.getSenseTarget()));
            addMachineButton(MachineButtonFactory.strongWeakRedstoneButton(44, 62, tile.isStrongSignal()));
            addMachineButton(MachineButtonFactory.equalityButton(104, 62, tile.getEquality()));
            addMachineButton(MachineButtonFactory.senseAmountButton(122, 64, tile.getSenseAmount()));
        } else {
            addMachineButton(MachineButtonFactory.tickSpeedButton(tile.getTickSpeed()));
            addMachineButton(MachineButtonFactory.sensorTargetButton(56, 38, tile.getSenseTarget()));
            addMachineButton(MachineButtonFactory.strongWeakRedstoneButton(20, 38, tile.isStrongSignal()));
            addMachineButtons(MachineButtonFactory.filterButtons(
                    tile.getFilterState().isAllowList(),
                    tile.getFilterState().isCompareNbt(),
                    -1
            ));
        }
    }
}
