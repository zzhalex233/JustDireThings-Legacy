package com.zzhalex.justdirethings.registry;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.item.equipment.ItemJDTArmor;
import com.zzhalex.justdirethings.common.item.equipment.ItemJDTAxe;
import com.zzhalex.justdirethings.common.item.equipment.ItemJDTBow;
import com.zzhalex.justdirethings.common.item.equipment.ItemJDTHoe;
import com.zzhalex.justdirethings.common.item.equipment.ItemJDTPaxel;
import com.zzhalex.justdirethings.common.item.equipment.ItemJDTPickaxe;
import com.zzhalex.justdirethings.common.item.equipment.ItemJDTShovel;
import com.zzhalex.justdirethings.common.item.equipment.ItemJDTSword;
import com.zzhalex.justdirethings.common.item.equipment.ItemJDTWrench;
import com.zzhalex.justdirethings.common.item.material.JDTArmorMaterial;
import com.zzhalex.justdirethings.common.item.material.JDTToolTier;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
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
public final class ModEquipmentItems {

    private static final Map<String, Item> TOOL_ITEMS = new LinkedHashMap<>();
    private static final Map<String, Item> BOW_ITEMS = new LinkedHashMap<>();
    private static final Map<String, Item> ARMOR_ITEMS = new LinkedHashMap<>();

    static {
        registerTool(new ItemJDTSword("ferricore_sword", JDTToolTier.FERRICORE));
        registerTool(new ItemJDTPickaxe("ferricore_pickaxe", JDTToolTier.FERRICORE));
        registerTool(new ItemJDTShovel("ferricore_shovel", JDTToolTier.FERRICORE));
        registerTool(new ItemJDTAxe("ferricore_axe", JDTToolTier.FERRICORE, 7.0F, -3.1F));
        registerTool(new ItemJDTHoe("ferricore_hoe", JDTToolTier.FERRICORE));

        registerTool(new ItemJDTSword("blazegold_sword", JDTToolTier.BLAZEGOLD));
        registerTool(new ItemJDTPickaxe("blazegold_pickaxe", JDTToolTier.BLAZEGOLD));
        registerTool(new ItemJDTShovel("blazegold_shovel", JDTToolTier.BLAZEGOLD));
        registerTool(new ItemJDTAxe("blazegold_axe", JDTToolTier.BLAZEGOLD, 8.0F, -3.0F));
        registerTool(new ItemJDTHoe("blazegold_hoe", JDTToolTier.BLAZEGOLD));

        registerTool(new ItemJDTSword("celestigem_sword", JDTToolTier.CELESTIGEM));
        registerTool(new ItemJDTPickaxe("celestigem_pickaxe", JDTToolTier.CELESTIGEM));
        registerTool(new ItemJDTShovel("celestigem_shovel", JDTToolTier.CELESTIGEM));
        registerTool(new ItemJDTAxe("celestigem_axe", JDTToolTier.CELESTIGEM, 8.5F, -2.9F));
        registerTool(new ItemJDTHoe("celestigem_hoe", JDTToolTier.CELESTIGEM));

        registerTool(new ItemJDTSword("eclipsealloy_sword", JDTToolTier.ECLIPSEALLOY));
        registerTool(new ItemJDTPickaxe("eclipsealloy_pickaxe", JDTToolTier.ECLIPSEALLOY));
        registerTool(new ItemJDTShovel("eclipsealloy_shovel", JDTToolTier.ECLIPSEALLOY));
        registerTool(new ItemJDTAxe("eclipsealloy_axe", JDTToolTier.ECLIPSEALLOY, 9.0F, -2.8F));
        registerTool(new ItemJDTHoe("eclipsealloy_hoe", JDTToolTier.ECLIPSEALLOY));

        registerTool(new ItemJDTPaxel("celestigem_paxel", JDTToolTier.CELESTIGEM, 6.0F, -2.8F));
        registerTool(new ItemJDTPaxel("eclipsealloy_paxel", JDTToolTier.ECLIPSEALLOY, 7.0F, -2.8F));
        registerTool(new ItemJDTWrench("ferricore_wrench", JDTToolTier.FERRICORE));

        registerBow(new ItemJDTBow("bow_ferricore", JDTToolTier.FERRICORE, 512));
        registerBow(new ItemJDTBow("bow_blazegold", JDTToolTier.BLAZEGOLD, 768));
        registerBow(new ItemJDTBow("bow_celestigem", JDTToolTier.CELESTIGEM, 1024));
        registerBow(new ItemJDTBow("bow_eclipsealloy", JDTToolTier.ECLIPSEALLOY, 1536));

        registerArmor(new ItemJDTArmor("ferricore_boots", JDTArmorMaterial.FERRICORE, EntityEquipmentSlot.FEET));
        registerArmor(new ItemJDTArmor("ferricore_chestplate", JDTArmorMaterial.FERRICORE, EntityEquipmentSlot.CHEST));
        registerArmor(new ItemJDTArmor("ferricore_leggings", JDTArmorMaterial.FERRICORE, EntityEquipmentSlot.LEGS));
        registerArmor(new ItemJDTArmor("ferricore_helmet", JDTArmorMaterial.FERRICORE, EntityEquipmentSlot.HEAD));

        registerArmor(new ItemJDTArmor("blazegold_boots", JDTArmorMaterial.BLAZEGOLD, EntityEquipmentSlot.FEET));
        registerArmor(new ItemJDTArmor("blazegold_chestplate", JDTArmorMaterial.BLAZEGOLD, EntityEquipmentSlot.CHEST));
        registerArmor(new ItemJDTArmor("blazegold_leggings", JDTArmorMaterial.BLAZEGOLD, EntityEquipmentSlot.LEGS));
        registerArmor(new ItemJDTArmor("blazegold_helmet", JDTArmorMaterial.BLAZEGOLD, EntityEquipmentSlot.HEAD));

        registerArmor(new ItemJDTArmor("celestigem_boots", JDTArmorMaterial.CELESTIGEM, EntityEquipmentSlot.FEET));
        registerArmor(new ItemJDTArmor("celestigem_chestplate", JDTArmorMaterial.CELESTIGEM, EntityEquipmentSlot.CHEST));
        registerArmor(new ItemJDTArmor("celestigem_leggings", JDTArmorMaterial.CELESTIGEM, EntityEquipmentSlot.LEGS));
        registerArmor(new ItemJDTArmor("celestigem_helmet", JDTArmorMaterial.CELESTIGEM, EntityEquipmentSlot.HEAD));

        registerArmor(new ItemJDTArmor("eclipsealloy_boots", JDTArmorMaterial.ECLIPSEALLOY, EntityEquipmentSlot.FEET));
        registerArmor(new ItemJDTArmor("eclipsealloy_chestplate", JDTArmorMaterial.ECLIPSEALLOY, EntityEquipmentSlot.CHEST));
        registerArmor(new ItemJDTArmor("eclipsealloy_leggings", JDTArmorMaterial.ECLIPSEALLOY, EntityEquipmentSlot.LEGS));
        registerArmor(new ItemJDTArmor("eclipsealloy_helmet", JDTArmorMaterial.ECLIPSEALLOY, EntityEquipmentSlot.HEAD));
    }

