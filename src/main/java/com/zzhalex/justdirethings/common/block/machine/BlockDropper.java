package com.zzhalex.justdirethings.common.block.machine;

import com.zzhalex.justdirethings.common.tile.machine.TileDropper;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockDropper extends BlockMachineBase {

    public BlockDropper() {
        this("droppert1");
    }

    public BlockDropper(String registryPath) {
        super(registryPath, ModContainers.GUI_DROPPER);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return isTierTwo() ? new TileDropper.T2() : new TileDropper.T1();
    }

    private boolean isTierTwo() {
        return getRegistryName() != null && getRegistryName().getPath().endsWith("t2");
    }
}
