package com.zzhalex.justdirethings.registry;

import com.zzhalex.justdirethings.common.item.misc.ItemCreatureCatcher;
import com.zzhalex.justdirethings.common.item.misc.FluidCanisterItem;
import com.zzhalex.justdirethings.common.item.misc.FuelCanisterItem;
import com.zzhalex.justdirethings.common.item.misc.ItemMachineSettingsCopier;
import com.zzhalex.justdirethings.common.item.misc.ItemTotemOfDeathRecall;
import com.zzhalex.justdirethings.common.item.misc.PocketGeneratorItem;
import com.zzhalex.justdirethings.common.item.misc.PotionCanisterItem;
import com.zzhalex.justdirethings.common.item.tool.ItemPortalGunV2;
import com.zzhalex.justdirethings.common.item.tool.ItemPolymorphicWandV2;
import com.zzhalex.justdirethings.common.item.tool.ItemTimeWand;
import com.zzhalex.justdirethings.common.item.tool.ItemBlazejetWand;
import com.zzhalex.justdirethings.common.item.tool.ItemEclipsegateWand;
import com.zzhalex.justdirethings.common.item.tool.ItemVoidshiftWand;
import com.zzhalex.justdirethings.common.item.tool.ItemPolymorphicWand;
import com.zzhalex.justdirethings.common.item.tool.ItemPortalGun;
import com.zzhalex.justdirethings.Reference;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class ModItems {

    private static final Map<String, Item> ITEMS = new LinkedHashMap<>();

    public static final ItemBlock UPGRADE_STATION_ITEM = createBlockItem(ModBlocks.UPGRADE_STATION);
    public static final ItemBlock GENERATOR_T1_ITEM = createBlockItem(ModBlocks.GENERATOR_T1);
    public static final ItemBlock GENERATOR_FLUID_T1_ITEM = createBlockItem(ModBlocks.GENERATOR_FLUID_T1);
    public static final ItemBlock ITEMCOLLECTOR_ITEM = createBlockItem(ModBlocks.ITEMCOLLECTOR);
    public static final ItemBlock BLOCK_BREAKER_T1_ITEM = createBlockItem(ModBlocks.BLOCK_BREAKER_T1);
    public static final ItemBlock BLOCK_BREAKER_T2_ITEM = createBlockItem(ModBlocks.BLOCK_BREAKER_T2);
    public static final ItemBlock BLOCK_PLACER_T1_ITEM = createBlockItem(ModBlocks.BLOCK_PLACER_T1);
    public static final ItemBlock BLOCK_PLACER_T2_ITEM = createBlockItem(ModBlocks.BLOCK_PLACER_T2);
    public static final ItemBlock CLICKER_T1_ITEM = createBlockItem(ModBlocks.CLICKER_T1);
    public static final ItemBlock CLICKER_T2_ITEM = createBlockItem(ModBlocks.CLICKER_T2);
    public static final ItemBlock SENSOR_T1_ITEM = createBlockItem(ModBlocks.SENSOR_T1);
    public static final ItemBlock SENSOR_T2_ITEM = createBlockItem(ModBlocks.SENSOR_T2);
    public static final ItemBlock DROPPER_T1_ITEM = createBlockItem(ModBlocks.DROPPER_T1);
    public static final ItemBlock DROPPER_T2_ITEM = createBlockItem(ModBlocks.DROPPER_T2);
    public static final ItemBlock BLOCK_SWAPPER_T1_ITEM = createBlockItem(ModBlocks.BLOCK_SWAPPER_T1);
    public static final ItemBlock BLOCK_SWAPPER_T2_ITEM = createBlockItem(ModBlocks.BLOCK_SWAPPER_T2);
    public static final ItemBlock FLUID_PLACER_T1_ITEM = createBlockItem(ModBlocks.FLUID_PLACER_T1);
    public static final ItemBlock FLUID_PLACER_T2_ITEM = createBlockItem(ModBlocks.FLUID_PLACER_T2);
    public static final ItemBlock FLUID_COLLECTOR_T1_ITEM = createBlockItem(ModBlocks.FLUID_COLLECTOR_T1);
    public static final ItemBlock FLUID_COLLECTOR_T2_ITEM = createBlockItem(ModBlocks.FLUID_COLLECTOR_T2);
    public static final ItemBlock INVENTORY_HOLDER_ITEM = createBlockItem(ModBlocks.INVENTORY_HOLDER);
    public static final ItemBlock EXPERIENCEHOLDER_ITEM = createBlockItem(ModBlocks.EXPERIENCEHOLDER);
    public static final ItemBlock ENERGYTRANSMITTER_ITEM = createBlockItem(ModBlocks.ENERGYTRANSMITTER);
    public static final ItemBlock PLAYERACCESSOR_ITEM = createBlockItem(ModBlocks.PLAYERACCESSOR);
    public static final ItemBlock PARADOX_MACHINE_ITEM = createBlockItem(ModBlocks.PARADOX_MACHINE);
    public static final Item POCKET_GENERATOR = createItem("pocket_generator", new PocketGeneratorItem());
    public static final Item FLUID_CANISTER = createItem("fluid_canister", new FluidCanisterItem());
    public static final Item POTION_CANISTER = createItem("potion_canister", new PotionCanisterItem());
    public static final Item FUEL_CANISTER = createItem("fuel_canister", new FuelCanisterItem());
    public static final Item TOTEM_OF_DEATH_RECALL = createItem("totem_of_death_recall", new ItemTotemOfDeathRecall());
    public static final Item BLAZEJET_WAND = createItem("blazejet_wand", new ItemBlazejetWand());
    public static final Item VOIDSHIFT_WAND = createItem("voidshift_wand", new ItemVoidshiftWand());
    public static final Item ECLIPSEGATE_WAND = createItem("eclipsegate_wand", new ItemEclipsegateWand());
    public static final Item CREATURE_CATCHER = createItem("creaturecatcher", new ItemCreatureCatcher());
    public static final Item MACHINE_SETTINGS_COPIER = createItem("machinesettingscopier", new ItemMachineSettingsCopier());
    public static final Item PORTAL_GUN = createItem("portalgun", new ItemPortalGun());
    public static final Item POLYMORPHIC_WAND = createItem("polymorphic_wand", new ItemPolymorphicWand());
    public static final Item POLYMORPHIC_WAND_V2 = createItem("polymorphic_wand_v2", new ItemPolymorphicWandV2());
    public static final Item TIME_WAND = createItem("time_wand", new ItemTimeWand());
    public static final Item PORTAL_GUN_V2 = createItem("portalgun_v2", new ItemPortalGunV2());

    private ModItems() {
    }

    public static Item getItem(String id) {
        return ITEMS.get(id);
    }

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(UPGRADE_STATION_ITEM);
        event.getRegistry().register(GENERATOR_T1_ITEM);
        event.getRegistry().register(GENERATOR_FLUID_T1_ITEM);
        event.getRegistry().register(ITEMCOLLECTOR_ITEM);
        event.getRegistry().register(BLOCK_BREAKER_T1_ITEM);
        event.getRegistry().register(BLOCK_BREAKER_T2_ITEM);
        event.getRegistry().register(BLOCK_PLACER_T1_ITEM);
        event.getRegistry().register(BLOCK_PLACER_T2_ITEM);
        event.getRegistry().register(CLICKER_T1_ITEM);
        event.getRegistry().register(CLICKER_T2_ITEM);
        event.getRegistry().register(SENSOR_T1_ITEM);
        event.getRegistry().register(SENSOR_T2_ITEM);
        event.getRegistry().register(DROPPER_T1_ITEM);
        event.getRegistry().register(DROPPER_T2_ITEM);
        event.getRegistry().register(BLOCK_SWAPPER_T1_ITEM);
        event.getRegistry().register(BLOCK_SWAPPER_T2_ITEM);
        event.getRegistry().register(FLUID_PLACER_T1_ITEM);
        event.getRegistry().register(FLUID_PLACER_T2_ITEM);
        event.getRegistry().register(FLUID_COLLECTOR_T1_ITEM);
        event.getRegistry().register(FLUID_COLLECTOR_T2_ITEM);
        event.getRegistry().register(INVENTORY_HOLDER_ITEM);
        event.getRegistry().register(EXPERIENCEHOLDER_ITEM);
        event.getRegistry().register(ENERGYTRANSMITTER_ITEM);
        event.getRegistry().register(PLAYERACCESSOR_ITEM);
        event.getRegistry().register(PARADOX_MACHINE_ITEM);
        event.getRegistry().register(POCKET_GENERATOR);
        event.getRegistry().register(FLUID_CANISTER);
        event.getRegistry().register(POTION_CANISTER);
        event.getRegistry().register(FUEL_CANISTER);
        event.getRegistry().register(TOTEM_OF_DEATH_RECALL);
        event.getRegistry().register(BLAZEJET_WAND);
        event.getRegistry().register(VOIDSHIFT_WAND);
        event.getRegistry().register(ECLIPSEGATE_WAND);
        event.getRegistry().register(CREATURE_CATCHER);
        event.getRegistry().register(MACHINE_SETTINGS_COPIER);
        event.getRegistry().register(PORTAL_GUN);
        event.getRegistry().register(POLYMORPHIC_WAND);
        event.getRegistry().register(POLYMORPHIC_WAND_V2);
        event.getRegistry().register(TIME_WAND);
        event.getRegistry().register(PORTAL_GUN_V2);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        registerModel(UPGRADE_STATION_ITEM);
        registerModel(GENERATOR_T1_ITEM);
        registerModel(GENERATOR_FLUID_T1_ITEM);
        registerModel(ITEMCOLLECTOR_ITEM);
        registerModel(BLOCK_BREAKER_T1_ITEM);
        registerModel(BLOCK_BREAKER_T2_ITEM);
        registerModel(BLOCK_PLACER_T1_ITEM);
        registerModel(BLOCK_PLACER_T2_ITEM);
        registerModel(CLICKER_T1_ITEM);
        registerModel(CLICKER_T2_ITEM);
        registerModel(SENSOR_T1_ITEM);
        registerModel(SENSOR_T2_ITEM);
        registerModel(DROPPER_T1_ITEM);
        registerModel(DROPPER_T2_ITEM);
        registerModel(BLOCK_SWAPPER_T1_ITEM);
        registerModel(BLOCK_SWAPPER_T2_ITEM);
        registerModel(FLUID_PLACER_T1_ITEM);
        registerModel(FLUID_PLACER_T2_ITEM);
        registerModel(FLUID_COLLECTOR_T1_ITEM);
        registerModel(FLUID_COLLECTOR_T2_ITEM);
        registerModel(INVENTORY_HOLDER_ITEM);
        registerModel(EXPERIENCEHOLDER_ITEM);
        registerModel(ENERGYTRANSMITTER_ITEM);
        registerModel(PLAYERACCESSOR_ITEM);
        registerModel(PARADOX_MACHINE_ITEM);
        registerModel(POCKET_GENERATOR);
        registerModel(FLUID_CANISTER);
        registerModel(POTION_CANISTER);
        registerModel(FUEL_CANISTER);
        registerModel(TOTEM_OF_DEATH_RECALL);
        registerModel(BLAZEJET_WAND);
        registerModel(VOIDSHIFT_WAND);
        registerModel(ECLIPSEGATE_WAND);
        ModelBakery.registerItemVariants(CREATURE_CATCHER,
                new ResourceLocation(Reference.MOD_ID, "creaturecatcher_base"),
                new ResourceLocation(Reference.MOD_ID, "creaturecatcher_bottom"),
                new ResourceLocation(Reference.MOD_ID, "creaturecatcher_shield"));
        registerModel(CREATURE_CATCHER);
        registerModel(MACHINE_SETTINGS_COPIER);
        registerModel(PORTAL_GUN);
        registerModel(POLYMORPHIC_WAND);
        registerModel(POLYMORPHIC_WAND_V2);
        registerModel(TIME_WAND);
        registerModel(PORTAL_GUN_V2);
    }

    private static Item createItem(String registryPath, Item item) {
        item.setRegistryName(Reference.MOD_ID, registryPath);
        if (item.getTranslationKey().contains("null")) {
            item.setTranslationKey(Reference.MOD_ID + "." + registryPath);
        }
        item.setCreativeTab(ModCreativeTabs.JUST_DIRE_THINGS);
        ITEMS.put(registryPath, item);
        return item;
    }

    private static ItemBlock createBlockItem(net.minecraft.block.Block block) {
        ItemBlock itemBlock = new ItemBlock(block);
        itemBlock.setRegistryName(block.getRegistryName());
        itemBlock.setCreativeTab(ModCreativeTabs.JUST_DIRE_THINGS);
        return itemBlock;
    }

    @SideOnly(Side.CLIENT)
    private static void registerModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
