package com.zzhalex.justdirethings.client.render;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public enum ThingFinder {
    INSTANCE;

    private static final int SCAN_RADIUS = 10;
    private static final long SCAN_DURATION_MILLIS = 10000L;
    private static final long PARTICLE_INTERVAL_MILLIS = 500L;

    private static final List<BlockPos> oreBlocksList = new ArrayList<>();
    private static final List<Entity> entityList = new ArrayList<>();

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
            drawXRayOreBlocks(event);
        }
        if (!oreBlocksList.isEmpty() && currentTime - blockParticlesStartTime < SCAN_DURATION_MILLIS
                && currentTime - lastBlockDrawTime >= PARTICLE_INTERVAL_MILLIS) {
            AlwaysVisibleParticleHelper.addBlockParticles(event, oreBlocksList, 2, 90, 255, 90, 220);
            lastBlockDrawTime = currentTime;
        }
        if (!entityList.isEmpty() && currentTime - entityParticlesStartTime < SCAN_DURATION_MILLIS
                && currentTime - lastEntityDrawTime >= PARTICLE_INTERVAL_MILLIS) {
            discoverMobs(player, false);
            AlwaysVisibleParticleHelper.addEntityParticles(event, entityList, 5, 120, 220, 255, 230);
            lastEntityDrawTime = currentTime;
        }
        AlwaysVisibleParticleHelper.renderActive(event);
        expireOldResults(currentTime);
    }

    private static void discoverOres(EntityPlayer player, Ability toolAbility, ItemStack stack) {
        oreBlocksList.clear();
        BlockPos playerPos = getPlayerOnPos(player);
        for (BlockPos blockPos : BlockPos.getAllInBox(playerPos.add(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),
                playerPos.add(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS))) {
            if (isValidBlock(blockPos, player, stack)) {
                oreBlocksList.add(blockPos.toImmutable());
            }
        }

        long currentTime = System.currentTimeMillis();
        if (toolAbility == Ability.OREXRAY) {
            xRayStartTime = currentTime;
        } else if (toolAbility == Ability.ORESCANNER) {
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
        BlockPos playerPos = getPlayerOnPos(player);
        AxisAlignedBB searchArea = new AxisAlignedBB(playerPos.add(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),
                playerPos.add(SCAN_RADIUS + 1, SCAN_RADIUS + 1, SCAN_RADIUS + 1));
        entityList.addAll(player.world.getEntitiesWithinAABB(EntityLiving.class, searchArea, entity -> entity instanceof IMob));
        if (startTimer) {
            entityParticlesStartTime = System.currentTimeMillis();
        }
    }

    private static void drawXRayOreBlocks(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        Entity viewer = minecraft.getRenderViewEntity();
        World world = minecraft.world;
        if (viewer == null || world == null) {
            return;
        }

        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.getPartialTicks();
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.getPartialTicks();
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.getPartialTicks();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-viewerX, -viewerY, -viewerZ);
        prepareXRayState();
        try {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            minecraft.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            for (BlockPos pos : oreBlocksList) {
                IBlockState state = world.getBlockState(pos);
                if (!isOreBlock(state)) {
                    continue;
                }
                GlStateManager.pushMatrix();
                GlStateManager.translate(pos.getX() + 0.0005D, pos.getY() + 0.0005D, pos.getZ() + 0.0005D);
                GlStateManager.scale(0.999F, 0.999F, 0.999F);
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
                renderXRayModelQuads(buffer, minecraft.getBlockRendererDispatcher().getModelForState(state), state);
                tessellator.draw();
                GlStateManager.popMatrix();
            }
        } finally {
            restoreXRayState();
            GlStateManager.popMatrix();
        }
    }

    private static void prepareXRayState() {
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
        GlStateManager.depthFunc(GL11.GL_GREATER);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        GlStateManager.depthMask(false);
        GlStateManager.color(1.35F, 1.35F, 1.35F, 0.82F);
    }

    private static void restoreXRayState() {
        GlStateManager.depthMask(true);
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glLineWidth(1.0F);
    }

    private static void renderXRayModelQuads(BufferBuilder buffer, IBakedModel model, IBlockState state) {
        for (EnumFacing face : EnumFacing.values()) {
            for (BakedQuad quad : model.getQuads(state, face, 0L)) {
                LightUtil.renderQuadColor(buffer, quad, 0xD0FFFFFF);
            }
        }
        for (BakedQuad quad : model.getQuads(state, null, 0L)) {
            LightUtil.renderQuadColor(buffer, quad, 0xD0FFFFFF);
        }
    }

    private static void expireOldResults(long currentTime) {
        if (currentTime - xRayStartTime >= SCAN_DURATION_MILLIS && currentTime - blockParticlesStartTime >= SCAN_DURATION_MILLIS) {
            oreBlocksList.clear();
        }
        if (currentTime - entityParticlesStartTime >= SCAN_DURATION_MILLIS) {
            entityList.clear();
        }
    }

    private static BlockPos getPlayerOnPos(EntityPlayer player) {
        return new BlockPos(player.posX, player.posY - 0.2D, player.posZ);
    }
}
