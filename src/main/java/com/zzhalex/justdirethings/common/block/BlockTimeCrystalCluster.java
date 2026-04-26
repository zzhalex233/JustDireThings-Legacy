package com.zzhalex.justdirethings.common.block;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.registry.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockTimeCrystalCluster extends Block {

    public static final PropertyDirection FACING = PropertyDirection.create("facing");

    private final AxisAlignedBB downBox;
    private final AxisAlignedBB upBox;
    private final AxisAlignedBB northBox;
    private final AxisAlignedBB southBox;
    private final AxisAlignedBB westBox;
    private final AxisAlignedBB eastBox;

    public BlockTimeCrystalCluster(String registryPath, float radius, float depth) {
        super(Material.GLASS);
        setRegistryName(new ResourceLocation(Reference.MOD_ID, registryPath));
        setTranslationKey(Reference.MOD_ID + "." + registryPath);
        setCreativeTab(ModCreativeTabs.JUST_DIRE_THINGS);
        setSoundType(SoundType.GLASS);
        setHardness(0.4F);
        setResistance(1.0F);
        setLightLevel(0.85F);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.UP));

        float min = 0.5F - radius;
        float max = 0.5F + radius;
        downBox = new AxisAlignedBB(min, 1.0F - depth, min, max, 1.0F, max);
        upBox = new AxisAlignedBB(min, 0.0F, min, max, depth, max);
        northBox = new AxisAlignedBB(min, min, 1.0F - depth, max, max, 1.0F);
        southBox = new AxisAlignedBB(min, min, 0.0F, max, max, depth);
        westBox = new AxisAlignedBB(1.0F - depth, min, min, 1.0F, max, max);
        eastBox = new AxisAlignedBB(0.0F, min, min, depth, max, max);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta));
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        EnumFacing facing = state.getValue(FACING);
        switch (facing) {
            case DOWN:
                return downBox;
            case NORTH:
                return northBox;
            case SOUTH:
                return southBox;
            case WEST:
                return westBox;
            case EAST:
                return eastBox;
            case UP:
            default:
                return upBox;
        }
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }
}
