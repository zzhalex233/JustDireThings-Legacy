package com.zzhalex.justdirethings.client.gui;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.client.gui.base.GuiTooltipContainer;
import com.zzhalex.justdirethings.common.container.ContainerPotionCanister;
import com.zzhalex.justdirethings.common.item.misc.PotionCanisterItem;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GuiPotionCanister extends GuiTooltipContainer {

    private static final ResourceLocation BACKGROUND = new ResourceLocation(Reference.MOD_ID, "textures/gui/fuelcanister.png");
    private static final ResourceLocation FLUIDBAR = new ResourceLocation(Reference.MOD_ID, "textures/gui/fluidbar.png");

    private final InventoryPlayer playerInventory;
    private final ContainerPotionCanister container;

    public GuiPotionCanister(InventoryPlayer playerInventory, ContainerPotionCanister container) {
        super(container);
        this.playerInventory = playerInventory;
        this.container = container;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = I18n.format("item.justdirethings.potion_canister.name");
        fontRenderer.drawString(title, xSize / 2 - fontRenderer.getStringWidth(title) / 2, 6, 4210752);
        fontRenderer.drawString(I18n.format("justdirethings.gui.player_inventory"), 8, ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int left = (width - xSize) / 2;
        int top = (height - ySize) / 2;

        mc.getTextureManager().bindTexture(BACKGROUND);
        drawTexturedModalRect(left, top, 0, 0, xSize, ySize);

        mc.getTextureManager().bindTexture(FLUIDBAR);
        drawTexturedModalRect(left + 5, top + 5, 0, 0, 18, 72);
        int filled = (PotionCanisterItem.getPotionAmount(container.getBoundStack()) * 70) / PotionCanisterItem.MAX_MB;
        if (filled > 0) {
            int color = PotionUtils.getPotionColorFromEffectList(PotionCanisterItem.getPotionType(container.getBoundStack()).getEffects());
            drawPotionFluid(left + 6, top + 76, 16, filled, color);
        }
        mc.getTextureManager().bindTexture(FLUIDBAR);
        drawTexturedModalRect(left + 5, top + 5, 18, 0, 18, 72);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawFluidBarTooltip(mouseX, mouseY);
    }

    private void drawPotionFluid(int startX, int bottomY, int width, int height, int color) {
        PotionType potionType = PotionCanisterItem.getPotionType(container.getBoundStack());
        if (potionType.getEffects().isEmpty() || PotionCanisterItem.getPotionAmount(container.getBoundStack()) <= 0) {
            return;
        }

        TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite("minecraft:blocks/water_still");
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        GlStateManager.color(red, green, blue, 1.0F);
        GlStateManager.enableBlend();

        int yOffset = 0;
        while (yOffset < height) {
            int drawHeight = Math.min(16, height - yOffset);
            int drawY = bottomY - yOffset - drawHeight;
            int xOffset = 0;
            while (xOffset < width) {
                int drawWidth = Math.min(16, width - xOffset);
                drawTexturedFluidQuad(startX + xOffset, drawY, drawWidth, drawHeight, sprite);
                xOffset += drawWidth;
            }
            yOffset += drawHeight;
        }

        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawTexturedFluidQuad(int x, int y, int width, int height, TextureAtlasSprite sprite) {
        float uMin = sprite.getMinU();
        float uMax = uMin + (sprite.getMaxU() - uMin) * (width / 16.0F);
        float vMin = sprite.getMinV();
        float vMax = vMin + (sprite.getMaxV() - vMin) * (height / 16.0F);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, 0).tex(uMin, vMax).endVertex();
        buffer.pos(x + width, y + height, 0).tex(uMax, vMax).endVertex();
        buffer.pos(x + width, y, 0).tex(uMax, vMin).endVertex();
        buffer.pos(x, y, 0).tex(uMin, vMin).endVertex();
        tessellator.draw();
    }

    private void drawFluidBarTooltip(int mouseX, int mouseY) {
        int amount = PotionCanisterItem.getPotionAmount(container.getBoundStack());
        PotionType potionType = PotionCanisterItem.getPotionType(container.getBoundStack());
        if (amount <= 0 || potionType.getEffects().isEmpty()) {
            return;
        }
        if (mouseX < guiLeft + 5 || mouseX >= guiLeft + 23 || mouseY < guiTop + 5 || mouseY >= guiTop + 77) {
            return;
        }

        ItemStack fakePotion = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), potionType);
        List<String> lines = new ArrayList<>();
        lines.add(I18n.format("justdirethings.screen.fluid",
                I18n.format(potionType.getNamePrefixed("potion.effect.")),
                formatNumber(amount),
                formatNumber(PotionCanisterItem.MAX_MB)));
        PotionUtils.addPotionTooltip(fakePotion, lines, 1.0F);
        drawHoveringText(lines, mouseX, mouseY);
    }

    private static String formatNumber(double value) {
        long rounded = Math.round(value);
        if (Math.abs(value - rounded) < 0.001D) {
            return Long.toString(rounded);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
