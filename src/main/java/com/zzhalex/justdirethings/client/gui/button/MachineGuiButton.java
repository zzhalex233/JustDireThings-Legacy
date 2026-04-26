package com.zzhalex.justdirethings.client.gui.button;

import com.zzhalex.justdirethings.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class MachineGuiButton extends GuiButton {

    private final ButtonDefinition definition;
    private int value;

    public MachineGuiButton(int id, int left, int top, ButtonDefinition definition) {
        super(id, left + definition.getX(), top + definition.getY(), definition.getWidth(), definition.getHeight(), "");
        this.definition = definition;
        this.value = definition.getValue();
    }

    public ButtonDefinition getDefinition() {
        return definition;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = clamp(value);
    }

    public int nextValue(boolean decrement) {
        if (definition.getKind() == ButtonDefinition.Kind.VALUE_ADJUST) {
            value = clamp(value + definition.getStep());
            return value;
        }

        if (definition.getKind() == ButtonDefinition.Kind.NUMBER) {
            value = clamp(value + (decrement ? -1 : 1));
            return value;
        }

        if (definition.getKind() == ButtonDefinition.Kind.GRAYSCALE) {
            value = value == 0 ? 1 : 0;
            return value;
        }

        int size = Math.max(1, definition.getStates().size());
        value = (value + (decrement ? -1 : 1) + size) % size;
        return value;
    }

    private int clamp(int nextValue) {
        if (nextValue < definition.getMin()) {
            return definition.getMin();
        }
        if (nextValue > definition.getMax()) {
            return definition.getMax();
        }
        return nextValue;
    }

    public String getTooltipKey() {
        if (definition.getKind() == ButtonDefinition.Kind.NUMBER || definition.getKind() == ButtonDefinition.Kind.VALUE_ADJUST) {
            return definition.getLocalizationKey();
        }
        if (definition.getKind() == ButtonDefinition.Kind.GRAYSCALE) {
            return definition.getLocalizationKey();
        }
        if (definition.getStates().isEmpty()) {
            return "";
        }
        return definition.getStates().get(Math.max(0, Math.min(value, definition.getStates().size() - 1))).getLocalizationKey();
    }

    public String getTooltipText() {
        String key = getTooltipKey();
        if (key.isEmpty()) {
            return "";
        }
        if (definition.getKind() == ButtonDefinition.Kind.NUMBER || definition.getKind() == ButtonDefinition.Kind.VALUE_ADJUST) {
            return I18n.format(key, value);
        }
        return I18n.format(key);
    }

    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!visible) {
            return;
        }

        hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        if (definition.getKind() == ButtonDefinition.Kind.NUMBER) {
            drawRect(x, y, x + width, y + height, 0xFF353535);
            drawRect(x + 1, y + 1, x + width - 1, y + height - 1, 0xFFD8D8D8);
            String valueText = Integer.toString(value);
            mc.fontRenderer.drawString(valueText, x + width / 2 - mc.fontRenderer.getStringWidth(valueText) / 2, y + 2, 0x404040);
            return;
        }

        String texturePath = definition.getStates().get(Math.max(0, Math.min(value, definition.getStates().size() - 1))).getTexturePath();
        if (definition.getKind() == ButtonDefinition.Kind.VALUE_ADJUST) {
            texturePath = definition.getStates().get(0).getTexturePath();
        }
        mc.getTextureManager().bindTexture(new ResourceLocation(Reference.MOD_ID, texturePath));
        if (definition.getKind() == ButtonDefinition.Kind.GRAYSCALE && value == 0) {
            GlStateManager.color(0.33F, 0.33F, 0.33F, 1.0F);
        } else {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
