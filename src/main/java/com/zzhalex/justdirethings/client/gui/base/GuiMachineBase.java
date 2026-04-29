package com.zzhalex.justdirethings.client.gui.base;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.client.gui.GuiNineSlice;
import com.zzhalex.justdirethings.client.gui.button.ButtonDefinition;
import com.zzhalex.justdirethings.client.gui.button.MachineButtonFactory;
import com.zzhalex.justdirethings.client.gui.button.MachineGuiButton;
import com.zzhalex.justdirethings.client.gui.widget.WidgetEnergyBar;
import com.zzhalex.justdirethings.client.gui.widget.WidgetFluidBar;
import com.zzhalex.justdirethings.common.container.base.ContainerMachineBase;
import com.zzhalex.justdirethings.common.tile.base.MachineSettingKeys;
import com.zzhalex.justdirethings.common.tile.base.TileAdvancedMachine;
import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import com.zzhalex.justdirethings.common.tile.machine.TileBlockBreaker;
import com.zzhalex.justdirethings.common.tile.machine.TileBlockPlacer;
import com.zzhalex.justdirethings.common.tile.machine.TileBlockSwapper;
import com.zzhalex.justdirethings.common.tile.machine.TileClicker;
import com.zzhalex.justdirethings.common.tile.machine.TileDropper;
import com.zzhalex.justdirethings.common.tile.machine.TileEnergyTransmitter;
import com.zzhalex.justdirethings.common.tile.machine.TileExperienceHolder;
import com.zzhalex.justdirethings.common.tile.machine.TileFluidCollector;
import com.zzhalex.justdirethings.common.tile.machine.TileFluidGenerator;
import com.zzhalex.justdirethings.common.tile.machine.TileFluidPlacer;
import com.zzhalex.justdirethings.common.tile.machine.TileGenerator;
import com.zzhalex.justdirethings.common.tile.machine.TileInventoryHolder;
import com.zzhalex.justdirethings.common.tile.machine.TileItemCollector;
import com.zzhalex.justdirethings.common.tile.machine.TilePlayerAccessor;
import com.zzhalex.justdirethings.common.tile.machine.TileSensor;
import com.zzhalex.justdirethings.network.JDTNetwork;
import com.zzhalex.justdirethings.network.message.MessageMachineSetting;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.IOException;

public class GuiMachineBase extends GuiTooltipContainer {

    private static final String CLICK_TARGET = MachineSettingKeys.CLICK_TARGET;
    private static final String CLICK_TYPE = MachineSettingKeys.CLICK_TYPE;
    private static final String DROP_COUNT = MachineSettingKeys.DROP_COUNT;
    private static final String PICKUP_DELAY = MachineSettingKeys.PICKUP_DELAY;

    private static final ResourceLocation DEFAULT_BACKGROUND = new ResourceLocation(Reference.MOD_ID, "textures/gui/sprites/background.png");
    private static final ResourceLocation SLOT_BACKGROUND = new ResourceLocation(Reference.MOD_ID, "textures/gui/justslot.png");
    protected static final int BASE_X_SIZE = 176;

    private final List<WidgetEnergyBar> energyBars = new ArrayList<>();
    private final List<WidgetFluidBar> fluidBars = new ArrayList<>();
    private final List<MachineGuiButton> machineButtons = new ArrayList<>();
    private final WidgetEnergyBar advancedEnergyBar = new WidgetEnergyBar(5, 5, 18, 72);
    protected int topSectionLeft;
    protected int topSectionTop;
    protected int topSectionWidth;
    protected int topSectionHeight;
    protected int extraWidth;
    protected int extraHeight;
    protected int baseYSize = 166;
    private int slotDisplayOffsetX;
    private int slotDisplayOffsetY;

    public GuiMachineBase(Container inventorySlotsIn) {
        super(inventorySlotsIn);
        this.xSize = BASE_X_SIZE;
        this.ySize = baseYSize;
    }

    protected void addEnergyBar(WidgetEnergyBar widget) {
        energyBars.add(widget);
    }

    protected void addFluidBar(WidgetFluidBar widget) {
        fluidBars.add(widget);
    }

    protected ResourceLocation getBackgroundTexture() {
        return DEFAULT_BACKGROUND;
    }

