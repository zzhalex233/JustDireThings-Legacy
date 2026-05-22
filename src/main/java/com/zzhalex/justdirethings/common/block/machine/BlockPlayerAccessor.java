package com.zzhalex.justdirethings.common.block.machine;

import com.zzhalex.justdirethings.common.tile.machine.TilePlayerAccessor;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPlayerAccessor extends BlockMachineBase {

    public BlockPlayerAccessor() {
        this("playeraccessor");
    }

    public BlockPlayerAccessor(String registryPath) {
        super(registryPath, ModContainers.GUI_PLAYER_ACCESSOR);
        setHardness(2.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TilePlayerAccessor();
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState();
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state;
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state;
    }

    @Override
    public IBlockState direRotate(IBlockState state, World world, BlockPos pos, Rotation rotation) {
        return state;
    }

    @Override
    public IBlockState direRotate(IBlockState state, Rotation rotation) {
        return state;
    }
}
