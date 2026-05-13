package com.zzhalex.justdirethings.client.render;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.entity.EntityPortal;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class RenderPortal extends Render<EntityPortal> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/block/portal_shader.png");
    private static final double FRAME_EDGE_THICKNESS = 0.025D;

    public RenderPortal(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityPortal entity, double x, double y, double z, float entityYaw, float partialTicks) {
        AxisAlignedBB bounds = entity.getEntityBoundingBox().offset(-entity.posX, -entity.posY, -entity.posZ).offset(x, y, z);
        float progress = PortalRenderState.animationProgress(
                entity.ticksExisted,
                entity.getDeathCounter(),
                entity.isDying(),
                partialTicks,
                EntityPortal.ANIMATION_COOLDOWN
        );
        float red = entity.isPrimary() ? 0.0F : 1.0F;
        float green = entity.isPrimary() ? 0.6F : 0.65F;
        float blue = entity.isPrimary() ? 1.0F : 0.0F;
        float alpha = 0.85F;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        bindEntityTexture(entity);
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        boolean shaderBound = PortalShaderProgram.bind((entity.ticksExisted + partialTicks) / 24000.0F, 16);
        drawPortalPlane(entity, bounds, progress);
        if (shaderBound) {
            PortalShaderProgram.unbind();
        }
        GlStateManager.disableTexture2D();
        drawFlatFrame(bounds, PortalRenderState.planeAxisForFacing(entity.getFacing()), red, green, blue, alpha);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityPortal entity) {
        return TEXTURE;
    }

    private void drawPortalPlane(EntityPortal entity, AxisAlignedBB bounds, float progress) {
        double minX = bounds.minX;
        double maxX = bounds.maxX;
        double minY = bounds.minY;
        double maxY = bounds.maxY;
        double minZ = bounds.minZ;
        double maxZ = bounds.maxZ;

        double centerX = (minX + maxX) * 0.5D;
        double centerY = (minY + maxY) * 0.5D;
        double centerZ = (minZ + maxZ) * 0.5D;

        if (bounds.getAverageEdgeLength() <= 0.0D) {
            return;
        }

        if (PortalRenderState.planeAxisForFacing(entity.getFacing()) == net.minecraft.util.EnumFacing.Axis.Z) {
            double animatedMinX = MathHelper.clampedLerp(centerX, minX, progress);
            double animatedMaxX = MathHelper.clampedLerp(centerX, maxX, progress);
            double animatedMinY = MathHelper.clampedLerp(maxY, minY, progress);
            drawQuad(animatedMinX, animatedMaxX, animatedMinY, maxY, centerZ, false);
            drawQuad(animatedMaxX, animatedMinX, animatedMinY, maxY, centerZ, false);
        } else if (PortalRenderState.planeAxisForFacing(entity.getFacing()) == net.minecraft.util.EnumFacing.Axis.X) {
            double animatedMinZ = MathHelper.clampedLerp(centerZ, minZ, progress);
            double animatedMaxZ = MathHelper.clampedLerp(centerZ, maxZ, progress);
            double animatedMinY = MathHelper.clampedLerp(maxY, minY, progress);
            drawQuadZ(centerX, animatedMinZ, animatedMaxZ, animatedMinY, maxY, false);
            drawQuadZ(centerX, animatedMaxZ, animatedMinZ, animatedMinY, maxY, false);
        } else {
            double animatedMinX = MathHelper.clampedLerp(centerX, minX, progress);
            double animatedMaxX = MathHelper.clampedLerp(centerX, maxX, progress);
            double animatedMinZ = MathHelper.clampedLerp(centerZ, minZ, progress);
            double animatedMaxZ = MathHelper.clampedLerp(centerZ, maxZ, progress);
            drawQuadTop(animatedMinX, animatedMaxX, minY, animatedMinZ, animatedMaxZ, false);
            drawQuadTop(animatedMaxX, animatedMinX, minY, animatedMinZ, animatedMaxZ, false);
        }
    }

    private void drawQuad(double minX, double maxX, double minY, double maxY, double z, boolean flipped) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        if (!flipped) {
            buffer.pos(minX, minY, z).tex(0.0D, 0.0D).endVertex();
            buffer.pos(maxX, minY, z).tex(1.0D, 0.0D).endVertex();
            buffer.pos(maxX, maxY, z).tex(1.0D, 1.0D).endVertex();
            buffer.pos(minX, maxY, z).tex(0.0D, 1.0D).endVertex();
        } else {
            buffer.pos(minX, minY, z).tex(1.0D, 0.0D).endVertex();
            buffer.pos(maxX, minY, z).tex(0.0D, 0.0D).endVertex();
            buffer.pos(maxX, maxY, z).tex(0.0D, 1.0D).endVertex();
            buffer.pos(minX, maxY, z).tex(1.0D, 1.0D).endVertex();
        }
        tessellator.draw();
    }

    private void drawQuadZ(double x, double minZ, double maxZ, double minY, double maxY, boolean flipped) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        if (!flipped) {
            buffer.pos(x, minY, minZ).tex(0.0D, 0.0D).endVertex();
            buffer.pos(x, minY, maxZ).tex(1.0D, 0.0D).endVertex();
            buffer.pos(x, maxY, maxZ).tex(1.0D, 1.0D).endVertex();
            buffer.pos(x, maxY, minZ).tex(0.0D, 1.0D).endVertex();
        } else {
            buffer.pos(x, minY, minZ).tex(1.0D, 0.0D).endVertex();
            buffer.pos(x, minY, maxZ).tex(0.0D, 0.0D).endVertex();
            buffer.pos(x, maxY, maxZ).tex(0.0D, 1.0D).endVertex();
            buffer.pos(x, maxY, minZ).tex(1.0D, 1.0D).endVertex();
        }
        tessellator.draw();
    }

    private void drawQuadTop(double minX, double maxX, double y, double minZ, double maxZ, boolean flipped) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        if (!flipped) {
            buffer.pos(minX, y, minZ).tex(0.0D, 0.0D).endVertex();
            buffer.pos(maxX, y, minZ).tex(1.0D, 0.0D).endVertex();
            buffer.pos(maxX, y, maxZ).tex(1.0D, 1.0D).endVertex();
            buffer.pos(minX, y, maxZ).tex(0.0D, 1.0D).endVertex();
        } else {
            buffer.pos(minX, y, minZ).tex(1.0D, 0.0D).endVertex();
            buffer.pos(maxX, y, minZ).tex(0.0D, 0.0D).endVertex();
            buffer.pos(maxX, y, maxZ).tex(0.0D, 1.0D).endVertex();
            buffer.pos(minX, y, maxZ).tex(1.0D, 1.0D).endVertex();
        }
        tessellator.draw();
    }

    private void drawFlatFrame(AxisAlignedBB bounds, net.minecraft.util.EnumFacing.Axis planeAxis, float red, float green, float blue, float alpha) {
        List<AxisAlignedBB> boxes = PortalRenderState.frameBoxes(bounds, FRAME_EDGE_THICKNESS, planeAxis);
        for (AxisAlignedBB box : boxes) {
            drawSolidBox(box, red, green, blue, alpha);
        }
    }

    private void drawSolidBox(AxisAlignedBB box, float red, float green, float blue, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        addBoxVertices(buffer, box, red, green, blue, alpha);
        tessellator.draw();
    }

    private void addBoxVertices(BufferBuilder buffer, AxisAlignedBB box, float red, float green, float blue, float alpha) {
        double minX = box.minX;
        double minY = box.minY;
        double minZ = box.minZ;
        double maxX = box.maxX;
        double maxY = box.maxY;
        double maxZ = box.maxZ;

        addFace(buffer, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, red, green, blue, alpha);
        addFace(buffer, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ, red, green, blue, alpha);
        addFace(buffer, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, red, green, blue, alpha);
        addFace(buffer, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
        addFace(buffer, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
        addFace(buffer, maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
    }

    private void addFace(BufferBuilder buffer,
                         double x1, double y1, double z1,
                         double x2, double y2, double z2,
                         double x3, double y3, double z3,
                         double x4, double y4, double z4,
                         float red, float green, float blue, float alpha) {
        buffer.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
        buffer.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
        buffer.pos(x3, y3, z3).color(red, green, blue, alpha).endVertex();
        buffer.pos(x4, y4, z4).color(red, green, blue, alpha).endVertex();
    }
}
