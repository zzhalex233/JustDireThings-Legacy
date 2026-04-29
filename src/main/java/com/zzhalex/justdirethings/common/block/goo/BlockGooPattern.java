package com.zzhalex.justdirethings.common.block.goo;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.registry.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

public class BlockGooPattern extends Block {

    public static final PropertyInteger GOOSTAGE = PropertyInteger.create("goostage", 0, 11);

    public BlockGooPattern(String registryPath) {
        super(Material.CLAY);
        setRegistryName(new ResourceLocation(Reference.MOD_ID, registryPath));
        setTranslationKey(Reference.MOD_ID + "." + registryPath);
        setCreativeTab(ModCreativeTabs.JUST_DIRE_THINGS);
        setSoundType(SoundType.SLIME);
        setHardness(2.0F);
        setResistance(2.0F);
        setDefaultState(blockState.getBaseState().withProperty(GOOSTAGE, 0));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(GOOSTAGE, Math.max(0, Math.min(11, meta)));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(GOOSTAGE);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, GOOSTAGE);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }
}
