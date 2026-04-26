package com.zzhalex.justdirethings.client.gui;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.container.ContainerUpgradeStation;
import com.zzhalex.justdirethings.common.tile.TileUpgradeStation;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

public class GuiUpgradeStation extends GuiContainer {

    private static final ResourceLocation BACKGROUND = new ResourceLocation(Reference.MOD_ID, "textures/gui/sprites/background.png");
    private static final ResourceLocation SLOT_BACKGROUND = new ResourceLocation(Reference.MOD_ID, "textures/gui/justslot.png");

    private final InventoryPlayer playerInventory;
    private final TileUpgradeStation tile;

    public GuiUpgradeStation(InventoryPlayer playerInventory, TileUpgradeStation tile) {
        super(new ContainerUpgradeStation(playerInventory, tile));
        this.playerInventory = playerInventory;
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(I18n.format("tile.justdirethings.upgrade_station.name"), 8, 6, 4210752);
        fontRenderer.drawString(I18n.format("justdirethings.gui.player_inventory"), 8, ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(BACKGROUND);
        int left = (width - xSize) / 2;
        int top = (height - ySize) / 2;
        GuiNineSlice.draw(left, top, xSize, ySize);

        mc.getTextureManager().bindTexture(SLOT_BACKGROUND);
        for (Slot slot : inventorySlots.inventorySlots) {
            drawTexturedModalRect(left + slot.xPos - 1, top + slot.yPos - 1, 0, 0, 18, 18);
        }
    }
}
