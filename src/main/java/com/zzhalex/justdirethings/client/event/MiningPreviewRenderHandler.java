package com.zzhalex.justdirethings.client.event;

import com.zzhalex.justdirethings.common.event.ToolMiningAbilityHandler;
import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SideOnly(Side.CLIENT)
public enum MiningPreviewRenderHandler {
    INSTANCE;

    private static int miningTicks;
    private static BlockPos destroyPos = BlockPos.ORIGIN;
    private static Set<BlockPos> destroyPositions = Collections.emptySet();

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer player = minecraft.player;
        if (player == null || minecraft.world == null || minecraft.objectMouseOver == null || minecraft.objectMouseOver.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }

        ItemStack stack = player.getHeldItemMainhand();
        if (!shouldRenderPreview(stack)) {
            return;
        }

        BlockPos origin = minecraft.objectMouseOver.getBlockPos();
        IBlockState state = minecraft.world.getBlockState(origin);
        Set<BlockPos> breakPositions = ToolMiningAbilityHandler.getBreakBlockPositionsForPreview(stack, minecraft.world, origin, player, state);
        if (breakPositions.isEmpty()) {
            return;
        }

        double viewerX = minecraft.getRenderViewEntity().lastTickPosX + (minecraft.getRenderViewEntity().posX - minecraft.getRenderViewEntity().lastTickPosX) * event.getPartialTicks();
        double viewerY = minecraft.getRenderViewEntity().lastTickPosY + (minecraft.getRenderViewEntity().posY - minecraft.getRenderViewEntity().lastTickPosY) * event.getPartialTicks();
        double viewerZ = minecraft.getRenderViewEntity().lastTickPosZ + (minecraft.getRenderViewEntity().posZ - minecraft.getRenderViewEntity().lastTickPosZ) * event.getPartialTicks();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-viewerX, -viewerY, -viewerZ);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        GlStateManager.glLineWidth(2.0F);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);

        try {
            for (BlockPos pos : breakPositions) {
                if (pos.equals(origin)) {
                    continue;
                }
                IBlockState previewState = minecraft.world.getBlockState(pos);
                AxisAlignedBB box = previewState.getSelectedBoundingBox(minecraft.world, pos);
                if (box == null) {
                    continue;
                }
                drawOutline(box.grow(0.002D), 0.0F, 0.0F, 0.0F, 0.4F);
            }
        } finally {
            GlStateManager.glLineWidth(1.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.depthMask(true);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer player = minecraft.player;
        World world = minecraft.world;
        if (player == null || world == null) {
            clearDestroyProgress(player, world);
            destroyPos = BlockPos.ORIGIN;
            miningTicks = 0;
            return;
        }

        ItemStack stack = player.getHeldItemMainhand();
        if (!shouldRenderCrumbling(stack)) {
            clearDestroyProgress(player, world);
            destroyPos = BlockPos.ORIGIN;
            miningTicks = 0;
            return;
        }

        RayTraceResult target = minecraft.objectMouseOver;
        boolean attacking = minecraft.gameSettings != null && minecraft.gameSettings.keyBindAttack.isKeyDown();
        if (!attacking || target == null || target.typeOfHit != RayTraceResult.Type.BLOCK || target.getBlockPos() == null) {
            clearDestroyProgress(player, world);
            destroyPos = BlockPos.ORIGIN;
            miningTicks = 0;
            return;
        }

        BlockPos blockPos = target.getBlockPos();
        if (blockPos.equals(destroyPos)) {
            miningTicks++;
        } else {
            clearDestroyProgress(player, world);
            destroyPos = blockPos.toImmutable();
            miningTicks = 0;
        }
        incrementDestroyProgress(player, world, stack, blockPos);
    }

    private static void incrementDestroyProgress(EntityPlayer player, World world, ItemStack stack, BlockPos origin) {
        IBlockState state = world.getBlockState(origin);
        Set<BlockPos> breakPositions = ToolMiningAbilityHandler.getBreakBlockPositionsForPreview(stack, world, origin, player, state);
        if (breakPositions.isEmpty()) {
            clearDestroyProgress(player, world);
            return;
        }
        destroyPositions = new HashSet<>(breakPositions);

        float progress = state.getPlayerRelativeBlockHardness(player, world, origin) * (miningTicks + 1);
        int stage = (int) (progress * 10.0F);
        for (BlockPos pos : breakPositions) {
            if (pos.equals(origin)) {
                continue;
            }
            world.sendBlockBreakProgress(player.getEntityId() + generatePosHash(pos), pos, stage);
        }
    }

    private static void clearDestroyProgress(EntityPlayer player, World world) {
        if (player == null || world == null || destroyPositions.isEmpty()) {
            return;
        }
        for (BlockPos pos : destroyPositions) {
            if (pos.equals(destroyPos)) {
                continue;
            }
            world.sendBlockBreakProgress(player.getEntityId() + generatePosHash(pos), pos, -1);
        }
        destroyPositions = Collections.emptySet();
    }

    private static boolean shouldRenderPreview(ItemStack stack) {
        return shouldPreviewAnyMultiBreak(stack);
    }

    private static boolean shouldRenderCrumbling(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ToggleableTool)) {
            return false;
        }
        return hasEnabledAbility(stack, Ability.HAMMER) || hasEnabledAbility(stack, Ability.OREMINER);
    }

    private static boolean shouldPreviewAnyMultiBreak(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ToggleableTool)) {
            return false;
        }
        return hasEnabledAbility(stack, Ability.HAMMER)
                || hasEnabledAbility(stack, Ability.OREMINER)
                || hasEnabledAbility(stack, Ability.TREEFELLER)
                || hasEnabledAbility(stack, Ability.SKYSWEEPER);
    }

    private static boolean hasEnabledAbility(ItemStack stack, Ability ability) {
        ToggleableTool tool = (ToggleableTool) stack.getItem();
        return tool.supportsAbility(ability)
                && tool.hasInstalledAbility(stack, ability)
                && tool.getSetting(stack, ability);
    }

    private static int generatePosHash(BlockPos blockPos) {
        return (31 * 31 * blockPos.getX()) + (31 * blockPos.getY()) + blockPos.getZ();
    }

    private static void drawOutline(AxisAlignedBB box, float red, float green, float blue, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        addLine(buffer, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, red, green, blue, alpha);
        addLine(buffer, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, red, green, blue, alpha);
        addLine(buffer, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ, red, green, blue, alpha);
        addLine(buffer, box.minX, box.minY, box.maxZ, box.minX, box.minY, box.minZ, red, green, blue, alpha);
        addLine(buffer, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, red, green, blue, alpha);
        addLine(buffer, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha);
        addLine(buffer, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ, red, green, blue, alpha);
        addLine(buffer, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ, red, green, blue, alpha);
        addLine(buffer, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, red, green, blue, alpha);
        addLine(buffer, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, red, green, blue, alpha);
        addLine(buffer, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha);
        addLine(buffer, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, red, green, blue, alpha);
        tessellator.draw();
    }

    private static void addLine(BufferBuilder buffer, double x1, double y1, double z1, double x2, double y2, double z2,
                                float red, float green, float blue, float alpha) {
        buffer.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
        buffer.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
    }
}
