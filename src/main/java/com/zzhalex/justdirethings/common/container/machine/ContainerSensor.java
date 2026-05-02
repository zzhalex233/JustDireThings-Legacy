package com.zzhalex.justdirethings.common.container.machine;

import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import com.zzhalex.justdirethings.common.tile.machine.TileSensor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;

public class ContainerSensor extends ContainerMachineBase {

    private final TileSensor tile;

    public ContainerSensor(InventoryPlayer playerInventory, TileSensor tile) {
        super(playerInventory, tile, null);
        this.tile = tile;
        addFilterSlots(tile.getFilterHandler(), tile instanceof TileSensor.T2 ? 8 : 80, tile instanceof TileSensor.T2 ? 54 : 13, 9);
        addPlayerInventory(playerInventory, 8, 84);
    }

    public TileSensor getTile() {
        return tile;
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        if (slotId >= 0 && slotId < TileSensor.FILTER_SLOT_COUNT && dragType == 1 && clickTypeIn == ClickType.PICKUP) {
            return ItemStack.EMPTY;
        }
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }
}
