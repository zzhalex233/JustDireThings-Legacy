package com.zzhalex.justdirethings.common.tile.machine;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public final class FakePlayerMath {

    private FakePlayerMath() {
    }

    public static float pitchForFacing(EnumFacing facing) {
        if (facing == EnumFacing.DOWN) {
            return 90.0F;
        }
        if (facing == EnumFacing.UP) {
            return -90.0F;
        }
        return 0.0F;
    }

    public static float yawForFacing(EnumFacing facing) {
        if (facing == EnumFacing.SOUTH) {
            return 0.0F;
        }
        if (facing == EnumFacing.WEST) {
            return 90.0F;
        }
        if (facing == EnumFacing.NORTH) {
            return 180.0F;
        }
        if (facing == EnumFacing.EAST) {
            return -90.0F;
        }
        return 0.0F;
    }

    public static BlockPos targetPos(BlockPos origin, EnumFacing facing) {
        return origin.offset(facing);
    }
}
