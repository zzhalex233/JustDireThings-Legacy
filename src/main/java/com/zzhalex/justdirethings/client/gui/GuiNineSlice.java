package com.zzhalex.justdirethings.client.gui;

import net.minecraft.client.gui.Gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GuiNineSlice {

    public static final int TEXTURE_WIDTH = 236;
    public static final int TEXTURE_HEIGHT = 34;
    public static final int BORDER = 8;

    private GuiNineSlice() {
    }

    public static List<Slice> buildSlices(int x, int y, int width, int height) {
        if (width < BORDER * 2 || height < BORDER * 2) {
            throw new IllegalArgumentException("Nine-slice panel is too small: " + width + "x" + height);
        }

        int centerSourceWidth = TEXTURE_WIDTH - BORDER * 2;
        int centerSourceHeight = TEXTURE_HEIGHT - BORDER * 2;
        int centerTargetWidth = width - BORDER * 2;
        int centerTargetHeight = height - BORDER * 2;

        List<Slice> slices = new ArrayList<>(9);
        slices.add(new Slice(x, y, 0, 0, BORDER, BORDER, BORDER, BORDER));
        slices.add(new Slice(x + BORDER, y, BORDER, 0, centerSourceWidth, BORDER, centerTargetWidth, BORDER));
        slices.add(new Slice(x + width - BORDER, y, TEXTURE_WIDTH - BORDER, 0, BORDER, BORDER, BORDER, BORDER));

        slices.add(new Slice(x, y + BORDER, 0, BORDER, BORDER, centerSourceHeight, BORDER, centerTargetHeight));
        slices.add(new Slice(x + BORDER, y + BORDER, BORDER, BORDER, centerSourceWidth, centerSourceHeight, centerTargetWidth, centerTargetHeight));
        slices.add(new Slice(x + width - BORDER, y + BORDER, TEXTURE_WIDTH - BORDER, BORDER, BORDER, centerSourceHeight, BORDER, centerTargetHeight));

        slices.add(new Slice(x, y + height - BORDER, 0, TEXTURE_HEIGHT - BORDER, BORDER, BORDER, BORDER, BORDER));
        slices.add(new Slice(x + BORDER, y + height - BORDER, BORDER, TEXTURE_HEIGHT - BORDER, centerSourceWidth, BORDER, centerTargetWidth, BORDER));
        slices.add(new Slice(x + width - BORDER, y + height - BORDER, TEXTURE_WIDTH - BORDER, TEXTURE_HEIGHT - BORDER, BORDER, BORDER, BORDER, BORDER));
        return Collections.unmodifiableList(slices);
    }

    public static void draw(int x, int y, int width, int height) {
        for (Slice slice : buildSlices(x, y, width, height)) {
            Gui.drawScaledCustomSizeModalRect(
                    slice.targetX,
                    slice.targetY,
                    slice.sourceX,
                    slice.sourceY,
                    slice.sourceWidth,
                    slice.sourceHeight,
                    slice.targetWidth,
                    slice.targetHeight,
                    TEXTURE_WIDTH,
                    TEXTURE_HEIGHT
            );
        }
    }

    public static final class Slice {
        public final int targetX;
        public final int targetY;
        public final int sourceX;
        public final int sourceY;
        public final int sourceWidth;
        public final int sourceHeight;
        public final int targetWidth;
        public final int targetHeight;

        private Slice(int targetX, int targetY, int sourceX, int sourceY, int sourceWidth, int sourceHeight, int targetWidth, int targetHeight) {
            this.targetX = targetX;
            this.targetY = targetY;
            this.sourceX = sourceX;
            this.sourceY = sourceY;
            this.sourceWidth = sourceWidth;
            this.sourceHeight = sourceHeight;
            this.targetWidth = targetWidth;
            this.targetHeight = targetHeight;
        }
    }
}
