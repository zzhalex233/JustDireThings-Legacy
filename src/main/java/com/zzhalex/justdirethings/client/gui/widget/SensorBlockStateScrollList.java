package com.zzhalex.justdirethings.client.gui.widget;

import com.zzhalex.justdirethings.client.gui.SensorBlockStatePanelLayout;
import com.zzhalex.justdirethings.common.tile.machine.TileSensor;
import com.zzhalex.justdirethings.network.JDTNetwork;
import com.zzhalex.justdirethings.network.message.MessageBlockStateFilter;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SensorBlockStateScrollList extends GuiSlot {

    private final SensorPanelBridge bridge;
    private final TileSensor tile;
    private final int listLeft;
    private final int listWidth;
    private final List<Entry> entries = new ArrayList<>();
    private ItemStack stateStack = ItemStack.EMPTY;

    public SensorBlockStateScrollList(SensorPanelBridge bridge, TileSensor tile, int left, int width, int top, int bottom) {
        super(Minecraft.getMinecraft(), width, bottom - top, top, bottom, 18);
        this.bridge = bridge;
        this.tile = tile;
        this.listLeft = left;
        this.listWidth = width;
        setSlotXBoundsFromLeft(left);
    }

    public ItemStack getStateStack() {
        return stateStack;
    }

    public void setStateStack(ItemStack stack) {
        this.stateStack = stack == null ? ItemStack.EMPTY : stack.copy();
    }

    public void refreshList() {
        entries.clear();
        IBlockState state = tile.getStateForStack(stateStack);
        if (state == null) {
            return;
        }
        for (IProperty<?> property : state.getPropertyKeys()) {
            Comparable<?> value = tile.getSensorProperties(bridge.getBlockStateSlot()).get(property);
            entries.add(new Entry(property, value, state.getValue(property), tile.getAllowedValues(stateStack, property)));
        }
    }

    @Override
    protected int getSize() {
        return entries.size();
    }

    @Override
    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        if (slotIndex < 0 || slotIndex >= entries.size()) {
            return;
        }
        entries.get(slotIndex).cycle();
        refreshList();
    }

    @Override
    protected boolean isSelected(int slotIndex) {
        return false;
    }

    @Override
    protected void drawBackground() {
    }

    @Override
    protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
    }

    @Override
    protected int getScrollBarX() {
        return listLeft + listWidth - SensorBlockStatePanelLayout.SCROLLBAR_RIGHT_PADDING;
    }

    @Override
    public int getListWidth() {
        return listWidth;
    }

    @Override
    protected void drawSlot(int entryIdx, int right, int top, int height, int mouseX, int mouseY, float partialTicks) {
        if (entryIdx < 0 || entryIdx >= entries.size()) {
            return;
        }
        Entry entry = entries.get(entryIdx);
        String name = SensorBlockStatePanelLayout.trimToPixelWidth(Minecraft.getMinecraft().fontRenderer, entry.property.getName(), listWidth - 8);
        String value = SensorBlockStatePanelLayout.trimToPixelWidth(Minecraft.getMinecraft().fontRenderer, entry.displayValue(), listWidth - 8);
        Minecraft.getMinecraft().fontRenderer.drawString(name, listLeft + 3, top + 2, 0xFFFFFF);
        Minecraft.getMinecraft().fontRenderer.drawString(value, listLeft + 3, top + 2 + Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT, 0xCCCCCC);
    }

    @Override
    protected void drawContainerBackground(net.minecraft.client.renderer.Tessellator tessellator) {
        Gui.drawRect(listLeft, top, listLeft + listWidth, bottom, 0xD0101010);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseX < listLeft || mouseX >= listLeft + listWidth || mouseY < top || mouseY >= bottom) {
            return false;
        }
        int row = (mouseY - top + (int) amountScrolled) / slotHeight;
        if (row >= 0 && row < getSize()) {
            elementClicked(row, false, mouseX, mouseY);
        }
        return true;
    }

    public interface SensorPanelBridge {
        int getWindowId();

        int getBlockStateSlot();
    }

    private final class Entry {
        private final IProperty<?> property;
        private final Comparable<?> currentValue;
        private final Comparable<?> defaultValue;
        private final List<Comparable<?>> values;
        private final boolean any;

        private Entry(IProperty<?> property, Comparable<?> currentValue, Comparable<?> defaultValue, List<Comparable<?>> values) {
            this.property = property;
            this.currentValue = currentValue == null ? defaultValue : currentValue;
            this.defaultValue = defaultValue;
            this.values = values == null ? new ArrayList<Comparable<?>>() : values;
            this.any = currentValue == null;
        }

        private String displayValue() {
            return any ? SensorBlockStatePanelLayout.anyLabel() : String.valueOf(currentValue);
        }

        private void cycle() {
            if (values.isEmpty()) {
                return;
            }
            int currentIndex = values.indexOf(currentValue);
            int anyIndex = values.indexOf(defaultValue);
            int nextIndex;
            boolean nextAny = false;
            if (currentIndex == anyIndex && any) {
                nextIndex = anyIndex;
            } else {
                nextIndex = (currentIndex + 1) % values.size();
                nextAny = nextIndex == anyIndex;
            }
            Comparable<?> nextValue = values.get(nextIndex);
            tile.setSensorProperty(bridge.getBlockStateSlot(), property, nextValue, nextAny);
            JDTNetwork.getChannel().sendToServer(new MessageBlockStateFilter(
                    bridge.getWindowId(),
                    bridge.getBlockStateSlot(),
                    property.getName(),
                    nextAny ? "" : propertyName(property, nextValue)
            ));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String propertyName(IProperty property, Comparable value) {
        return property.getName(value);
    }
}
