package com.zzhalex.justdirethings.common.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemTimeCrystal extends ItemSimpleContent {

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        String key = (System.currentTimeMillis() / 10000L) % 2L == 0L
                ? "justdirethings.timecrystaltooltip"
                : "justdirethings.timecrystaltooltiptwo";
        tooltip.add(TextFormatting.DARK_AQUA + I18n.format(key));
    }
}
