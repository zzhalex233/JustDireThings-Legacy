package com.zzhalex.justdirethings.client.render;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SideOnly(Side.CLIENT)
public enum ThingFinder {
    INSTANCE;

    private static final int SCAN_RADIUS = 10;
    private static final long SCAN_DURATION_MILLIS = 10000L;
    private static final long PARTICLE_INTERVAL_MILLIS = 500L;

    private static final List<BlockPos> oreBlocksList = new ArrayList<>();
    private static final List<Entity> entityList = new ArrayList<>();
    private static final Random RANDOM = new Random();

    private static long xRayStartTime;
    private static long blockParticlesStartTime;
    private static long entityParticlesStartTime;
    private static long lastBlockDrawTime;
    private static long lastEntityDrawTime;

    public static void discover(EntityPlayer player, Ability toolAbility, ItemStack stack) {
        if (player == null || player.world == null || toolAbility == null || stack == null || stack.isEmpty()) {
            return;
        }
        if (toolAbility == Ability.MOBSCANNER) {
            discoverMobs(player, true);
        } else if (toolAbility == Ability.ORESCANNER || toolAbility == Ability.OREXRAY) {
            discoverOres(player, toolAbility, stack);
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null || minecraft.player == null || minecraft.getRenderViewEntity() == null) {
            return;
        }

        EntityPlayer player = minecraft.player;
        long currentTime = System.currentTimeMillis();
        if (!oreBlocksList.isEmpty() && currentTime - xRayStartTime < SCAN_DURATION_MILLIS) {
            drawXRayOreBoxes(event);
        }
        if (!oreBlocksList.isEmpty() && currentTime - blockParticlesStartTime < SCAN_DURATION_MILLIS
                && currentTime - lastBlockDrawTime >= PARTICLE_INTERVAL_MILLIS) {
            drawParticlesOre(player);
            lastBlockDrawTime = currentTime;
        }
        if (!entityList.isEmpty() && currentTime - entityParticlesStartTime < SCAN_DURATION_MILLIS
                && currentTime - lastEntityDrawTime >= PARTICLE_INTERVAL_MILLIS) {
            discoverMobs(player, false);
            drawParticlesEntity(player);
            lastEntityDrawTime = currentTime;
        }
        expireOldResults(currentTime);
    }

    private static void discoverOres(EntityPlayer player, Ability toolAbility, ItemStack stack) {
        oreBlocksList.clear();
        BlockPos playerPos = player.getPosition();
        for (BlockPos blockPos : BlockPos.getAllInBox(playerPos.add(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),
                playerPos.add(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS))) {
            if (isValidBlock(blockPos, player, stack)) {
                oreBlocksList.add(blockPos.toImmutable());
            }
        }

        long currentTime = System.currentTimeMillis();
        if (toolAbility == Ability.OREXRAY) {
            xRayStartTime = currentTime;
        } else {
            blockParticlesStartTime = currentTime;
        }
    }

    private static boolean isValidBlock(BlockPos blockPos, EntityPlayer player, ItemStack stack) {
        IBlockState blockState = player.world.getBlockState(blockPos);
        if (!isOreBlock(blockState)) {
            return false;
        }
        return isCorrectToolForOre(blockState, stack);
    }

    private static boolean isOreBlock(IBlockState blockState) {
        Block block = blockState.getBlock();
        Item item = Item.getItemFromBlock(block);
        if (item == null) {
            return false;
        }
        ItemStack oreStack = new ItemStack(item, 1, block.getMetaFromState(blockState));
        if (oreStack.isEmpty()) {
            return false;
        }
        for (int id : OreDictionary.getOreIDs(oreStack)) {
            if (OreDictionary.getOreName(id).startsWith("ore")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isCorrectToolForOre(IBlockState blockState, ItemStack stack) {
        return stack == null || stack.isEmpty() || stack.canHarvestBlock(blockState) || stack.getDestroySpeed(blockState) > 1.0F;
    }

    private static void discoverMobs(EntityPlayer player, boolean startTimer) {
        entityList.clear();
        BlockPos playerPos = player.getPosition();
        AxisAlignedBB searchArea = new AxisAlignedBB(playerPos.add(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),
                playerPos.add(SCAN_RADIUS + 1, SCAN_RADIUS + 1, SCAN_RADIUS + 1));
        entityList.addAll(player.world.getEntitiesWithinAABB(EntityLiving.class, searchArea, entity -> entity instanceof IMob));
        if (startTimer) {
            entityParticlesStartTime = System.currentTimeMillis();
        }
    }

    private static void drawParticlesOre(EntityPlayer player) {
        for (BlockPos pos : oreBlocksList) {
            for (int i = 0; i < 2; i++) {
                player.world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY,
                        pos.getX() + RANDOM.nextDouble(),
                        pos.getY() + RANDOM.nextDouble(),
                        pos.getZ() + RANDOM.nextDouble(),
                        0.0D, 0.0D, 0.0D);
            }
        }
    }

    private static void drawParticlesEntity(EntityPlayer player) {
        for (Entity entity : entityList) {
            AxisAlignedBB bounds = entity.getEntityBoundingBox();
            for (int i = 0; i < 5; i++) {
                player.world.spawnParticle(EnumParticleTypes.PORTAL,
                        randomBetween(bounds.minX, bounds.maxX),
                        randomBetween(bounds.minY, bounds.maxY),
                        randomBetween(bounds.minZ, bounds.maxZ),
                        0.0D, 0.0D, 0.0D);
            }
        }
    }

    private static double randomBetween(double min, double max) {
        return min + (max - min) * RANDOM.nextDouble();
    }

    private static void drawXRayOreBoxes(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        Entity viewer = minecraft.getRenderViewEntity();
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.getPartialTicks();
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.getPartialTicks();
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.getPartialTicks();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-viewerX, -viewerY, -viewerZ);
        prepareXRayState();
        try {
            for (BlockPos pos : oreBlocksList) {
                drawOreBox(new AxisAlignedBB(pos).grow(0.002D));
            }
        } finally {
            restoreXRayState();
            GlStateManager.popMatrix();
        }
    }

    private static void prepareXRayState() {
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableDepth();
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

    private static void restoreXRayState() {
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glLineWidth(1.0F);
    }

    private static void drawOreBox(AxisAlignedBB box) {
        drawSolidBox(box, 50, 210, 255, 54);
        drawWireBox(box, 70, 255, 255, 210);
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

    private static void expireOldResults(long currentTime) {
        if (currentTime - xRayStartTime >= SCAN_DURATION_MILLIS && currentTime - blockParticlesStartTime >= SCAN_DURATION_MILLIS) {
            oreBlocksList.clear();
        }
        if (currentTime - entityParticlesStartTime >= SCAN_DURATION_MILLIS) {
            entityList.clear();
        }
    }
}
