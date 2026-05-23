package com.zzhalex.justdirethings.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unchecked")
public final class ClientPlayerPreviewRenderer {

    private static final float PLAYER_MODEL_SCALE = 0.9375F;

    private ClientPlayerPreviewRenderer() {
    }

    public static void renderInventoryHolderPlayer(AbstractClientPlayer player, double x, double y, double z, float partialTicks) {
        float rotation = (System.currentTimeMillis() % 7200L) / 20.0F;
        player.setLocationAndAngles(0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
        player.rotationYaw = 0.0F;
        player.prevRotationYaw = 0.0F;
        player.rotationPitch = 0.0F;
        player.prevRotationPitch = 0.0F;
        player.renderYawOffset = 0.0F;
        player.prevRenderYawOffset = 0.0F;
        player.rotationYawHead = 0.0F;
        player.prevRotationYawHead = 0.0F;

        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        boolean oldRenderShadow = renderManager.isRenderShadow();
        float previousBrightnessX = OpenGlHelper.lastBrightnessX;
        float previousBrightnessY = OpenGlHelper.lastBrightnessY;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableTexture2D();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            GlStateManager.translate(x + 0.5D, y + 1.0D, z + 0.5D);
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.rotate(rotation, 0.0F, 1.0F, 0.0F);
            RenderHelper.enableStandardItemLighting();

            renderManager.setRenderShadow(false);
            renderManager.renderEntity(player, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, false);
        } finally {
            renderManager.setRenderShadow(oldRenderShadow);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, previousBrightnessX, previousBrightnessY);
            GlStateManager.disableRescaleNormal();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
            GL11.glPopAttrib();
            RenderStateHelper.syncGlStateManagerCache();
        }
    }

    public static void renderTransparentLandingPlayer(AbstractClientPlayer player, double x, double y, double z, float partialTicks, float alpha) {
        Minecraft minecraft = Minecraft.getMinecraft();
        Render<AbstractClientPlayer> renderer = minecraft.getRenderManager().getEntityRenderObject(player);
        if (!(renderer instanceof RenderPlayer)) {
            return;
        }

        RenderPlayer renderPlayer = (RenderPlayer) renderer;
        ModelPlayer model = renderPlayer.getMainModel();
        float previousBrightnessX = OpenGlHelper.lastBrightnessX;
        float previousBrightnessY = OpenGlHelper.lastBrightnessY;
        boolean oldRenderShadow = minecraft.getRenderManager().isRenderShadow();

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GlStateManager.pushMatrix();
        try {
            minecraft.getRenderManager().setRenderShadow(false);
            applyLandingLight(player.world, x, y, z);
            minecraft.getTextureManager().bindTexture(renderPlayer.getEntityTexture(player));

            GlStateManager.translate(x, y, z);
            float bodyYaw = interpolateRotation(player.prevRenderYawOffset, player.renderYawOffset, partialTicks);
            float headYaw = interpolateRotation(player.prevRotationYawHead, player.rotationYawHead, partialTicks);
            float netHeadYaw = headYaw - bodyYaw;
            float headPitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;

            GlStateManager.rotate(180.0F - bodyYaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.scale(-1.0F, -1.0F, 1.0F);
            GlStateManager.scale(PLAYER_MODEL_SCALE, PLAYER_MODEL_SCALE, PLAYER_MODEL_SCALE);
            GlStateManager.translate(0.0F, -1.501F, 0.0F);

            float limbSwingAmount = 0.0F;
            float limbSwing = 0.0F;
            if (player.isEntityAlive()) {
                limbSwingAmount = player.prevLimbSwingAmount + (player.limbSwingAmount - player.prevLimbSwingAmount) * partialTicks;
                limbSwing = player.limbSwing - player.limbSwingAmount * (1.0F - partialTicks);
                if (player.isChild()) {
                    limbSwing *= 3.0F;
                }
                if (limbSwingAmount > 1.0F) {
                    limbSwingAmount = 1.0F;
                }
            }

            syncPlayerModelState(model, player);
            model.swingProgress = 0.0F;
            model.isRiding = false;
            model.isChild = player.isChild();
            model.setLivingAnimations(player, limbSwing, limbSwingAmount, partialTicks);
            model.setRotationAngles(limbSwing, limbSwingAmount, 0.0F, netHeadYaw, headPitch, 0.0625F, player);

            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableLighting();
            GlStateManager.depthMask(false);
            GlStateManager.tryBlendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO
            );
            GlStateManager.color(1.0F, 1.0F, 1.0F, MathHelper.clamp(alpha, 0.0F, 1.0F));
            model.render(player, limbSwing, limbSwingAmount, 0.0F, netHeadYaw, headPitch, 0.0625F);
        } finally {
            minecraft.getRenderManager().setRenderShadow(oldRenderShadow);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, previousBrightnessX, previousBrightnessY);
            GlStateManager.depthMask(true);
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
            GL11.glPopAttrib();
            RenderStateHelper.syncGlStateManagerCache();
        }
    }

    private static void syncPlayerModelState(ModelPlayer model, AbstractClientPlayer player) {
        if (player.isSpectator()) {
            model.setVisible(false);
            model.bipedHead.showModel = true;
            model.bipedHeadwear.showModel = true;
            return;
        }

        model.setVisible(true);
        model.bipedHeadwear.showModel = player.isWearing(EnumPlayerModelParts.HAT);
        model.bipedBodyWear.showModel = player.isWearing(EnumPlayerModelParts.JACKET);
        model.bipedLeftLegwear.showModel = player.isWearing(EnumPlayerModelParts.LEFT_PANTS_LEG);
        model.bipedRightLegwear.showModel = player.isWearing(EnumPlayerModelParts.RIGHT_PANTS_LEG);
        model.bipedLeftArmwear.showModel = player.isWearing(EnumPlayerModelParts.LEFT_SLEEVE);
        model.bipedRightArmwear.showModel = player.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE);
        model.isSneak = player.isSneaking();
        model.leftArmPose = ModelBiped.ArmPose.EMPTY;
        model.rightArmPose = ModelBiped.ArmPose.EMPTY;
    }

    private static void applyLandingLight(World world, double x, double y, double z) {
        if (world == null) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            return;
        }
        int light = world.getCombinedLight(new BlockPos(x, y, z), 0);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light % 65536, light / 65536);
    }

    private static float interpolateRotation(float previous, float current, float partialTicks) {
        float delta = current - previous;
        while (delta < -180.0F) {
            delta += 360.0F;
        }
        while (delta >= 180.0F) {
            delta -= 360.0F;
        }
        return previous + partialTicks * delta;
    }
}
