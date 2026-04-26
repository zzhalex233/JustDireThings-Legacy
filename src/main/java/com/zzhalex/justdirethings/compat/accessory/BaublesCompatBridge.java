package com.zzhalex.justdirethings.compat.accessory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Loader;

public final class BaublesCompatBridge {

    private static final String MOD_ID = "baubles";

    private BaublesCompatBridge() {
    }

    public static boolean isLoaded() {
        return Loader.isModLoaded(MOD_ID);
    }

    public static AccessoryInventoryBridge create(EntityPlayer player) {
        return BubblesCompatBridge.create(player);
    }
}
