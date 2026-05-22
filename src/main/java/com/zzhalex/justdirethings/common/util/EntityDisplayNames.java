package com.zzhalex.justdirethings.common.util;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class EntityDisplayNames {

    private EntityDisplayNames() {
    }

    public static ITextComponent translationComponent(String entityId) {
        String key = translationKey(entityId);
        return key.isEmpty() ? new TextComponentTranslation(entityId) : new TextComponentTranslation(key);
    }

    @SideOnly(Side.CLIENT)
    public static String translatedName(String entityId) {
        String key = translationKey(entityId);
        return key.isEmpty() ? entityId : I18n.format(key);
    }

    public static String translationKey(String entityId) {
        if (entityId == null || entityId.isEmpty()) {
            return "";
        }
        String key = EntityList.getTranslationName(new ResourceLocation(entityId));
        if (key == null || key.isEmpty()) {
            return entityId;
        }
        return "entity." + key + ".name";
    }
}
