package com.zzhalex.justdirethings.common.item.base;

import com.zzhalex.justdirethings.common.util.DimensionDisplayHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Objects;

public final class BoundInventoryHelper {

    private static final String TAG_BOUND = "JDTWrenchBoundTo";
    private static final String TAG_DIMENSION = "Dimension";
    private static final String TAG_X = "X";
    private static final String TAG_Y = "Y";
    private static final String TAG_Z = "Z";
    private static final String TAG_SIDE = "Side";

    private BoundInventoryHelper() {
    }

    @Nullable
    public static BoundLocation getBoundTo(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()) {
            return null;
        }
        NBTTagCompound root = stack.getTagCompound();
        if (root == null || !root.hasKey(TAG_BOUND)) {
            return null;
        }
        NBTTagCompound bound = root.getCompoundTag(TAG_BOUND);
        EnumFacing side = null;
        if (bound.hasKey(TAG_SIDE)) {
            int sideIndex = bound.getInteger(TAG_SIDE);
            if (sideIndex >= 0 && sideIndex < EnumFacing.values().length) {
                side = EnumFacing.values()[sideIndex];
            }
        }
        return new BoundLocation(
                bound.getInteger(TAG_DIMENSION),
                new BlockPos(bound.getInteger(TAG_X), bound.getInteger(TAG_Y), bound.getInteger(TAG_Z)),
                side
        );
    }

    public static void setBoundTo(ItemStack stack, BoundLocation boundLocation) {
        if (stack == null || stack.isEmpty() || boundLocation == null) {
            return;
        }
        NBTTagCompound root = stack.getTagCompound();
        if (root == null) {
            root = new NBTTagCompound();
            stack.setTagCompound(root);
        }
        NBTTagCompound bound = new NBTTagCompound();
        bound.setInteger(TAG_DIMENSION, boundLocation.getDimension());
        bound.setInteger(TAG_X, boundLocation.getPos().getX());
        bound.setInteger(TAG_Y, boundLocation.getPos().getY());
        bound.setInteger(TAG_Z, boundLocation.getPos().getZ());
        if (boundLocation.getSide() != null) {
            bound.setInteger(TAG_SIDE, boundLocation.getSide().ordinal());
        }
        root.setTag(TAG_BOUND, bound);
    }

    public static void removeBoundTo(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()) {
            return;
        }
        NBTTagCompound root = stack.getTagCompound();
        if (root != null) {
            root.removeTag(TAG_BOUND);
        }
    }

    public static final class BoundLocation {
        private final int dimension;
        private final BlockPos pos;
        private final EnumFacing side;

        public BoundLocation(int dimension, BlockPos pos) {
            this(dimension, pos, null);
        }

        public BoundLocation(int dimension, BlockPos pos, @Nullable EnumFacing side) {
            this.dimension = dimension;
            this.pos = pos == null ? BlockPos.ORIGIN : pos;
            this.side = side;
        }

        public int getDimension() {
            return dimension;
        }

        public String getDimensionName() {
            return DimensionDisplayHelper.getDimensionName(dimension);
        }

        public BlockPos getPos() {
            return pos;
        }

        @Nullable
        public EnumFacing getSide() {
            return side;
        }

        public String toShortString() {
            return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof BoundLocation)) {
                return false;
            }
            BoundLocation other = (BoundLocation) obj;
            return dimension == other.dimension && pos.equals(other.pos) && side == other.side;
        }

        @Override
        public int hashCode() {
            return Objects.hash(dimension, pos, side);
        }
    }
}
