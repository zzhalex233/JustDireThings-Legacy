package com.zzhalex.justdirethings.common.block.machine;

import com.zzhalex.justdirethings.common.tile.machine.TilePlayerAccessor;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockPlayerAccessor extends BlockMachineBase {

    public BlockPlayerAccessor() {
        this("playeraccessor");
    }

    public BlockPlayerAccessor(String registryPath) {
        super(registryPath, ModContainers.GUI_PLAYER_ACCESSOR);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TilePlayerAccessor();
    }
}
