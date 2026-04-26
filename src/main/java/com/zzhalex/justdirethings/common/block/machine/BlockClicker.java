package com.zzhalex.justdirethings.common.block.machine;

import com.zzhalex.justdirethings.common.tile.machine.TileClicker;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockClicker extends BlockMachineBase {

    public BlockClicker() {
        this("clickert1");
    }

    public BlockClicker(String registryPath) {
        super(registryPath, ModContainers.GUI_CLICKER);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return isTierTwo() ? new TileClicker.T2() : new TileClicker.T1();
    }

    private boolean isTierTwo() {
        return getRegistryName() != null && getRegistryName().getPath().endsWith("t2");
    }
}
