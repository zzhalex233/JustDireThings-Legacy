package com.zzhalex.justdirethings.client.render.tile;

import com.zzhalex.justdirethings.common.block.goo.BlockGooBlock;
import com.zzhalex.justdirethings.common.block.goo.BlockGooPattern;
import com.zzhalex.justdirethings.common.tile.goo.TileGooBlock;
import com.zzhalex.justdirethings.registry.ModContentItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

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
        if (blockentity == null || blockentity.getWorld() == null) return;

        GlStateManager.pushMatrix();
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

        GlStateManager.popMatrix();
    }

    @Override
    public boolean isGlobalRenderer(TileGooBlock blockentity) {
        return true;
    }

    // ── floating item (dead goo) ──────────────────────────────────────

    private ItemStack getNextItemFromTier(int tier) {
        List<ItemStack> items = revivalItemsForTier(tier);
        if (items.isEmpty()) return ItemStack.EMPTY;
        if (currentItemIndex >= items.size()) currentItemIndex = 0;
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
        if (cachedItemStack.isEmpty()) return;
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
            case UP:    return new Vec3d(0.5D, 1.0D + offset, 0.5D - nudge);
            case DOWN:  return new Vec3d(0.5D, 0.0D - offset, 0.5D + nudge);
            case NORTH: return new Vec3d(0.5D, 0.5D - nudge, 0.0D - offset);
            case SOUTH: return new Vec3d(0.5D, 0.5D - nudge, 1.0D + offset);
            case WEST:  return new Vec3d(0.0D - offset, 0.5D - nudge, 0.5D);
            case EAST: default: return new Vec3d(1.0D + offset, 0.5D - nudge, 0.5D);
        }
    }

    private void applyRotationForSide(EnumFacing direction) {
        switch (direction) {
            case UP:    GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F); break;
            case DOWN:  GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F); break;
            case NORTH: GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F); break;
            case WEST:  GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F); break;
            case EAST:  GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F); break;
            case SOUTH: default: break;
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

    // ── infection overlay (6-face decal) ────────────────────────────

    private static final String TEX_PREFIX = "justdirethings:block/goopatterns/goorender_";
    private static final String TEX_TOP = "justdirethings:block/goopatterns/goopatterblock_top";
    private static final float FACE_OFFSET = 0.001F;

    public void renderTextures(EnumFacing direction, TileGooBlock blockentity, float partialTicks, int remainingTicks, int maxTicks) {
        if (maxTicks <= 0) return;
        float percentComplete = (1.0F - (float) remainingTicks / (float) maxTicks) * 100.0F;
        int stage = Math.max(0, Math.min(BlockGooPattern.GOOSTAGE.getAllowedValues().size() - 1,
                (int) (percentComplete / PERCENTAGE_DIVISOR)));
        if (stage > 0) {
            renderOverlay(direction, blockentity, 1.0F, stage - 1);
        }
        float base = stage * PERCENTAGE_DIVISOR;
        float patternAlpha = Math.max(0.0F, Math.min(1.0F, (percentComplete - base) / PERCENTAGE_DIVISOR));
        renderOverlay(direction, blockentity, patternAlpha, stage);
    }

    public void renderOverlay(EnumFacing direction, TileGooBlock blockentity, float transparency, int stage) {
        IBlockState renderState = blockentity.getRenderStateFor(direction);
        if (renderState == null || renderState.getBlock() == Blocks.AIR) return;

        int tier = blockentity.getTier();
        float tierOffset = tier / 2000.0F;
        float tierScale = 1.0F + tier / 1000.0F;

        IBlockState gooState = blockentity.getWorld().getBlockState(blockentity.getPos());
        IBakedModel gooModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(gooState);
        TextureAtlasSprite gooSprite = gooModel.getParticleTexture();

        TextureMap atlas = Minecraft.getMinecraft().getTextureMapBlocks();
        TextureAtlasSprite patternDown = atlas.getAtlasSprite(TEX_PREFIX + "full");
        TextureAtlasSprite patternUp = stage <= 8 ? atlas.getAtlasSprite(TEX_PREFIX + "blank")
                : stage == 9 ? atlas.getAtlasSprite(TEX_TOP)
                : atlas.getAtlasSprite(TEX_PREFIX + "full");
        TextureAtlasSprite patternSide = stage <= 8 ? atlas.getAtlasSprite(TEX_PREFIX + "side" + stage)
                : atlas.getAtlasSprite(TEX_PREFIX + "full");

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.pushMatrix();
        GlStateManager.translate(direction.getXOffset(), direction.getYOffset(), direction.getZOffset());
        GlStateManager.translate(-tierOffset, -tierOffset, -tierOffset);
        GlStateManager.scale(tierScale, tierScale, tierScale);
        applyDirectionRotation(direction);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.01F);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        GlStateManager.color(1.0F, 1.0F, 1.0F, transparency);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        for (EnumFacing face : EnumFacing.values()) {
            TextureAtlasSprite sprite;
            switch (face) {
                case DOWN:  sprite = patternDown; break;
                case UP:    sprite = patternUp; break;
                default:    sprite = patternSide; break;
            }
            addFaceQuad(buf, face, sprite, FACE_OFFSET);
        }
        tess.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    private static void applyDirectionRotation(EnumFacing direction) {
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
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
        GlStateManager.translate(-0.5F, -0.5F, -0.5F);
    }

    private static void addFaceQuad(BufferBuilder buf, EnumFacing face, TextureAtlasSprite s, float o) {
        float u0 = s.getMinU(), u1 = s.getMaxU();
        float v0 = s.getMinV(), v1 = s.getMaxV();
        switch (face) {
            case UP:
                buf.pos(0, 1 + o, 1).tex(u0, v1).endVertex();
                buf.pos(1, 1 + o, 1).tex(u1, v1).endVertex();
                buf.pos(1, 1 + o, 0).tex(u1, v0).endVertex();
                buf.pos(0, 1 + o, 0).tex(u0, v0).endVertex();
                break;
            case DOWN:
                buf.pos(0, -o, 0).tex(u0, v0).endVertex();
                buf.pos(1, -o, 0).tex(u1, v0).endVertex();
                buf.pos(1, -o, 1).tex(u1, v1).endVertex();
                buf.pos(0, -o, 1).tex(u0, v1).endVertex();
                break;
            case NORTH:
                buf.pos(1, 1, -o).tex(u0, v0).endVertex();
                buf.pos(0, 1, -o).tex(u1, v0).endVertex();
                buf.pos(0, 0, -o).tex(u1, v1).endVertex();
                buf.pos(1, 0, -o).tex(u0, v1).endVertex();
                break;
            case SOUTH:
                buf.pos(0, 1, 1 + o).tex(u0, v0).endVertex();
                buf.pos(1, 1, 1 + o).tex(u1, v0).endVertex();
                buf.pos(1, 0, 1 + o).tex(u1, v1).endVertex();
                buf.pos(0, 0, 1 + o).tex(u0, v1).endVertex();
                break;
            case WEST:
                buf.pos(-o, 1, 0).tex(u0, v0).endVertex();
                buf.pos(-o, 1, 1).tex(u1, v0).endVertex();
                buf.pos(-o, 0, 1).tex(u1, v1).endVertex();
                buf.pos(-o, 0, 0).tex(u0, v1).endVertex();
                break;
            case EAST:
                buf.pos(1 + o, 1, 1).tex(u0, v0).endVertex();
                buf.pos(1 + o, 1, 0).tex(u1, v0).endVertex();
                buf.pos(1 + o, 0, 0).tex(u1, v1).endVertex();
                buf.pos(1 + o, 0, 1).tex(u0, v1).endVertex();
                break;
        }
    }

    // ── revival items ─────────────────────────────────────────────────

    private List<ItemStack> revivalItemsForTier(int tier) {
        List<ItemStack> stacks = new ArrayList<>();
        switch (tier) {
            case 1: addItem(stacks, Items.SUGAR); addItem(stacks, Items.ROTTEN_FLESH); break;
            case 2: addItem(stacks, Items.NETHER_WART); addItem(stacks, Items.BLAZE_POWDER); break;
            case 3: addItem(stacks, Items.CHORUS_FRUIT); addItem(stacks, Items.ENDER_PEARL); break;
            case 4:
                addRegisteredItem(stacks, "futuremc:sculk");
                addRegisteredItem(stacks, "futuremc:sculk_catalyst");
                addItem(stacks, Items.NETHER_STAR);
                addItem(stacks, Items.ENDER_EYE);
                addItem(stacks, ModContentItems.getItem("time_crystal"));
                addItem(stacks, ModContentItems.getItem("eclipsealloy_ingot"));
                break;
            default: break;
        }
        return stacks;
    }

    private static void addRegisteredItem(List<ItemStack> stacks, String registryName) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(registryName));
        addItem(stacks, item);
    }

    private static void addItem(List<ItemStack> stacks, Item item) {
        if (item != null && item != Items.AIR) stacks.add(new ItemStack(item));
    }
}
