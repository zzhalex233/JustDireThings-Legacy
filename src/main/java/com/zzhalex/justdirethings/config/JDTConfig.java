package com.zzhalex.justdirethings.config;

import com.zzhalex.justdirethings.Reference;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Reference.MOD_ID, name = Reference.MOD_ID)
public final class JDTConfig {

    @Config.Name("enableFallbackCompat")
    @Config.Comment("Allow fixed fallback mappings when a modern content source such as FutureMC is unavailable.")
    @Config.RequiresMcRestart
    public static boolean enableFallbackCompat = true;

    @Config.Name("enableFutureMcMaterialResolution")
    @Config.Comment("Prefer FutureMC items and blocks when available.")
    @Config.RequiresMcRestart
    public static boolean enableFutureMcMaterialResolution = true;

    @Config.Name("pocketGeneratorMaxFe")
    @Config.Comment("Maximum FE buffer for the pocket generator.")
    public static int pocketGeneratorMaxFe = 100000;

    @Config.Name("timeWandRfCost")
    @Config.Comment("Base RF cost per Time Wand acceleration tier.")
    public static int timeWandRfCost = 100;

    @Config.Name("timeWandRfCapacity")
    @Config.Comment("Maximum FE buffer for the Time Wand.")
    public static int timeWandRfCapacity = 100000;

    @Config.Name("timeWandFluidCost")
    @Config.Comment("Base Time Fluid cost per Time Wand acceleration tier.")
    public static double timeWandFluidCost = 0.5D;

    @Config.Name("timeWandMaxMultiplier")
    @Config.Comment("Maximum acceleration multiplier for the Time Wand. Should stay a power of two.")
    public static int timeWandMaxMultiplier = 256;

    @Config.Name("timeWandFakePlayerAllowed")
    @Config.Comment("Allow fake players to use the Time Wand.")
    public static boolean timeWandFakePlayerAllowed = true;

    @Config.Name("portalGunV2RfCapacity")
    @Config.Comment("Maximum FE buffer for the Portal Gun V2.")
    public static int portalGunV2RfCapacity = 100000;

    @Config.Name("portalGunV2RfCost")
    @Config.Comment("FE cost to fire the Portal Gun V2.")
    public static int portalGunV2RfCost = 500;

    @Config.Name("portalGunV1RfCapacity")
    @Config.Comment("Maximum FE buffer for the classic Portal Gun.")
    public static int portalGunV1RfCapacity = 100000;

    @Config.Name("portalGunV1RfCost")
    @Config.Comment("FE cost to fire the classic Portal Gun.")
    public static int portalGunV1RfCost = 1000;

    @Config.Name("paradoxRestrictedMobs")
    @Config.Comment("Strip risky mob inventory and equipment data during paradox restore.")
    public static boolean paradoxRestrictedMobs = false;

    private JDTConfig() {
    }

    public static void sync() {
        ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    public static final class EventHandler {

        private EventHandler() {
        }

        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (Reference.MOD_ID.equals(event.getModID())) {
                sync();
            }
        }
    }
}
