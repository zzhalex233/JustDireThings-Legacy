package com.zzhalex.justdirethings.client.gui.machine;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.client.gui.base.GuiMachineBase;
import com.zzhalex.justdirethings.client.gui.button.MachineButtonFactory;
import com.zzhalex.justdirethings.client.gui.widget.WidgetFluidBar;
import com.zzhalex.justdirethings.common.container.machine.ContainerParadoxMachine;
import com.zzhalex.justdirethings.common.tile.machine.TileParadoxMachine;
import com.zzhalex.justdirethings.common.util.FluidDisplayHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class GuiParadoxMachine extends GuiMachineBase {

    private static final ResourceLocation PARADOX_BAR = new ResourceLocation(Reference.MOD_ID, "textures/gui/paradoxbar.png");
    private final ContainerParadoxMachine container;
    private final WidgetFluidBar fluidBar;

    public GuiParadoxMachine(ContainerParadoxMachine container) {
        super(container);
        this.container = container;
        this.fluidBar = new WidgetFluidBar(getFluidBarOffset(), 5, 18, 72);
        addFluidBar(fluidBar);
    }

    @Override
    protected void setTopSection() {
        extraWidth = 80;
        extraHeight = 0;
    }

    @Override
    protected void addMachineButtons() {
        TileParadoxMachine tile = container.getTile();
        if (tile == null) {
            return;
        }
        addMachineButton(MachineButtonFactory.tickSpeedButton(tile.getTickSpeed()));
        addMachineButton(MachineButtonFactory.redstoneButton(134, 62, tile.getRedstoneState().getMode().ordinal()));
        addAdvancedMachineButtons(tile);
        addMachineButton(MachineButtonFactory.renderParadoxButton(98, 62, tile.shouldRenderParadox()));
        addMachineButton(MachineButtonFactory.paradoxSnapshotButton(116, 62));
        addMachineButton(MachineButtonFactory.paradoxTargetButton(56, 38, tile.getTargetType()));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        TileParadoxMachine tile = container.getTile();
        if (tile != null) {
            fluidBar.setValue(
                    tile.getFluidState().getAmount(),
                    tile.getFluidState().getCapacity(),
                    tile.getFluidState().getFluidName()
            );
        }
    }

    @Override
    protected int getFluidBarOffset() {
        return 24;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        TileParadoxMachine tile = container.getTile();
        if (tile == null) {
            return;
        }

        mc.getTextureManager().bindTexture(PARADOX_BAR);
        int barLeft = topSectionLeft + topSectionWidth - 18 - 5;
        drawTexturedModalRect(barLeft, topSectionTop + 5, 0, 0, 18, 72);
        float maxEnergy = tile.getMaxParadoxEnergy();
        if (tile.getParadoxEnergy() > 0.0F && maxEnergy > 0.0F) {
            int height = MathHelper.clamp((int) (tile.getParadoxEnergy() * 70.0F / maxEnergy), 0, 70);
            Color color = Color.getHSBColor((System.currentTimeMillis() % 10800L) / 10800.0F, 1.0F, 1.0F);
            net.minecraft.client.renderer.GlStateManager.color(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, 1.0F);
            drawTexturedModalRect(barLeft + 1, topSectionTop + 5 + 72 - 2 - height, 19, 69 - height, 17, height + 1);
            net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    protected boolean drawEnergyBarTooltip(int mouseX, int mouseY) {
        TileParadoxMachine tile = container.getTile();
        if (tile != null && mouseX >= topSectionLeft + getEnergyBarOffset() && mouseX < topSectionLeft + getEnergyBarOffset() + 18
                && mouseY >= topSectionTop + 5 && mouseY < topSectionTop + 77) {
            List<String> tooltip = new ArrayList<>();
            tooltip.add(I18n.format("justdirethings.screen.energy", tile.getEnergyState().getStoredEnergy(), tile.getEnergyState().getCapacity()));
            tooltip.add(I18n.format("justdirethings.screen.paradoxenergycost", tile.getEnergyCost(tile.getPreviewBlockCount(), tile.getPreviewEntityCount())));
            drawHoveringText(tooltip, mouseX, mouseY, fontRenderer);
            return true;
        }
        return super.drawEnergyBarTooltip(mouseX, mouseY);
    }

    @Override
    protected boolean drawFluidBarTooltip(int mouseX, int mouseY) {
        TileParadoxMachine tile = container.getTile();
        if (tile != null && mouseX >= topSectionLeft + getFluidBarOffset() && mouseX < topSectionLeft + getFluidBarOffset() + 18
                && mouseY >= topSectionTop + 5 && mouseY < topSectionTop + 77) {
            List<String> tooltip = new ArrayList<>();
            String fluidName = FluidDisplayHelper.getLocalizedName(tile.getFluidState().getFluidName(), tile.getFluidState().getAmount());
            if (!fluidName.isEmpty()) {
                tooltip.add(I18n.format("justdirethings.screen.fluid", fluidName, tile.getFluidState().getAmount(), tile.getFluidState().getCapacity()));
            }
            tooltip.add(I18n.format("justdirethings.screen.paradoxfluidcost", tile.getFluidCost(tile.getPreviewBlockCount(), tile.getPreviewEntityCount())));
            drawHoveringText(tooltip, mouseX, mouseY, fontRenderer);
            return true;
        }
        return super.drawFluidBarTooltip(mouseX, mouseY);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawParadoxBarTooltip(mouseX, mouseY);
    }

    private void drawParadoxBarTooltip(int mouseX, int mouseY) {
        TileParadoxMachine tile = container.getTile();
        int barLeft = topSectionLeft + topSectionWidth - 18 - 5;
        if (tile != null && mouseX >= barLeft && mouseX < barLeft + 18 && mouseY >= topSectionTop + 5 && mouseY < topSectionTop + 77) {
            drawHoveringText(
                    java.util.Collections.singletonList(I18n.format("justdirethings.paradoxenergy", String.format(java.util.Locale.ROOT, "%.2f", tile.getParadoxEnergy()), tile.getMaxParadoxEnergy())),
                    mouseX,
                    mouseY,
                    fontRenderer
            );
        }
    }
}
