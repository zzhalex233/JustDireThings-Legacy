package com.zzhalex.justdirethings.common.item.base;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;

public final class FluidPickupHelper {

    private FluidPickupHelper() {
    }

    public static boolean pickupSourceFluid(World world, EntityPlayer player, ItemStack stack, RayTraceResult hit, @Nullable Fluid expectedFluid) {
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return false;
        }

        BlockPos pos = hit.getBlockPos();
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (!(block instanceof BlockFluidBase)) {
            return false;
        }

        BlockFluidBase fluidBlock = (BlockFluidBase) block;
        FluidStack drained = fluidBlock.drain(world, pos, false);
        if (drained == null || drained.amount < 1000) {
            return false;
        }
        if (expectedFluid != null && drained.getFluid() != expectedFluid) {
            return false;
        }

        IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack);
        if (handler == null) {
            return expectedFluid != null;
        }

        FluidStack bucket = new FluidStack(drained.getFluid(), 1000);
        if (handler.fill(bucket, false) != 1000) {
            return expectedFluid != null;
        }

        if (!world.isRemote) {
            FluidStack pickedUp = fluidBlock.drain(world, pos, true);
            if (pickedUp != null && pickedUp.amount > 0) {
                handler.fill(new FluidStack(pickedUp.getFluid(), Math.min(1000, pickedUp.amount)), true);
                world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        }
        return true;
    }
}
