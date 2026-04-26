package com.zzhalex.justdirethings.client.render;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.entity.EntityPortalProjectile;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class RenderPortalProjectile extends Render<EntityPortalProjectile> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/entity/portal_projectile.png");

    public RenderPortalProjectile(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityPortalProjectile entity, double x, double y, double z, float entityYaw, float partialTicks) {
        bindEntityTexture(entity);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + 0.15D, z);
        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        PortalProjectileRenderState.Rotation rotation = PortalProjectileRenderState.rotationForAge(entity.ticksExisted, partialTicks);
        GlStateManager.rotate((float) rotation.getYawDegrees(), 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float) rotation.getPitchDegrees(), 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate((float) rotation.getRollDegrees(), 0.0F, 0.0F, 1.0F);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);

        drawModel(PortalProjectileRenderState.modelBoxes());

        GlStateManager.disableBlend();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    private void drawModel(List<AxisAlignedBB> boxes) {
        for (AxisAlignedBB box : boxes) {
            drawBox(box);
        }
    }

    private void drawBox(AxisAlignedBB box) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
        addBoxVertices(buffer, box);
        tessellator.draw();
    }

    private void addBoxVertices(BufferBuilder buffer, AxisAlignedBB box) {
        double minX = box.minX;
        double minY = box.minY;
        double minZ = box.minZ;
        double maxX = box.maxX;
        double maxY = box.maxY;
        double maxZ = box.maxZ;

        addFace(buffer, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, 0.0F, 0.0F, 1.0F);
        addFace(buffer, maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, 0.0F, 0.0F, -1.0F);
        addFace(buffer, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, 1.0F, 0.0F, 0.0F);
        addFace(buffer, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, -1.0F, 0.0F, 0.0F);
        addFace(buffer, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ, 0.0F, 1.0F, 0.0F);
        addFace(buffer, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, 0.0F, -1.0F, 0.0F);
    }

    private void addFace(BufferBuilder buffer,
                         double x1, double y1, double z1,
                         double x2, double y2, double z2,
                         double x3, double y3, double z3,
                         double x4, double y4, double z4,
                         float normalX, float normalY, float normalZ) {
        buffer.pos(x1, y1, z1).tex(0.0D, 1.0D).normal(normalX, normalY, normalZ).endVertex();
        buffer.pos(x2, y2, z2).tex(1.0D, 1.0D).normal(normalX, normalY, normalZ).endVertex();
        buffer.pos(x3, y3, z3).tex(1.0D, 0.0D).normal(normalX, normalY, normalZ).endVertex();
        buffer.pos(x4, y4, z4).tex(0.0D, 0.0D).normal(normalX, normalY, normalZ).endVertex();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityPortalProjectile entity) {
        return TEXTURE;
    }
}
