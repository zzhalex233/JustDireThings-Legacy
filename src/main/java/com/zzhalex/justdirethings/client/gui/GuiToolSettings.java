package com.zzhalex.justdirethings.client.gui;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.client.gui.base.GuiTooltipContainer;
import com.zzhalex.justdirethings.common.container.ContainerToolSettings;
import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.AbilityParams;
import com.zzhalex.justdirethings.common.item.base.LeftClickableTool;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import com.zzhalex.justdirethings.data.tool.AbilityBinding;
import com.zzhalex.justdirethings.network.JDTNetwork;
import com.zzhalex.justdirethings.network.message.MessageToolBindingSetting;
import com.zzhalex.justdirethings.network.message.MessageToolRefreshSlots;
import com.zzhalex.justdirethings.network.message.MessageToolSlotSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GuiToolSettings extends GuiTooltipContainer {

    private static final ResourceLocation BACKGROUND = new ResourceLocation(Reference.MOD_ID, "textures/gui/settings.png");
    private static final ResourceLocation SLOT_BACKGROUND = new ResourceLocation(Reference.MOD_ID, "textures/gui/justslot.png");

    private final InventoryPlayer playerInventory;
    private int selectedSlotIndex;
    private AbilityButton shownAbilityButton;
    private final List<GuiButton> optionalButtons = new ArrayList<>();
    private final Map<AbilityButton, SliderButton> sliders = new HashMap<>();
    private final Map<AbilityButton, BindingModeButton> leftRightClickButtons = new HashMap<>();
    private final Map<AbilityButton, KeybindButton> bindingButtons = new HashMap<>();
    private final Map<AbilityButton, CustomSettingButton> customSettingsButtons = new HashMap<>();
    private final Map<AbilityButton, RequireEquippedButton> requireEquippedButtons = new HashMap<>();
    private boolean bindingEnabled;
    private AbilityButton bindingTarget;
    private final Map<Ability, Boolean> pendingRequireEquipped = new HashMap<>();
    private int nextOptionButtonId;
    private int lastObservedSlot = -1;
    private ItemStack lastObservedStack = ItemStack.EMPTY;

    public GuiToolSettings(InventoryPlayer playerInventory, ContainerToolSettings container) {
        super(container);
        this.playerInventory = playerInventory;
        this.xSize = 176;
        this.ySize = 166;
        this.selectedSlotIndex = findInitialToolSlot(playerInventory);
    }

    @Override
    public void initGui() {
        super.initGui();
        refreshDynamicSlots();
        rebuildAbilityButtons();
        rememberSelectedStack();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        ItemStack stack = selectedToolStack();
        if (lastObservedSlot != selectedSlotIndex || !ItemStack.areItemStacksEqual(stack, lastObservedStack)) {
            refreshDynamicSlots();
            rebuildAbilityButtons();
            rememberSelectedStack();
        }
    }

    private void rebuildAbilityButtons() {
        Ability previousShownAbility = shownAbilityButton == null ? null : shownAbilityButton.ability;
        buttonList.removeIf(button -> button instanceof ToolSettingsButton);
        optionalButtons.clear();
        sliders.clear();
        leftRightClickButtons.clear();
        bindingButtons.clear();
        customSettingsButtons.clear();
        requireEquippedButtons.clear();
        pendingRequireEquipped.clear();
        shownAbilityButton = null;
        bindingEnabled = false;
        bindingTarget = null;
        nextOptionButtonId = 2000;

        ItemStack stack = selectedToolStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof ToggleableTool)) {
            return;
        }

        ToggleableTool tool = (ToggleableTool) stack.getItem();
        List<Ability> abilities = new ArrayList<>(tool.getSupportedAbilities());
        abilities.sort(Comparator.comparingInt(Ability::getTier).thenComparing(Ability::getId));

        int counter = 0;
        int startX = guiLeft + 5;
        int startY = guiTop + 25;
        for (Ability ability : abilities) {
            if (!tool.hasInstalledAbility(stack, ability)) {
                continue;
            }
            int x = startX + (counter / 2) * 18;
            int y = startY + (counter % 2) * 18;
            AbilityButton button = new AbilityButton(1000 + counter, x, y, ability, tool.getSetting(stack, ability), tool.getToolValue(stack, ability));
            buttonList.add(button);
            addOptionalButtons(button, tool, stack);
            counter++;
        }

        if (previousShownAbility != null) {
            restoreExpandedAbility(previousShownAbility);
        }
    }

    private void addOptionalButtons(AbilityButton button, ToggleableTool tool, ItemStack stack) {
        Ability ability = button.ability;
        int optionX = guiLeft + 5;
        int optionY = guiTop + 25;
        AbilityParams params = tool.getAbilityParams(ability);
        if (ability.getSettingType() == Ability.SettingType.SLIDER && params.minSlider != params.maxSlider) {
            SliderButton slider = new SliderButton(nextOptionId(), optionX + 20, optionY - 18, ability, params, tool.getToolValue(stack, ability));
            sliders.put(button, slider);
        }

        if (stack.getItem() instanceof LeftClickableTool && ability.isBindable()) {
            int mode = LeftClickableTool.getBindingMode(stack, ability);
            if (ability.getBindingType() == Ability.BindingType.CUSTOM_ONLY) {
                mode = 2;
            }
            AbilityBinding binding = LeftClickableTool.getAbilityBinding(stack, ability);
            boolean requireEquipped = binding == null || binding.isRequireEquipped();
            BindingModeButton bindingButton = new BindingModeButton(nextOptionId(), optionX + 125, optionY - 18, ability, mode, ability.getBindingType());
            KeybindButton keybindButton = new KeybindButton(nextOptionId(), optionX + 143, optionY - 18, ability);
            RequireEquippedButton requireButton = new RequireEquippedButton(nextOptionId(), optionX + 125, optionY, ability, requireEquipped);
            leftRightClickButtons.put(button, bindingButton);
            bindingButtons.put(button, keybindButton);
            requireEquippedButtons.put(button, requireButton);
        }

        if (ability.hasCustomSetting()) {
            CustomSettingButton customButton = new CustomSettingButton(nextOptionId(), optionX + 143, optionY, ability, tool.getCustomSetting(stack, ability));
            customSettingsButtons.put(button, customButton);
        }
    }

    private int nextOptionId() {
        return nextOptionButtonId++;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button instanceof AbilityButton) {
            activateAbilityButton((AbilityButton) button, false);
            return;
        }
        if (button instanceof SliderButton) {
            activateSliderButton((SliderButton) button);
            return;
        }
        if (button instanceof BindingModeButton) {
            activateBindingModeButton((BindingModeButton) button);
            return;
        }
        if (button instanceof KeybindButton) {
            activateKeybindButton((KeybindButton) button);
            return;
        }
        if (button instanceof RequireEquippedButton) {
            activateRequireEquippedButton((RequireEquippedButton) button);
            return;
        }
        if (button instanceof CustomSettingButton) {
            activateCustomSettingButton((CustomSettingButton) button);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (bindingEnabled && bindingTarget != null && mouseButton != 0 && mouseButton != 1) {
            sendCustomBinding(bindingTarget, mouseButton, true);
            return;
        }

        if (mouseButton == 1) {
            for (GuiButton button : new ArrayList<>(buttonList)) {
                if (button instanceof AbilityButton && button.mousePressed(mc, mouseX, mouseY)) {
                    toggleExpandedAbility((AbilityButton) button);
                    return;
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);

        Slot hovered = getSlotAt(mouseX, mouseY);
        if (hovered != null && hovered.getHasStack() && hovered.getStack().getItem() instanceof ToggleableTool) {
            selectedSlotIndex = hovered.getSlotIndex();
            refreshDynamicSlots();
            rebuildAbilityButtons();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (bindingEnabled && bindingTarget != null) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                clearCustomBinding(bindingTarget);
            } else {
                sendCustomBinding(bindingTarget, keyCode, false);
            }
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawButtonTooltips(mouseX, mouseY);
    }

    private void toggleExpandedAbility(AbilityButton button) {
        buttonList.removeAll(optionalButtons);
        optionalButtons.clear();
        bindingEnabled = false;
        bindingTarget = null;

        if (button == shownAbilityButton) {
            shownAbilityButton = null;
            return;
        }

        shownAbilityButton = button;
        addOptionalButton(sliders.get(button));
        addOptionalButton(leftRightClickButtons.get(button));
        if (showCustomBinding(button)) {
            addOptionalButton(bindingButtons.get(button));
            addOptionalButton(requireEquippedButtons.get(button));
        }
        addOptionalButton(customSettingsButtons.get(button));
    }

    private void addOptionalButton(GuiButton button) {
        if (button == null) {
            return;
        }
        optionalButtons.add(button);
        buttonList.add(button);
    }

    private void restoreExpandedAbility(Ability ability) {
        AbilityButton button = abilityButtonFor(ability);
        if (button == null) {
            return;
        }
        shownAbilityButton = button;
        addOptionalButton(sliders.get(button));
        addOptionalButton(leftRightClickButtons.get(button));
        if (showCustomBinding(button)) {
            addOptionalButton(bindingButtons.get(button));
            addOptionalButton(requireEquippedButtons.get(button));
        }
        addOptionalButton(customSettingsButtons.get(button));
    }

    private void refreshExpandedAbility(AbilityButton button) {
        if (shownAbilityButton == null || shownAbilityButton.ability != button.ability) {
            return;
        }
        AbilityButton selected = shownAbilityButton;
        buttonList.removeAll(optionalButtons);
        optionalButtons.clear();
        addOptionalButton(sliders.get(selected));
        addOptionalButton(leftRightClickButtons.get(selected));
        if (showCustomBinding(selected)) {
            addOptionalButton(bindingButtons.get(selected));
            addOptionalButton(requireEquippedButtons.get(selected));
        }
        addOptionalButton(customSettingsButtons.get(selected));
    }

    private boolean showCustomBinding(AbilityButton button) {
        BindingModeButton bindingButton = leftRightClickButtons.get(button);
        return bindingButton != null && bindingButton.getMode() == 2;
    }

    private void activateAbilityButton(AbilityButton button, boolean rightClick) {
        Ability ability = button.ability;
        int mode;
        int value = -1;
        if (ability.getSettingType() == Ability.SettingType.CYCLE) {
            mode = 1;
        } else if (ability.getSettingType() == Ability.SettingType.SLIDER) {
            mode = 0;
        } else {
            mode = 0;
        }
        sendSlotSetting(ability, mode, value);

        ToolSettingClientState.applyLocalPreview(selectedToolStack(), ability, mode, value);
        rebuildAbilityButtons();
    }

    private void activateSliderButton(SliderButton button) {
        sendSlotSetting(button.ability, 2, button.getValue());
        ToolSettingClientState.applyLocalPreview(selectedToolStack(), button.ability, 2, button.getValue());
        rebuildAbilityButtons();
    }

    private void activateBindingModeButton(BindingModeButton button) {
        button.cycleMode();
        AbilityButton abilityButton = abilityButtonFor(button.ability);
        if (abilityButton == null) {
            return;
        }

        if (button.getMode() == 2) {
            AbilityBinding binding = LeftClickableTool.getAbilityBinding(selectedToolStack(), button.ability);
            sendBinding(button.ability, button.getMode(), binding == null ? -1 : binding.getKeyCode(), binding != null && binding.isMouseBinding(), requireEquipped(button.ability));
        } else {
            sendBinding(button.ability, button.getMode(), -1, false, true);
        }
        LeftClickableTool.setBindingMode(selectedToolStack(), button.ability, button.getMode());
        refreshExpandedAbility(abilityButton);
    }

    private void activateKeybindButton(KeybindButton button) {
        AbilityButton abilityButton = abilityButtonFor(button.ability);
        if (abilityButton == null) {
            return;
        }
        bindingEnabled = !bindingEnabled;
        bindingTarget = bindingEnabled ? abilityButton : null;
        button.active = bindingEnabled;
    }

    private void activateRequireEquippedButton(RequireEquippedButton button) {
        button.toggle();
        AbilityBinding binding = LeftClickableTool.getAbilityBinding(selectedToolStack(), button.ability);
        sendBinding(button.ability, 2, binding == null ? -1 : binding.getKeyCode(), binding != null && binding.isMouseBinding(), button.requiresEquipped());
        pendingRequireEquipped.put(button.ability, button.requiresEquipped());
        if (binding != null) {
            LeftClickableTool.addToCustomBindingList(selectedToolStack(), new AbilityBinding(button.ability.getId(), binding.getKeyCode(), binding.isMouseBinding(), button.requiresEquipped()));
        }
    }

    private void activateCustomSettingButton(CustomSettingButton button) {
        button.cycleValue();
        sendSlotSetting(button.ability, 3, button.getValue());
        ToolSettingClientState.applyLocalPreview(selectedToolStack(), button.ability, 3, button.getValue());
        rebuildAbilityButtons();
    }

    private void sendCustomBinding(AbilityButton button, int keyCode, boolean mouse) {
        sendBinding(button.ability, 2, keyCode, mouse, requireEquipped(button.ability));
        LeftClickableTool.addToCustomBindingList(selectedToolStack(), new AbilityBinding(button.ability.getId(), keyCode, mouse, requireEquipped(button.ability)));
        bindingEnabled = false;
        bindingTarget = null;
        rebuildAbilityButtons();
    }

    private void clearCustomBinding(AbilityButton button) {
        sendBinding(button.ability, 2, -1, false, requireEquipped(button.ability));
        LeftClickableTool.removeFromCustomBindingList(selectedToolStack(), button.ability);
        bindingEnabled = false;
        bindingTarget = null;
        rebuildAbilityButtons();
    }

    private void sendSlotSetting(Ability ability, int mode, int value) {
        JDTNetwork.getChannel().sendToServer(new MessageToolSlotSetting(ability.getId(), selectedSlotIndex, mode, value));
    }

    private void sendBinding(Ability ability, int button, int keyCode, boolean mouse, boolean requireEquipped) {
        JDTNetwork.getChannel().sendToServer(new MessageToolBindingSetting(selectedSlotIndex, ability.getId(), button, keyCode, mouse, requireEquipped));
    }

    private boolean requireEquipped(Ability ability) {
        if (pendingRequireEquipped.containsKey(ability)) {
            return pendingRequireEquipped.get(ability);
        }
        AbilityBinding binding = LeftClickableTool.getAbilityBinding(selectedToolStack(), ability);
        return binding == null || binding.isRequireEquipped();
    }

    private AbilityButton abilityButtonFor(Ability ability) {
        for (GuiButton button : buttonList) {
            if (button instanceof AbilityButton && ((AbilityButton) button).ability == ability) {
                return (AbilityButton) button;
            }
        }
        if (shownAbilityButton != null && shownAbilityButton.ability == ability) {
            return shownAbilityButton;
        }
        return null;
    }

    private void drawButtonTooltips(int mouseX, int mouseY) {
        for (GuiButton button : buttonList) {
            if (button instanceof ToolSettingsButton && contains(button, mouseX, mouseY)) {
                List<String> lines = ((ToolSettingsButton) button).getTooltipLines(this);
                if (!lines.isEmpty()) {
                    drawHoveringText(lines, mouseX, mouseY);
                }
                return;
            }
        }
    }

    private Slot getSlotAt(int mouseX, int mouseY) {
        for (Slot slot : inventorySlots.inventorySlots) {
            if (isPointInRegion(slot.xPos, slot.yPos, 16, 16, mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }

    private ItemStack selectedToolStack() {
        return playerInventory.getStackInSlot(selectedSlotIndex);
    }

    private void refreshDynamicSlots() {
        if (inventorySlots instanceof ContainerToolSettings) {
            ((ContainerToolSettings) inventorySlots).refreshSlots(selectedToolStack());
        }
        JDTNetwork.getChannel().sendToServer(new MessageToolRefreshSlots(selectedSlotIndex));
    }

    private void rememberSelectedStack() {
        lastObservedSlot = selectedSlotIndex;
        ItemStack stack = selectedToolStack();
        lastObservedStack = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }

    private static int findInitialToolSlot(InventoryPlayer inventory) {
        ItemStack mainHand = inventory.getCurrentItem();
        if (!mainHand.isEmpty() && mainHand.getItem() instanceof ToggleableTool) {
            return inventory.currentItem;
        }
        ItemStack offHand = inventory.getStackInSlot(40);
        if (!offHand.isEmpty() && offHand.getItem() instanceof ToggleableTool) {
            return 40;
        }
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof ToggleableTool) {
                return slot;
            }
        }
        return inventory.currentItem;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        ItemStack stack = selectedToolStack();
        if (!stack.isEmpty()) {
            itemRender.renderItemAndEffectIntoGUI(stack, 5, 5);
            itemRender.renderItemOverlayIntoGUI(fontRenderer, stack, 5, 5, null);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(BACKGROUND);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        mc.getTextureManager().bindTexture(SLOT_BACKGROUND);
        for (Slot slot : ((ContainerToolSettings) inventorySlots).getDynamicSlots()) {
            drawTexturedModalRect(guiLeft + slot.xPos - 1, guiTop + slot.yPos - 1, 0, 0, 18, 18);
        }
    }

    private static boolean contains(GuiButton button, int mouseX, int mouseY) {
        return mouseX >= button.x && mouseY >= button.y && mouseX < button.x + button.width && mouseY < button.y + button.height;
    }

    private static String formatNumber(double value) {
        long rounded = Math.round(value);
        if (Math.abs(value - rounded) < 0.001D) {
            return Long.toString(rounded);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static final class ToolSettingClientState {
        private static void applyLocalPreview(ItemStack stack, Ability ability, int mode, int value) {
            if (stack.isEmpty() || !(stack.getItem() instanceof ToggleableTool)) {
                return;
            }
            ToggleableTool tool = (ToggleableTool) stack.getItem();
            if (mode == 0) {
                tool.toggleSetting(stack, ability);
            } else if (mode == 1) {
                tool.cycleSetting(stack, ability);
            } else if (mode == 2) {
                tool.setToolValue(stack, ability, value);
            } else if (mode == 3) {
                tool.setCustomSetting(stack, ability, value);
            }
        }
    }

    private abstract static class ToolSettingsButton extends GuiButton {
        private final ResourceLocation[] textures;
        private final String[] localizationKeys;
        protected int textureIndex;

        private ToolSettingsButton(int buttonId, int x, int y, int width, int height, ResourceLocation[] textures, String[] localizationKeys, int textureIndex) {
            super(buttonId, x, y, width, height, "");
            this.textures = textures;
            this.localizationKeys = localizationKeys;
            this.textureIndex = Math.max(0, Math.min(textureIndex, textures.length - 1));
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (!visible) {
                return;
            }

            hovered = contains(this, mouseX, mouseY);
            drawTexture(mc);
        }

        protected void drawTexture(Minecraft mc) {
            mc.getTextureManager().bindTexture(textures[textureIndex]);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        }

        protected String localizationKey() {
            return localizationKeys[Math.max(0, Math.min(textureIndex, localizationKeys.length - 1))];
        }

        protected List<String> getTooltipLines(GuiToolSettings gui) {
            List<String> lines = new ArrayList<>();
            String key = localizationKey();
            if (!key.isEmpty()) {
                lines.add(I18n.format(key));
            }
            return lines;
        }
    }

    private static final class AbilityButton extends ToolSettingsButton {
        private final Ability ability;
        private final boolean active;
        private final int value;

        private AbilityButton(int buttonId, int x, int y, Ability ability, boolean active, int value) {
            super(buttonId, x, y, 16, 16, new ResourceLocation[] {iconFor(ability, active, value)}, new String[] {ability.getTranslationKey()}, 0);
            this.ability = ability;
            this.active = active;
            this.value = value;
        }

        @Override
        protected void drawTexture(Minecraft mc) {
            mc.getTextureManager().bindTexture(iconFor(ability, active, value));
            GlStateManager.color(active ? 1.0F : 0.35F, active ? 1.0F : 0.35F, active ? 1.0F : 0.35F, 1.0F);
            drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        @Override
        protected List<String> getTooltipLines(GuiToolSettings gui) {
            List<String> lines = super.getTooltipLines(gui);
            if (gui.sliders.containsKey(this) || gui.leftRightClickButtons.containsKey(this) || gui.customSettingsButtons.containsKey(this)) {
                lines.add(I18n.format("justdirethings.screen.rightclicksettings"));
            }
            return lines;
        }

        private static ResourceLocation iconFor(Ability ability, boolean active, int value) {
            String icon;
            if (ability == Ability.HAMMER) {
                icon = !active ? "ignore" : "hammer" + Math.max(3, value);
            } else {
                icon = ability.getId();
            }
            return new ResourceLocation(Reference.MOD_ID, "textures/gui/buttons/" + icon + ".png");
        }
    }

    private static final class SliderButton extends ToolSettingsButton {
        private final Ability ability;
        private final AbilityParams params;
        private int value;

        private SliderButton(int buttonId, int x, int y, Ability ability, AbilityParams params, int value) {
            super(buttonId, x, y, 100, 15, new ResourceLocation[] {buttonTexture("blankbutton.png")}, new String[] {ability.getTranslationKey()}, 0);
            this.ability = ability;
            this.params = params;
            this.value = value;
        }

        @Override
        public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
            if (!super.mousePressed(mc, mouseX, mouseY)) {
                return false;
            }
            double percent = Math.max(0.0D, Math.min(1.0D, (mouseX - x) / (double) width));
            int raw = params.minSlider + (int) Math.round((params.maxSlider - params.minSlider) * percent);
            int stepped = params.minSlider + Math.round((raw - params.minSlider) / (float) params.increment) * params.increment;
            value = Math.max(params.minSlider, Math.min(params.maxSlider, stepped));
            return true;
        }

        @Override
        protected void drawTexture(Minecraft mc) {
            drawRect(x, y + 5, x + width, y + 10, 0xFF353535);
            double range = Math.max(1.0D, params.maxSlider - params.minSlider);
            int filled = (int) Math.round(((value - params.minSlider) / range) * width);
            drawRect(x, y + 5, x + filled, y + 10, 0xFF5AA7FF);
            String text = I18n.format(ability.getTranslationKey()) + ": " + formatNumber(value);
            mc.fontRenderer.drawString(text, x + width / 2 - mc.fontRenderer.getStringWidth(text) / 2, y - 1, 0x404040);
        }

        private int getValue() {
            return value;
        }
    }

    private static final class BindingModeButton extends ToolSettingsButton {
        private final Ability ability;
        private final Ability.BindingType bindingType;

        private BindingModeButton(int buttonId, int x, int y, Ability ability, int mode, Ability.BindingType bindingType) {
            super(buttonId, x, y, 16, 16, bindingTextures(bindingType), bindingLocalizationKeys(bindingType), bindingTextureIndex(mode, bindingType));
            this.ability = ability;
            this.bindingType = bindingType;
        }

        private void cycleMode() {
            if (bindingType == Ability.BindingType.CUSTOM_ONLY) {
                textureIndex = 0;
            } else {
                textureIndex = (textureIndex + 1) % 3;
            }
        }

        private int getMode() {
            return bindingType == Ability.BindingType.CUSTOM_ONLY ? 2 : textureIndex;
        }

        @Override
        protected List<String> getTooltipLines(GuiToolSettings gui) {
            List<String> lines = super.getTooltipLines(gui);
            if (getMode() == 2) {
                AbilityBinding binding = LeftClickableTool.getAbilityBinding(gui.selectedToolStack(), ability);
                lines.add(bindingLine(binding));
            }
            return lines;
        }
    }

    private static final class KeybindButton extends ToolSettingsButton {
        private final Ability ability;
        private boolean active;

        private KeybindButton(int buttonId, int x, int y, Ability ability) {
            super(buttonId, x, y, 16, 16, new ResourceLocation[] {buttonTexture("click-hold.png")}, new String[] {"justdirethings.screen.setbinding"}, 0);
            this.ability = ability;
        }

        @Override
        protected void drawTexture(Minecraft mc) {
            mc.getTextureManager().bindTexture(buttonTexture("click-hold.png"));
            GlStateManager.color(active ? 1.0F : 0.45F, active ? 1.0F : 0.45F, active ? 1.0F : 0.45F, 1.0F);
            drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        @Override
        protected List<String> getTooltipLines(GuiToolSettings gui) {
            List<String> lines = super.getTooltipLines(gui);
            AbilityBinding binding = LeftClickableTool.getAbilityBinding(gui.selectedToolStack(), ability);
            lines.add(bindingLine(binding));
            return lines;
        }
    }

    private static final class RequireEquippedButton extends ToolSettingsButton {
        private final Ability ability;

        private RequireEquippedButton(int buttonId, int x, int y, Ability ability, boolean requireEquipped) {
            super(buttonId, x, y, 16, 16,
                    new ResourceLocation[] {buttonTexture("requireequipped.png"), buttonTexture("notreequireequipped.png")},
                    new String[] {"justdirethings.screen.requireequipped", "justdirethings.screen.notrequireequipped"},
                    requireEquipped ? 0 : 1);
            this.ability = ability;
        }

        private void toggle() {
            textureIndex = textureIndex == 0 ? 1 : 0;
        }

        private boolean requiresEquipped() {
            return textureIndex == 0;
        }
    }

    private static final class CustomSettingButton extends ToolSettingsButton {
        private final Ability ability;

        private CustomSettingButton(int buttonId, int x, int y, Ability ability, int value) {
            super(buttonId, x, y, 16, 16, customTextures(ability), customLocalizationKeys(ability), Math.max(0, Math.min(1, value)));
            this.ability = ability;
        }

        private void cycleValue() {
            textureIndex = (textureIndex + 1) % 2;
        }

        private int getValue() {
            return textureIndex;
        }
    }

    private static ResourceLocation[] bindingTextures(Ability.BindingType bindingType) {
        if (bindingType == Ability.BindingType.CUSTOM_ONLY) {
            return new ResourceLocation[] {buttonTexture("click-sneak.png")};
        }
        return new ResourceLocation[] {buttonTexture("click-right.png"), buttonTexture("click-left.png"), buttonTexture("click-sneak.png")};
    }

    private static String[] bindingLocalizationKeys(Ability.BindingType bindingType) {
        if (bindingType == Ability.BindingType.CUSTOM_ONLY) {
            return new String[] {"justdirethings.screen.click-custom"};
        }
        return new String[] {"justdirethings.screen.click-right", "justdirethings.screen.click-left", "justdirethings.screen.click-custom"};
    }

    private static int bindingTextureIndex(int mode, Ability.BindingType bindingType) {
        if (bindingType == Ability.BindingType.CUSTOM_ONLY) {
            return 0;
        }
        return Math.max(0, Math.min(2, mode));
    }

    private static ResourceLocation[] customTextures(Ability ability) {
        if (ability.getCustomSettingType() == Ability.CustomSettingType.TARGET) {
            return new ResourceLocation[] {buttonTexture("mobscanner.png"), buttonTexture("entity-all.png")};
        }
        return new ResourceLocation[] {buttonTexture("showfakeplayer.png"), buttonTexture("decoy.png")};
    }

    private static String[] customLocalizationKeys(Ability ability) {
        if (ability.getCustomSettingType() == Ability.CustomSettingType.TARGET) {
            return new String[] {"justdirethings.screen.target-hostile", "justdirethings.screen.target-living"};
        }
        return new String[] {"justdirethings.screen.showrender", "justdirethings.screen.hiderender"};
    }

    private static String bindingLine(AbilityBinding binding) {
        if (binding == null) {
            return I18n.format("justdirethings.unbound-screen");
        }
        if (binding.isMouseBinding()) {
            String name = Mouse.getButtonName(binding.getKeyCode());
            return I18n.format("justdirethings.bound-mouse", name == null ? binding.getKeyCode() : name);
        }
        String keyName = Keyboard.getKeyName(binding.getKeyCode());
        return I18n.format("justdirethings.bound-key", keyName == null ? binding.getKeyCode() : keyName);
    }

    private static ResourceLocation buttonTexture(String textureName) {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/buttons/" + textureName);
    }
}
