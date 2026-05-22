package com.zzhalex.justdirethings.client.gui.upstream;

import com.zzhalex.justdirethings.client.ClientPortalKeys;
import com.zzhalex.justdirethings.common.item.tool.ItemPortalGunV2;
import com.zzhalex.justdirethings.common.portal.PortalLinkData;
import com.zzhalex.justdirethings.network.JDTNetwork;
import com.zzhalex.justdirethings.network.message.MessagePortalGunFavoriteChange;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class AdvPortalEditMenu extends GuiScreen {

    private static final int BUTTON_SAVE = 1;
    private static final int BUTTON_CANCEL = 2;

    private final ItemStack portalGun;
    private final int slotSelected;
    private int ticksOpened;
    private GuiTextField nameField;
    private GuiTextField xPos;
    private GuiTextField yPos;
    private GuiTextField zPos;

    public AdvPortalEditMenu(ItemStack portalGun, int slotSelected) {
        this.portalGun = portalGun == null ? ItemStack.EMPTY : portalGun;
        this.slotSelected = slotSelected;
    }

    @Override
    public void initGui() {
        nameField = new GuiTextField(0, fontRenderer, width / 2 - 75, height / 2 - 34, 200, fontRenderer.FONT_HEIGHT + 3);
        xPos = new GuiTextField(1, fontRenderer, width / 2 - 75, height / 2 - 20, 60, fontRenderer.FONT_HEIGHT + 3);
        yPos = new GuiTextField(2, fontRenderer, width / 2 - 5, height / 2 - 20, 60, fontRenderer.FONT_HEIGHT + 3);
        zPos = new GuiTextField(3, fontRenderer, width / 2 + 65, height / 2 - 20, 60, fontRenderer.FONT_HEIGHT + 3);
        updateNameField();

        nameField.setMaxStringLength(15);
        nameField.setFocused(true);
        xPos.setEnabled(false);
        yPos.setEnabled(false);
        zPos.setEnabled(false);

        buttonList.clear();
        buttonList.add(new GuiButton(BUTTON_SAVE, width / 2 - 75, height / 2, 120, 16, I18n.format("justdirethings.screen.save_close")));
        buttonList.add(new GuiButton(BUTTON_CANCEL, width / 2 + 60, height / 2, 65, 16, I18n.format("justdirethings.screen.cancel")));
    }

    @Override
    public void updateScreen() {
        ticksOpened++;
        nameField.updateCursorCounter();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == BUTTON_SAVE) {
            addFavorite();
        } else if (button.id == BUTTON_CANCEL) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (ticksOpened < 400 && keyCode == ClientPortalKeys.TOGGLE_TOOL.getKeyCode()) {
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
            return;
        }
        if (nameField.textboxKeyTyped(typedChar, keyCode)) {
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        nameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        nameField.drawTextBox();
        xPos.drawTextBox();
        yPos.drawTextBox();
        zPos.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public void addFavorite() {
        PortalLinkData.PortalDestination destination = getFavorite(slotSelected);
        Vec3d coordinates = destination.isEmpty()
                ? Vec3d.ZERO
                : new Vec3d(destination.getX(), destination.getY(), destination.getZ());
        JDTNetwork.getChannel().sendToServer(new MessagePortalGunFavoriteChange(slotSelected, true, nameField.getText(), true, coordinates));
        mc.displayGuiScreen(null);
    }

    public void updateNameField() {
        PortalLinkData.PortalDestination destination = currentDestination();
        nameField.setText(destination.isEmpty() ? "UNNAMED" : destination.getName());
        xPos.setText(String.format("%.2f", destination.getX()));
        yPos.setText(String.format("%.2f", destination.getY()));
        zPos.setText(String.format("%.2f", destination.getZ()));
    }

    private PortalLinkData.PortalDestination currentDestination() {
        PortalLinkData.PortalDestination destination = getFavorite(slotSelected);
        if (!destination.isEmpty()) {
            return destination;
        }
        EntityPlayer player = Minecraft.getMinecraft().player;
        return player == null ? PortalLinkData.PortalDestination.EMPTY : PortalLinkData.PortalDestination.fromPlayer(player, "UNNAMED");
    }

    private PortalLinkData.PortalDestination getFavorite(int slot) {
        if (portalGun != null && !portalGun.isEmpty() && portalGun.getItem() instanceof ItemPortalGunV2) {
            return ((ItemPortalGunV2) portalGun.getItem()).getLinkData(portalGun).getFavorite(slot);
        }
        return PortalLinkData.PortalDestination.EMPTY;
    }
}
