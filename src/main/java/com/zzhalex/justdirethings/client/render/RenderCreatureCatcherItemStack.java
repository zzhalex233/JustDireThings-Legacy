package com.zzhalex.justdirethings.client.render;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.entity.EntityCreatureCatcher;
import com.zzhalex.justdirethings.common.item.misc.ItemCreatureCatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderCreatureCatcherItemStack extends TileEntityItemStackRenderer {

    private static final ModelResourceLocation BASE_MODEL = new ModelResourceLocation(Reference.MOD_ID + ":creaturecatcher_base", "inventory");
    private static final ModelResourceLocation BOTTOM_MODEL = new ModelResourceLocation(Reference.MOD_ID + ":creaturecatcher_bottom", "inventory");
    private static final ModelResourceLocation SHIELD_MODEL = new ModelResourceLocation(Reference.MOD_ID + ":creaturecatcher_shield", "inventory");

    @Override
    public void renderByItem(ItemStack stack) {
        renderByItem(stack, Minecraft.getMinecraft().getRenderPartialTicks());
    }

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try {
            if (ItemCreatureCatcher.hasEntity(stack)) {
                renderBottomModel(stack);
                renderCapturedEntity(stack, partialTicks);
                renderShieldModel(stack);
            } else {
                renderBaseModel(stack);
            }
        } finally {
            GL11.glPopAttrib();
            syncGlStateManagerCache();
        }
    }

    private static void syncGlStateManagerCache() {
        forceSetToggle(GL11.glIsEnabled(GL11.GL_DEPTH_TEST), GlStateManager::enableDepth, GlStateManager::disableDepth);
        forceSetToggle(GL11.glIsEnabled(GL11.GL_BLEND), GlStateManager::enableBlend, GlStateManager::disableBlend);
        forceSetToggle(GL11.glIsEnabled(GL11.GL_CULL_FACE), GlStateManager::enableCull, GlStateManager::disableCull);
        forceSetToggle(GL11.glIsEnabled(GL11.GL_LIGHTING), GlStateManager::enableLighting, GlStateManager::disableLighting);
        forceSetToggle(GL11.glIsEnabled(GL11.GL_ALPHA_TEST), GlStateManager::enableAlpha, GlStateManager::disableAlpha);
        forceSetToggle(GL11.glIsEnabled(GL11.GL_FOG), GlStateManager::enableFog, GlStateManager::disableFog);
        forceSetToggle(GL11.glIsEnabled(GL11.GL_TEXTURE_2D), GlStateManager::enableTexture2D, GlStateManager::disableTexture2D);
        boolean dm = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        GlStateManager.depthMask(!dm);
        GlStateManager.depthMask(dm);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.color(0.0F, 0.0F, 0.0F, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.bindTexture(0);
    }

    private static void forceSetToggle(boolean desired, Runnable enable, Runnable disable) {
        if (desired) {
            disable.run();
            enable.run();
        } else {
            enable.run();
            disable.run();
        }
    }

    private void renderBaseModel(ItemStack stack) {
        renderModel(stack, BASE_MODEL);
    }

    private void renderBottomModel(ItemStack stack) {
        renderModel(stack, BOTTOM_MODEL);
    }

    private void renderShieldModel(ItemStack stack) {
        Minecraft minecraft = Minecraft.getMinecraft();
        IBakedModel model = minecraft.getRenderItem().getItemModelMesher()
                .getModelManager().getModel(SHIELD_MODEL);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        GlStateManager.enableBlend();
        GlStateManager.enableCull();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);
        GlStateManager.disableLighting();

        minecraft.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.5F, -0.5F, -0.5F);
        renderModelDirectly(model);
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    private static void renderModelDirectly(IBakedModel model) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
        for (EnumFacing face : EnumFacing.values()) {
            for (BakedQuad quad : model.getQuads(null, face, 0L)) {
                LightUtil.renderQuadColor(buffer, quad, 0xFFFFFFFF);
            }
        }
        for (BakedQuad quad : model.getQuads(null, null, 0L)) {
            LightUtil.renderQuadColor(buffer, quad, 0xFFFFFFFF);
        }
        tessellator.draw();
    }

    private void renderModel(ItemStack stack, ModelResourceLocation modelLocation) {
        Minecraft minecraft = Minecraft.getMinecraft();
        RenderItem renderItem = minecraft.getRenderItem();
        IBakedModel model = renderItem.getItemModelMesher().getModelManager().getModel(modelLocation);

        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        renderItem.renderItem(stack, model);
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
        GlStateManager.rotate(150.0F, 0.0F, 1.0F, 0.0F);

        RenderManager renderManager = minecraft.getRenderManager();
        boolean oldRenderShadow = renderManager.isRenderShadow();
        renderManager.setRenderShadow(false);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableCull();
        renderManager.renderEntity(living, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, false);
        renderManager.setRenderShadow(oldRenderShadow);
        GlStateManager.popMatrix();
    }
}
