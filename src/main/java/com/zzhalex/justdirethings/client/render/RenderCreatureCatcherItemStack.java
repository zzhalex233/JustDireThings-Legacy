package com.zzhalex.justdirethings.client.render;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.entity.EntityCreatureCatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderCreatureCatcherItemStack extends TileEntityItemStackRenderer {

    private static final ModelResourceLocation BASE_MODEL = new ModelResourceLocation(Reference.MOD_ID + ":creaturecatcher_base", "inventory");

    @Override
    public void renderByItem(ItemStack stack) {
        renderByItem(stack, Minecraft.getMinecraft().getRenderPartialTicks());
    }

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        renderBaseModel(stack);
        renderCapturedEntity(stack, partialTicks);
    }

    private void renderBaseModel(ItemStack stack) {
        Minecraft minecraft = Minecraft.getMinecraft();
        RenderItem renderItem = minecraft.getRenderItem();
        IBakedModel baseModel = renderItem.getItemModelMesher().getModelManager().getModel(BASE_MODEL);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        renderItem.renderItem(stack, baseModel);
        GlStateManager.popMatrix();
    }

    private void renderCapturedEntity(ItemStack stack, float partialTicks) {
        Minecraft minecraft = Minecraft.getMinecraft();
        Entity entity = EntityCreatureCatcher.createCapturedEntity(stack, minecraft.world);
        if (!(entity instanceof EntityLivingBase)) {
            return;
        }

        EntityLivingBase living = (EntityLivingBase) entity;
        EntityCreatureCatcher.resetCapturedEntityVisualState(living);
        living.setLocationAndAngles(0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
        living.renderYawOffset = 0.0F;
        living.prevRenderYawOffset = 0.0F;
        living.rotationYawHead = 0.0F;
        living.prevRotationYawHead = 0.0F;

        AxisAlignedBB bounds = living.getEntityBoundingBox();
        double height = Math.max(0.01D, bounds.maxY - bounds.minY);
        double width = Math.max(bounds.maxX - bounds.minX, bounds.maxZ - bounds.minZ);
        float scale = 0.5F / (float) Math.max(width, height);

        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        GlStateManager.translate(0.0F, (float) (-height * scale * 0.5D), 0.0F);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(-30.0F, 0.0F, 1.0F, 0.0F);

        RenderManager renderManager = minecraft.getRenderManager();
        boolean oldRenderShadow = renderManager.isRenderShadow();
        boolean depthWasEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        renderManager.setRenderShadow(false);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableDepth();
        renderManager.renderEntity(living, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, false);
        restoreDepthState(depthWasEnabled);
        renderManager.setRenderShadow(oldRenderShadow);
        GlStateManager.popMatrix();
    }

    private static void restoreDepthState(boolean depthWasEnabled) {
        if (depthWasEnabled) {
            GlStateManager.enableDepth();
        } else {
            GlStateManager.disableDepth();
        }
    }
}
