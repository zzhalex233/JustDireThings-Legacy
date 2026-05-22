package com.zzhalex.justdirethings.client.gui.machine;

import com.zzhalex.justdirethings.client.gui.base.GuiMachineBase;
import com.zzhalex.justdirethings.client.gui.button.ButtonDefinition;
import com.zzhalex.justdirethings.client.gui.button.MachineButtonFactory;
import com.zzhalex.justdirethings.common.container.slot.SlotInventoryHolder;
import com.zzhalex.justdirethings.common.container.machine.ContainerInventoryHolder;
import com.zzhalex.justdirethings.common.tile.base.MachineSettingKeys;
import com.zzhalex.justdirethings.common.tile.machine.TileInventoryHolder;
import com.zzhalex.justdirethings.network.JDTNetwork;
import com.zzhalex.justdirethings.network.message.MessageMachineSetting;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.io.IOException;

public class GuiInventoryHolder extends GuiMachineBase {

    private final ContainerInventoryHolder container;

    public GuiInventoryHolder(ContainerInventoryHolder container) {
        super(container);
        this.container = container;
    }

    @Override
    protected void setTopSection() {
        extraWidth = 0;
        extraHeight = 24;
    }

    @Override
    protected void drawInventorySectionBackground(int left, int top) {
        drawBackgroundPanel(left, top + 75, BASE_X_SIZE, baseYSize - 55);
    }

    @Override
    protected void addMachineButtons() {
        TileInventoryHolder tile = container.getTile();
        addMachineButton(MachineButtonFactory.filterOnlyButton(134, 22, MachineSettingKeys.FILTER_ONLY, tile.isFiltersOnly()));
        addMachineButton(MachineButtonFactory.compareNbtFilterButton(152, 22, tile.isCompareNbt()));
        addMachineButton(MachineButtonFactory.compareCountsButton(134, 4, MachineSettingKeys.COMPARE_COUNTS, tile.isCompareCounts()));
        addMachineButton(MachineButtonFactory.filterOnlyButton(26, 22, MachineSettingKeys.AUTOMATED_FILTER_ONLY, tile.isAutomatedFiltersOnly()));
        addMachineButton(MachineButtonFactory.compareCountsButton(26, 4, MachineSettingKeys.AUTOMATED_COMPARE_COUNTS, tile.isAutomatedCompareCounts()));
        addMachineButton(MachineButtonFactory.renderPlayerButton(8, 4, tile.isRenderPlayer()));
        addMachineButton(MachineButtonFactory.sendInventoryButton(134, 132));
        addMachineButton(MachineButtonFactory.pullInventoryButton(26, 132));
        addMachineButton(MachineButtonFactory.swapInventoryButton(152, 132));
    }

    @Override
    protected void addMachineButton(ButtonDefinition definition) {
        if (MachineSettingKeys.FILTER_COMPARE_NBT.equals(definition.getSettingKey())) {
            definition = ButtonDefinition.grayscale(
                    definition.getX(),
                    definition.getY(),
                    MachineSettingKeys.INVENTORY_COMPARE_NBT,
                    container.getTile().isCompareNbt(),
                    definition.getStates().get(0)
            );
        }
        super.addMachineButton(definition);
    }

    @Override
    protected void drawAfterContainerBeforeTooltips(int mouseX, int mouseY, float partialTicks) {
        super.drawAfterContainerBeforeTooltips(mouseX, mouseY, partialTicks);
        renderGhostFilters();
        renderSelectedSlotBorder();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0 && isCtrlKeyDown()) {
            Slot hovered = getSlotUnderMouse();
            if (hovered instanceof SlotInventoryHolder) {
                if (isShiftKeyDown() && hovered.getSlotIndex() >= 27 && hovered.getSlotIndex() < 36) {
                    container.getTile().setRenderedSlot(hovered.getSlotIndex());
                    sendInventorySetting(MachineSettingKeys.INVENTORY_RENDERED_SLOT, hovered.getSlotIndex());
                } else {
                    sendInventorySetting(MachineSettingKeys.INVENTORY_SAVE_SLOT, hovered.getSlotIndex());
                }
                mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        Slot hovered = getSlotUnderMouse();
        if (hovered instanceof SlotInventoryHolder && hovered.getStack().isEmpty()) {
            ItemStack filterStack = container.getTile().getFilterHandler().getStackInSlot(hovered.getSlotIndex());
            if (!filterStack.isEmpty()) {
                drawHoveringText(filterStack.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips
                        ? net.minecraft.client.util.ITooltipFlag.TooltipFlags.ADVANCED
                        : net.minecraft.client.util.ITooltipFlag.TooltipFlags.NORMAL), mouseX, mouseY);
                return;
            }
        }
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    private void renderGhostFilters() {
        for (Slot slot : inventorySlots.inventorySlots) {
            if (!(slot instanceof SlotInventoryHolder) || !slot.getStack().isEmpty()) {
                continue;
            }
            ItemStack filterStack = container.getTile().getFilterHandler().getStackInSlot(slot.getSlotIndex());
            if (filterStack.isEmpty()) {
                continue;
            }
            int x = guiLeft + slot.xPos;
            int y = guiTop + slot.yPos;
            GlStateManager.pushMatrix();
            GlStateManager.enableDepth();
            itemRender.renderItemAndEffectIntoGUI(filterStack, x, y);
            itemRender.renderItemOverlayIntoGUI(fontRenderer, filterStack, x, y, null);
            GlStateManager.disableDepth();
            drawRect(x, y, x + 16, y + 16, 0x80888888);
            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
        }
    }

    private void renderSelectedSlotBorder() {
        int renderedSlot = container.getTile().getRenderedSlot();
        for (Slot slot : inventorySlots.inventorySlots) {
            if (slot instanceof SlotInventoryHolder && slot.getSlotIndex() == renderedSlot) {
                GlStateManager.disableLighting();
                int left = guiLeft + slot.xPos - 1;
                int top = guiTop + slot.yPos - 1;
                drawRect(left, top, left + 18, top + 1, 0xFFFF3030);
                drawRect(left, top + 17, left + 18, top + 18, 0xFFFF3030);
                drawRect(left, top, left + 1, top + 18, 0xFFFF3030);
                drawRect(left + 17, top, left + 18, top + 18, 0xFFFF3030);
                return;
            }
        }
    }

    private void sendInventorySetting(String key, int value) {
        JDTNetwork.getChannel().sendToServer(new MessageMachineSetting(inventorySlots.windowId, key, value));
    }
}
