package com.zzhalex.justdirethings.common.item;

import com.zzhalex.justdirethings.common.item.tooltip.TooltipHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemPolymorphicCatalyst extends ItemSimpleContent {

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (GuiScreen.isShiftKeyDown()) {
            tooltip.add(TextFormatting.LIGHT_PURPLE + I18n.format("justdirethings.hint.dropinwater"));
        } else {
            TooltipHelper.appendShiftForInfo(stack, tooltip);
        }
    }
}