    @Override
    public void initGui() {
        setTopSection();
        this.xSize = BASE_X_SIZE + extraWidth;
        this.ySize = baseYSize;
        super.initGui();
        applySlotDisplayOffset(extraWidth / 2, 0);
        calculateTopSection();
        syncAdvancedEnergyBar();
        machineButtons.clear();
        addMachineButtons();
    }

    protected void setTopSection() {
        TileMachineBase machine = getMachineTile();
        if (machine instanceof TileAdvancedMachine || machine instanceof TileEnergyTransmitter) {
            extraWidth = 60;
        } else if (machine instanceof TileItemCollector) {
            extraWidth = 20;
        } else {
            extraWidth = 0;
        }
        extraHeight = 0;
    }

    private void syncAdvancedEnergyBar() {
        boolean needsAdvancedBar = getMachineTile() instanceof TileAdvancedMachine;
        if (needsAdvancedBar && !energyBars.contains(advancedEnergyBar)) {
            energyBars.add(advancedEnergyBar);
        } else if (!needsAdvancedBar) {
            energyBars.remove(advancedEnergyBar);
        }
    }

    protected void calculateTopSection() {
        topSectionWidth = BASE_X_SIZE + extraWidth;
        topSectionHeight = baseYSize + extraHeight - 64;
        topSectionLeft = guiLeft;
        topSectionTop = guiTop - extraHeight - 26;
    }

    protected int getEnergyBarOffset() {
        return 5;
    }

    protected int getFluidBarOffset() {
        TileMachineBase machine = getMachineTile();
        if (machine instanceof TileAdvancedMachine) {
            return 204;
        }
        if (machine != null && machine.getEnergyState().getCapacity() > 0) {
            return 24;
        }
        return getEnergyBarOffset();
    }

