package com.zzhalex.justdirethings.client.gui.machine;

import com.zzhalex.justdirethings.client.gui.base.GuiMachineBase;
import com.zzhalex.justdirethings.client.gui.button.MachineButtonFactory;
import com.zzhalex.justdirethings.client.gui.button.MachineGuiButton;
import com.zzhalex.justdirethings.common.container.machine.ContainerExperienceHolder;
import com.zzhalex.justdirethings.common.tile.base.MachineSettingKeys;
import com.zzhalex.justdirethings.common.tile.machine.TileExperienceHolder;
import com.zzhalex.justdirethings.common.util.ExperienceUtils;
import com.zzhalex.justdirethings.network.JDTNetwork;
import com.zzhalex.justdirethings.network.message.MessageMachineSetting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

import java.io.IOException;

public class GuiExperienceHolder extends GuiMachineBase {

    private final ContainerExperienceHolder container;

    public GuiExperienceHolder(ContainerExperienceHolder container) {
        super(container);
        this.container = container;
    }

    @Override
    protected void addMachineButtons() {
        TileExperienceHolder tile = container.getTile();
        addMachineButtons(MachineButtonFactory.wideTimedMachineButtons(
                tile.getTickSpeed(),
                tile.getRedstoneState().getMode().ordinal()
        ));
        addMachineButtons(MachineButtonFactory.areaButtons(
                tile.getAreaState().isRenderArea(),
                tile.getAreaState().getXRadius(),
                tile.getAreaState().getYRadius(),
                tile.getAreaState().getZRadius(),
                tile.getAreaState().getXOffset(),
                tile.getAreaState().getYOffset(),
                tile.getAreaState().getZOffset()
        ));

        int center = topSectionWidth / 2;
        int topSectionOffset = extraWidth / 2;
        addMachineButton(MachineButtonFactory.storeExperienceButton(center + 15 - topSectionOffset, 62));
        addMachineButton(MachineButtonFactory.extractExperienceButton(center - 33 - topSectionOffset, 62));
        addMachineButton(MachineButtonFactory.targetExperienceButton(center - 57 - topSectionOffset, 64, tile.getTargetExperience()));
        addMachineButton(MachineButtonFactory.ownerOnlyButton(center - 75 - topSectionOffset, 62, tile.isOwnerOnly()));
        addMachineButton(MachineButtonFactory.collectExperienceButton(center + 15 - topSectionOffset, 42, tile.isCollectExperience()));
        addMachineButton(MachineButtonFactory.showParticlesButton(center + 31 - topSectionOffset, 42, tile.isShowParticles()));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        drawExperienceBar();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button instanceof MachineGuiButton) {
            MachineGuiButton machineButton = (MachineGuiButton) button;
            String key = machineButton.getDefinition().getSettingKey();
            if (MachineSettingKeys.STORE_EXPERIENCE.equals(key) || MachineSettingKeys.EXTRACT_EXPERIENCE.equals(key)) {
                int amount = isCtrlKeyDown() ? -1 : isShiftKeyDown() ? 10 : 1;
                JDTNetwork.getChannel().sendToServer(new MessageMachineSetting(inventorySlots.windowId, key, amount));
                return;
            }
        }
        super.actionPerformed(button);
    }

    private void drawExperienceBar() {
        int barLeft = topSectionLeft + (topSectionWidth - 182) / 2;
        int barTop = topSectionTop + topSectionHeight - 15;
        mc.getTextureManager().bindTexture(Gui.ICONS);
        drawTexturedModalRect(barLeft, barTop, 0, 64, 182, 5);
        int filled = (int) (ExperienceUtils.getProgressToNextLevel(container.getTile().getStoredExperience()) * 183.0F);
        if (filled > 0) {
            drawTexturedModalRect(barLeft, barTop, 0, 69, filled, 5);
        }
        String level = Integer.toString(ExperienceUtils.getLevelFromTotalExperience(container.getTile().getStoredExperience()));
        int textX = topSectionLeft + topSectionWidth / 2 - getMachineTextWidth(level) / 2;
        int textY = topSectionTop + 62 + fontRenderer.FONT_HEIGHT / 2;
        drawMachineText(level, textX + 1, textY, 0);
        drawMachineText(level, textX - 1, textY, 0);
        drawMachineText(level, textX, textY + 1, 0);
        drawMachineText(level, textX, textY - 1, 0);
        drawMachineText(level, textX, textY, 0x80FF20);
    }
}
