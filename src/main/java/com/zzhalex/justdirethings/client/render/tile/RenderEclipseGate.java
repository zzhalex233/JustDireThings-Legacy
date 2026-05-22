package com.zzhalex.justdirethings.client.render.tile;

import com.zzhalex.justdirethings.common.tile.TileEclipseGate;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEclipseGate extends TileEntitySpecialRenderer<TileEclipseGate> {

    private static final ResourceLocation END_PORTAL_TEXTURE = new ResourceLocation("textures/entity/end_portal.png");
    private static final int ANIMATION_TICKS = 80;
    private static final float ZERO_SMALL = 0.484375F;
    private static final float ONE_SMALL = 0.515625F;
    private static final float ZERO_BIG = 0.46875F;
    private static final float ONE_BIG = 0.53125F;

    @Override
    public void render(TileEclipseGate tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (tile == null || tile.getWorld() == null) {
            return;
        }

        float age = Math.floorMod(tile.getWorld().getTotalWorldTime(), ANIMATION_TICKS) + partialTicks;
        float lerp = age / ANIMATION_TICKS;
        float zero;
        float one;
        if (age < ANIMATION_TICKS / 2.0F) {
            zero = (float) MathHelper.clampedLerp(ZERO_SMALL, ZERO_BIG, lerp);
            one = (float) MathHelper.clampedLerp(ONE_SMALL, ONE_BIG, lerp);
        } else {
            zero = (float) MathHelper.clampedLerp(ZERO_BIG, ZERO_SMALL, lerp);
            one = (float) MathHelper.clampedLerp(ONE_BIG, ONE_SMALL, lerp);
        }

        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(x, y, z);
            renderPortalCube(zero, one);
        } finally {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean isGlobalRenderer(TileEclipseGate tile) {
        return true;
    }

    private void renderPortalCube(float zero, float one) {
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        bindTexture(END_PORTAL_TEXTURE);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        renderCube(buffer, zero, one);
        tessellator.draw();
    }

    private void renderCube(BufferBuilder buffer, float zero, float one) {
        float center = 0.5F;
        float diff = one - zero;
        renderFace(buffer, zero, one, center, center + diff, one, one, one, one);
        renderFace(buffer, zero, one, center + diff, center, zero, zero, zero, zero);
        renderFace(buffer, one, one, center + diff, center, zero, one, one, zero);
        renderFace(buffer, zero, zero, center, center + diff, zero, one, one, zero);
        renderFace(buffer, zero, one, center, center, zero, zero, one, one);
        renderFace(buffer, zero, one, center + diff, center + diff, one, one, zero, zero);
    }

    private void renderFace(BufferBuilder buffer, float x1, float x2, float y1, float y2, float z1, float z2, float z3, float z4) {
        vertex(buffer, x1, y1, z1, 0.0F, 0.0F);
        vertex(buffer, x2, y1, z2, 1.0F, 0.0F);
        vertex(buffer, x2, y2, z3, 1.0F, 1.0F);
        vertex(buffer, x1, y2, z4, 0.0F, 1.0F);
    }

    private void vertex(BufferBuilder buffer, float x, float y, float z, float u, float v) {
        buffer.pos(x, y, z).tex(u, v).color(255, 255, 255, 255).endVertex();
    }
}
