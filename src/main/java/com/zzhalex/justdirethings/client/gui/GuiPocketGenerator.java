package com.zzhalex.justdirethings.client.gui;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.client.gui.base.GuiTooltipContainer;
import com.zzhalex.justdirethings.common.container.ContainerPocketGenerator;
import com.zzhalex.justdirethings.common.item.fuel.FuelBurnHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GuiPocketGenerator extends GuiTooltipContainer {

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
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(BACKGROUND);
        int left = (width - xSize) / 2;
        int top = (height - ySize) / 2;
        drawTexturedModalRect(left, top, 0, 0, xSize, ySize);
        drawBurnMeter(left, top);
        drawEnergyMeter(left, top);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawEnergyTooltip(mouseX, mouseY);
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        if (drawFuelSlotTooltip(mouseX, mouseY)) {
            return;
        }
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    private void drawBurnMeter(int left, int top) {
        int maxBurn = container.getMaxBurnTicks();
        if (maxBurn <= 0) {
            return;
        }

        int remaining = Math.max(0, Math.min(13, (container.getRemainingBurnTicks() * 13) / maxBurn));
        if (remaining > 0) {
            drawTexturedModalRect(left + 80, top + 17 + 13 - remaining, 176, 13 - remaining, 14, remaining + 1);
        }
    }

    private void drawEnergyMeter(int left, int top) {
        int maxEnergy = container.getMaxEnergy();
        if (maxEnergy <= 0) {
            return;
        }

        int remaining = Math.max(0, Math.min(70, (container.getStoredEnergy() * 70) / maxEnergy));
        if (remaining > 0) {
            drawTexturedModalRect(left + 8, top + 78 - remaining, 176, 84 - remaining, 16, remaining + 1);
        }
    }

    private void drawEnergyTooltip(int mouseX, int mouseY) {
        int left = guiLeft + 7;
        int top = guiTop + 7;
        if (mouseX <= left || mouseX >= left + 18 || mouseY <= top || mouseY >= top + 73) {
            return;
        }

        List<String> lines = new ArrayList<>();
        lines.add(I18n.format("justdirethings.screen.energy", formatNumber(container.getStoredEnergy()), formatNumber(container.getMaxEnergy())));
        if (container.getRemainingBurnTicks() <= 0) {
            lines.add(I18n.format("justdirethings.screen.no_fuel"));
        } else {
            lines.add(I18n.format("justdirethings.screen.burn_time", formatSeconds(container.getRemainingBurnTicks())));
        }
        lines.add(I18n.format("justdirethings.screen.fepertick", formatNumber(container.getRemainingBurnTicks() > 0 ? container.getFePerTick() : 0)));
        drawHoveringText(lines, mouseX, mouseY);
    }

    private boolean drawFuelSlotTooltip(int mouseX, int mouseY) {
        if (!mc.player.inventory.getItemStack().isEmpty()) {
            return false;
        }

        Slot hovered = getSlotUnderMouse();
        if (hovered == null || !hovered.getHasStack()) {
            return false;
        }

        ItemStack fuelStack = hovered.getStack();
        int burnTime = FuelBurnHelper.getBurnTime(fuelStack);
        if (burnTime <= 0) {
            return false;
        }

        List<String> tooltip = fuelStack.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips
                ? ITooltipFlag.TooltipFlags.ADVANCED
                : ITooltipFlag.TooltipFlags.NORMAL);
        tooltip.add(TextFormatting.RED + I18n.format("justdirethings.screen.burnspeedmultiplier", getFuelBurnSpeedMultiplier(fuelStack)));
        drawHoveringText(tooltip, mouseX, mouseY);
        return true;
    }

    private static int getFuelBurnSpeedMultiplier(ItemStack fuelStack) {
        return FuelBurnHelper.getBurnSpeedMultiplier(fuelStack);
    }

    private static String formatSeconds(int ticks) {
        return formatNumber(ticks / 20.0D);
    }

    private static String formatNumber(double value) {
        long rounded = Math.round(value);
        if (Math.abs(value - rounded) < 0.001D) {
            return Long.toString(rounded);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
