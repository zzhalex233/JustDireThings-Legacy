package com.zzhalex.justdirethings.common.block.machine;

import com.zzhalex.justdirethings.common.tile.machine.TileSensor;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSensor extends BlockMachineBase {

    public BlockSensor() {
        this("sensort1");
    }

    public BlockSensor(String registryPath) {
        super(registryPath, ModContainers.GUI_SENSOR);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return isTierTwo() ? new TileSensor.T2() : new TileSensor.T1();
    }

    private boolean isTierTwo() {
        return getRegistryName() != null && getRegistryName().getPath().endsWith("t2");
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        if (isFacingSide(blockState, side)) {
            return 0;
        }
        TileEntity tileEntity = blockAccess.getTileEntity(pos);
        return tileEntity instanceof TileSensor ? ((TileSensor) tileEntity).getSignalStrength() : 0;
    }

    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        TileEntity tileEntity = blockAccess.getTileEntity(pos);
        if (!(tileEntity instanceof TileSensor) || !((TileSensor) tileEntity).isStrongSignal()) {
            return 0;
        }
        return getWeakPower(blockState, blockAccess, pos, side);
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        return tileEntity instanceof TileSensor ? ((TileSensor) tileEntity).getSignalStrength() : 0;
    }

    private boolean isFacingSide(IBlockState blockState, EnumFacing side) {
        return blockState.getPropertyKeys().contains(FACING) && side == blockState.getValue(FACING).getOpposite();
    }
}
