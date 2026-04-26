package com.zzhalex.justdirethings.compat.content;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class FutureMcCompat {

    public static final String MOD_ID = "futuremc";

    private FutureMcCompat() {
    }

    public static boolean isLoaded() {
        return Loader.isModLoaded(MOD_ID);
    }

    public static boolean hasRegisteredItem(ResourceLocation id) {
        return isLoaded() && ForgeRegistries.ITEMS.containsKey(id);
    }

    public static Item findItem(ResourceLocation id) {
        if (!hasRegisteredItem(id)) {
            return null;
        }
        return ForgeRegistries.ITEMS.getValue(id);
    }
}
