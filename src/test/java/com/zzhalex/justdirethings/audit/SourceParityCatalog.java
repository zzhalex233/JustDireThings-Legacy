package com.zzhalex.justdirethings.audit;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

final class SourceParityCatalog {

    // Upstream: source/JustDireThings-main/.../setup/Registration.java, block registrations.
    static final Set<String> MACHINE_BLOCK_IDS = linkedSet(
            "itemcollector",
            "blockbreakert1",
            "blockbreakert2",
            "blockplacert1",
            "blockplacert2",
            "clickert1",
            "clickert2",
            "sensort1",
            "sensort2",
            "droppert1",
            "droppert2",
            "blockswappert1",
            "blockswappert2",
            "playeraccessor",
            "fluidplacert1",
            "fluidplacert2",
            "fluidcollectort1",
            "fluidcollectort2",
            "paradoxmachine",
            "inventory_holder",
            "experienceholder",
            "generatort1",
            "generatorfluidt1",
            "energytransmitter"
    );

    // Upstream: Registration.java goo, resource, and time-crystal block registrations.
    static final Set<String> CONTENT_BLOCK_IDS = linkedSet(
            "gooblock_tier1",
            "gooblock_tier2",
            "gooblock_tier3",
            "gooblock_tier4",
            "goopatternblock",
            "goosoil_tier1",
            "goosoil_tier2",
            "goosoil_tier3",
            "goosoil_tier4",
            "eclipsegateblock",
            "raw_ferricore_ore",
            "raw_blazegold_ore",
            "raw_celestigem_ore",
            "raw_eclipsealloy_ore",
            "raw_coal_t1_ore",
            "raw_coal_t2_ore",
            "raw_coal_t3_ore",
            "raw_coal_t4_ore",
            "time_crystal_block",
            "time_crystal_budding_block",
            "time_crystal_cluster",
            "time_crystal_cluster_small",
            "time_crystal_cluster_medium",
            "time_crystal_cluster_large",
            "ferricore_block",
            "blazegold_block",
            "celestigem_block",
            "eclipsealloy_block",
            "coalblock_t1",
            "coalblock_t2",
            "coalblock_t3",
            "coalblock_t4",
            "charcoal"
    );

    static final Set<String> BLOCK_IDS = union(MACHINE_BLOCK_IDS, CONTENT_BLOCK_IDS);

    // Upstream: Registration.java ITEMS, TOOLS, BOWS, ARMORS, and UPGRADES registers.
    static final Set<String> SPECIAL_ITEM_IDS = linkedSet(
            "fuel_canister",
            "pocket_generator",
            "ferricore_wrench",
            "totem_of_death_recall",
            "blazejet_wand",
            "voidshift_wand",
            "eclipsegate_wand",
            "time_wand",
            "creaturecatcher",
            "machinesettingscopier",
            "portalgun",
            "portalgun_v2",
            "fluid_canister",
            "potion_canister",
            "polymorphic_wand",
            "polymorphic_wand_v2"
    );

    static final Set<String> TOOL_AND_ARMOR_ITEM_IDS = linkedSet(
            "ferricore_sword",
            "ferricore_pickaxe",
            "ferricore_shovel",
            "ferricore_axe",
            "ferricore_hoe",
            "blazegold_sword",
            "blazegold_pickaxe",
            "blazegold_shovel",
            "blazegold_axe",
            "blazegold_hoe",
            "celestigem_sword",
            "celestigem_pickaxe",
            "celestigem_shovel",
            "celestigem_axe",
            "celestigem_hoe",
            "eclipsealloy_sword",
            "eclipsealloy_pickaxe",
            "eclipsealloy_shovel",
            "eclipsealloy_axe",
            "eclipsealloy_hoe",
            "celestigem_paxel",
            "eclipsealloy_paxel",
            "bow_ferricore",
            "bow_blazegold",
            "bow_celestigem",
            "bow_eclipsealloy",
            "ferricore_boots",
            "ferricore_chestplate",
            "ferricore_leggings",
            "ferricore_helmet",
            "blazegold_boots",
            "blazegold_chestplate",
            "blazegold_leggings",
            "blazegold_helmet",
            "celestigem_boots",
            "celestigem_chestplate",
            "celestigem_leggings",
            "celestigem_helmet",
            "eclipsealloy_boots",
            "eclipsealloy_chestplate",
            "eclipsealloy_leggings",
            "eclipsealloy_helmet"
    );

    static final Set<String> RESOURCE_ITEM_IDS = linkedSet(
            "raw_ferricore",
            "raw_blazegold",
            "raw_eclipsealloy",
            "ferricore_ingot",
            "blazegold_ingot",
            "celestigem",
            "eclipsealloy_ingot",
            "coal_t1",
            "coal_t2",
            "coal_t3",
            "coal_t4",
            "polymorphic_catalyst",
            "portal_fluid_catalyst",
            "time_crystal",
            "template_ferricore",
            "template_blazegold",
            "template_celestigem",
            "template_eclipsealloy"
    );

