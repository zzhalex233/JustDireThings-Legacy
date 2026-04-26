package com.zzhalex.justdirethings.common.block;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.registry.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockTimeCrystalBudding extends Block {

    public static final PropertyInteger STAGE = PropertyInteger.create("stage", 0, 3);

    public BlockTimeCrystalBudding() {
        super(Material.GLASS);
        setRegistryName(new ResourceLocation(Reference.MOD_ID, "time_crystal_budding_block"));
        setTranslationKey(Reference.MOD_ID + ".time_crystal_budding_block");
        setCreativeTab(ModCreativeTabs.JUST_DIRE_THINGS);
        setSoundType(SoundType.GLASS);
        setHardness(1.5F);
        setResistance(3.0F);
        setLightLevel(0.75F);
        setTickRandomly(true);
        setDefaultState(blockState.getBaseState().withProperty(STAGE, 0));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(STAGE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(STAGE, Math.max(0, Math.min(3, meta)));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, STAGE);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (worldIn.isRemote) {
            return;
        }
        int stage = state.getValue(STAGE);
        if (stage < 3 && rand.nextInt(6) == 0) {
            worldIn.setBlockState(pos, state.withProperty(STAGE, stage + 1), 2);
        }
    }
}
