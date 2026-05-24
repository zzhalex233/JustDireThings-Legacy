package com.zzhalex.justdirethings.client.event;

import com.zzhalex.justdirethings.client.render.tile.RenderGooBlock;
import com.zzhalex.justdirethings.common.goo.CustomGooRuntime;
import com.zzhalex.justdirethings.common.recipe.custom.GooCatalystRegistry;
import com.zzhalex.justdirethings.common.recipe.custom.JDTBlockStateSpec;
import com.zzhalex.justdirethings.common.tile.goo.TileGooBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.Set;

@SideOnly(Side.CLIENT)
public enum CustomGooClientRenderHandler {
    INSTANCE;

    private static final int RENDER_RADIUS = 64;

    private final RenderGooBlock renderer = new RenderGooBlock();

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null || minecraft.getRenderViewEntity() == null) {
            return;
        }

        Entity viewer = minecraft.getRenderViewEntity();
        Set<BlockPos> visibleDeadTiles = new HashSet<>();
        TileGooBlock.Custom targetTile = targetedCustomGooTile(minecraft);
        if (targetTile != null) {
            visibleDeadTiles.add(targetTile.getPos());
        }
        CustomGooRuntime.pruneDeadRenderTiles(minecraft.world, visibleDeadTiles);

        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.getPartialTicks();
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.getPartialTicks();
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.getPartialTicks();

        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(-viewerX, -viewerY, -viewerZ);
            Set<BlockPos> rendered = new HashSet<>();
            BlockPos targetPos = targetTile == null ? null : targetTile.getPos();
            for (TileGooBlock.Custom tile : CustomGooRuntime.renderTiles(minecraft.world, viewer.getPosition(), RENDER_RADIUS)) {
                renderTile(tile, event.getPartialTicks(), rendered, tile.getPos().equals(targetPos));
            }
            if (targetTile != null) {
                renderTile(targetTile, event.getPartialTicks(), rendered, true);
            }
        } finally {
            GlStateManager.popMatrix();
        }
    }

    private TileGooBlock.Custom targetedCustomGooTile(Minecraft minecraft) {
        RayTraceResult hit = minecraft.objectMouseOver;
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK || hit.getBlockPos() == null) {
            return null;
        }

        BlockPos pos = hit.getBlockPos();
        IBlockState state = minecraft.world.getBlockState(pos);
        if (!GooCatalystRegistry.isCustomGoo(JDTBlockStateSpec.fromState(state))) {
            return null;
        }
        return CustomGooRuntime.getOrCreate(minecraft.world, pos);
    }

    private void renderTile(TileGooBlock.Custom tile, float partialTicks, Set<BlockPos> rendered, boolean renderRevivalItems) {
        BlockPos pos = tile.getPos();
        if (rendered.add(pos)) {
            renderer.renderCustom(tile, pos.getX(), pos.getY(), pos.getZ(), partialTicks, renderRevivalItems);
        }
    }
}
