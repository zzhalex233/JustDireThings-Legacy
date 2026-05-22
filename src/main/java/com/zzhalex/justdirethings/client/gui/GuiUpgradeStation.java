package com.zzhalex.justdirethings.client.gui;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.client.gui.base.GuiTooltipContainer;
import com.zzhalex.justdirethings.common.container.ContainerUpgradeStation;
import com.zzhalex.justdirethings.common.tile.TileUpgradeStation;
import com.zzhalex.justdirethings.registry.ModRecipes;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiUpgradeStation extends GuiTooltipContainer {

    private static final ResourceLocation BACKGROUND = new ResourceLocation(Reference.MOD_ID, "textures/gui/container/upgrade_station_smithing.png");
    private static final ResourceLocation ERROR = new ResourceLocation(Reference.MOD_ID, "textures/gui/container/upgrade_station_error.png");
    private static final ResourceLocation SLOT = new ResourceLocation(Reference.MOD_ID, "textures/gui/justslot.png");

    private final TileUpgradeStation tile;

    public GuiUpgradeStation(InventoryPlayer playerInventory, TileUpgradeStation tile) {
        super(new ContainerUpgradeStation(playerInventory, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(I18n.format("tile.justdirethings.upgrade_station.name"), 44, 15, 4210752);
        fontRenderer.drawString(I18n.format("container.inventory"), 8, ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(BACKGROUND);
        int left = (width - xSize) / 2;
        int top = (height - ySize) / 2;
        drawTexturedModalRect(left, top, 0, 0, xSize, ySize);
        if (!ModRecipes.areSmithingTemplatesEnabled()) {
            mc.getTextureManager().bindTexture(SLOT);
            drawModalRectWithCustomSizedTexture(left + ContainerUpgradeStation.TEMPLATE_SLOT_X - 1, top + ContainerUpgradeStation.SMITHING_SLOT_Y - 1, 0, 0, 18, 18, 256, 256);
        }
        if (showsError()) {
            mc.getTextureManager().bindTexture(ERROR);
            drawModalRectWithCustomSizedTexture(left + 65, top + 46, 0, 0, 28, 21, 28, 21);
        }
    }

    private boolean showsError() {
        return (!ModRecipes.areSmithingTemplatesEnabled() || !tile.getStackInSlot(TileUpgradeStation.SLOT_TEMPLATE).isEmpty())
                && !tile.getStackInSlot(TileUpgradeStation.SLOT_BASE).isEmpty()
                && !tile.getStackInSlot(TileUpgradeStation.SLOT_ADDITION).isEmpty()
                && tile.getStackInSlot(TileUpgradeStation.SLOT_OUTPUT).isEmpty();
    }
}
