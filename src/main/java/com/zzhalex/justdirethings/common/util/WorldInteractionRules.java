package com.zzhalex.justdirethings.common.util;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

public final class WorldInteractionRules {

    private WorldInteractionRules() {
    }

    public static IBlockState orientPlacementState(IBlockState state, EnumFacing machineFacing, EnumFacing horizontalFallback) {
        if (state == null) {
            return null;
        }

        if (state.getProperties().containsKey(BlockDirectional.FACING)) {
            return state.withProperty(BlockDirectional.FACING, normalizeFacing(machineFacing));
        }
        if (state.getProperties().containsKey(BlockHorizontal.FACING)) {
            return state.withProperty(BlockHorizontal.FACING, horizontalFacing(machineFacing, horizontalFallback));
        }
        return state;
    }

    public static boolean isInfiniteWaterSource(boolean centerIsSource, int horizontalSourceNeighbors) {
        return centerIsSource && horizontalSourceNeighbors >= 2;
    }

    public static EnumFacing horizontalFacing(EnumFacing machineFacing, EnumFacing horizontalFallback) {
        if (machineFacing != null && machineFacing.getAxis().isHorizontal()) {
            return machineFacing;
        }
        if (horizontalFallback != null && horizontalFallback.getAxis().isHorizontal()) {
            return horizontalFallback;
        }
        return EnumFacing.NORTH;
    }

    private static EnumFacing normalizeFacing(EnumFacing facing) {
        return facing == null ? EnumFacing.NORTH : facing;
    }
}
