package com.zzhalex.justdirethings.client.gui.machine;

import com.zzhalex.justdirethings.client.gui.SensorBlockStatePanelLayout;
import com.zzhalex.justdirethings.client.gui.base.GuiMachineBase;
import com.zzhalex.justdirethings.client.gui.button.MachineButtonFactory;
import com.zzhalex.justdirethings.client.gui.widget.SensorBlockStateScrollList;
import com.zzhalex.justdirethings.common.container.machine.ContainerSensor;
import com.zzhalex.justdirethings.common.container.slot.SlotFilterItemHandler;
import com.zzhalex.justdirethings.common.tile.machine.TileSensor;
import com.zzhalex.justdirethings.network.JDTNetwork;
import com.zzhalex.justdirethings.network.message.MessageBlockStateFilter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiSensor extends GuiMachineBase implements SensorBlockStateScrollList.SensorPanelBridge {

    private final ContainerSensor container;
    private SensorBlockStateScrollList scrollPanel;
    private boolean showBlockStates;
    private int blockStateSlot = -1;
    private ItemStack stateItemStack = ItemStack.EMPTY;
    private final List<ItemStack> filterStackCache = new ArrayList<>();

    public GuiSensor(ContainerSensor container) {
        super(container);
        this.container = container;
    }

    @Override
    public void initGui() {
        super.initGui();
        populateFilterStackCache();
        int left = topSectionLeft + SensorBlockStatePanelLayout.LIST_LEFT_OFFSET;
        int top = topSectionTop + SensorBlockStatePanelLayout.LIST_TOP_OFFSET;
        int bottom = topSectionTop + topSectionHeight - SensorBlockStatePanelLayout.LIST_BOTTOM_MARGIN;
        scrollPanel = new SensorBlockStateScrollList(this, container.getTile(), left, SensorBlockStatePanelLayout.LIST_WIDTH, top, bottom);
    }

    @Override
    protected void setTopSection() {
        extraWidth = container.getTile() instanceof TileSensor.T2 ? 60 : 0;
        extraHeight = 0;
    }

    @Override
    protected void addMachineButtons() {
        TileSensor tile = container.getTile();
        addMachineButton(MachineButtonFactory.tickSpeedButton(tile.getTickSpeed()));
        if (tile instanceof TileSensor.T2) {
            addMachineButtons(MachineButtonFactory.areaButtons(
                    tile.getAreaState().isRenderArea(),
                    tile.getAreaState().getXRadius(),
                    tile.getAreaState().getYRadius(),
                    tile.getAreaState().getZRadius(),
                    tile.getAreaState().getXOffset(),
                    tile.getAreaState().getYOffset(),
                    tile.getAreaState().getZOffset()
            ));
            addMachineButton(MachineButtonFactory.allowListButton(8, 62, tile.getFilterState().isAllowList()));
            addMachineButton(MachineButtonFactory.sensorTargetButton(26, 62, tile.getSenseTarget()));
            addMachineButton(MachineButtonFactory.strongWeakRedstoneButton(44, 62, tile.isStrongSignal()));
            addMachineButton(MachineButtonFactory.equalityButton(104, 62, tile.getEquality()));
            addMachineButton(MachineButtonFactory.senseAmountButton(122, 64, tile.getSenseAmount()));
        } else {
            addMachineButton(MachineButtonFactory.allowListButton(38, 38, tile.getFilterState().isAllowList()));
            addMachineButton(MachineButtonFactory.sensorTargetButton(56, 38, tile.getSenseTarget()));
            addMachineButton(MachineButtonFactory.strongWeakRedstoneButton(20, 38, tile.isStrongSignal()));
        }
    }

    @Override
    protected void drawAdditionalBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        validateFilterStackCache();
        if (!showBlockStates) {
            return;
        }
        drawBackgroundPanel(topSectionLeft - SensorBlockStatePanelLayout.PANEL_WIDTH, topSectionTop,
                SensorBlockStatePanelLayout.PANEL_WIDTH, topSectionHeight);
        refreshStateWindowIfNeeded();
    }

    @Override
    protected void drawAfterContainerBeforeTooltips(int mouseX, int mouseY, float partialTicks) {
        super.drawAfterContainerBeforeTooltips(mouseX, mouseY, partialTicks);
        if (showBlockStates && scrollPanel != null) {
            GlStateManager.disableLighting();
            scrollPanel.drawScreen(mouseX, mouseY, partialTicks);
            GlStateManager.enableLighting();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (showBlockStates && scrollPanel != null && scrollPanel.mouseClicked(mouseX, mouseY, mouseButton)) {
            return;
        }
        if (mouseButton == 1) {
            Slot hovered = findSlotAt(mouseX, mouseY);
            if (hovered instanceof SlotFilterItemHandler) {
                toggleBlockStatePanel(hovered);
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected boolean hasClickedOutside(int mouseX, int mouseY, int guiLeftIn, int guiTopIn) {
        if (showBlockStates
                && mouseX >= topSectionLeft + SensorBlockStatePanelLayout.PANEL_CLICK_LEFT_OFFSET
                && mouseX < topSectionLeft
                && mouseY >= topSectionTop
                && mouseY < topSectionTop + topSectionHeight) {
            return false;
        }
        return super.hasClickedOutside(mouseX, mouseY, guiLeftIn, guiTopIn);
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        Slot hovered = getSlotUnderMouse();
        if (hovered instanceof SlotFilterItemHandler) {
            List<String> tooltip = new ArrayList<>();
            tooltip.add(TextFormatting.RED + I18n.format("justdirethings.screen.rightclicksettings"));
            if (hovered.getHasStack()) {
                tooltip.addAll(hovered.getStack().getTooltip(mc.player, mc.gameSettings.advancedItemTooltips
                        ? net.minecraft.client.util.ITooltipFlag.TooltipFlags.ADVANCED
                        : net.minecraft.client.util.ITooltipFlag.TooltipFlags.NORMAL));
            }
            drawHoveringText(tooltip, mouseX, mouseY);
            return;
        }
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    private Slot findSlotAt(int mouseX, int mouseY) {
        for (Slot slot : inventorySlots.inventorySlots) {
            int left = guiLeft + slot.xPos;
            int top = guiTop + slot.yPos;
            if (mouseX >= left && mouseX < left + 16 && mouseY >= top && mouseY < top + 16) {
                return slot;
            }
        }
        return null;
    }

    private void toggleBlockStatePanel(Slot slot) {
        if (showBlockStates && blockStateSlot == slot.getSlotIndex()) {
            closeBlockStatePanel();
            return;
        }
        blockStateSlot = slot.getSlotIndex();
        stateItemStack = slot.getStack().copy();
        showBlockStates = true;
        if (scrollPanel != null) {
            scrollPanel.setStateStack(stateItemStack);
            scrollPanel.refreshList();
        }
    }

    private void closeBlockStatePanel() {
        showBlockStates = false;
        blockStateSlot = -1;
        stateItemStack = ItemStack.EMPTY;
        if (scrollPanel != null) {
            scrollPanel.setStateStack(ItemStack.EMPTY);
            scrollPanel.refreshList();
        }
    }

    private void refreshStateWindowIfNeeded() {
        if (blockStateSlot < 0 || blockStateSlot >= container.getTile().getFilterHandler().getSlots()) {
            closeBlockStatePanel();
            return;
        }
        ItemStack current = container.getTile().getFilterHandler().getStackInSlot(blockStateSlot);
        if (!ItemStack.areItemStacksEqual(current, stateItemStack)) {
            stateItemStack = current.copy();
            if (scrollPanel != null) {
                scrollPanel.setStateStack(stateItemStack);
                scrollPanel.refreshList();
            }
        }
    }

    private void populateFilterStackCache() {
        filterStackCache.clear();
        for (int slot = 0; slot < container.getTile().getFilterHandler().getSlots(); slot++) {
            filterStackCache.add(container.getTile().getFilterHandler().getStackInSlot(slot).copy());
        }
    }

    private void validateFilterStackCache() {
        for (int slot = 0; slot < filterStackCache.size(); slot++) {
            ItemStack current = container.getTile().getFilterHandler().getStackInSlot(slot);
            if (!ItemStack.areItemStacksEqual(current, filterStackCache.get(slot))) {
                container.getTile().clearSensorProperties(slot);
                JDTNetwork.getChannel().sendToServer(new MessageBlockStateFilter(getWindowId(), slot, "", ""));
                filterStackCache.set(slot, current.copy());
            }
        }
    }

    @Override
    public int getWindowId() {
        return inventorySlots.windowId;
    }

    @Override
    public int getBlockStateSlot() {
        return blockStateSlot;
    }
}
