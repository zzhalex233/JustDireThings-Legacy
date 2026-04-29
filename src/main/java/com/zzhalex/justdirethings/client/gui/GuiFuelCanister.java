package com.zzhalex.justdirethings.client.gui;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.client.gui.base.GuiTooltipContainer;
import com.zzhalex.justdirethings.common.container.ContainerFuelCanister;
import com.zzhalex.justdirethings.common.item.fuel.FuelBurnHelper;
import com.zzhalex.justdirethings.common.item.misc.FuelCanisterItem;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Locale;

public class GuiFuelCanister extends GuiTooltipContainer {

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
        String itemsLine = I18n.format("justdirethings.fuelcanisteritemsamt", formatNumber(container.getFuelLevel() / (double) FuelCanisterItem.MINIMUM_TICKS_CONSUMED));
        String fuelLine = I18n.format("justdirethings.fuelcanisteramt", formatNumber(container.getFuelLevel()));
        fontRenderer.drawString(itemsLine, xSize / 2 - fontRenderer.getStringWidth(itemsLine) / 2, 5, 4210752);
        fontRenderer.drawString(fuelLine, xSize / 2 - fontRenderer.getStringWidth(fuelLine) / 2, 15, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(BACKGROUND);
        int left = (width - xSize) / 2;
        int top = (height - ySize) / 2;
        drawTexturedModalRect(left, top, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawAfterContainerBeforeTooltips(int mouseX, int mouseY, float partialTicks) {
        drawInvalidSlotOverlays();
    }

    private void drawInvalidSlotOverlays() {
        for (Slot slot : inventorySlots.inventorySlots) {
            if (slot.getHasStack() && !slot.isItemValid(slot.getStack())) {
                drawRect(guiLeft + slot.xPos, guiTop + slot.yPos, guiLeft + slot.xPos + 16, guiTop + slot.yPos + 16, 0x7FFF0000);
            }
        }
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        if (mc.player.inventory.getItemStack().isEmpty()) {
            Slot hovered = getSlotUnderMouse();
            if (hovered != null && hovered.getHasStack()) {
                ItemStack stack = hovered.getStack();
                List<String> tooltip = stack.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips
                        ? ITooltipFlag.TooltipFlags.ADVANCED
                        : ITooltipFlag.TooltipFlags.NORMAL);
                appendFuelTooltip(stack, tooltip);
                drawHoveringText(tooltip, mouseX, mouseY);
                return;
            }
        }
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    private static void appendFuelTooltip(ItemStack stack, List<String> tooltip) {
        if (!isFuelCanisterInput(stack)) {
            return;
        }

        int fuelPerPiece = FuelBurnHelper.getBurnTime(stack);
        if (GuiScreen.isShiftKeyDown()) {
            tooltip.add(TextFormatting.AQUA + I18n.format("justdirethings.fuelcanisteramt", fuelPerPiece));
            tooltip.add(TextFormatting.AQUA + I18n.format("justdirethings.fuelcanisteramtstack", fuelPerPiece * stack.getCount()));
        } else {
            tooltip.add(TextFormatting.AQUA + I18n.format("justdirethings.fuelcanisteritemsamt", formatNumber(fuelPerPiece / (double) FuelCanisterItem.MINIMUM_TICKS_CONSUMED)));
            tooltip.add(TextFormatting.AQUA + I18n.format("justdirethings.fuelcanisteritemsamtstack", formatNumber((fuelPerPiece * stack.getCount()) / (double) FuelCanisterItem.MINIMUM_TICKS_CONSUMED)));
        }
    }

    private static boolean isFuelCanisterInput(ItemStack stack) {
        return !stack.isEmpty()
                && !(stack.getItem() instanceof FuelCanisterItem)
                && FuelBurnHelper.getBurnTime(stack) > 0
                && !FuelBurnHelper.hasContainerRemainder(stack);
    }

    private static String formatNumber(double value) {
        long rounded = Math.round(value);
        if (Math.abs(value - rounded) < 0.001D) {
            return Long.toString(rounded);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
