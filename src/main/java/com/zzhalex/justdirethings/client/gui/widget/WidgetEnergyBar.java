package com.zzhalex.justdirethings.client.gui.widget;

import com.zzhalex.justdirethings.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class WidgetEnergyBar {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/powerbar.png");
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private int current;
    private int max = 1;

    public WidgetEnergyBar(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setValue(int current, int max) {
        this.current = Math.max(0, current);
        this.max = Math.max(1, max);
    }

    public void draw(int guiLeft, int guiTop) {
        int filled = (int) Math.round((double) current / (double) max * height);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        Gui.drawModalRectWithCustomSizedTexture(guiLeft + x, guiTop + y, 0, 0, width, height, 36, 72);
        if (filled > 0) {
            Gui.drawModalRectWithCustomSizedTexture(guiLeft + x + 1, guiTop + y + height - filled, 19, 71 - filled, width - 1, filled, 36, 72);
        }
    }

    public boolean contains(int guiLeft, int guiTop, int mouseX, int mouseY) {
        int left = guiLeft + x;
        int top = guiTop + y;
        return mouseX >= left && mouseX < left + width && mouseY >= top && mouseY < top + height;
    }

    public List<String> getTooltipLines() {
        return Collections.singletonList(I18n.format(
                "justdirethings.screen.energy",
                formatAmount(current),
                formatAmount(max)
        ));
    }

    private static String formatAmount(int amount) {
        return NumberFormat.getIntegerInstance(Locale.ROOT).format(amount);
    }
}
