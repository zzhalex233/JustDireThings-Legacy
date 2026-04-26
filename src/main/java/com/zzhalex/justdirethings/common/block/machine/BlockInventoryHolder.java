package com.zzhalex.justdirethings.common.block.machine;

import com.zzhalex.justdirethings.common.tile.machine.TileInventoryHolder;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockInventoryHolder extends BlockMachineBase {

    public BlockInventoryHolder() {
        super("inventory_holder", ModContainers.GUI_INVENTORY_HOLDER);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileInventoryHolder();
    }
}