    static final Set<String> UPGRADE_ITEM_IDS = linkedSet(
            "upgrade_blank",
            "upgrade_mobscanner",
            "upgrade_oreminer",
            "upgrade_orescanner",
            "upgrade_lawnmower",
            "upgrade_skysweeper",
            "upgrade_treefeller",
            "upgrade_leafbreaker",
            "upgrade_runspeed",
            "upgrade_walkspeed",
            "upgrade_stepheight",
            "upgrade_jumpboost",
            "upgrade_mindfog",
            "upgrade_invulnerability",
            "upgrade_potionarrow",
            "upgrade_smelter",
            "upgrade_smoker",
            "upgrade_hammer",
            "upgrade_cauterizewounds",
            "upgrade_swimspeed",
            "upgrade_groundstomp",
            "upgrade_extinguish",
            "upgrade_stupefy",
            "upgrade_splash",
            "upgrade_elytra",
            "upgrade_dropteleport",
            "upgrade_negatefalldamage",
            "upgrade_nightvision",
            "upgrade_decoy",
            "upgrade_lingering",
            "upgrade_homing",
            "upgrade_waterbreathing",
            "upgrade_orexray",
            "upgrade_glowing",
            "upgrade_instabreak",
            "upgrade_earthquake",
            "upgrade_noai",
            "upgrade_flight",
            "upgrade_lavaimmunity",
            "upgrade_phase",
            "upgrade_deathprotection",
            "upgrade_debuffremover",
            "upgrade_epicarrow",
            "upgrade_time_protection"
    );

    static final Set<String> ITEM_IDS = union(
            union(SPECIAL_ITEM_IDS, TOOL_AND_ARMOR_ITEM_IDS),
            union(RESOURCE_ITEM_IDS, UPGRADE_ITEM_IDS),
            BLOCK_IDS
    );

    // Upstream: Registration.java fluid groups, represented as 1.12 base fluid IDs.
    static final Set<String> FLUID_IDS = linkedSet(
            "polymorphic_fluid",
            "portal_fluid",
            "time_fluid",
            "unstable_portal_fluid",
            "unrefined_t2_fluid",
            "refined_t2_fluid",
            "unrefined_t3_fluid",
            "refined_t3_fluid",
            "unrefined_t4_fluid",
            "refined_t4_fluid",
            "xp_fluid"
    );

    // Upstream: Registration.java ENTITY_TYPES registers.
    static final Set<String> ENTITY_IDS = linkedSet(
            "creature_catcher",
            "justdirearrow",
            "portal_projectile",
            "portal_entity",
            "decoy_entity",
            "justdireareaeffectcloud",
            "time_wand_entity",
            "paradox_entity"
    );

    // Upstream: Registration.java RECIPE_TYPES and RECIPE_SERIALIZERS registers.
    static final Set<String> RECIPE_TYPE_IDS = linkedSet(
            "goospreadrecipe",
            "goospreadrecipe_tag",
            "fluiddroprecipe",
            "abilityrecipe",
            "paxelrecipe"
    );

    static final Set<String> RECIPE_SERIALIZER_IDS = linkedSet(
            "goospread",
            "goospread_tag",
            "fluiddrop",
            "ability",
            "paxel"
    );

    // Upstream: Registration.java SOUND_REGISTRY registers.
    static final Set<String> SOUND_IDS = linkedSet(
            "beep",
            "portal_gun_close",
            "portal_gun_open",
            "paradox_ambient"
    );

    // Upstream: ClientSetup.registerScreens and client/screens package.
    static final Set<String> SCREEN_CLASS_NAMES = linkedSet(
            "FuelCanisterScreen",
            "PocketGeneratorScreen",
            "ToolSettingScreen",
            "ItemCollectorScreen",
            "BlockBreakerT1Screen",
            "BlockBreakerT2Screen",
            "BlockPlacerT1Screen",
            "BlockPlacerT2Screen",
            "ClickerT1Screen",
            "ClickerT2Screen",
            "SensorT1Screen",
            "SensorT2Screen",
            "DropperT1Screen",
            "DropperT2Screen",
            "GeneratorT1Screen",
            "GeneratorFluidT1Screen",
            "EnergyTransmitterScreen",
            "BlockSwapperT1Screen",
            "BlockSwapperT2Screen",
            "PlayerAccessorScreen",
            "FluidPlacerT1Screen",
            "FluidPlacerT2Screen",
            "FluidCollectorT1Screen",
            "FluidCollectorT2Screen",
            "PotionCanisterScreen",
            "ParadoxMachineScreen",
            "InventoryHolderScreen",
            "ExperienceHolderScreen",
            "MachineSettingsCopierScreen",
            "AdvPortalRadialMenu",
            "AdvPortalEditMenu"
    );

