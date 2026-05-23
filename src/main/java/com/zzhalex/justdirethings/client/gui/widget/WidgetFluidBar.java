package com.zzhalex.justdirethings.client.gui.widget;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.item.tooltip.TooltipHelper;
import com.zzhalex.justdirethings.common.util.FluidDisplayHelper;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;
import java.util.List;

public class WidgetFluidBar {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/fluidbar.png");
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private int current;
    private int max = 1;
    private int color = 0xFF3F51B5;
    private String fluidName = "";

    public WidgetFluidBar(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setValue(int current, int max, int color) {
        this.current = Math.max(0, current);
        this.max = Math.max(1, max);
        this.color = color;
        this.fluidName = "";
    }

    public void setValue(int current, int max, String fluidName) {
        this.current = Math.max(0, current);
        this.max = Math.max(1, max);
        this.fluidName = fluidName == null ? "" : fluidName;
    }

    public void draw(int guiLeft, int guiTop) {
        int innerHeight = height - 2;
        int filled = Math.max(0, Math.min(innerHeight, (current * innerHeight) / max));
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        Gui.drawModalRectWithCustomSizedTexture(guiLeft + x, guiTop + y, 0, 0, width, height, 36, 72);
        if (filled > 0) {
            drawFluid(guiLeft + x + 1, guiTop + y + height - 1 - filled, width - 2, filled);
        }
        Gui.drawModalRectWithCustomSizedTexture(guiLeft + x, guiTop + y, 18, 0, width, height, 36, 72);
    }

    public boolean contains(int guiLeft, int guiTop, int mouseX, int mouseY) {
        int left = guiLeft + x;
        int top = guiTop + y;
        return mouseX >= left && mouseX < left + width && mouseY >= top && mouseY < top + height;
    }

    public List<String> getTooltipLines() {
        String displayName = getFluidDisplayName();
        if (displayName.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(I18n.format(
                "justdirethings.screen.fluid",
                displayName,
                TooltipHelper.formatTooltipValue(current),
                TooltipHelper.formatTooltipValue(max)
        ));
    }

    private void drawFluid(int left, int top, int drawWidth, int drawHeight) {
        Fluid fluid = FluidRegistry.getFluid(fluidName);
        if (fluid == null) {
            Gui.drawRect(left, top, left + drawWidth, top + drawHeight, color);
            return;
        }

        FluidStack stack = new FluidStack(fluid, current);
        ResourceLocation still = fluid.getStill(stack);
        if (still == null) {
            Gui.drawRect(left, top, left + drawWidth, top + drawHeight, color);
            return;
        }

        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(still.toString());
        int tint = fluid.getColor(stack);
        float red = (float) (tint >> 16 & 255) / 255.0F;
        float green = (float) (tint >> 8 & 255) / 255.0F;
        float blue = (float) (tint & 255) / 255.0F;

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.color(red, green, blue, 1.0F);
        drawSprite(left, top, drawWidth, drawHeight, sprite);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
    }

    private static void drawSprite(int left, int top, int width, int height, TextureAtlasSprite sprite) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(left, top + height, 0).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
        buffer.pos(left + width, top + height, 0).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
        buffer.pos(left + width, top, 0).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
        buffer.pos(left, top, 0).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
        tessellator.draw();
    }

    private String getFluidDisplayName() {
        return FluidDisplayHelper.getLocalizedName(fluidName, current);
    }

}
