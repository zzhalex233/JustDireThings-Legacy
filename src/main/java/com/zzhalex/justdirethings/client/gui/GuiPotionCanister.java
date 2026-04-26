package com.zzhalex.justdirethings.client.gui;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.container.ContainerPotionCanister;
import com.zzhalex.justdirethings.common.item.misc.PotionCanisterItem;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;

public class GuiPotionCanister extends GuiContainer {

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
            Gui.drawRect(left + 6, top + 76 - filled, left + 22, top + 75, 0xFF000000 | color);
        }
        mc.getTextureManager().bindTexture(FLUIDBAR);
        drawTexturedModalRect(left + 5, top + 5, 18, 0, 18, 72);
    }
}
