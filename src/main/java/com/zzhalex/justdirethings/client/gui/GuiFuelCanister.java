package com.zzhalex.justdirethings.client.gui;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.container.ContainerFuelCanister;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiFuelCanister extends GuiContainer {

    private static final ResourceLocation BACKGROUND = new ResourceLocation(Reference.MOD_ID, "textures/gui/fuelcanister.png");

    private final InventoryPlayer playerInventory;
    private final ContainerFuelCanister container;

    public GuiFuelCanister(InventoryPlayer playerInventory, ContainerFuelCanister container) {
        super(container);
        this.playerInventory = playerInventory;
        this.container = container;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = I18n.format("item.justdirethings.fuel_canister.name");
        String itemsLine = I18n.format("justdirethings.gui.items", container.getFuelItemsEquivalent());
        String fuelLine = I18n.format("justdirethings.gui.fuel", container.getFuelLevel());
        fontRenderer.drawString(title, xSize / 2 - fontRenderer.getStringWidth(title) / 2, 6, 4210752);
        fontRenderer.drawString(itemsLine, xSize / 2 - fontRenderer.getStringWidth(itemsLine) / 2, 16, 4210752);
        fontRenderer.drawString(fuelLine, xSize / 2 - fontRenderer.getStringWidth(fuelLine) / 2, 26, 4210752);
        fontRenderer.drawString(I18n.format("justdirethings.gui.player_inventory"), 8, ySize - 96 + 2, 4210752);
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
