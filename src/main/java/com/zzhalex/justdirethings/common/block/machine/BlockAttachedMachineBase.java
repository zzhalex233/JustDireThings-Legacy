package com.zzhalex.justdirethings.common.block.machine;

import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public abstract class BlockAttachedMachineBase extends BlockMachineBase {

    private static final AxisAlignedBB[] DEFAULT_ATTACHMENT_SHAPES = new AxisAlignedBB[] {
            box(3, 0, 3, 13, 11, 13),
            box(3, 5, 3, 13, 16, 13),
            box(3, 3, 0, 13, 13, 11),
            box(3, 3, 5, 13, 13, 16),
            box(0, 3, 3, 11, 13, 13),
            box(5, 3, 3, 16, 13, 13)
    };

    private final AxisAlignedBB[] SHAPES;

    protected BlockAttachedMachineBase(String registryPath, int guiId) {
        this(registryPath, guiId, DEFAULT_ATTACHMENT_SHAPES);
    }

    protected BlockAttachedMachineBase(String registryPath, int guiId, AxisAlignedBB[] shapes) {
        super(registryPath, guiId);
        this.SHAPES = shapes;
    }

    @Override
    public IBlockState getStateForPlacement(
            World worldIn,
            BlockPos pos,
            EnumFacing facing,
            float hitX,
            float hitY,
            float hitZ,
            int meta,
            EntityLivingBase placer
    ) {
        return getDefaultState().withProperty(FACING, facing.getOpposite());
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return getShape(state);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return getShape(blockState);
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean causesSuffocation(IBlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    private AxisAlignedBB getShape(IBlockState state) {
        if (!state.getPropertyKeys().contains(FACING)) {
            return FULL_BLOCK_AABB;
        }
        int index = state.getValue(FACING).getIndex();
        return index >= 0 && index < SHAPES.length ? SHAPES[index] : FULL_BLOCK_AABB;
    }

    private static AxisAlignedBB box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new AxisAlignedBB(minX / 16.0D, minY / 16.0D, minZ / 16.0D, maxX / 16.0D, maxY / 16.0D, maxZ / 16.0D);
    }
}
