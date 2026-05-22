package com.zzhalex.justdirethings.client.gui.machine;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.client.gui.base.GuiMachineBase;
import com.zzhalex.justdirethings.client.gui.button.MachineButtonFactory;
import com.zzhalex.justdirethings.common.container.machine.ContainerBlockSwapper;
import com.zzhalex.justdirethings.common.tile.base.MachineSettingKeys;
import com.zzhalex.justdirethings.common.tile.base.TileAdvancedMachine;
import com.zzhalex.justdirethings.common.tile.machine.TileBlockSwapper;
import com.zzhalex.justdirethings.common.util.DimensionDisplayHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.Collections;

public class GuiBlockSwapper extends GuiMachineBase {

    private static final String SWAP_BLOCKS = MachineSettingKeys.SWAP_BLOCKS;
    private static final String SWAP_ENTITY_TYPE = MachineSettingKeys.SWAP_ENTITY_TYPE;
    private static final ResourceLocation ACTIVE_BUTTON = new ResourceLocation(Reference.MOD_ID, "textures/gui/buttons/active.png");
    private static final ResourceLocation INACTIVE_BUTTON = new ResourceLocation(Reference.MOD_ID, "textures/gui/buttons/inactive.png");

    private final ContainerBlockSwapper container;

    public GuiBlockSwapper(ContainerBlockSwapper container) {
        super(container);
        this.container = container;
    }

    @Override
    protected void addMachineButtons() {
        TileBlockSwapper tile = container.getTile();
        if (tile instanceof TileAdvancedMachine) {
            addMachineButton(MachineButtonFactory.tickSpeedButton(tile.getTickSpeed()));
            addMachineButton(MachineButtonFactory.redstoneButton(134, 62, tile.getRedstoneState().getMode().ordinal()));
            addAdvancedMachineButtons((TileAdvancedMachine) tile);
            addMachineButton(MachineButtonFactory.swapEntityTypeButton(26, 44, tile.getSwapEntityType()));
            addMachineButton(MachineButtonFactory.swapBlocksButton(8, 44, tile.isSwapBlocks()));
        } else {
            addMachineButton(MachineButtonFactory.tickSpeedButton(tile.getTickSpeed()));
            addMachineButton(MachineButtonFactory.swapEntityTypeButton(106, 38, tile.getSwapEntityType()));
            addMachineButton(MachineButtonFactory.swapBlocksButton(88, 38, tile.isSwapBlocks()));
            addMachineButton(MachineButtonFactory.redstoneButton(124, 38, tile.getRedstoneState().getMode().ordinal()));
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        mc.getTextureManager().bindTexture(container.getTile().doesPartnerExist() ? ACTIVE_BUTTON : INACTIVE_BUTTON);
        int activeX = container.getTile() instanceof TileAdvancedMachine ? topSectionLeft + 156 : topSectionLeft + 70;
        drawModalRectWithCustomSizedTexture(activeX, topSectionTop + 38, 0, 0, 16, 16, 16, 16);
    }

    @Override
    protected void drawAfterContainerBeforeTooltips(int mouseX, int mouseY, float partialTicks) {
        super.drawAfterContainerBeforeTooltips(mouseX, mouseY, partialTicks);
        int activeX = container.getTile() instanceof TileAdvancedMachine ? topSectionLeft + 156 : topSectionLeft + 70;
        int activeY = topSectionTop + 38;
        if (mouseX < activeX || mouseX >= activeX + 16 || mouseY < activeY || mouseY >= activeY + 16) {
            return;
        }

        TileBlockSwapper tile = container.getTile();
        if (tile.getBoundTo() == null) {
            drawHoveringText(Collections.singletonList(TextFormatting.DARK_RED + I18n.format("justdirethings.unbound-screen")), mouseX, mouseY);
            return;
        }

        boolean partnerExists = tile.doesPartnerExist();
        String key = partnerExists ? "justdirethings.boundto" : "justdirethings.boundto-missing";
        TextFormatting color = partnerExists ? TextFormatting.BLUE : TextFormatting.DARK_RED;
        String text = I18n.format(
                key,
                DimensionDisplayHelper.getTranslatedDimensionName(tile.getBoundDimension()),
                "[" + tile.getBoundTo().getX() + ", " + tile.getBoundTo().getY() + ", " + tile.getBoundTo().getZ() + "]"
        );
        drawHoveringText(Collections.singletonList(color + text), mouseX, mouseY);
    }
}