    protected void addMachineButtons() {
        TileMachineBase machine = getMachineTile();
        if (machine == null) {
            return;
        }

        if (machine instanceof TileItemCollector) {
            TileItemCollector itemCollector = (TileItemCollector) machine;
            addMachineButtons(MachineButtonFactory.wideTimedMachineButtons(
                    machine.getTickSpeed(),
                    machine.getRedstoneState().getMode().ordinal()
            ));
            addMachineButtons(MachineButtonFactory.areaButtons(
                    itemCollector.getAreaState().isRenderArea(),
                    itemCollector.getAreaState().getXRadius(),
                    itemCollector.getAreaState().getYRadius(),
                    itemCollector.getAreaState().getZRadius(),
                    itemCollector.getAreaState().getXOffset(),
                    itemCollector.getAreaState().getYOffset(),
                    itemCollector.getAreaState().getZOffset()
            ));
            addMachineButtons(MachineButtonFactory.filterButtons(
                    itemCollector.getFilterState().isAllowList(),
                    itemCollector.getFilterState().isCompareNbt(),
                    itemCollector.getFilterState().getBlockItemFilter()
            ));
            addMachineButton(MachineButtonFactory.showParticlesButton(98, 62, itemCollector.isShowParticles()));
            addMachineButton(MachineButtonFactory.respectPickupDelayButton(itemCollector.isRespectPickupDelay()));
            return;
        }

        if (machine instanceof TileClicker) {
            TileClicker clicker = (TileClicker) machine;
            if (machine instanceof TileAdvancedMachine) {
                addMachineButton(MachineButtonFactory.tickSpeedButton(machine.getTickSpeed()));
                addMachineButton(MachineButtonFactory.redstoneButton(134, 62, machine.getRedstoneState().getMode().ordinal()));
                addAdvancedMachineButtons((TileAdvancedMachine) machine);
                addMachineButton(MachineButtonFactory.directionButton(116, 62, machine.getDirection()));
                addMachineButton(MachineButtonFactory.clickTargetButton(44, 62, clicker.getClickTarget()));
                addMachineButton(MachineButtonFactory.clickTypeButton(44, 44, clicker.getClickType()));
                addMachineButton(MachineButtonFactory.sneakClickButton(26, 44, clicker.isSneaking()));
                addMachineButton(MachineButtonFactory.showFakePlayerButton(8, 44, clicker.isShowFakePlayer()));
            } else {
                addMachineButton(MachineButtonFactory.tickSpeedButton(machine.getTickSpeed()));
                addMachineButton(MachineButtonFactory.directionButton(122, 38, machine.getDirection()));
                addMachineButton(MachineButtonFactory.clickTargetButton(56, 38, clicker.getClickTarget()));
                addMachineButton(MachineButtonFactory.clickTypeButton(38, 38, clicker.getClickType()));
                addMachineButton(MachineButtonFactory.sneakClickButton(20, 38, clicker.isSneaking()));
                addMachineButton(MachineButtonFactory.showFakePlayerButton(2, 38, clicker.isShowFakePlayer()));
                addMachineButton(MachineButtonFactory.redstoneButton(104, 38, machine.getRedstoneState().getMode().ordinal()));
            }
            return;
        }

        if (machine instanceof TileDropper) {
            TileDropper dropper = (TileDropper) machine;
            if (machine instanceof TileAdvancedMachine) {
                addMachineButton(MachineButtonFactory.tickSpeedButton(machine.getTickSpeed()));
                addMachineButton(MachineButtonFactory.redstoneButton(134, 62, machine.getRedstoneState().getMode().ordinal()));
                addDropperAdvancedMachineButtons((TileAdvancedMachine) machine);
                addMachineButton(MachineButtonFactory.directionButton(122, 38, machine.getDirection()));
                addMachineButton(MachineButtonFactory.dropCountButton(20, 41, dropper.getDropCount()));
                addMachineButton(MachineButtonFactory.pickupDelayButton(20, 27, dropper.getPickupDelay()));
            } else {
                addMachineButton(MachineButtonFactory.tickSpeedButton(machine.getTickSpeed()));
                addMachineButton(MachineButtonFactory.directionButton(122, 38, machine.getDirection()));
                addMachineButton(MachineButtonFactory.dropCountButton(50, 41, dropper.getDropCount()));
                addMachineButton(MachineButtonFactory.pickupDelayButton(50, 27, dropper.getPickupDelay()));
                addMachineButton(MachineButtonFactory.redstoneButton(104, 38, machine.getRedstoneState().getMode().ordinal()));
            }
            return;
        }

        if (machine instanceof TileBlockPlacer) {
            if (machine instanceof TileAdvancedMachine) {
                addMachineButton(MachineButtonFactory.tickSpeedButton(machine.getTickSpeed()));
                addMachineButton(MachineButtonFactory.redstoneButton(134, 62, machine.getRedstoneState().getMode().ordinal()));
                addAdvancedMachineButtons((TileAdvancedMachine) machine);
                addMachineButton(MachineButtonFactory.directionButton(116, 62, machine.getDirection()));
            } else {
                addMachineButtons(MachineButtonFactory.compactTimedDirectionalMachineButtons(
                        machine.getTickSpeed(),
                        machine.getRedstoneState().getMode().ordinal(),
                        machine.getDirection()
                ));
            }
            return;
        }

        if (machine instanceof TileBlockBreaker) {
            TileBlockBreaker blockBreaker = (TileBlockBreaker) machine;
            if (machine instanceof TileAdvancedMachine) {
                addMachineButton(MachineButtonFactory.tickSpeedButton(machine.getTickSpeed()));
                addMachineButton(MachineButtonFactory.redstoneButton(134, 62, machine.getRedstoneState().getMode().ordinal()));
                addAdvancedMachineButtons((TileAdvancedMachine) machine);
                addMachineButton(MachineButtonFactory.directionButton(116, 62, machine.getDirection()));
                addMachineButton(MachineButtonFactory.sneakClickButton(8, 44, blockBreaker.isSneaking()));
            } else {
                addMachineButtons(MachineButtonFactory.compactTimedMachineButtons(
                        machine.getTickSpeed(),
                        machine.getRedstoneState().getMode().ordinal()
                ));
                addMachineButton(MachineButtonFactory.sneakClickButton(56, 38, blockBreaker.isSneaking()));
            }
            return;
        }

        if (machine instanceof TileBlockSwapper) {
            addMachineButton(MachineButtonFactory.tickSpeedButton(machine.getTickSpeed()));
            addMachineButton(MachineButtonFactory.redstoneButton(124, 38, machine.getRedstoneState().getMode().ordinal()));
            if (machine instanceof TileAdvancedMachine) {
                addAdvancedMachineButtons((TileAdvancedMachine) machine);
            }
            return;
        }

        if (machine instanceof TileGenerator || machine instanceof TileFluidGenerator) {
            addMachineButton(MachineButtonFactory.redstoneButton(104, 38, machine.getRedstoneState().getMode().ordinal()));
            return;
        }

        if (machine instanceof TileFluidCollector || machine instanceof TileFluidPlacer) {
            if (machine instanceof TileAdvancedMachine) {
                addMachineButton(MachineButtonFactory.tickSpeedButton(machine.getTickSpeed()));
                addMachineButton(MachineButtonFactory.redstoneButton(134, 62, machine.getRedstoneState().getMode().ordinal()));
                addAdvancedMachineButtons((TileAdvancedMachine) machine);
                if (machine instanceof TileFluidPlacer) {
                    addMachineButton(MachineButtonFactory.directionButton(116, 62, machine.getDirection()));
                }
            } else {
                addMachineButtons(MachineButtonFactory.wideTimedMachineButtons(
                        machine.getTickSpeed(),
                        machine.getRedstoneState().getMode().ordinal()
                ));
            }
            return;
        }

        if (machine instanceof TileEnergyTransmitter) {
            addMachineButton(MachineButtonFactory.redstoneButton(134, 62, machine.getRedstoneState().getMode().ordinal()));
            return;
        }

        if (machine instanceof TileSensor) {
            addMachineButton(MachineButtonFactory.tickSpeedButton(machine.getTickSpeed()));
            return;
        }

        if (machine instanceof TileInventoryHolder || machine instanceof TileExperienceHolder || machine instanceof TilePlayerAccessor) {
            return;
        }
    }

