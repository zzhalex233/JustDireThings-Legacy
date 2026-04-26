package com.zzhalex.justdirethings.client.gui.machine;

import com.zzhalex.justdirethings.client.gui.base.GuiMachineBase;
import com.zzhalex.justdirethings.client.gui.button.MachineButtonFactory;
import com.zzhalex.justdirethings.common.container.machine.ContainerExperienceHolder;
import com.zzhalex.justdirethings.common.tile.base.MachineSettingKeys;
import com.zzhalex.justdirethings.common.tile.machine.TileExperienceHolder;
import com.zzhalex.justdirethings.common.util.ExperienceUtils;

public class GuiExperienceHolder extends GuiMachineBase {

    private static final String STORE_EXPERIENCE = MachineSettingKeys.STORE_EXPERIENCE;
    private static final String EXTRACT_EXPERIENCE = MachineSettingKeys.EXTRACT_EXPERIENCE;
    private static final String TARGET_EXPERIENCE = MachineSettingKeys.TARGET_EXPERIENCE;

    private final ContainerExperienceHolder container;

    public GuiExperienceHolder(ContainerExperienceHolder container) {
        super(container);
        this.container = container;
    }

    @Override
    protected void addMachineButtons() {
        TileExperienceHolder tile = container.getTile();
        int center = topSectionWidth / 2;
        addMachineButton(MachineButtonFactory.storeExperienceButton(center + 15, 62));
        addMachineButton(MachineButtonFactory.extractExperienceButton(center - 33, 62));
        addMachineButton(MachineButtonFactory.targetExperienceButton(center - 57, 64, tile.getTargetExperience()));
        addMachineButton(MachineButtonFactory.ownerOnlyButton(center - 75, 62, tile.isOwnerOnly()));
        addMachineButton(MachineButtonFactory.collectExperienceButton(center + 15, 42, tile.isCollectExperience()));
        addMachineButton(MachineButtonFactory.showParticlesButton(center + 31, 42, tile.isShowParticles()));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        drawExperienceBar();
    }

    private void drawExperienceBar() {
        int barLeft = topSectionLeft + (topSectionWidth - 182) / 2;
        int barTop = topSectionTop + topSectionHeight - 15;
        drawRect(barLeft, barTop, barLeft + 182, barTop + 5, 0xFF4B2D13);
        int filled = (int) (ExperienceUtils.getProgressToNextLevel(container.getTile().getStoredExperience()) * 182.0F);
        if (filled > 0) {
            drawRect(barLeft, barTop, barLeft + filled, barTop + 5, 0xFF80FF20);
        }
        String level = Integer.toString(ExperienceUtils.getLevelFromTotalExperience(container.getTile().getStoredExperience()));
        int textX = barLeft + 91 - getMachineTextWidth(level) / 2;
        int textY = barTop - 12;
        drawMachineText(level, textX + 1, textY, 0);
        drawMachineText(level, textX - 1, textY, 0);
        drawMachineText(level, textX, textY + 1, 0);
        drawMachineText(level, textX, textY - 1, 0);
        drawMachineText(level, textX, textY, 0x80FF20);
    }
}
