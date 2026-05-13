package com.zzhalex.justdirethings.coremod.hooks;

import com.zzhalex.justdirethings.common.item.base.FluidBackedItem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;

public final class ItemOverlayHooks {

    private ItemOverlayHooks() {
    }

    public static void renderItemOverlayIntoGUI(ItemStack stack, int xPosition, int yPosition) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof FluidBackedItem)) {
            return;
        }

        FluidBackedItem fluidItem = (FluidBackedItem) stack.getItem();
        if (!fluidItem.isFluidBarVisible(stack)) {
            return;
        }

        int barY = stack.getItem().showDurabilityBar(stack) ? yPosition + 11 : yPosition + 13;
        int width = Math.max(0, Math.min(13, fluidItem.getFluidBarWidth(stack)));
        int color = fluidItem.getFluidBarColor(stack);

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        draw(buffer, xPosition + 2, barY, 13, 2, 48, 48, 48, 255);
        if (width > 0) {
            draw(buffer, xPosition + 2, barY, width, 1, color >> 16 & 255, color >> 8 & 255, color & 255, 255);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
    }

    private static void draw(BufferBuilder buffer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x, y, 0.0D).color(red, green, blue, alpha).endVertex();
        buffer.pos(x, y + height, 0.0D).color(red, green, blue, alpha).endVertex();
        buffer.pos(x + width, y + height, 0.0D).color(red, green, blue, alpha).endVertex();
        buffer.pos(x + width, y, 0.0D).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().draw();
    }
}
