package com.zzhalex.justdirethings.common.block.machine;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.function.Predicate;

final class FluidContainerInteractionHelper {

    private FluidContainerInteractionHelper() {
    }

    static boolean tryHandleFluidContainer(World world, BlockPos pos, EntityPlayer player, EnumHand hand, ItemStack heldStack, EnumFacing facing, Predicate<FluidStack> canDrainHeldFluid) {
        if (heldStack.isEmpty()) {
            return false;
        }
        IFluidHandlerItem itemHandler = FluidUtil.getFluidHandler(heldStack);
        if (itemHandler == null) {
            return false;
        }
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity == null || !tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing)) {
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
        return tryDrainHeldContainer(world, pos, player, hand, itemHandler, blockHandler, canDrainHeldFluid);
    }

    private static boolean tryFillHeldContainer(World world, BlockPos pos, EntityPlayer player, EnumHand hand, IFluidHandlerItem itemHandler, IFluidHandler blockHandler) {
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

    private static boolean canReceiveFromBlock(IFluidHandlerItem itemHandler, IFluidHandler blockHandler) {
        if (itemHandler.getTankProperties().length == 0 || blockHandler.getTankProperties().length == 0) {
            return false;
        }
        FluidStack itemContents = itemHandler.getTankProperties()[0].getContents();
        FluidStack blockContents = blockHandler.getTankProperties()[0].getContents();
        int itemAmount = itemContents == null ? 0 : itemContents.amount;
        int itemCapacity = itemHandler.getTankProperties()[0].getCapacity();
        return itemAmount < itemCapacity && blockContents != null && blockContents.amount > 0;
    }

    private static boolean tryDrainHeldContainer(World world, BlockPos pos, EntityPlayer player, EnumHand hand, IFluidHandlerItem itemHandler, IFluidHandler blockHandler, Predicate<FluidStack> canDrainHeldFluid) {
        FluidStack fluidStack = itemHandler.getTankProperties().length == 0 ? null : itemHandler.getTankProperties()[0].getContents();
        if (fluidStack == null || fluidStack.amount <= 0 || !canDrainHeldFluid.test(fluidStack)) {
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
