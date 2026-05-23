package com.zzhalex.justdirethings.client.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
final class RenderStateHelper {

    private RenderStateHelper() {
    }

    static void syncGlStateManagerCache() {
        forceSetToggle(GL11.glIsEnabled(GL11.GL_DEPTH_TEST), GlStateManager::enableDepth, GlStateManager::disableDepth);
        forceSetToggle(GL11.glIsEnabled(GL11.GL_BLEND), GlStateManager::enableBlend, GlStateManager::disableBlend);
        forceSetToggle(GL11.glIsEnabled(GL11.GL_CULL_FACE), GlStateManager::enableCull, GlStateManager::disableCull);
        forceSetToggle(GL11.glIsEnabled(GL11.GL_LIGHTING), GlStateManager::enableLighting, GlStateManager::disableLighting);
        forceSetToggle(GL11.glIsEnabled(GL11.GL_ALPHA_TEST), GlStateManager::enableAlpha, GlStateManager::disableAlpha);
        forceSetToggle(GL11.glIsEnabled(GL11.GL_FOG), GlStateManager::enableFog, GlStateManager::disableFog);
        forceSetToggle(GL11.glIsEnabled(GL11.GL_TEXTURE_2D), GlStateManager::enableTexture2D, GlStateManager::disableTexture2D);
        boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        GlStateManager.depthMask(!depthMask);
        GlStateManager.depthMask(depthMask);
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
}
