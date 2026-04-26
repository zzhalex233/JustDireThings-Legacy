package com.zzhalex.justdirethings.client.render.tile;

import com.zzhalex.justdirethings.common.tile.base.MachineAreaState;
import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class RenderMachineArea extends TileEntitySpecialRenderer<TileMachineBase> {

    @Override
    public void render(TileMachineBase tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (tile == null || !tile.getAreaState().isRenderArea()) {
            return;
        }

        MachineAreaState areaState = tile.getAreaState();
        AxisAlignedBB area = areaState.createArea(BlockPos.ORIGIN);
        AxisAlignedBB offsetOnlyArea = areaState.createOffsetOnlyArea(BlockPos.ORIGIN);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);

        RenderGlobal.drawSelectionBoundingBox(area, 0.0F, 1.0F, 0.0F, 1.0F);
        drawSolidBox(area, 1.0F, 0.0F, 0.0F, 0.125F);
        if (areaState.getXRadius() > 0.0D || areaState.getYRadius() > 0.0D || areaState.getZRadius() > 0.0D) {
            RenderGlobal.drawSelectionBoundingBox(offsetOnlyArea, 1.0F, 1.0F, 1.0F, 1.0F);
            drawSolidBox(offsetOnlyArea, 0.0F, 0.0F, 1.0F, 0.125F);
        }

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    @Override
    public boolean isGlobalRenderer(TileMachineBase tile) {
        return true;
    }

    private static void drawSolidBox(AxisAlignedBB box, float red, float green, float blue, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        addFace(buffer, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ, red, green, blue, alpha);
        addFace(buffer, box.minX, box.maxY, box.minZ, box.minX, box.maxY, box.maxZ, box.maxX, box.maxY, box.maxZ, box.maxX, box.maxY, box.minZ, red, green, blue, alpha);
        addFace(buffer, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, box.maxX, box.minY, box.minZ, red, green, blue, alpha);
        addFace(buffer, box.minX, box.minY, box.maxZ, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ, red, green, blue, alpha);
        addFace(buffer, box.minX, box.minY, box.minZ, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ, red, green, blue, alpha);
        addFace(buffer, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, box.maxX, box.minY, box.maxZ, red, green, blue, alpha);
        tessellator.draw();
    }

    private static void addFace(BufferBuilder buffer, double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, float red, float green, float blue, float alpha) {
        buffer.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
        buffer.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
        buffer.pos(x3, y3, z3).color(red, green, blue, alpha).endVertex();
        buffer.pos(x4, y4, z4).color(red, green, blue, alpha).endVertex();
    }
}
