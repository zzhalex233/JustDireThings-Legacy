package com.zzhalex.justdirethings.common.block.machine;

import com.zzhalex.justdirethings.common.item.block.ItemBlockExperienceHolder;
import com.zzhalex.justdirethings.common.tile.machine.TileExperienceHolder;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockExperienceHolder extends BlockAttachedMachineBase {

    public BlockExperienceHolder() {
        this("experienceholder");
    }

    public BlockExperienceHolder(String registryPath) {
        super(registryPath, ModContainers.GUI_EXPERIENCE_HOLDER);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileExperienceHolder();
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
        if (!(tileEntity instanceof TileExperienceHolder) || !((TileExperienceHolder) tileEntity).hasPortableData()) {
            return;
        }
        ItemStack drop = new ItemStack(this);
        ItemBlockExperienceHolder.writePortableData(drop, (TileExperienceHolder) tileEntity);
        drops.clear();
        drops.add(drop);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        worldIn.removeTileEntity(pos);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack heldStack = playerIn.getHeldItem(hand);
        if (FluidContainerInteractionHelper.tryHandleFluidContainer(worldIn, pos, playerIn, hand, heldStack, facing, fluidStack -> true)) {
            return true;
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }
}
