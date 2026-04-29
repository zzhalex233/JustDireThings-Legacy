package com.zzhalex.justdirethings.client.render.tile;

import com.zzhalex.justdirethings.common.tile.base.MachineAreaState;
import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public final class RenderMachineArea {

    private static final int PRIMARY_LINE_RED = 0;
    private static final int PRIMARY_LINE_GREEN = 255;
    private static final int PRIMARY_LINE_BLUE = 0;
    private static final int PRIMARY_FILL_RED = 255;
    private static final int PRIMARY_FILL_GREEN = 0;
    private static final int PRIMARY_FILL_BLUE = 0;
    private static final int OFFSET_LINE_RED = 255;
    private static final int OFFSET_LINE_GREEN = 255;
    private static final int OFFSET_LINE_BLUE = 255;
    private static final int OFFSET_FILL_RED = 0;
    private static final int OFFSET_FILL_GREEN = 0;
    private static final int OFFSET_FILL_BLUE = 255;
    private static final int LINE_ALPHA = 255;
    private static final int FILL_ALPHA = 32;

    private RenderMachineArea() {
    }

    public static void prepareGlState() {
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        GlStateManager.depthMask(false);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void restoreGlState() {
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glLineWidth(1.0F);
    }

    public static void render(TileMachineBase tile) {
        if (tile == null || !tile.getAreaState().isRenderArea()) {
            return;
        }

        drawMachineArea(tile.getAreaState(), tile.getPos());
    }

    public static void drawMachineArea(MachineAreaState areaState, BlockPos origin) {
        AxisAlignedBB area = areaState.createArea(origin);
        AxisAlignedBB offsetOnlyArea = areaState.createOffsetOnlyArea(origin);
        boolean hasAreaRadius = areaState.getXRadius() > 0.0D
                || areaState.getYRadius() > 0.0D
                || areaState.getZRadius() > 0.0D;

        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glPolygonOffset(-1.0F, -10.0F);
        drawSolidBox(area, PRIMARY_FILL_RED, PRIMARY_FILL_GREEN, PRIMARY_FILL_BLUE, FILL_ALPHA);
        if (hasAreaRadius) {
            drawSolidBox(offsetOnlyArea, OFFSET_FILL_RED, OFFSET_FILL_GREEN, OFFSET_FILL_BLUE, FILL_ALPHA);
        }
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);

        drawWireBox(area, PRIMARY_LINE_RED, PRIMARY_LINE_GREEN, PRIMARY_LINE_BLUE, LINE_ALPHA);
        if (hasAreaRadius) {
            drawWireBox(offsetOnlyArea, OFFSET_LINE_RED, OFFSET_LINE_GREEN, OFFSET_LINE_BLUE, LINE_ALPHA);
        }
    }

    private static void drawWireBox(AxisAlignedBB box, int red, int green, int blue, int alpha) {
        GL11.glLineWidth(2.0F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        addLine(buffer, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, red, green, blue, alpha);
        addLine(buffer, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, red, green, blue, alpha);
        addLine(buffer, box.minX, box.minY, box.minZ, box.minX, box.minY, box.maxZ, red, green, blue, alpha);
        addLine(buffer, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, red, green, blue, alpha);
        addLine(buffer, box.maxX, box.maxY, box.minZ, box.minX, box.maxY, box.minZ, red, green, blue, alpha);
        addLine(buffer, box.minX, box.maxY, box.minZ, box.minX, box.maxY, box.maxZ, red, green, blue, alpha);
        addLine(buffer, box.minX, box.minY, box.maxZ, box.maxX, box.minY, box.maxZ, red, green, blue, alpha);
        addLine(buffer, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha);
        addLine(buffer, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ, red, green, blue, alpha);
        addLine(buffer, box.minX, box.maxY, box.maxZ, box.minX, box.minY, box.maxZ, red, green, blue, alpha);
        addLine(buffer, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, red, green, blue, alpha);
        addLine(buffer, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha);
        tessellator.draw();
    }

    private static void drawSolidBox(AxisAlignedBB box, int red, int green, int blue, int alpha) {
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

    private static void addFace(BufferBuilder buffer, double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, int red, int green, int blue, int alpha) {
        buffer.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
        buffer.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
        buffer.pos(x3, y3, z3).color(red, green, blue, alpha).endVertex();
        buffer.pos(x4, y4, z4).color(red, green, blue, alpha).endVertex();
    }

    private static void addLine(BufferBuilder buffer, double x1, double y1, double z1, double x2, double y2, double z2, int red, int green, int blue, int alpha) {
        buffer.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
        buffer.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
    }
}
