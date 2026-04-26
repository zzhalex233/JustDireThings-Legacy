package com.zzhalex.justdirethings.compat.accessory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.fml.common.Loader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class BubblesCompatBridge {

    private static final String PRIMARY_MOD_ID = "baubles";
    private static final String LEGACY_MOD_ID = "bubbles";
    private static final String API_CLASS = "baubles.api.BaublesApi";
    private static final String GET_HANDLER_METHOD = "getBaublesHandler";

    private BubblesCompatBridge() {
    }

    public static boolean isLoaded() {
        return Loader.isModLoaded(PRIMARY_MOD_ID) || Loader.isModLoaded(LEGACY_MOD_ID);
    }

    public static AccessoryInventoryBridge create(EntityPlayer player) {
        if (player == null) {
            return AccessoryInventoryBridge.empty();
        }
        try {
            Class<?> apiClass = Class.forName(API_CLASS);
            Method getBaublesHandler = apiClass.getMethod(GET_HANDLER_METHOD, EntityPlayer.class);
            Object handler = getBaublesHandler.invoke(null, player);
            if (handler instanceof IItemHandler) {
                return AccessoryInventoryBridge.fromItemHandler((IItemHandler) handler);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | LinkageError ignored) {
            return AccessoryInventoryBridge.empty();
        }
        return AccessoryInventoryBridge.empty();
    }
}