    protected void addMachineButtons(List<ButtonDefinition> definitions) {
        for (ButtonDefinition definition : definitions) {
            addMachineButton(definition);
        }
    }

    protected void addAdvancedMachineButtons(TileAdvancedMachine advancedMachine) {
        TileMachineBase machine = advancedMachine.getMachine();
        addMachineButtons(MachineButtonFactory.areaButtons(
                machine.getAreaState().isRenderArea(),
                machine.getAreaState().getXRadius(),
                machine.getAreaState().getYRadius(),
                machine.getAreaState().getZRadius(),
                machine.getAreaState().getXOffset(),
                machine.getAreaState().getYOffset(),
                machine.getAreaState().getZOffset()
        ));
        if (advancedMachine.getFilterHandler() != null) {
            addMachineButtons(MachineButtonFactory.filterButtons(
                    machine.getFilterState().isAllowList(),
                    machine.getFilterState().isCompareNbt(),
                    machine.getFilterState().getBlockItemFilter()
            ));
        }
    }

    protected void addDropperAdvancedMachineButtons(TileAdvancedMachine advancedMachine) {
        TileMachineBase machine = advancedMachine.getMachine();
        addMachineButtons(MachineButtonFactory.offsetOnlyAreaButtons(
                machine.getAreaState().isRenderArea(),
                machine.getAreaState().getXOffset(),
                machine.getAreaState().getYOffset(),
                machine.getAreaState().getZOffset()
        ));
        if (advancedMachine.getFilterHandler() != null) {
            addMachineButton(MachineButtonFactory.compareNbtFilterButton(8, 62, machine.getFilterState().isCompareNbt()));
        }
    }

    protected void addMachineButton(ButtonDefinition definition) {
        MachineGuiButton button = new MachineGuiButton(buttonList.size(), getButtonBaseLeft(), topSectionTop, definition);
        machineButtons.add(button);
        buttonList.add(button);
    }

    protected int getButtonBaseLeft() {
        return getBaseGuiLeft();
    }

    protected int getBaseGuiLeft() {
        return guiLeft + extraWidth / 2;
    }

    protected int getBaseGuiTop() {
        return guiTop;
    }

    private void applySlotDisplayOffset(int offsetX, int offsetY) {
        int deltaX = offsetX - slotDisplayOffsetX;
        int deltaY = offsetY - slotDisplayOffsetY;
        if (deltaX == 0 && deltaY == 0) {
            return;
        }
        for (Slot slot : inventorySlots.inventorySlots) {
            slot.xPos += deltaX;
            slot.yPos += deltaY;
        }
        slotDisplayOffsetX = offsetX;
        slotDisplayOffsetY = offsetY;
    }

    protected TileMachineBase getMachineTile() {
        if (inventorySlots instanceof ContainerMachineBase) {
            return ((ContainerMachineBase) inventorySlots).getMachine();
        }
        return null;
    }

