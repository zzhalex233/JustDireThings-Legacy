package com.zzhalex.justdirethings.client.gui.machine;

import com.zzhalex.justdirethings.client.gui.base.GuiMachineBase;
import com.zzhalex.justdirethings.client.gui.button.MachineButtonFactory;
import com.zzhalex.justdirethings.common.container.machine.ContainerPlayerAccessor;
import com.zzhalex.justdirethings.common.tile.base.MachineSettingKeys;
import com.zzhalex.justdirethings.common.tile.machine.TilePlayerAccessor;
import net.minecraft.util.EnumFacing;

public class GuiPlayerAccessor extends GuiMachineBase {

    private final ContainerPlayerAccessor container;

    public GuiPlayerAccessor(ContainerPlayerAccessor container) {
        super(container);
        this.container = container;
        this.baseYSize = 222;
    }

    @Override
    protected void addMachineButtons() {
        TilePlayerAccessor tile = container.getTile();
        addMachineButton(MachineButtonFactory.inventoryConnectionButton(80, 22, MachineSettingKeys.INVENTORY_CONNECTION_UP, tile.getInventoryConnectionTypeIndex(EnumFacing.UP)));
        addMachineButton(MachineButtonFactory.inventoryConnectionButton(80, 58, MachineSettingKeys.INVENTORY_CONNECTION_DOWN, tile.getInventoryConnectionTypeIndex(EnumFacing.DOWN)));
        addMachineButton(MachineButtonFactory.inventoryConnectionButton(80, 40, MachineSettingKeys.INVENTORY_CONNECTION_NORTH, tile.getInventoryConnectionTypeIndex(EnumFacing.NORTH)));
        addMachineButton(MachineButtonFactory.inventoryConnectionButton(62, 58, MachineSettingKeys.INVENTORY_CONNECTION_SOUTH, tile.getInventoryConnectionTypeIndex(EnumFacing.SOUTH)));
        addMachineButton(MachineButtonFactory.inventoryConnectionButton(62, 40, MachineSettingKeys.INVENTORY_CONNECTION_WEST, tile.getInventoryConnectionTypeIndex(EnumFacing.WEST)));
        addMachineButton(MachineButtonFactory.inventoryConnectionButton(98, 40, MachineSettingKeys.INVENTORY_CONNECTION_EAST, tile.getInventoryConnectionTypeIndex(EnumFacing.EAST)));
    }
}
