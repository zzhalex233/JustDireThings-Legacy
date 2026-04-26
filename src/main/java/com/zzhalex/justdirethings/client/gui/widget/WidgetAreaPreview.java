package com.zzhalex.justdirethings.client.gui.widget;

import net.minecraft.client.gui.Gui;

public class WidgetAreaPreview {

    private final int x;
    private final int y;
    private final int size;
    private boolean enabled = true;

    public WidgetAreaPreview(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void draw(int guiLeft, int guiTop) {
        int borderColor = enabled ? 0xFFE0B000 : 0xFF555555;
        Gui.drawRect(guiLeft + x, guiTop + y, guiLeft + x + size, guiTop + y + 1, borderColor);
        Gui.drawRect(guiLeft + x, guiTop + y + size - 1, guiLeft + x + size, guiTop + y + size, borderColor);
        Gui.drawRect(guiLeft + x, guiTop + y, guiLeft + x + 1, guiTop + y + size, borderColor);
        Gui.drawRect(guiLeft + x + size - 1, guiTop + y, guiLeft + x + size, guiTop + y + size, borderColor);
    }
}
