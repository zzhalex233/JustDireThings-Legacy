package com.zzhalex.justdirethings.client.gui.upstream;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.client.gui.GuiNineSlice;
import com.zzhalex.justdirethings.client.gui.button.ButtonDefinition;
import com.zzhalex.justdirethings.client.gui.button.MachineGuiButton;
import com.zzhalex.justdirethings.common.item.misc.ItemMachineSettingsCopier;
import com.zzhalex.justdirethings.network.JDTNetwork;
import com.zzhalex.justdirethings.network.message.MessageCopyMachineSettings;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Collections;

public class MachineSettingsCopierScreen extends GuiScreen {

    private static final int BUTTON_AREA = 0;
    private static final int BUTTON_OFFSET = 1;
    private static final int BUTTON_FILTER = 2;
    private static final int BUTTON_REDSTONE = 3;
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;
    private static final int PANEL_WIDTH = 140;
    private static final int PANEL_HEIGHT = 60;
    private static final ResourceLocation BACKGROUND = new ResourceLocation(Reference.MOD_ID, "textures/gui/sprites/background.png");

    private final ItemStack stack;
    private boolean area;
    private boolean offset;
    private boolean filter;
    private boolean redstone;
    private int panelLeft;
    private int panelTop;

    public MachineSettingsCopierScreen(ItemStack stack) {
        this.stack = stack == null ? ItemStack.EMPTY : stack;
        area = ItemMachineSettingsCopier.getCopyArea(this.stack);
        offset = ItemMachineSettingsCopier.getCopyOffset(this.stack);
        filter = ItemMachineSettingsCopier.getCopyFilter(this.stack);
        redstone = ItemMachineSettingsCopier.getCopyRedstone(this.stack);
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int guiLeft = (width - GUI_WIDTH) / 2;
        int guiTop = (height - GUI_HEIGHT) / 2;
        panelLeft = guiLeft - 10;
        panelTop = guiTop;
        buttonList.add(button(BUTTON_AREA, PANEL_WIDTH / 2 - 40, 12, "area.png", "justdirethings.screen.copy_area", area));
        buttonList.add(button(BUTTON_OFFSET, PANEL_WIDTH / 2 + 24, 12, "stepheight.png", "justdirethings.screen.copy_offset", offset));
        buttonList.add(button(BUTTON_FILTER, PANEL_WIDTH / 2 - 40, 32, "matchnbttrue.png", "justdirethings.screen.copy_filter", filter));
        buttonList.add(button(BUTTON_REDSTONE, PANEL_WIDTH / 2 + 24, 32, "redstonepulse.png", "justdirethings.screen.copy_redstone", redstone));
    }

    private MachineGuiButton button(int id, int x, int y, String texture, String key, boolean active) {
        return new MachineGuiButton(id, panelLeft, panelTop, ButtonDefinition.grayscale(x, y, key, active,
                new ButtonDefinition.State("textures/gui/buttons/" + texture, key)));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!(button instanceof MachineGuiButton)) {
            return;
        }
        boolean active = ((MachineGuiButton) button).nextValue(false) == 1;
        if (button.id == BUTTON_AREA) {
            area = active;
        } else if (button.id == BUTTON_OFFSET) {
            offset = active;
        } else if (button.id == BUTTON_FILTER) {
            filter = active;
        } else if (button.id == BUTTON_REDSTONE) {
            redstone = active;
        } else {
            return;
        }
        ItemMachineSettingsCopier.setSettings(stack, area, offset, filter, redstone);
        JDTNetwork.getChannel().sendToServer(new MessageCopyMachineSettings(area, offset, filter, redstone));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        mc.getTextureManager().bindTexture(BACKGROUND);
        GuiNineSlice.draw(panelLeft, panelTop - 20, PANEL_WIDTH, 20);
        GuiNineSlice.draw(panelLeft, panelTop, PANEL_WIDTH, PANEL_HEIGHT);
        String title = stack.isEmpty() ? "" : stack.getDisplayName();
        fontRenderer.drawString(title, panelLeft + PANEL_WIDTH / 2 - fontRenderer.getStringWidth(title) / 2, panelTop - 14, 0x404040);
        super.drawScreen(mouseX, mouseY, partialTicks);
        for (GuiButton button : buttonList) {
            if (button instanceof MachineGuiButton && button.isMouseOver()) {
                String tooltip = ((MachineGuiButton) button).getTooltipText();
                if (!tooltip.isEmpty()) {
                    drawHoveringText(Collections.singletonList(tooltip), mouseX, mouseY, fontRenderer);
                }
                break;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1 || mc.gameSettings.keyBindInventory.getKeyCode() == keyCode) {
            mc.displayGuiScreen(null);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
