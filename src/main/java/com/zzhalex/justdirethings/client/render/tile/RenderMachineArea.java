package com.zzhalex.justdirethings.client.render.tile;

import com.zzhalex.justdirethings.common.tile.base.MachineAreaState;
import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import com.zzhalex.justdirethings.common.tile.machine.TileParadoxMachine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Map;

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

    public static void render(TileMachineBase tile, float partialTicks) {
        if (tile == null) {
            return;
        }

        if (tile.getAreaState().isRenderArea()) {
            drawMachineArea(tile.getAreaState(), tile.getPos());
        }
        if (tile instanceof TileParadoxMachine) {
            renderParadoxPreview((TileParadoxMachine) tile, partialTicks);
        }
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

    private static void renderParadoxPreview(TileParadoxMachine tile, float partialTicks) {
        if (!tile.isRunning() && !tile.shouldRenderParadox()) {
            return;
        }

        float alpha = tile.isRunning()
                ? MathHelper.clamp(0.05F + (tile.getTimeRunning() / (float) Math.max(1, tile.getRunTime())) * 0.95F, 0.05F, 1.0F)
                : 0.5F;
        Map<BlockPos, IBlockState> blockStates = tile.getPreviewBlockStates();
        if (!blockStates.isEmpty()) {
            renderParadoxBlocks(blockStates, alpha);
        }

        Map<Vec3d, EntityLivingBase> entities = tile.getPreviewEntities();
        if (!entities.isEmpty()) {
            int entityAlpha = tile.isRunning() ? MathHelper.clamp((int) (alpha * 255.0F), 16, 255) : 175;
            renderParadoxEntities(entities, partialTicks, entityAlpha);
        }
    }

    private static void renderParadoxBlocks(Map<BlockPos, IBlockState> blockStates, float alpha) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.world == null) {
            return;
        }

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(false);
        minecraft.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        int packedColor = (MathHelper.clamp((int) (alpha * 255.0F), 0, 255) << 24) | 0x00FFFFFF;
        for (Map.Entry<BlockPos, IBlockState> entry : blockStates.entrySet()) {
            BlockPos blockPos = entry.getKey();
            IBlockState state = entry.getValue();
            IBakedModel model = minecraft.getBlockRendererDispatcher().getModelForState(state);
            GlStateManager.pushMatrix();
            GlStateManager.translate(blockPos.getX() + 0.0005D, blockPos.getY() + 0.0005D, blockPos.getZ() + 0.0005D);
            GlStateManager.scale(0.999F, 0.999F, 0.999F);
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
            renderBlockModelQuads(buffer, model, state, packedColor);
            tessellator.draw();
            GlStateManager.popMatrix();
        }
        GlStateManager.disableTexture2D();
    }

    private static void renderBlockModelQuads(BufferBuilder buffer, IBakedModel model, IBlockState state, int packedColor) {
        for (EnumFacing face : EnumFacing.values()) {
            for (BakedQuad quad : model.getQuads(state, face, 0L)) {
                LightUtil.renderQuadColor(buffer, quad, packedColor);
            }
        }
        for (BakedQuad quad : model.getQuads(state, null, 0L)) {
            LightUtil.renderQuadColor(buffer, quad, packedColor);
        }
    }

    private static void renderParadoxEntities(Map<Vec3d, EntityLivingBase> entities, float partialTicks, int alpha) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.world == null) {
            return;
        }

        RenderManager renderManager = minecraft.getRenderManager();
        boolean oldRenderShadow = renderManager.isRenderShadow();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);
        RenderHelper.enableStandardItemLighting();
        renderManager.setRenderShadow(false);
        try {
            for (Map.Entry<Vec3d, EntityLivingBase> entry : entities.entrySet()) {
                EntityLivingBase entity = entry.getValue();
                Vec3d position = entry.getKey();
                if (entity == null || position == null) {
                    continue;
                }
                entity.setLocationAndAngles(position.x, position.y, position.z, entity.rotationYaw, entity.rotationPitch);
                entity.prevPosX = entity.posX;
                entity.prevPosY = entity.posY;
                entity.prevPosZ = entity.posZ;
                GlStateManager.color(1.0F, 1.0F, 1.0F, alpha / 255.0F);
                renderManager.renderEntity(entity, position.x, position.y, position.z, entity.renderYawOffset, partialTicks, false);
            }
        } finally {
            renderManager.setRenderShadow(oldRenderShadow);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            RenderHelper.disableStandardItemLighting();
            prepareGlState();
        }
    }

}
