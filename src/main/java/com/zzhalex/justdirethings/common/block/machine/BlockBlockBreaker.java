package com.zzhalex.justdirethings.common.block.machine;

import com.zzhalex.justdirethings.common.tile.machine.TileBlockBreaker;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockBlockBreaker extends BlockMachineBase {

    public BlockBlockBreaker() {
        this("blockbreakert1");
    }

    public BlockBlockBreaker(String registryPath) {
        super(registryPath, ModContainers.GUI_BLOCK_BREAKER);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return isTierTwo() ? new TileBlockBreaker.T2() : new TileBlockBreaker.T1();
    }

    private boolean isTierTwo() {
        return getRegistryName() != null && getRegistryName().getPath().endsWith("t2");
    }
}
