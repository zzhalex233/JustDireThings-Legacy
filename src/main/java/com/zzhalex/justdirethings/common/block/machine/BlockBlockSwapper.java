package com.zzhalex.justdirethings.common.block.machine;

import com.zzhalex.justdirethings.common.tile.machine.TileBlockSwapper;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockBlockSwapper extends BlockMachineBase {

    public BlockBlockSwapper() {
        this("blockswappert1");
    }

    public BlockBlockSwapper(String registryPath) {
        super(registryPath, ModContainers.GUI_BLOCK_SWAPPER);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return isTierTwo() ? new TileBlockSwapper.T2() : new TileBlockSwapper.T1();
    }

    private boolean isTierTwo() {
        return getRegistryName() != null && getRegistryName().getPath().endsWith("t2");
    }
}
