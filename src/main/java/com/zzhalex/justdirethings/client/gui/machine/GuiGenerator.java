package com.zzhalex.justdirethings.client.gui.machine;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.client.gui.base.GuiMachineBase;
import com.zzhalex.justdirethings.client.gui.widget.WidgetEnergyBar;
import com.zzhalex.justdirethings.common.container.machine.ContainerGenerator;
import com.zzhalex.justdirethings.common.item.fuel.FuelBurnHelper;
import com.zzhalex.justdirethings.common.item.tooltip.TooltipHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

public class GuiGenerator extends GuiMachineBase {

    private static final ResourceLocation JUST_SLOT = new ResourceLocation(Reference.MOD_ID, "textures/gui/justslot.png");
    private static final BigDecimal TICKS_PER_SECOND = new BigDecimal(20);

    private final ContainerGenerator container;
    private final WidgetEnergyBar energyBar = new WidgetEnergyBar(5, 5, 18, 72);

    public GuiGenerator(ContainerGenerator container) {
        super(container);
        this.container = container;
        addEnergyBar(energyBar);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        drawBurnProgress();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        energyBar.setValue(container.getTile().getEnergyState().getStoredEnergy(), container.getTile().getEnergyState().getCapacity());
    }

    private void drawBurnProgress() {
        int left = getBaseGuiLeft() + 79;
        int top = getBaseGuiTop() + 30;
        mc.getTextureManager().bindTexture(JUST_SLOT);
        drawTexturedModalRect(left, top, 0, 18, 18, 18);

        int maxBurn = container.getTile().getMaxBurn();
        int remaining = container.getTile().getBurnRemaining();
        if (maxBurn <= 0) {
            return;
        }
        int height = remaining * 18 / maxBurn;
        drawTexturedModalRect(left, top + 18 - height, 18, 36 - height, 18, height + 3);
    }

    @Override
    protected boolean drawEnergyBarTooltip(int mouseX, int mouseY) {
        if (!energyBar.contains(topSectionLeft, topSectionTop, mouseX, mouseY)) {
            return false;
        }

        int burnRemaining = container.getTile().getBurnRemaining();
        drawHoveringText(Arrays.asList(
                I18n.format(
                        "justdirethings.screen.energy",
                        TooltipHelper.formatTooltipValue(container.getTile().getEnergyState().getStoredEnergy()),
                        TooltipHelper.formatTooltipValue(container.getTile().getEnergyState().getCapacity())
                ),
                I18n.format("justdirethings.screen.fepertick", TooltipHelper.formatNumber(burnRemaining > 0 ? container.getTile().getFePerTick() : 0)),
                burnRemaining <= 0
                        ? I18n.format("justdirethings.screen.no_fuel")
                        : I18n.format("justdirethings.screen.burn_time", ticksInSeconds(burnRemaining))
        ), mouseX, mouseY, fontRenderer);
        return true;
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        Slot hovered = getSlotUnderMouse();
        if (mc.player.inventory.getItemStack().isEmpty() && hovered != null && hovered.getHasStack()) {
            ItemStack fuelStack = hovered.getStack();
            if (FuelBurnHelper.getBurnTime(fuelStack) > 0) {
                List<String> tooltip = fuelStack.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips
                        ? ITooltipFlag.TooltipFlags.ADVANCED
                        : ITooltipFlag.TooltipFlags.NORMAL);
                tooltip.add(TextFormatting.RED + I18n.format(
                        "justdirethings.screen.burnspeedmultiplier",
                        FuelBurnHelper.getBurnSpeedMultiplier(fuelStack)
                ));
                drawHoveringText(tooltip, mouseX, mouseY);
                return;
            }
        }
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    private static String ticksInSeconds(int ticks) {
        return new BigDecimal(ticks).divide(TICKS_PER_SECOND, 1, RoundingMode.HALF_UP).toString();
    }
}
