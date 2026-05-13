package com.zzhalex.justdirethings.common.util;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public final class MiningCollect {

    private MiningCollect() {
    }

    public interface BlockValidator {
        boolean isValid(World world, BlockPos pos, EntityPlayer player, ItemStack stack, EnumFacing side);
    }

    public static Set<BlockPos> collect(EntityPlayer player, BlockPos origin, World world, int range, ItemStack stack, BlockValidator validator) {
        Set<BlockPos> coordinates = new HashSet<>();
        IBlockState originState = world.getBlockState(origin);
        if (originState.getBlockHardness(world, origin) == 0.0F) {
            return coordinates;
        }

        EnumFacing side = getTargetLookDirection(player);
        int clampedRange = Math.max(1, range);
        if (clampedRange == 1) {
            if (validator.isValid(world, origin, player, stack, side)) {
                coordinates.add(origin.toImmutable());
            }
            return coordinates;
        }

        boolean vertical = side.getAxis() == EnumFacing.Axis.Y;
        EnumFacing up = vertical ? player.getHorizontalFacing() : EnumFacing.UP;
        EnumFacing down = up.getOpposite();
        EnumFacing right = vertical ? up.rotateY() : side.rotateYCCW();
        EnumFacing left = right.getOpposite();

        int midRange = (clampedRange - 1) / 2;
        int upRange = midRange;
        int downRange = midRange;
        if (!vertical && clampedRange > 3 && Math.abs(player.posY - origin.getY()) < 2.0D) {
            downRange = 1;
            upRange = clampedRange - 2;
        }

        BlockPos topLeft = origin.offset(up, upRange).offset(left, midRange);
        BlockPos bottomRight = origin.offset(down, downRange).offset(right, midRange);
        for (BlockPos pos : BlockPos.getAllInBox(topLeft, bottomRight)) {
            BlockPos immutablePos = pos.toImmutable();
            if (validator.isValid(world, immutablePos, player, stack, side)) {
                coordinates.add(immutablePos);
            }
        }
        return coordinates;
    }

    public static int getHammerRange(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ToggleableTool)) {
            return 1;
        }
        return ((ToggleableTool) stack.getItem()).getToolValue(stack, Ability.HAMMER);
    }

    public static boolean isNonAirBreakableBlock(World world, BlockPos pos) {
        IBlockState blockState = world.getBlockState(pos);
        if (blockState.getMaterial() == Material.AIR || blockState.getBlock() == Blocks.AIR) {
            return false;
        }
        return blockState.getBlockHardness(world, pos) >= 0.0F;
    }

    public static EnumFacing getTargetLookDirection(EntityPlayer player) {
        double reach = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE) == null
                ? 5.0D
                : player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
        RayTraceResult hitResult = player.rayTrace(reach, 1.0F);
        if (hitResult != null && hitResult.typeOfHit == RayTraceResult.Type.BLOCK && hitResult.sideHit != null) {
            return hitResult.sideHit.getOpposite();
        }
        return player.getHorizontalFacing();
    }
}
