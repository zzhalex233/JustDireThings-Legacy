package com.zzhalex.justdirethings.client.gui.machine;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.client.gui.base.GuiMachineBase;
import com.zzhalex.justdirethings.client.gui.button.MachineButtonFactory;
import com.zzhalex.justdirethings.common.container.machine.ContainerBlockSwapper;
import com.zzhalex.justdirethings.common.tile.base.MachineSettingKeys;
import com.zzhalex.justdirethings.common.tile.machine.TileBlockSwapper;
import net.minecraft.util.ResourceLocation;

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
        addMachineButton(MachineButtonFactory.tickSpeedButton(tile.getTickSpeed()));
        addMachineButton(MachineButtonFactory.swapEntityTypeButton(106, 38, tile.getSwapEntityType()));
        addMachineButton(MachineButtonFactory.swapBlocksButton(88, 38, tile.isSwapBlocks()));
        addMachineButton(MachineButtonFactory.redstoneButton(124, 38, tile.getRedstoneState().getMode().ordinal()));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        mc.getTextureManager().bindTexture(container.getTile().doesPartnerExist() ? ACTIVE_BUTTON : INACTIVE_BUTTON);
        drawModalRectWithCustomSizedTexture(topSectionLeft + 70, topSectionTop + 38, 0, 0, 16, 16, 16, 16);
    }
}
