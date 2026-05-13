package com.zzhalex.justdirethings.client.render;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.ArrayList;
import java.util.List;

public final class PortalRenderState {

    private static final double FRAME_NORMAL_THICKNESS = 0.002D;

    private PortalRenderState() {
    }

    public static float animationProgress(int ticksExisted, int deathCounter, boolean dying, float partialTicks, int maxTicks) {
        if (maxTicks <= 0) {
            return 1.0F;
        }

        if (dying) {
            float deathTicks = deathCounter + partialTicks;
            return (maxTicks - Math.min(deathTicks, maxTicks)) / (float) maxTicks;
        }

        float totalTicks = ticksExisted + partialTicks;
        return Math.min(totalTicks / maxTicks, 1.0F);
    }

    public static List<AxisAlignedBB> frameBoxes(AxisAlignedBB bounds, double thickness, EnumFacing.Axis planeAxis) {
        List<AxisAlignedBB> boxes = new ArrayList<>(4);
        if (bounds == null || planeAxis == null || thickness <= 0.0D) {
            return boxes;
        }

        double minX = bounds.minX;
        double minY = bounds.minY;
        double minZ = bounds.minZ;
        double maxX = bounds.maxX;
        double maxY = bounds.maxY;
        double maxZ = bounds.maxZ;

        if (planeAxis == EnumFacing.Axis.Z) {
            Range normal = centeredRange(bounds.minZ, bounds.maxZ, FRAME_NORMAL_THICKNESS);
            boxes.add(new AxisAlignedBB(minX, minY, normal.min, minX + thickness, maxY, normal.max));
            boxes.add(new AxisAlignedBB(maxX - thickness, minY, normal.min, maxX, maxY, normal.max));
            boxes.add(new AxisAlignedBB(minX, minY, normal.min, maxX, minY + thickness, normal.max));
            boxes.add(new AxisAlignedBB(minX, maxY - thickness, normal.min, maxX, maxY, normal.max));
        } else if (planeAxis == EnumFacing.Axis.X) {
            Range normal = centeredRange(bounds.minX, bounds.maxX, FRAME_NORMAL_THICKNESS);
            boxes.add(new AxisAlignedBB(normal.min, minY, minZ, normal.max, maxY, minZ + thickness));
            boxes.add(new AxisAlignedBB(normal.min, minY, maxZ - thickness, normal.max, maxY, maxZ));
            boxes.add(new AxisAlignedBB(normal.min, minY, minZ, normal.max, minY + thickness, maxZ));
            boxes.add(new AxisAlignedBB(normal.min, maxY - thickness, minZ, normal.max, maxY, maxZ));
        } else {
            Range normal = anchoredRange(bounds.minY, bounds.maxY, FRAME_NORMAL_THICKNESS);
            boxes.add(new AxisAlignedBB(minX, normal.min, minZ, minX + thickness, normal.max, maxZ));
            boxes.add(new AxisAlignedBB(maxX - thickness, normal.min, minZ, maxX, normal.max, maxZ));
            boxes.add(new AxisAlignedBB(minX, normal.min, minZ, maxX, normal.max, minZ + thickness));
            boxes.add(new AxisAlignedBB(minX, normal.min, maxZ - thickness, maxX, normal.max, maxZ));
        }
        return boxes;
    }

    public static EnumFacing.Axis planeAxisForFacing(EnumFacing facing) {
        return facing == null ? EnumFacing.Axis.Z : facing.getAxis();
    }

    private static Range centeredRange(double min, double max, double desiredThickness) {
        double available = Math.max(0.0D, max - min);
        double thickness = Math.min(desiredThickness, available);
        double center = (min + max) * 0.5D;
        return new Range(center - thickness * 0.5D, center + thickness * 0.5D);
    }

    private static Range anchoredRange(double min, double max, double desiredThickness) {
        return new Range(min, min + Math.min(desiredThickness, Math.max(0.0D, max - min)));
    }

    private static final class Range {
        private final double min;
        private final double max;

        private Range(double min, double max) {
            this.min = min;
            this.max = max;
        }
    }
}
