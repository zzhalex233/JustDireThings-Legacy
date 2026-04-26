package com.zzhalex.justdirethings.common.block.machine;

import com.zzhalex.justdirethings.common.tile.machine.TileBlockPlacer;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockBlockPlacer extends BlockMachineBase {

    public BlockBlockPlacer() {
        this("blockplacert1");
    }

    public BlockBlockPlacer(String registryPath) {
        super(registryPath, ModContainers.GUI_BLOCK_PLACER);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return isTierTwo() ? new TileBlockPlacer.T2() : new TileBlockPlacer.T1();
    }

    private boolean isTierTwo() {
        return getRegistryName() != null && getRegistryName().getPath().endsWith("t2");
    }
}
