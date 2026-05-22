package com.zzhalex.justdirethings.common.block;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.registry.ModContentItems;
import com.zzhalex.justdirethings.registry.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockTimeCrystalCluster extends Block {

    public static final PropertyDirection FACING = PropertyDirection.create("facing");

    private final AxisAlignedBB downBox;
    private final AxisAlignedBB upBox;
    private final AxisAlignedBB northBox;
    private final AxisAlignedBB southBox;
    private final AxisAlignedBB westBox;
    private final AxisAlignedBB eastBox;
    private final boolean fullyGrown;

    public BlockTimeCrystalCluster(String registryPath, float heightPixels, float aabbOffsetPixels, int lightLevel, boolean fullyGrown, SoundType soundType) {
        super(Material.GLASS);
        this.fullyGrown = fullyGrown;
        setRegistryName(new ResourceLocation(Reference.MOD_ID, registryPath));
        setTranslationKey(Reference.MOD_ID + "." + registryPath);
        setCreativeTab(ModCreativeTabs.JUST_DIRE_THINGS);
        setSoundType(soundType);
        setHardness(1.5F);
        setResistance(1.5F);
        setLightLevel(lightLevel / 15.0F);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.UP));

        float min = aabbOffsetPixels / 16.0F;
        float max = 1.0F - min;
        float depth = heightPixels / 16.0F;
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
        EnumFacing facing = EnumFacing.byIndex(meta);
        return getDefaultState().withProperty(FACING, facing == null ? EnumFacing.UP : facing);
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        if (canAttachTo(worldIn, pos, facing)) {
            return getDefaultState().withProperty(FACING, facing);
        }
        for (EnumFacing candidate : EnumFacing.values()) {
            if (canAttachTo(worldIn, pos, candidate)) {
                return getDefaultState().withProperty(FACING, candidate);
            }
        }
        return getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (canAttachTo(worldIn, pos, facing)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
        return canAttachTo(worldIn, pos, side);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!canAttachTo(worldIn, pos, state.getValue(FACING))) {
            worldIn.setBlockToAir(pos);
        }
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
        return getBoundingBox(blockState, worldIn, pos);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public EnumPushReaction getPushReaction(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return true;
    }

    @Override
    protected boolean canSilkHarvest() {
        return true;
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(this);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return fullyGrown ? ModContentItems.getItem("time_crystal") : Items.AIR;
    }

    @Override
    public int quantityDropped(Random random) {
        return fullyGrown ? 1 : 0;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        if (fullyGrown) {
            drops.add(new ItemStack(ModContentItems.getItem("time_crystal")));
        }
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(this);
    }

    private boolean canAttachTo(World world, BlockPos pos, EnumFacing facing) {
        BlockPos supportPos = pos.offset(facing.getOpposite());
        IBlockState supportState = world.getBlockState(supportPos);
        return supportState.isSideSolid(world, supportPos, facing);
    }
}