    private ModEquipmentItems() {
    }

    public static List<String> toolItemIds() {
        return new ArrayList<>(TOOL_ITEMS.keySet());
    }

    public static Collection<Item> toolItems() {
        return Collections.unmodifiableCollection(TOOL_ITEMS.values());
    }

    public static List<String> bowItemIds() {
        return new ArrayList<>(BOW_ITEMS.keySet());
    }

    public static Collection<Item> bowItems() {
        return Collections.unmodifiableCollection(BOW_ITEMS.values());
    }

    public static List<String> armorItemIds() {
        return new ArrayList<>(ARMOR_ITEMS.keySet());
    }

    public static Collection<Item> armorItems() {
        return Collections.unmodifiableCollection(ARMOR_ITEMS.values());
    }

    public static Collection<Item> allItems() {
        List<Item> allItems = new ArrayList<>(TOOL_ITEMS.values().size() + BOW_ITEMS.values().size() + ARMOR_ITEMS.values().size());
        allItems.addAll(TOOL_ITEMS.values());
        allItems.addAll(BOW_ITEMS.values());
        allItems.addAll(ARMOR_ITEMS.values());
        return Collections.unmodifiableList(allItems);
    }

    public static Item getItem(String id) {
        Item item = TOOL_ITEMS.get(id);
        if (item != null) {
            return item;
        }
        item = BOW_ITEMS.get(id);
        if (item != null) {
            return item;
        }
        return ARMOR_ITEMS.get(id);
    }

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        registerAll(event, TOOL_ITEMS.values());
        registerAll(event, BOW_ITEMS.values());
        registerAll(event, ARMOR_ITEMS.values());
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        registerModels(TOOL_ITEMS.values());
        registerModels(BOW_ITEMS.values());
        registerModels(ARMOR_ITEMS.values());
    }

    private static void registerTool(Item item) {
        TOOL_ITEMS.put(item.getRegistryName().getPath(), item);
    }

    private static void registerBow(Item item) {
        BOW_ITEMS.put(item.getRegistryName().getPath(), item);
    }

    private static void registerArmor(Item item) {
        ARMOR_ITEMS.put(item.getRegistryName().getPath(), item);
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
}
