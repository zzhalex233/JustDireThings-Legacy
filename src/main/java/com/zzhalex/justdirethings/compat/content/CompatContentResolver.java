package com.zzhalex.justdirethings.compat.content;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public final class CompatContentResolver {

    private CompatContentResolver() {
    }

    public static ResourceLocation fallbackId(CompatContentKey key) {
        return key.getFallbackId();
    }

    public static String fallbackIdString(CompatContentKey key) {
        return key.getFallbackIdString();
    }

    public static ResourceLocation futureMcId(CompatContentKey key) {
        return key.getFutureMcId();
    }

    public static boolean hasFutureMcSupport(CompatContentKey key) {
        return key.getFutureMcId() != null;
    }

    public static ResourceLocation preferredItemId(CompatContentKey key) {
        ResourceLocation vanillaId = key.getVanillaId();
        if (vanillaId != null) {
            return vanillaId;
        }

        ResourceLocation futureMcId = key.getFutureMcId();
        if (futureMcId != null && FutureMcCompat.hasRegisteredItem(futureMcId)) {
            return futureMcId;
        }

        return key.getFallbackId();
    }

    public static Item resolveItem(CompatContentKey key) {
        ResourceLocation preferredId = preferredItemId(key);
        if (preferredId == null) {
            return null;
        }

        if (FutureMcCompat.MOD_ID.equals(preferredId.getNamespace())) {
            return FutureMcCompat.findItem(preferredId);
        }

        return net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.getValue(preferredId);
    }
}
