package com.zzhalex.justdirethings.common.block.machine;

import com.zzhalex.justdirethings.common.tile.machine.TileFluidPlacer;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockFluidPlacer extends BlockMachineBase {

    public BlockFluidPlacer() {
        this("fluidplacert1");
    }

    public BlockFluidPlacer(String registryPath) {
        super(registryPath, ModContainers.GUI_FLUID_PLACER);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return isTierTwo() ? new TileFluidPlacer.T2() : new TileFluidPlacer.T1();
    }

    private boolean isTierTwo() {
        return getRegistryName() != null && getRegistryName().getPath().endsWith("t2");
    }
}
