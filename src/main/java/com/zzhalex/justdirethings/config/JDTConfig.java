package com.zzhalex.justdirethings.config;

import com.zzhalex.justdirethings.Reference;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Reference.MOD_ID, name = Reference.MOD_ID)
public final class JDTConfig {

    @Config.Name("toolMaxBreakFerricore")
    @Config.Comment("Maximum connected blocks a Ferricore tool can break through mining abilities such as Ore Miner, Tree Feller, and Sky Sweeper.")
    public static int toolMaxBreakFerricore = 64;

    @Config.Name("toolMaxBreakBlazegold")
    @Config.Comment("Maximum connected blocks a Blazegold tool can break through mining abilities such as Ore Miner, Tree Feller, and Sky Sweeper.")
    public static int toolMaxBreakBlazegold = 128;

    @Config.Name("toolMaxBreakCelestigem")
    @Config.Comment("Maximum connected blocks a Celestigem tool can break through mining abilities such as Ore Miner, Tree Feller, and Sky Sweeper.")
    public static int toolMaxBreakCelestigem = 192;

    @Config.Name("toolMaxBreakEclipsealloy")
    @Config.Comment("Maximum connected blocks an Eclipse Alloy tool can break through mining abilities such as Ore Miner, Tree Feller, and Sky Sweeper.")
    public static int toolMaxBreakEclipsealloy = 256;

    @Config.Name("disabledAbilities")
    @Config.Comment("Ability ids to disable globally. Disabled abilities are hidden from all tools and cannot be used or installed.")
    public static String[] disabledAbilities = new String[0];

    @Config.Name("pocketGeneratorMaxFe")
    @Config.Comment("Maximum FE buffer for the pocket generator.")
    public static int pocketGeneratorMaxFe = 1000000;

    @Config.Name("pocketGeneratorFePerFuelTick")
    @Config.Comment("FE created per vanilla fuel burn tick by the pocket generator.")
    public static int pocketGeneratorFePerFuelTick = 15;

    @Config.Name("pocketGeneratorBurnSpeedMultiplier")
    @Config.Comment("Base burn speed multiplier for the pocket generator.")
    public static int pocketGeneratorBurnSpeedMultiplier = 4;

    @Config.Name("pocketGeneratorFePerTick")
    @Config.Comment("FE per tick output budget for the pocket generator.")
    public static int pocketGeneratorFePerTick = 5000;

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

    @Config.Name("timeCrystalCustomDimensions")
    @Config.Comment("Customize Time Crystal growth dimensions. When false, stage 1 charges outside the Nether and End, stage 2 in the Nether, and stage 3 in the End.")
    public static boolean timeCrystalCustomDimensions = false;

    @Config.Name("timeCrystalStage1Dimensions")
    @Config.Comment("Dimensions where Budding Time Crystal blocks can advance to stage 1 when custom dimensions are enabled. Accepts dimension ids such as 0 or names such as minecraft:overworld.")
    public static String[] timeCrystalStage1Dimensions = new String[0];

    @Config.Name("timeCrystalStage2Dimensions")
    @Config.Comment("Dimensions where Budding Time Crystal blocks can advance to stage 2 when custom dimensions are enabled. Accepts dimension ids such as -1 or names such as minecraft:the_nether.")
    public static String[] timeCrystalStage2Dimensions = new String[0];

    @Config.Name("timeCrystalStage3Dimensions")
    @Config.Comment("Dimensions where Budding Time Crystal blocks can advance to stage 3 when custom dimensions are enabled. Accepts dimension ids such as 1 or names such as minecraft:the_end.")
    public static String[] timeCrystalStage3Dimensions = new String[0];

    @Config.RequiresMcRestart
    @Config.Name("enableSmithingTemplates")
    @Config.Comment("Enable the four Just Dire Things smithing template items and require them for tier upgrades in the Upgrade Station.")
    public static boolean enableSmithingTemplates = true;

    @Config.Name("gooCanDie")
    @Config.Comment("Allow goo blocks to return to their dead state after completing a goo spread recipe.")
    public static boolean gooCanDie = true;

    @Config.Name("gooDeathChance")
    @Config.Comment("Chance for a live goo block to die after finishing one adjacent conversion.")
    public static double gooDeathChance = 0.1D;

    @Config.Name("portalGunV2RfCapacity")
    @Config.Comment("Maximum FE buffer for the Portal Gun V2.")
    public static int portalGunV2RfCapacity = 1000000;

    @Config.Name("portalGunV2RfCost")
    @Config.Comment("FE cost to fire the Portal Gun V2.")
    public static int portalGunV2RfCost = 5000;

    @Config.Name("portalGunV1RfCapacity")
    @Config.Comment("Maximum FE buffer for the classic Portal Gun.")
    public static int portalGunV1RfCapacity = 100000;

    @Config.Name("portalGunV1RfCost")
    @Config.Comment("FE cost to fire the classic Portal Gun.")
    public static int portalGunV1RfCost = 1000;

