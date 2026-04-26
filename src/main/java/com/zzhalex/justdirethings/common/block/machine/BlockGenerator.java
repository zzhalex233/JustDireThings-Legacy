package com.zzhalex.justdirethings.common.block.machine;

import com.zzhalex.justdirethings.common.tile.machine.TileGenerator;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockGenerator extends BlockMachineBase {

    public BlockGenerator() {
        this("generatort1");
    }

    public BlockGenerator(String registryPath) {
        super(registryPath, ModContainers.GUI_GENERATOR);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileGenerator();
    }
}
