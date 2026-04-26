package com.zzhalex.justdirethings.common.block.machine;

import com.zzhalex.justdirethings.common.tile.machine.TileFluidCollector;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockFluidCollector extends BlockMachineBase {

    public BlockFluidCollector() {
        this("fluidcollectort1");
    }

    public BlockFluidCollector(String registryPath) {
        super(registryPath, ModContainers.GUI_FLUID_COLLECTOR);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return isTierTwo() ? new TileFluidCollector.T2() : new TileFluidCollector.T1();
    }

    private boolean isTierTwo() {
        return getRegistryName() != null && getRegistryName().getPath().endsWith("t2");
    }
}
