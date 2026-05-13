package com.zzhalex.justdirethings.registry;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.item.ItemGooBlock;
import com.zzhalex.justdirethings.common.item.ItemPolymorphicCatalyst;
import com.zzhalex.justdirethings.common.item.ItemSimpleContent;
import com.zzhalex.justdirethings.common.item.ItemTimeCrystal;
import com.zzhalex.justdirethings.common.item.ItemUpgradeContent;
import com.zzhalex.justdirethings.common.item.fuel.ItemDireCoal;
import com.zzhalex.justdirethings.common.item.fuel.ItemDireFuelBlock;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class ModContentItems {

    private static final Map<String, Item> BLOCK_ITEMS = new LinkedHashMap<>();
    private static final Map<String, Item> RESOURCE_ITEMS = new LinkedHashMap<>();
    private static final Map<String, Item> TEMPLATE_ITEMS = new LinkedHashMap<>();
    private static final Map<String, Item> UPGRADE_ITEMS = new LinkedHashMap<>();

    static {
        for (Block block : ModContentBlocks.allBlocks()) {
            registerBlockItem(block);
        }

        registerResource("raw_ferricore");
        registerResource("raw_blazegold");
        registerResource("raw_eclipsealloy");
        registerResource("ferricore_ingot");
        registerResource("blazegold_ingot");
        registerResource("celestigem");
        registerResource("eclipsealloy_ingot");
        registerResource("coal_t1", new ItemDireCoal(4800, 2));
        registerResource("coal_t2", new ItemDireCoal(14400, 4));
        registerResource("coal_t3", new ItemDireCoal(43200, 8));
        registerResource("coal_t4", new ItemDireCoal(129600, 16));
        registerResource("polymorphic_catalyst", new ItemPolymorphicCatalyst());
        registerResource("portal_fluid_catalyst");
        registerResource("time_crystal", new ItemTimeCrystal());

        registerTemplate("template_ferricore");
        registerTemplate("template_blazegold");
        registerTemplate("template_celestigem");
        registerTemplate("template_eclipsealloy");

        registerUpgrade("upgrade_blank");
        registerUpgrade("upgrade_mobscanner");
        registerUpgrade("upgrade_oreminer");
        registerUpgrade("upgrade_orescanner");
        registerUpgrade("upgrade_lawnmower");
        registerUpgrade("upgrade_skysweeper");
        registerUpgrade("upgrade_treefeller");
        registerUpgrade("upgrade_leafbreaker");
        registerUpgrade("upgrade_runspeed");
        registerUpgrade("upgrade_walkspeed");
        registerUpgrade("upgrade_stepheight");
        registerUpgrade("upgrade_jumpboost");
        registerUpgrade("upgrade_mindfog");
        registerUpgrade("upgrade_invulnerability");
        registerUpgrade("upgrade_potionarrow");
        registerUpgrade("upgrade_smelter");
        registerUpgrade("upgrade_smoker");
        registerUpgrade("upgrade_hammer");
        registerUpgrade("upgrade_cauterizewounds");
        registerUpgrade("upgrade_swimspeed");
        registerUpgrade("upgrade_groundstomp");
        registerUpgrade("upgrade_extinguish");
        registerUpgrade("upgrade_stupefy");
        registerUpgrade("upgrade_splash");
        registerUpgrade("upgrade_elytra");
        registerUpgrade("upgrade_dropteleport");
        registerUpgrade("upgrade_negatefalldamage");
        registerUpgrade("upgrade_nightvision");
        registerUpgrade("upgrade_decoy");
        registerUpgrade("upgrade_lingering");
        registerUpgrade("upgrade_homing");
        registerUpgrade("upgrade_waterbreathing");
        registerUpgrade("upgrade_orexray");
        registerUpgrade("upgrade_glowing");
        registerUpgrade("upgrade_instabreak");
        registerUpgrade("upgrade_earthquake");
        registerUpgrade("upgrade_noai");
        registerUpgrade("upgrade_flight");
        registerUpgrade("upgrade_lavaimmunity");
        registerUpgrade("upgrade_phase");
        registerUpgrade("upgrade_deathprotection");
        registerUpgrade("upgrade_debuffremover");
        registerUpgrade("upgrade_epicarrow");
        registerUpgrade("upgrade_time_protection");
    }

    private ModContentItems() {
    }

    public static List<String> resourceItemIds() {
        return new ArrayList<>(RESOURCE_ITEMS.keySet());
    }

    public static Collection<Item> blockItems() {
        return Collections.unmodifiableCollection(BLOCK_ITEMS.values());
    }

    public static Collection<Item> resourceItems() {
        return Collections.unmodifiableCollection(RESOURCE_ITEMS.values());
    }

    public static List<String> templateItemIds() {
        return new ArrayList<>(TEMPLATE_ITEMS.keySet());
    }

    public static Collection<Item> templateItems() {
        return Collections.unmodifiableCollection(TEMPLATE_ITEMS.values());
    }

    public static List<String> upgradeItemIds() {
        return new ArrayList<>(UPGRADE_ITEMS.keySet());
    }

    public static Collection<Item> upgradeItems() {
        return Collections.unmodifiableCollection(UPGRADE_ITEMS.values());
    }

    public static Item getItem(String id) {
        Item item = RESOURCE_ITEMS.get(id);
        if (item != null) {
            return item;
        }
        item = TEMPLATE_ITEMS.get(id);
        if (item != null) {
            return item;
        }
        item = UPGRADE_ITEMS.get(id);
        if (item != null) {
            return item;
        }
        return BLOCK_ITEMS.get(id);
    }

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        registerAll(event, BLOCK_ITEMS.values());
        registerAll(event, RESOURCE_ITEMS.values());
        registerAll(event, TEMPLATE_ITEMS.values());
        registerAll(event, UPGRADE_ITEMS.values());
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        registerModels(BLOCK_ITEMS.values());
        registerModels(RESOURCE_ITEMS.values());
        registerModels(TEMPLATE_ITEMS.values());
        registerModels(UPGRADE_ITEMS.values());
    }

    private static void registerAll(RegistryEvent.Register<Item> event, Collection<Item> items) {
        for (Item item : items) {
            event.getRegistry().register(item);
        }
    }

    @SideOnly(Side.CLIENT)
    private static void registerModels(Collection<Item> items) {
        for (Item item : items) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }

    private static void registerBlockItem(Block block) {
        ItemBlock itemBlock = createBlockItem(block);
        if (itemBlock == null) {
            return;
        }
        itemBlock.setRegistryName(block.getRegistryName());
        itemBlock.setCreativeTab(ModCreativeTabs.JUST_DIRE_THINGS);
        BLOCK_ITEMS.put(block.getRegistryName().getPath(), itemBlock);
    }

    private static void registerResource(String id) {
        RESOURCE_ITEMS.put(id, createItem(id));
    }

    private static void registerResource(String id, Item item) {
        RESOURCE_ITEMS.put(id, createItem(id, item));
    }

    private static void registerTemplate(String id) {
        TEMPLATE_ITEMS.put(id, createItem(id));
    }

    private static void registerUpgrade(String id) {
        UPGRADE_ITEMS.put(id, createItem(id, new ItemUpgradeContent()));
    }

    private static Item createItem(String id) {
        return createItem(id, new ItemSimpleContent());
    }

    private static Item createItem(String id, Item item) {
        item.setRegistryName(Reference.MOD_ID, id);
        item.setTranslationKey(Reference.MOD_ID + "." + id);
        return item;
    }

    private static ItemBlock createBlockItem(Block block) {
        String id = block.getRegistryName().getPath();
        switch (id) {
            case "goopatternblock":
                return null;
            case "coalblock_t1":
                return new ItemDireFuelBlock(block, 48000, 2);
            case "coalblock_t2":
                return new ItemDireFuelBlock(block, 144000, 4);
            case "coalblock_t3":
                return new ItemDireFuelBlock(block, 432000, 8);
            case "coalblock_t4":
                return new ItemDireFuelBlock(block, 1296000, 16);
            case "charcoal":
                return new ItemDireFuelBlock(block, 16000, 1);
            case "gooblock_tier1":
            case "gooblock_tier2":
            case "gooblock_tier3":
            case "gooblock_tier4":
                return new ItemGooBlock(block);
            default:
                return new ItemBlock(block);
        }
    }
}
