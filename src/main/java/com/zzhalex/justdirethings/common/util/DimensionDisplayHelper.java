package com.zzhalex.justdirethings.common.util;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class DimensionDisplayHelper {

    private DimensionDisplayHelper() {
    }

    public static String getDimensionName(int dimension) {
        try {
            WorldProvider provider = DimensionManager.getProvider(dimension);
            if (provider != null && provider.getDimensionType() != null) {
                return provider.getDimensionType().getName();
            }
        } catch (RuntimeException ignored) {
        }
        return Integer.toString(dimension);
    }

    public static ITextComponent getDimensionComponent(int dimension) {
        return new TextComponentString(getDimensionName(dimension));
    }

    @SideOnly(Side.CLIENT)
    public static String getTranslatedDimensionName(int dimension) {
        String name = getDimensionName(dimension);
        return I18n.hasKey(name) ? I18n.format(name) : name;
    }
}
