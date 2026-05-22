package com.zzhalex.justdirethings.client.render;

import com.zzhalex.justdirethings.common.entity.EntityParadox;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class RenderParadox extends Render<EntityParadox> {

    public RenderParadox(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityParadox entity, double x, double y, double z, float entityYaw, float partialTicks) {
        float radius = interpolateRadius(entity);
        float tickProgress = (entity.ticksExisted + partialTicks) % 50.0F;
        float pulsePhase = tickProgress / 50.0F;
        float pulseScale = 0.25F + 0.025F * (1.0F - Math.abs(2.0F * pulsePhase - 1.0F));
        float size = pulseScale * entity.getShrinkScale() * 0.25F * (float) Math.pow(radius, 1.25D);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + 0.5D, z);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);
        drawCore(size);
        drawLightning(entity, size * 4.0F);
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityParadox entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }

    private float interpolateRadius(EntityParadox entity) {
        float currentRadius = entity.getRadius() + 1.0F;
        float targetRadius = entity.getTargetRadius() + 1.0F;
        if (entity.getGrowthTicks() <= 0 || entity.growthDuration <= 0) {
            return currentRadius;
        }
        float progress = MathHelper.clamp((float) entity.getGrowthTicks() / (float) entity.growthDuration, 0.0F, 1.0F);
        return currentRadius + (targetRadius - currentRadius) * progress;
    }

    private void drawCore(float size) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        addQuad(buffer, -size, -size, 0.0D, size, -size, 0.0D, size, size, 0.0D, -size, size, 0.0D, 0, 0, 0, 220);
        addQuad(buffer, 0.0D, -size, -size, 0.0D, -size, size, 0.0D, size, size, 0.0D, size, -size, 0, 0, 0, 220);
        addQuad(buffer, -size, 0.0D, -size, size, 0.0D, -size, size, 0.0D, size, -size, 0.0D, size, 0, 0, 0, 220);
        tessellator.draw();
    }

    private void drawLightning(EntityParadox entity, float maxLength) {
        Random random = new Random(entity.ticksExisted);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < 8; i++) {
            if (random.nextFloat() > 0.25F) {
                continue;
            }
            float yaw = random.nextFloat() * 360.0F;
            float pitch = random.nextFloat() * 180.0F - 90.0F;
            float length = random.nextFloat() * maxLength;
            double endX = length * Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
            double endY = length * Math.sin(Math.toRadians(pitch));
            double endZ = length * Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
            int red = 75 + random.nextInt(26);
            int blue = random.nextInt(20);
            addLine(buffer, 0.0D, 0.0D, 0.0D, endX, endY, endZ, red, 0, blue, 255);
        }
        tessellator.draw();
    }

    private void addQuad(BufferBuilder buffer,
                         double x1, double y1, double z1,
                         double x2, double y2, double z2,
                         double x3, double y3, double z3,
                         double x4, double y4, double z4,
                         int red, int green, int blue, int alpha) {
        buffer.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
        buffer.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
        buffer.pos(x3, y3, z3).color(red, green, blue, alpha).endVertex();
        buffer.pos(x4, y4, z4).color(red, green, blue, alpha).endVertex();
    }

    private void addLine(BufferBuilder buffer,
                         double x1, double y1, double z1,
                         double x2, double y2, double z2,
                         int red, int green, int blue, int alpha) {
        buffer.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
        buffer.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
    }
}
