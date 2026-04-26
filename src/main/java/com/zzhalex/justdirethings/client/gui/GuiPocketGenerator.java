package com.zzhalex.justdirethings.client.gui;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.container.ContainerPocketGenerator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiPocketGenerator extends GuiContainer {

    private static final ResourceLocation BACKGROUND = new ResourceLocation(Reference.MOD_ID, "textures/gui/pocketgenerator.png");

    private final InventoryPlayer playerInventory;
    private final ContainerPocketGenerator container;

    public GuiPocketGenerator(InventoryPlayer playerInventory, ContainerPocketGenerator container) {
        super(container);
        this.playerInventory = playerInventory;
        this.container = container;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = I18n.format("item.justdirethings.pocket_generator.name");
        fontRenderer.drawString(title, xSize / 2 - fontRenderer.getStringWidth(title) / 2, 6, 4210752);
        fontRenderer.drawString(I18n.format("justdirethings.gui.player_inventory"), 8, ySize - 96 + 2, 4210752);
        fontRenderer.drawString(I18n.format("justdirethings.gui.energy", container.getStoredEnergy()), 94, 24, 4210752);
        fontRenderer.drawString(I18n.format("justdirethings.gui.burn", container.getRemainingBurnTicks(), Math.max(1, container.getMaxBurnTicks())), 94, 36, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(BACKGROUND);
        int left = (width - xSize) / 2;
        int top = (height - ySize) / 2;
        drawTexturedModalRect(left, top, 0, 0, xSize, ySize);
    }
}
