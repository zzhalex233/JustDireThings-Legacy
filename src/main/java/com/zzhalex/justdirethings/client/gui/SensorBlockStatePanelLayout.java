package com.zzhalex.justdirethings.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public final class SensorBlockStatePanelLayout {

    public static final int PANEL_WIDTH = 100;
    public static final int PANEL_CLICK_LEFT_OFFSET = -101;
    public static final int LIST_WIDTH = 90;
    public static final int LIST_LEFT_OFFSET = -95;
    public static final int LIST_TOP_OFFSET = 5;
    public static final int LIST_BOTTOM_MARGIN = 10;
    public static final int SCROLLBAR_RIGHT_PADDING = 5;

    private SensorBlockStatePanelLayout() {
    }

    public static String trimToWidth(String text, int maxChars) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxChars) {
            return text;
        }
        if (maxChars <= 3) {
            return text.substring(0, Math.max(0, maxChars));
        }
        return text.substring(0, maxChars - 3) + "...";
    }

    public static String trimToPixelWidth(FontRenderer font, String text, int pixelWidth) {
        if (font == null) {
            return trimToWidth(text, pixelWidth);
        }
        String value = text == null ? "" : text;
        if (font.getStringWidth(value) <= pixelWidth) {
            return value;
        }
        String trimmed = value;
        while (!trimmed.isEmpty() && font.getStringWidth(trimmed + "...") > pixelWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed + "...";
    }

    public static String anyLabel() {
        return "ANY";
    }

    public static FontRenderer font() {
        return Minecraft.getMinecraft().fontRenderer;
    }
}