    protected List<MachineGuiButton> getMachineButtons() {
        return Collections.unmodifiableList(machineButtons);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        calculateTopSection();
        int left = getBaseGuiLeft();
        int top = getBaseGuiTop();
        drawBackgroundPanel(topSectionLeft + 20, topSectionTop - 20, topSectionWidth - 40, 20);
        drawBackgroundPanel(topSectionLeft, topSectionTop, topSectionWidth, topSectionHeight);
        drawBackgroundPanel(left, top + 75, BASE_X_SIZE, baseYSize - 73);

        drawSlotBackgrounds(guiLeft, guiTop);

        for (WidgetEnergyBar widget : energyBars) {
            widget.draw(topSectionLeft, topSectionTop);
        }
        for (WidgetFluidBar widget : fluidBars) {
            widget.draw(topSectionLeft, topSectionTop);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        calculateTopSection();
        updateAdvancedEnergyBar();
        drawMachineTitle();
        drawAreaControlLabels();
    }

    private void updateAdvancedEnergyBar() {
        TileMachineBase machine = getMachineTile();
        if (machine instanceof TileAdvancedMachine) {
            advancedEnergyBar.setValue(machine.getEnergyState().getStoredEnergy(), machine.getEnergyState().getCapacity());
        }
    }

    protected void drawMachineTitle() {
        String title = getMachineTitle();
        if (title.isEmpty()) {
            return;
        }

        int titlePanelLeft = topSectionLeft - guiLeft + 20;
        int titlePanelWidth = topSectionWidth - 40;
        int titleX = titlePanelLeft + (titlePanelWidth - fontRenderer.getStringWidth(title)) / 2;
        int titleY = topSectionTop - guiTop - 14;
        fontRenderer.drawString(title, titleX, titleY, 4210752);
    }

    protected void drawAreaControlLabels() {
        TileMachineBase machine = getMachineTile();
        if (!(machine instanceof TileItemCollector) && !(machine instanceof TileEnergyTransmitter) && !(machine instanceof TileAdvancedMachine)) {
            return;
        }
        if (machine instanceof TileDropper && machine instanceof TileAdvancedMachine) {
            drawOffsetOnlyAreaControlLabels(machine);
            return;
        }

        int areaWidth = 158;
        int xStart = topSectionLeft + topSectionWidth / 2 - areaWidth / 2 - guiLeft;
        int top = topSectionTop - guiTop;
        drawMachineText("Rad", xStart - 4, top + 14, 4210752);
        drawMachineText("Off", xStart - 4, top + 29, 4210752);
        drawMachineText("X", xStart + 35, top + 4, 4210752);
        drawMachineText("Y", xStart + 85, top + 4, 4210752);
        drawMachineText("Z", xStart + 135, top + 4, 4210752);

        drawCenteredAreaValue(formatRadius(machine.getAreaState().getXRadius()), 25, 12);
        drawCenteredAreaValue(formatRadius(machine.getAreaState().getYRadius()), 75, 12);
        drawCenteredAreaValue(formatRadius(machine.getAreaState().getZRadius()), 125, 12);
        drawCenteredAreaValue(Integer.toString(machine.getAreaState().getXOffset()), 25, 27);
        drawCenteredAreaValue(Integer.toString(machine.getAreaState().getYOffset()), 75, 27);
        drawCenteredAreaValue(Integer.toString(machine.getAreaState().getZOffset()), 125, 27);
    }

    private void drawOffsetOnlyAreaControlLabels(TileMachineBase machine) {
        int areaWidth = 158;
        int xStart = topSectionLeft + topSectionWidth / 2 - areaWidth / 2 - guiLeft;
        int top = topSectionTop - guiTop;
        drawMachineText("Off", xStart - 4, top + 14, 4210752);
        drawMachineText("X", xStart + 35, top + 4, 4210752);
        drawMachineText("Y", xStart + 85, top + 4, 4210752);
        drawMachineText("Z", xStart + 135, top + 4, 4210752);

        drawCenteredAreaValue(Integer.toString(machine.getAreaState().getXOffset()), 25, 12);
        drawCenteredAreaValue(Integer.toString(machine.getAreaState().getYOffset()), 75, 12);
        drawCenteredAreaValue(Integer.toString(machine.getAreaState().getZOffset()), 125, 12);
    }

    private void drawCenteredAreaValue(String text, int x, int y) {
        int valueLeft = getAreaButtonBaseLeft() + x + 12;
        int valueWidth = 18;
        drawMachineText(text, valueLeft + (valueWidth - getMachineTextWidth(text)) / 2, topSectionTop - guiTop + y + 2, 4210752);
    }

    protected int getAreaButtonBaseLeft() {
        return extraWidth / 2;
    }

    private static String formatRadius(double radius) {
        if (Math.abs(radius - Math.rint(radius)) < 0.0001D) {
            return String.format(java.util.Locale.ROOT, "%.1f", radius);
        }
        return Double.toString(radius);
    }

    protected int getMachineTextWidth(String text) {
        return fontRenderer.getStringWidth(text);
    }

    protected void drawMachineText(String text, int x, int y, int color) {
        fontRenderer.drawString(text, x, y, color);
    }

    protected void drawMachineGuiText(String key, int x, int y, int color, Object... args) {
        drawMachineText(I18n.format(key, args), x, y, color);
    }

    protected String getMachineTitle() {
        TileMachineBase machine = getMachineTile();
        if (machine == null || machine.getBlockType() == null) {
            return "";
        }
        return I18n.format(machine.getBlockType().getTranslationKey() + ".name");
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button instanceof MachineGuiButton) {
            MachineGuiButton machineButton = (MachineGuiButton) button;
            sendMachineSetting(machineButton, false);
            return;
        }
        super.actionPerformed(button);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 1) {
            for (MachineGuiButton machineButton : machineButtons) {
                if (machineButton.contains(mouseX, mouseY)) {
                    sendMachineSetting(machineButton, true);
                    return;
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void sendMachineSetting(MachineGuiButton machineButton, boolean decrement) {
        int value = machineButton.nextValue(decrement);
        for (MachineGuiButton otherButton : machineButtons) {
            if (otherButton != machineButton && otherButton.getDefinition().getSettingKey().equals(machineButton.getDefinition().getSettingKey())) {
                otherButton.setValue(value);
            }
        }
        JDTNetwork.getChannel().sendToServer(new MessageMachineSetting(
                inventorySlots.windowId,
                machineButton.getDefinition().getSettingKey(),
                value
        ));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (drawEnergyBarTooltip(mouseX, mouseY)) {
            return;
        }
        if (drawFluidBarTooltip(mouseX, mouseY)) {
            return;
        }
        for (MachineGuiButton button : machineButtons) {
            if (button.isMouseOver()) {
                String tooltip = button.getTooltipText();
                if (!tooltip.isEmpty()) {
                    drawHoveringText(Collections.singletonList(tooltip), mouseX, mouseY, fontRenderer);
                }
                break;
            }
        }
    }

    @Override
    protected boolean hasClickedOutside(int mouseX, int mouseY, int guiLeftIn, int guiTopIn) {
        if (mouseX >= topSectionLeft && mouseX < topSectionLeft + topSectionWidth
                && mouseY >= topSectionTop && mouseY < topSectionTop + topSectionHeight) {
            return false;
        }
        return super.hasClickedOutside(mouseX, mouseY, guiLeftIn, guiTopIn);
    }

    protected boolean drawEnergyBarTooltip(int mouseX, int mouseY) {
        for (WidgetEnergyBar widget : energyBars) {
            if (widget.contains(topSectionLeft, topSectionTop, mouseX, mouseY)) {
                drawHoveringText(widget.getTooltipLines(), mouseX, mouseY, fontRenderer);
                return true;
            }
        }
        return false;
    }

    protected boolean drawFluidBarTooltip(int mouseX, int mouseY) {
        for (WidgetFluidBar widget : fluidBars) {
            if (widget.contains(topSectionLeft, topSectionTop, mouseX, mouseY)) {
                drawHoveringText(widget.getTooltipLines(), mouseX, mouseY, fontRenderer);
                return true;
            }
        }
        return false;
    }

    protected void drawBackgroundPanel(int left, int top, int panelWidth, int panelHeight) {
        mc.getTextureManager().bindTexture(getBackgroundTexture());
        GuiNineSlice.draw(left, top, panelWidth, panelHeight);
    }

    protected void drawSlotBackgrounds(int left, int top) {
        mc.getTextureManager().bindTexture(SLOT_BACKGROUND);
        for (Slot slot : inventorySlots.inventorySlots) {
            drawTexturedModalRect(left + slot.xPos - 1, top + slot.yPos - 1, 0, 0, 18, 18);
        }
    }
}
