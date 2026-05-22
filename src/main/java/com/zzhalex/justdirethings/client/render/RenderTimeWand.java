package com.zzhalex.justdirethings.client.render;

import com.zzhalex.justdirethings.common.entity.EntityTimeWand;
import com.zzhalex.justdirethings.config.JDTConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class RenderTimeWand extends Render<EntityTimeWand> {

    private static final float TEXT_PADDING = 0.13F;

    public RenderTimeWand(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityTimeWand entity, double x, double y, double z, float entityYaw, float partialTicks) {
        BlockPos acceleratedPos = entity.getAcceleratedPos();
        BlockPos renderPos = acceleratedPos == null ? entity.getPosition() : acceleratedPos;
        if (entity.world.isAirBlock(renderPos)) {
            return;
        }

        String rateLabel = TimeWandDisplayData.accelerationLabel(entity.getTickLevel());
        String timeLabel = TimeWandDisplayData.remainingTimeLabel(entity.getRemainingTime());
        float tickRateProgress = TimeWandDisplayData.tickRateProgress(entity.getTickLevel(), JDTConfig.timeWandMaxMultiplier);
        float remainingProgress = TimeWandDisplayData.remainingTimeProgress(entity.getRemainingTime(), entity.getTotalTime());
        boolean wasFogEnabled = GL11.glIsEnabled(GL11.GL_FOG);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        beginOverlayRender();

        renderProgressBarOnSide(0.3F, tickRateProgress, 0.0F, 1.0F, 0.0F, 0.5F);
        renderProgressBarOnSide(0.6F, remainingProgress, 1.0F, 0.0F, 0.0F, 0.5F);
        drawAllText(rateLabel, 0.39F);
        drawAllText(timeLabel, 0.69F);

        endOverlayRender(wasFogEnabled);
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    private void beginOverlayRender() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.doPolygonOffset(-3.0F, -3.0F);
        GlStateManager.enablePolygonOffset();
    }

    private void endOverlayRender(boolean wasFogEnabled) {
        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(0.0F, 0.0F);
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        if (wasFogEnabled) {
            GlStateManager.enableFog();
        }
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderProgressBarOnSide(float yStart, float progress, float red, float green, float blue, float alpha) {
        float barWidth = 0.8F;
        float barHeight = 0.1F;
        float barProgress = barWidth * Math.max(0.0F, Math.min(1.0F, progress));
        if (barProgress <= 0.0F) {
            return;
        }

        GlStateManager.disableTexture2D();
        renderBarFace(-0.4F, yStart, 0.5F, barProgress, barHeight, red, green, blue, alpha, 0.0F, 0.0F);
        renderBarFace(-0.4F, yStart, 0.5F, barProgress, barHeight, red, green, blue, alpha, 180.0F, 0.0F);
        renderBarFace(-0.4F, yStart, 0.5F, barProgress, barHeight, red, green, blue, alpha, 90.0F, 0.0F);
        renderBarFace(-0.4F, yStart, 0.5F, barProgress, barHeight, red, green, blue, alpha, -90.0F, 0.0F);
        renderBarFace(-0.4F, yStart - 0.5F, 1.0F, barProgress, barHeight, red, green, blue, alpha, 0.0F, -90.0F);
        renderBarFace(-0.4F, yStart - 0.5F, 0.0F, barProgress, barHeight, red, green, blue, alpha, 0.0F, 90.0F);
        GlStateManager.enableTexture2D();
    }

    private void renderBarFace(float xStart, float yStart, float zStart, float barWidth, float barHeight,
                               float red, float green, float blue, float alpha, float yaw, float pitch) {
        GlStateManager.pushMatrix();
        if (yaw != 0.0F) {
            GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
        }
        if (pitch != 0.0F) {
            GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(xStart, yStart, zStart).color(red, green, blue, alpha).endVertex();
        buffer.pos(xStart + barWidth, yStart, zStart).color(red, green, blue, alpha).endVertex();
        buffer.pos(xStart + barWidth, yStart + barHeight, zStart).color(red, green, blue, alpha).endVertex();
        buffer.pos(xStart, yStart + barHeight, zStart).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
    }

    private void drawAllText(String text, float yOffset) {
        drawText(text, -TEXT_PADDING, yOffset, 0.51F, 0.0F, 0.0F);
        drawText(text, TEXT_PADDING, yOffset, -0.51F, 180.0F, 0.0F);
        drawText(text, 0.51F, yOffset, TEXT_PADDING, 90.0F, 0.0F);
        drawText(text, -0.51F, yOffset, -TEXT_PADDING, -90.0F, 0.0F);
        drawText(text, -TEXT_PADDING, 1.01F, 0.5F - yOffset, 0.0F, 90.0F);
        drawText(text, -TEXT_PADDING, -0.01F, -0.5F + yOffset, 0.0F, -90.0F);
    }

    private void drawText(String text, float x, float y, float z, float yaw, float pitch) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.scale(0.01F, -0.01F, 0.01F);
        if (yaw != 0.0F) {
            GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
        }
        if (pitch != 0.0F) {
            GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        fontRenderer.drawString(text, 0.0F, 0.0F, 0xFFFFFF, false);
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityTimeWand entity) {
        return null;
    }
}
