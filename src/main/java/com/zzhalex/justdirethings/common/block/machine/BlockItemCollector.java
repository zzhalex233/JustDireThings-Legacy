package com.zzhalex.justdirethings.common.block.machine;

import com.zzhalex.justdirethings.common.tile.machine.TileItemCollector;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockItemCollector extends BlockAttachedMachineBase {

    public BlockItemCollector() {
        this("itemcollector");
    }

    public BlockItemCollector(String registryPath) {
        super(registryPath, ModContainers.GUI_ITEM_COLLECTOR);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileItemCollector();
    }
}
