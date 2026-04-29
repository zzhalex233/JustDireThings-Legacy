package com.zzhalex.justdirethings.common.item;

import com.zzhalex.justdirethings.common.item.tooltip.TooltipHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemUpgradeContent extends ItemSimpleContent {

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        String abilityId = TooltipHelper.abilityIdFromUpgradeItem(this);
        if (abilityId.isEmpty()) {
            return;
        }
        if (GuiScreen.isShiftKeyDown()) {
            TooltipHelper.appendUpgradeDetails(abilityId, tooltip);
        } else {
            TooltipHelper.appendShiftForInfo(stack, tooltip);
        }
    }
}
