package com.zzhalex.justdirethings.common.block.machine;

import com.zzhalex.justdirethings.common.tile.machine.TileInventoryHolder;
import com.zzhalex.justdirethings.common.item.block.ItemBlockInventoryHolder;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockInventoryHolder extends BlockMachineBase {

    public BlockInventoryHolder() {
        super("inventory_holder", ModContainers.GUI_INVENTORY_HOLDER);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileInventoryHolder();
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        return willHarvest || super.removedByPlayer(state, world, pos, player, false);
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity tileEntity, ItemStack stack) {
        super.harvestBlock(worldIn, player, pos, state, tileEntity, stack);
        worldIn.setBlockToAir(pos);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        super.getDrops(drops, world, pos, state, fortune);
        TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof TileInventoryHolder) || !((TileInventoryHolder) tileEntity).hasPortableData()) {
            return;
        }
        ItemStack drop = new ItemStack(this);
        ItemBlockInventoryHolder.writePortableData(drop, (TileInventoryHolder) tileEntity);
        drops.clear();
        drops.add(drop);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        worldIn.removeTileEntity(pos);
    }
}
