package com.zzhalex.justdirethings.client.render.tile;

import com.zzhalex.justdirethings.common.block.goo.BlockGooBlock;
import com.zzhalex.justdirethings.common.block.goo.BlockGooPattern;
import com.zzhalex.justdirethings.common.tile.goo.TileGooBlock;
import com.zzhalex.justdirethings.registry.ModContentBlocks;
import com.zzhalex.justdirethings.registry.ModContentItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class RenderGooBlock extends TileEntitySpecialRenderer<TileGooBlock> {

    private static final float PERCENTAGE_DIVISOR = 100.0F / BlockGooPattern.GOOSTAGE.getAllowedValues().size();

    private ItemStack cachedItemStack = ItemStack.EMPTY;
    private int currentItemIndex;
    private long lastChangeTime;

    @Override
    public void render(TileGooBlock blockentity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (blockentity == null || blockentity.getWorld() == null) {
            return;
        }

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(x, y, z);
            IBlockState blockState = blockentity.getWorld().getBlockState(blockentity.getPos());

            if (blockState.getBlock() instanceof BlockGooBlock && !blockState.getValue(BlockGooBlock.ALIVE)) {
                renderFloatingItem(blockentity, partialTicks);
            }

            for (EnumFacing direction : EnumFacing.values()) {
                int remainingTicks = blockentity.getRemainingTimeFor(direction);
                if (remainingTicks > 0) {
                    int maxTicks = blockentity.getCraftingDuration(direction);
                    renderTextures(direction, blockentity, partialTicks, remainingTicks, maxTicks);
                }
            }
        } finally {
            GlStateManager.popMatrix();
            GL11.glPopAttrib();
            syncGlStateManagerCache();
        }
    }

    @Override
    public boolean isGlobalRenderer(TileGooBlock blockentity) {
        return true;
    }

    private ItemStack getNextItemFromTier(int tier) {
        List<ItemStack> items = revivalItemsForTier(tier);
        if (items.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (currentItemIndex >= items.size()) {
            currentItemIndex = 0;
        }
        ItemStack next = items.get(currentItemIndex).copy();
        currentItemIndex = (currentItemIndex + 1) % items.size();
        return next;
    }

    private void renderFloatingItem(TileGooBlock blockentity, float partialTicks) {
        long currentTime = System.currentTimeMillis();
        long cycleDuration = 3600L;
        long elapsedTime = (currentTime - lastChangeTime) % cycleDuration;
        float fadeFactor = (float) (0.5D - 0.5D * Math.cos((2.0D * Math.PI * elapsedTime) / cycleDuration));

        if (cachedItemStack.isEmpty() || elapsedTime < 50L && currentTime - lastChangeTime >= cycleDuration) {
            cachedItemStack = getNextItemFromTier(blockentity.getTier());
            lastChangeTime = currentTime;
        }
        if (cachedItemStack.isEmpty()) {
            return;
        }

        boolean isBlockItem = cachedItemStack.getItem() instanceof ItemBlock;
        for (EnumFacing direction : EnumFacing.values()) {
            GlStateManager.pushMatrix();
            Vec3d itemPos = getOffsetPositionForSide(direction, isBlockItem);
            GlStateManager.translate(itemPos.x, itemPos.y, itemPos.z);
            applyRotationForSide(direction);
            GlStateManager.scale(0.6F, 0.6F, 0.6F);
            renderTransparentItemStack(cachedItemStack, fadeFactor);
            GlStateManager.popMatrix();
        }
    }

    private Vec3d getOffsetPositionForSide(EnumFacing direction, boolean isBlockItem) {
        double offset = 0.025D;
        double nudge = isBlockItem ? 0.10D : 0.05D;
        switch (direction) {
            case UP:
                return new Vec3d(0.5D, 1.0D + offset, 0.5D - nudge);
            case DOWN:
                return new Vec3d(0.5D, 0.0D - offset, 0.5D + nudge);
            case NORTH:
                return new Vec3d(0.5D, 0.5D - nudge, 0.0D - offset);
            case SOUTH:
                return new Vec3d(0.5D, 0.5D - nudge, 1.0D + offset);
            case WEST:
                return new Vec3d(0.0D - offset, 0.5D - nudge, 0.5D);
            case EAST:
            default:
                return new Vec3d(1.0D + offset, 0.5D - nudge, 0.5D);
        }
    }

    private void applyRotationForSide(EnumFacing direction) {
        switch (direction) {
            case UP:
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                break;
            case DOWN:
                GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
                break;
            case NORTH:
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                break;
            case WEST:
                GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                break;
            case EAST:
                GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                break;
            case SOUTH:
            default:
                break;
        }
    }

    private void renderTransparentItemStack(ItemStack itemStack, float fadeFactor) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, fadeFactor);
        RenderHelper.enableStandardItemLighting();
        Minecraft.getMinecraft().getRenderItem().renderItem(itemStack, ItemCameraTransforms.TransformType.GROUND);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public void renderTextures(EnumFacing direction, TileGooBlock blockentity, float partialTicks, int remainingTicks, int maxTicks) {
        if (maxTicks <= 0) {
            return;
        }

        float percentComplete = (1.0F - (float) remainingTicks / (float) maxTicks) * 100.0F;
        int stage = Math.max(0, Math.min(BlockGooPattern.GOOSTAGE.getAllowedValues().size() - 1, (int) (percentComplete / PERCENTAGE_DIVISOR)));
        if (stage > 0) {
            IBlockState previousPattern = ModContentBlocks.GOO_PATTERN_BLOCK.getDefaultState().withProperty(BlockGooPattern.GOOSTAGE, stage - 1);
            renderTexturePattern(direction, blockentity, 1.0F, previousPattern);
        }

        IBlockState currentPattern = ModContentBlocks.GOO_PATTERN_BLOCK.getDefaultState().withProperty(BlockGooPattern.GOOSTAGE, stage);
        float startOfCurrentStage = stage * PERCENTAGE_DIVISOR;
        float percentagePart = percentComplete - startOfCurrentStage;
        float patternAlpha = Math.max(0.0F, Math.min(1.0F, percentagePart / PERCENTAGE_DIVISOR));
        renderTexturePattern(direction, blockentity, patternAlpha, currentPattern);
    }

    public void renderTexturePattern(EnumFacing direction, TileGooBlock blockentity, float transparency, IBlockState pattern) {
        IBlockState renderState = blockentity.getWorld().getBlockState(blockentity.getPos());
        if (!(renderState.getBlock() instanceof BlockGooBlock) || renderState.getBlock() == Blocks.AIR) {
            return;
        }

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(direction.getXOffset(), direction.getYOffset(), direction.getZOffset());

            float translateF = (float) blockentity.getTier() / 2000.0F;
            GlStateManager.translate(-translateF, -translateF, -translateF);
            float scaleF = (float) blockentity.getTier() / 1000.0F;
            GlStateManager.scale(1.0F + scaleF, 1.0F + scaleF, 1.0F + scaleF);

            GlStateManager.translate(0.5F, 0.5F, 0.5F);
            applyDirectionRotation(direction);
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);

            BlockPos renderPos = blockentity.getPos().offset(direction);
            renderPatternDepthOnly(pattern);
            renderTargetDepthEqual(renderState, blockentity.getWorld(), renderPos, direction, transparency);
        } finally {
            GlStateManager.popMatrix();
            restoreOverlayState();
        }
    }

    private static void applyDirectionRotation(EnumFacing direction) {
        switch (direction) {
            case DOWN:
                GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
                break;
            case NORTH:
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
                break;
            case SOUTH:
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                break;
            case WEST:
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                break;
            case EAST:
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
                break;
            case UP:
            default:
                break;
        }
    }

    private void renderPatternDepthOnly(IBlockState pattern) {
        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(pattern);

        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.disableCull();
        GlStateManager.disableLighting();
        GlStateManager.colorMask(false, false, false, false);
        GlStateManager.depthMask(true);
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        renderModelQuads(model, pattern, 0xFFFFFFFF, null, null, null);
    }

    private void renderTargetDepthEqual(IBlockState renderState, IBlockAccess world, BlockPos pos, EnumFacing facing, float transparency) {
        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(renderState);
        BlockColors blockColors = Minecraft.getMinecraft().getBlockColors();

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.01F);
        GlStateManager.disableCull();
        GlStateManager.disableLighting();
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.depthMask(false);
        GlStateManager.depthFunc(GL11.GL_EQUAL);

        int alpha = (int) (Math.max(0.0F, Math.min(1.0F, transparency)) * 255.0F);
        int baseColor = alpha << 24 | 0x00FFFFFF;
        for (EnumFacing renderSide : EnumFacing.values()) {
            getAoDirection(facing, renderSide);
        }
        renderModelQuads(model, renderState, baseColor, blockColors, world, pos);
    }

    private void renderModelQuads(IBakedModel model, IBlockState state, int color, BlockColors blockColors, IBlockAccess world, BlockPos pos) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
        for (EnumFacing face : EnumFacing.values()) {
            for (BakedQuad quad : model.getQuads(state, face, 0L)) {
                LightUtil.renderQuadColor(buffer, quad, tintQuadColor(color, blockColors, state, world, pos, quad));
            }
        }
        for (BakedQuad quad : model.getQuads(state, null, 0L)) {
            LightUtil.renderQuadColor(buffer, quad, tintQuadColor(color, blockColors, state, world, pos, quad));
        }
        tessellator.draw();
    }

    private int tintQuadColor(int color, BlockColors blockColors, IBlockState state, IBlockAccess world, BlockPos pos, BakedQuad quad) {
        if (blockColors == null || world == null || pos == null || !quad.hasTintIndex()) {
            return color;
        }

        int tint = blockColors.colorMultiplier(state, world, pos, quad.getTintIndex());
        if (tint == -1) {
            return color;
        }

        int alpha = color & 0xFF000000;
        int red = ((color >> 16) & 255) * ((tint >> 16) & 255) / 255;
        int green = ((color >> 8) & 255) * ((tint >> 8) & 255) / 255;
        int blue = (color & 255) * (tint & 255) / 255;
        return alpha | red << 16 | green << 8 | blue;
    }

    private EnumFacing getAoDirection(EnumFacing facing, EnumFacing renderSide) {
        switch (renderSide) {
            case UP:
                return facing;
            case DOWN:
                return facing.getOpposite();
            case WEST:
                return facing == EnumFacing.DOWN || facing == EnumFacing.UP ? EnumFacing.WEST : facing.rotateY();
            case EAST:
                return facing == EnumFacing.DOWN || facing == EnumFacing.UP ? EnumFacing.EAST : facing.rotateYCCW();
            case NORTH:
                if (facing == EnumFacing.DOWN) return EnumFacing.SOUTH;
                if (facing == EnumFacing.UP) return EnumFacing.NORTH;
                return EnumFacing.UP;
            case SOUTH:
                if (facing == EnumFacing.DOWN) return EnumFacing.NORTH;
                if (facing == EnumFacing.UP) return EnumFacing.SOUTH;
                return EnumFacing.DOWN;
            default:
                return renderSide;
        }
    }

    private void restoreOverlayState() {
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.depthMask(true);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void syncGlStateManagerCache() {
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
        GlStateManager.depthFunc(GL11.glGetInteger(GL11.GL_DEPTH_FUNC));
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

    private List<ItemStack> revivalItemsForTier(int tier) {
        List<ItemStack> stacks = new ArrayList<>();
        switch (tier) {
            case 1:
                addItem(stacks, Items.SUGAR);
                addItem(stacks, Items.ROTTEN_FLESH);
                break;
            case 2:
                addItem(stacks, Items.NETHER_WART);
                addItem(stacks, Items.BLAZE_POWDER);
                break;
            case 3:
                addItem(stacks, Items.CHORUS_FRUIT);
                addItem(stacks, Items.ENDER_PEARL);
                break;
            case 4:
                addRegisteredItem(stacks, "futuremc:sculk");
                addRegisteredItem(stacks, "futuremc:sculk_catalyst");
                addItem(stacks, Items.NETHER_STAR);
                addItem(stacks, Items.ENDER_EYE);
                addItem(stacks, ModContentItems.getItem("time_crystal"));
                addItem(stacks, ModContentItems.getItem("eclipsealloy_ingot"));
                break;
            default:
                break;
        }
        return stacks;
    }

    private static void addRegisteredItem(List<ItemStack> stacks, String registryName) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(registryName));
        addItem(stacks, item);
    }

    private static void addItem(List<ItemStack> stacks, Item item) {
        if (item != null && item != Items.AIR) {
            stacks.add(new ItemStack(item));
        }
    }
}
