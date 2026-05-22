package com.zzhalex.justdirethings.common.block.machine;

import com.zzhalex.justdirethings.common.item.block.ItemBlockExperienceHolder;
import com.zzhalex.justdirethings.common.tile.machine.TileExperienceHolder;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.FluidUtil;

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
        if (tryHandleFluidContainer(worldIn, pos, playerIn, hand, heldStack, facing)) {
            return true;
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    private boolean tryHandleFluidContainer(World world, BlockPos pos, EntityPlayer player, EnumHand hand, ItemStack heldStack, EnumFacing facing) {
        if (heldStack.isEmpty()) {
            return false;
        }
        IFluidHandlerItem itemHandler = FluidUtil.getFluidHandler(heldStack);
        if (itemHandler == null) {
            return false;
        }
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity == null) {
            return false;
        }
        if (!tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing)) {
            return false;
        }
        IFluidHandler blockHandler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing);
        if (blockHandler == null) {
            return false;
        }
        if (world.isRemote) {
            return true;
        }

        if (tryFillHeldContainer(world, pos, player, hand, itemHandler, blockHandler)) {
            return true;
        }
        return tryDrainHeldContainer(world, pos, player, hand, itemHandler, blockHandler);
    }

    private boolean tryFillHeldContainer(World world, BlockPos pos, EntityPlayer player, EnumHand hand, IFluidHandlerItem itemHandler, IFluidHandler blockHandler) {
        if (!canReceiveFromBlock(itemHandler, blockHandler)) {
            return false;
        }
        int capacity = itemHandler.getTankProperties()[0].getCapacity();
        FluidStack testStack = blockHandler.drain(capacity, false);
        if (testStack == null || testStack.amount <= 0) {
            return false;
        }
        int amountFit = itemHandler.fill(testStack, false);
        if (amountFit <= 0) {
            return false;
        }
        FluidStack extractedStack = blockHandler.drain(amountFit, true);
        if (extractedStack == null || extractedStack.amount <= 0) {
            return false;
        }
        itemHandler.fill(extractedStack, true);
        player.setHeldItem(hand, itemHandler.getContainer());
        world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    private boolean canReceiveFromBlock(IFluidHandlerItem itemHandler, IFluidHandler blockHandler) {
        if (itemHandler.getTankProperties().length == 0 || blockHandler.getTankProperties().length == 0) {
            return false;
        }
        FluidStack itemContents = itemHandler.getTankProperties()[0].getContents();
        FluidStack blockContents = blockHandler.getTankProperties()[0].getContents();
        int itemAmount = itemContents == null ? 0 : itemContents.amount;
        int itemCapacity = itemHandler.getTankProperties()[0].getCapacity();
        return itemAmount < itemCapacity && blockContents != null && blockContents.amount > 0;
    }

    private boolean tryDrainHeldContainer(World world, BlockPos pos, EntityPlayer player, EnumHand hand, IFluidHandlerItem itemHandler, IFluidHandler blockHandler) {
        FluidStack fluidStack = itemHandler.getTankProperties().length == 0 ? null : itemHandler.getTankProperties()[0].getContents();
        if (fluidStack == null || fluidStack.amount <= 0) {
            return false;
        }
        int insertAmount = blockHandler.fill(fluidStack, false);
        if (insertAmount <= 0) {
            return false;
        }
        FluidStack extractedStack = itemHandler.drain(insertAmount, true);
        if (extractedStack == null || extractedStack.amount <= 0) {
            return false;
        }
        blockHandler.fill(extractedStack, true);
        player.setHeldItem(hand, itemHandler.getContainer());
        world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
        return true;
    }
}
