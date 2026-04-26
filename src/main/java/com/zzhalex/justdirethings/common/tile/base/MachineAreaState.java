package com.zzhalex.justdirethings.common.tile.base;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class MachineAreaState {

    public static final double MAX_RADIUS = 5.0D;
    public static final int MAX_OFFSET = 9;

    private double xRadius;
    private double yRadius;
    private double zRadius;
    private int xOffset;
    private int yOffset;
    private int zOffset;
    private boolean renderArea;

    public double getXRadius() {
        return xRadius;
    }

    public double getYRadius() {
        return yRadius;
    }

    public double getZRadius() {
        return zRadius;
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public int getZOffset() {
        return zOffset;
    }

    public boolean isRenderArea() {
        return renderArea;
    }

    public void setArea(double xRadius, double yRadius, double zRadius) {
        this.xRadius = clampRadius(xRadius);
        this.yRadius = clampRadius(yRadius);
        this.zRadius = clampRadius(zRadius);
    }

    public void setOffset(int xOffset, int yOffset, int zOffset) {
        this.xOffset = clampOffset(xOffset);
        this.yOffset = clampOffset(yOffset);
        this.zOffset = clampOffset(zOffset);
    }

    public void setRenderArea(boolean renderArea) {
        this.renderArea = renderArea;
    }

    public AxisAlignedBB createArea(BlockPos origin) {
        double x = getOffsetOrigin(origin.getX(), xOffset, xRadius);
        double y = getOffsetOrigin(origin.getY(), yOffset, yRadius);
        double z = getOffsetOrigin(origin.getZ(), zOffset, zRadius);
        return new AxisAlignedBB(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D).grow(xRadius, yRadius, zRadius);
    }

    public AxisAlignedBB createOffsetOnlyArea(BlockPos origin) {
        double x = getOffsetOnlyOrigin(origin.getX(), xOffset, xRadius);
        double y = getOffsetOnlyOrigin(origin.getY(), yOffset, yRadius);
        double z = getOffsetOnlyOrigin(origin.getZ(), zOffset, zRadius);
        return new AxisAlignedBB(
                x,
                y,
                z,
                x + getOffsetOnlySize(xRadius),
                y + getOffsetOnlySize(yRadius),
                z + getOffsetOnlySize(zRadius)
        );
    }

    public NBTTagCompound writeToNbt(NBTTagCompound tag) {
        tag.setDouble("XRadius", xRadius);
        tag.setDouble("YRadius", yRadius);
        tag.setDouble("ZRadius", zRadius);
        tag.setInteger("XOffset", xOffset);
        tag.setInteger("YOffset", yOffset);
        tag.setInteger("ZOffset", zOffset);
        tag.setBoolean("RenderArea", renderArea);
        return tag;
    }

    public void readFromNbt(NBTTagCompound tag) {
        setArea(tag.getDouble("XRadius"), tag.getDouble("YRadius"), tag.getDouble("ZRadius"));
        setOffset(tag.getInteger("XOffset"), tag.getInteger("YOffset"), tag.getInteger("ZOffset"));
        renderArea = tag.getBoolean("RenderArea");
    }

    private static double clampRadius(double value) {
        return Math.max(0.0D, Math.min(MAX_RADIUS, value));
    }

    private static int clampOffset(int value) {
        return Math.max(-MAX_OFFSET, Math.min(MAX_OFFSET, value));
    }

    private static double getOffsetOrigin(int origin, int offset, double radius) {
        double offsetOrigin = origin + offset;
        if (radius != Math.floor(radius)) {
            offsetOrigin += 0.5D;
        }
        return offsetOrigin;
    }

    private static double getOffsetOnlyOrigin(int origin, int offset, double radius) {
        double offsetOrigin = origin + offset;
        if (radius != Math.floor(radius)) {
            offsetOrigin += 0.75D;
        }
        return offsetOrigin;
    }

    private static double getOffsetOnlySize(double radius) {
        return radius != Math.floor(radius) ? 0.5D : 1.0D;
    }
}