    // Upstream: common/containers package.
    static final Set<String> CONTAINER_CLASS_NAMES = linkedSet(
            "BaseContainer",
            "BaseMachineContainer",
            "BlockBreakerT1Container",
            "BlockBreakerT2Container",
            "BlockPlacerT1Container",
            "BlockPlacerT2Container",
            "BlockSwapperT1Container",
            "BlockSwapperT2Container",
            "ClickerT1Container",
            "ClickerT2Container",
            "DropperT1Container",
            "DropperT2Container",
            "EnergyTransmitterContainer",
            "ExperienceHolderContainer",
            "FilterBasicHandler",
            "FilterBasicSlot",
            "FluidCollectorT1Container",
            "FluidCollectorT2Container",
            "FluidPlacerT1Container",
            "FluidPlacerT2Container",
            "FuelCanisterContainer",
            "FuelCanisterHandler",
            "FuelSlot",
            "GeneratorFluidT1Container",
            "GeneratorT1Container",
            "InventoryHolderContainer",
            "InventoryHolderSlot",
            "ItemCollectorContainer",
            "ParadoxMachineContainer",
            "PlayerAccessorContainer",
            "PlayerHandler",
            "PocketGeneratorContainer",
            "PotionCanisterContainer",
            "PotionCanisterHandler",
            "RefinedFuelSlot",
            "SensorT1Container",
            "SensorT2Container",
            "ToolSettingContainer"
    );

    // Upstream: common/network package.
    static final Set<String> NETWORK_CLASS_NAMES = linkedSet(
            "PacketHandler",
            "AreaAffectingPacket",
            "AreaAffectingPayload",
            "BlockStateFilterPacket",
            "BlockStateFilterPayload",
            "BreakerPacket",
            "BreakerPayload",
            "ClickerPacket",
            "ClickerPayload",
            "ClientSoundPacket",
            "ClientSoundPayload",
            "CopyMachineSettingsPacket",
            "CopyMachineSettingsPayload",
            "DirectionSettingPacket",
            "DirectionSettingPayload",
            "DropperSettingPacket",
            "DropperSettingPayload",
            "EnergyTransmitterPacket",
            "EnergyTransmitterSettingPayload",
            "ExperienceHolderPacket",
            "ExperienceHolderPayload",
            "ExperienceHolderSettingsPacket",
            "ExperienceHolderSettingsPayload",
            "FilterSettingPacket",
            "FilterSettingPayload",
            "GhostSlotPacket",
            "GhostSlotPayload",
            "InventoryHolderMoveItemsPacket",
            "InventoryHolderMoveItemsPayload",
            "InventoryHolderSaveSlotPacket",
            "InventoryHolderSaveSlotPayload",
            "InventoryHolderSettingsPacket",
            "InventoryHolderSettingsPayload",
            "ItemCollectorSettingsPacket",
            "ItemCollectorSettingsPayload",
            "LeftClickPacket",
            "LeftClickPayload",
            "ParadoxMachineSnapshotPayload",
            "ParadoxRenderPacket",
            "ParadoxRenderPayload",
            "ParadoxSnapshotPacket",
            "ParadoxSyncPacket",
            "ParadoxSyncPayload",
            "PlayerAccessorPacket",
            "PlayerAccessorPayload",
            "PortalGunFavoriteChangePacket",
            "PortalGunFavoriteChangePayload",
            "PortalGunFavoritePacket",
            "PortalGunFavoritePayload",
            "PortalGunLeftClickPacket",
            "PortalGunLeftClickPayload",
            "RedstoneSettingPacket",
            "RedstoneSettingPayload",
            "SensorPacket",
            "SensorPayload",
            "SwapperPacket",
            "SwapperPayload",
            "TickSpeedPacket",
            "TickSpeedPayload",
            "ToggleToolLeftRightClickPacket",
            "ToggleToolLeftRightClickPayload",
            "ToggleToolPacket",
            "ToggleToolPayload",
            "ToggleToolRefreshSlots",
            "ToggleToolRefreshSlotsPacket",
            "ToggleToolSlotPacket",
            "ToggleToolSlotPayload",
            "ToolSettingsGUIPacket",
            "ToolSettingsGUIPayload"
    );

    // Upstream: common/capabilities package.
    static final Set<String> CAPABILITY_CLASS_NAMES = linkedSet(
            "EnergyStorageItemstack",
            "EnergyStorageItemStackNoReceive",
            "EnergyStorageNoReceive",
            "ExperienceHolderFluidTank",
            "GeneratorFluidItemHandler",
            "GeneratorItemHandler",
            "InventoryHolderItemHandler",
            "JustDireFluidTank",
            "MachineEnergyStorage",
            "TransmitterEnergyStorage"
    );

    private SourceParityCatalog() {
    }

    @SafeVarargs
    private static <T> Set<T> union(Set<T>... sets) {
        LinkedHashSet<T> result = new LinkedHashSet<>();
        for (Set<T> set : sets) {
            result.addAll(set);
        }
        return result;
    }

    private static Set<String> linkedSet(String... ids) {
        return new LinkedHashSet<>(Arrays.asList(ids));
    }
}
