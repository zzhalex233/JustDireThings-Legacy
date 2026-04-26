package com.zzhalex.justdirethings.client.render;

import com.zzhalex.justdirethings.common.entity.EntityTimeWand;
import com.zzhalex.justdirethings.config.JDTConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class RenderTimeWand extends Render<EntityTimeWand> {

    public RenderTimeWand(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityTimeWand entity, double x, double y, double z, float entityYaw, float partialTicks) {
        BlockPos acceleratedPos = entity.getAcceleratedPos();
        if (acceleratedPos == null || entity.world.isAirBlock(acceleratedPos)) {
            return;
        }

        String rateLabel = TimeWandDisplayData.accelerationLabel(entity.getTickLevel());
        String timeLabel = TimeWandDisplayData.remainingTimeLabel(entity.getRemainingTime());
        float tickRateProgress = TimeWandDisplayData.tickRateProgress(entity.getTickLevel(), JDTConfig.timeWandMaxMultiplier);
        float remainingProgress = TimeWandDisplayData.remainingTimeProgress(entity.getRemainingTime(), entity.getTotalTime());

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        renderProgressBars(tickRateProgress, remainingProgress);
        drawAllFaces(rateLabel, 0.39F, 0xFFFFFF);
        drawAllFaces(timeLabel, 0.69F, 0xFFFFFF);
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    private void renderProgressBars(float tickRateProgress, float remainingProgress) {
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        drawBarsForAllFaces(0.30F, tickRateProgress, 0.0F, 1.0F, 0.0F, 0.5F);
        drawBarsForAllFaces(0.60F, remainingProgress, 1.0F, 0.0F, 0.0F, 0.5F);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private void drawBarsForAllFaces(float yOffset, float progress, float red, float green, float blue, float alpha) {
        float width = 0.8F * Math.max(0.0F, Math.min(1.0F, progress));
        if (width <= 0.0F) {
            return;
        }

        drawFaceBar(-0.4F, yOffset, 0.501F, width, red, green, blue, alpha, 0.0F, 0.0F);
        drawFaceBar(-0.4F, yOffset, -0.501F, width, red, green, blue, alpha, 180.0F, 0.0F);
        drawFaceBar(-0.4F, yOffset, 0.501F, width, red, green, blue, alpha, 90.0F, 0.0F);
        drawFaceBar(-0.4F, yOffset, -0.501F, width, red, green, blue, alpha, -90.0F, 0.0F);
        drawFaceBar(-0.4F, yOffset - 0.5F, 0.501F, width, red, green, blue, alpha, 0.0F, -90.0F);
        drawFaceBar(-0.4F, yOffset - 0.5F, -0.501F, width, red, green, blue, alpha, 0.0F, 90.0F);
    }

    private void drawFaceBar(float xStart, float yStart, float zStart, float width, float red, float green, float blue, float alpha, float yaw, float pitch) {
        GlStateManager.pushMatrix();
        if (yaw != 0.0F) {
            GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
        }
        if (pitch != 0.0F) {
            GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(xStart, yStart, zStart).color(red, green, blue, alpha).endVertex();
        buffer.pos(xStart + width, yStart, zStart).color(red, green, blue, alpha).endVertex();
        buffer.pos(xStart + width, yStart + 0.1F, zStart).color(red, green, blue, alpha).endVertex();
        buffer.pos(xStart, yStart + 0.1F, zStart).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
    }

    private void drawAllFaces(String text, float yOffset, int color) {
        drawFaceText(text, 0.0D, yOffset, 0.51D, 0.0F, 0.0F, color);
        drawFaceText(text, 0.0D, yOffset, -0.51D, 180.0F, 0.0F, color);
        drawFaceText(text, 0.51D, yOffset, 0.0D, 90.0F, 0.0F, color);
        drawFaceText(text, -0.51D, yOffset, 0.0D, -90.0F, 0.0F, color);
        drawFaceText(text, 0.0D, 1.01D, 0.5D - yOffset, 0.0F, -90.0F, color);
        drawFaceText(text, 0.0D, -0.01D, -0.5D + yOffset, 0.0F, 90.0F, color);
    }

    private void drawFaceText(String text, double x, double y, double z, float yaw, float pitch, int color) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        if (yaw != 0.0F) {
            GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
        }
        if (pitch != 0.0F) {
            GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
        }
        GlStateManager.scale(-0.01F, -0.01F, 0.01F);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        fontRenderer.drawString(text, -fontRenderer.getStringWidth(text) / 2.0F, 0.0F, color, false);
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityTimeWand entity) {
        return null;
    }
}
