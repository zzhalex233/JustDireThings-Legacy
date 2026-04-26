package com.zzhalex.justdirethings.client.render;

import com.zzhalex.justdirethings.common.entity.EntityCreatureCatcher;
import com.zzhalex.justdirethings.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class RenderCreatureCatcher extends RenderSnowball<EntityCreatureCatcher> {

    public RenderCreatureCatcher(RenderManager renderManager) {
        super(renderManager, ModItems.CREATURE_CATCHER, Minecraft.getMinecraft().getRenderItem());
    }

    @Override
    public ItemStack getStackToRender(EntityCreatureCatcher entity) {
        ItemStack returnStack = entity.getReturnStack();
        return returnStack.isEmpty() ? new ItemStack(ModItems.CREATURE_CATCHER) : returnStack;
    }

    @Override
    public void doRender(EntityCreatureCatcher entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        if (!entity.hasHit()) {
            return;
        }

        Entity mob = entity.createCapturedEntity(entity.getReturnStack());
        if (!(mob instanceof EntityLivingBase)) {
            return;
        }

        EntityLivingBase living = (EntityLivingBase) mob;
        EntityCreatureCatcher.resetCapturedEntityVisualState(living);
        living.renderYawOffset = entity.getCapturedEntityBodyRot();
        living.prevRenderYawOffset = entity.getCapturedEntityBodyRot();
        living.rotationYawHead = entity.getCapturedEntityHeadRot();
        living.prevRotationYawHead = entity.getCapturedEntityHeadRot();

        double startX = entity.isCapturing() ? entity.getCapturedEntityX() - entity.posX : 0.0D;
        double startY = entity.isCapturing() ? entity.getCapturedEntityY() - entity.posY : 0.0D;
        double startZ = entity.isCapturing() ? entity.getCapturedEntityZ() - entity.posZ : 0.0D;
        float animationTicks = Math.min(entity.getShrinkingTime() + partialTicks, entity.getAnimationTicks());
        float fraction = entity.getAnimationTicks() <= 0 ? 1.0F : animationTicks / entity.getAnimationTicks();
        if (!entity.isCapturing()) {
            fraction = 1.0F - fraction;
        }
        fraction = MathHelper.cos(fraction * (float) Math.PI) * -0.5F + 0.5F;

        double renderX = x + startX + (0.0D - startX) * fraction;
        double renderY = y + startY + (0.0D - startY) * fraction + entity.height * 0.5D;
        double renderZ = z + startZ + (0.0D - startZ) * fraction;
        float scale = 1.0F + (0.2F - 1.0F) * fraction;

        GlStateManager.pushMatrix();
        GlStateManager.translate(renderX, renderY, renderZ);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(-renderX, -renderY, -renderZ);
        renderManager.renderEntity(living, renderX, renderY, renderZ, living.renderYawOffset, partialTicks, false);
        GlStateManager.popMatrix();
    }
}
