package com.zzhalex.justdirethings.client.render;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.entity.EntityPortalProjectile;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderPortalProjectile extends Render<EntityPortalProjectile> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/entity/portal_projectile.png");
    private static final float MODEL_SCALE = 1.0F / 16.0F;
    private final ModelPortalProjectile model = new ModelPortalProjectile();

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
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
        float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
        model.render(entity, 0.0F, 0.0F, 0.0F, yaw, pitch, MODEL_SCALE);

        GlStateManager.disableBlend();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityPortalProjectile entity) {
        return TEXTURE;
    }
}