    @Config.Name("generatorT1FePerFuelTick")
    @Config.Comment("FE created per vanilla fuel burn tick by the Coal Generator T1.")
    public static int generatorT1FePerFuelTick = 15;

    @Config.Name("generatorT1BurnSpeedMultiplier")
    @Config.Comment("Base burn speed multiplier for the Coal Generator T1.")
    public static int generatorT1BurnSpeedMultiplier = 4;

    @Config.Name("generatorT1MaxFe")
    @Config.Comment("Maximum FE buffer for the Coal Generator T1.")
    public static int generatorT1MaxFe = 1000000;

    @Config.Name("generatorT1FePerTick")
    @Config.Comment("FE per tick output budget for the Coal Generator T1.")
    public static int generatorT1FePerTick = 1000;

    @Config.Name("generatorFluidT1MaxFe")
    @Config.Comment("Maximum FE buffer for the Fluid Generator T1.")
    public static int generatorFluidT1MaxFe = 5000000;

    @Config.Name("generatorFluidT1FePerTick")
    @Config.Comment("FE per tick output budget for the Fluid Generator T1.")
    public static int generatorFluidT1FePerTick = 5000;

    @Config.Name("fuelTier2FePerMb")
    @Config.Comment("FE produced per mB of Tier 2 refined fuel.")
    public static int fuelTier2FePerMb = 450;

    @Config.Name("fuelTier3FePerMb")
    @Config.Comment("FE produced per mB of Tier 3 refined fuel.")
    public static int fuelTier3FePerMb = 1300;

    @Config.Name("fuelTier4FePerMb")
    @Config.Comment("FE produced per mB of Tier 4 refined fuel.")
    public static int fuelTier4FePerMb = 4000;

    @Config.Name("energyTransmitterT1MaxRf")
    @Config.Comment("Maximum FE buffer for the Energy Transmitter T1.")
    public static int energyTransmitterT1MaxRf = 1000000;

    @Config.Name("energyTransmitterT1RfPerTick")
    @Config.Comment("Maximum FE transmitted per tick by the Energy Transmitter T1.")
    public static int energyTransmitterT1RfPerTick = 1000;

    @Config.Name("energyTransmitterT1LossPerBlock")
    @Config.Comment("Energy loss per Manhattan-distance block in percent.")
    public static double energyTransmitterT1LossPerBlock = 1.0D;

    @Config.Name("playerAccessorDimensionalBlacklisting")
    @Config.Comment("Enable dimension blacklist checks for the Player Accessor.")
    public static boolean playerAccessorDimensionalBlacklisting = false;

    @Config.Name("playerAccessorValidationTime")
    @Config.Comment("The frequency in ticks with which the Player Accessor validates the linked player.")
    @Config.RangeInt(min = 0, max = 5000)
    public static int playerAccessorValidationTime = 100;

    @Config.Name("playerAccessorBlacklistedDimensions")
    @Config.Comment("Dimension resource ids such as minecraft:overworld, legacy dimension names, or numeric ids where Player Accessor cannot access the linked player.")
    public static String[] playerAccessorBlacklistedDimensions = new String[0];

    @Config.Name("paradoxRfCapacity")
    @Config.Comment("Maximum FE buffer for the Paradox Machine.")
    public static int paradoxRfCapacity = 10000000;

    @Config.Name("paradoxFluidCapacity")
    @Config.Comment("Maximum Time Fluid buffer for the Paradox Machine.")
    public static int paradoxFluidCapacity = 16000;

    @Config.Name("paradoxRfPerBlock")
    @Config.Comment("FE cost to restore one block with the Paradox Machine.")
    public static int paradoxRfPerBlock = 250000;

    @Config.Name("paradoxRfPerEntity")
    @Config.Comment("FE cost to restore one entity with the Paradox Machine.")
    public static int paradoxRfPerEntity = 250000;

    @Config.Name("paradoxFluidPerBlock")
    @Config.Comment("Time Fluid cost in mB to restore one block with the Paradox Machine.")
    public static int paradoxFluidPerBlock = 50;

    @Config.Name("paradoxFluidPerEntity")
    @Config.Comment("Time Fluid cost in mB to restore one entity with the Paradox Machine.")
    public static int paradoxFluidPerEntity = 50;

    @Config.Name("paradoxEnergyPerBlock")
    @Config.Comment("Paradox Energy accumulated per restored block.")
    public static double paradoxEnergyPerBlock = 0.25D;

    @Config.Name("paradoxEnergyPerEntity")
    @Config.Comment("Paradox Energy accumulated per restored entity.")
    public static double paradoxEnergyPerEntity = 0.25D;

    @Config.Name("paradoxEnergyMax")
    @Config.Comment("Maximum Paradox Energy before the Paradox Machine spawns a Paradox.")
    public static double paradoxEnergyMax = 100.0D;

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
