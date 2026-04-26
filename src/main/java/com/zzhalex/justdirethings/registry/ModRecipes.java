package com.zzhalex.justdirethings.registry;

import com.zzhalex.justdirethings.common.recipe.AbilityInstallRecipe;
import com.zzhalex.justdirethings.common.recipe.PaxelFusionRecipe;
import com.zzhalex.justdirethings.common.recipe.TierUpgradeRecipe;
import com.zzhalex.justdirethings.common.recipe.UpgradeStationRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ModRecipes {

    public static final List<UpgradeStationRecipe> UPGRADE_STATION_RECIPES = createUpgradeStationRecipes();

    private ModRecipes() {
    }

    public static void register() {
        // PARITY STUB: upstream recipe IDs are cataloged, but 1.12 loaders/data are not ported yet.
        // Upgrade Station recipe registration is implemented incrementally as more item tiers land.
    }

    public static List<String> coreRecipeTypeIds() {
        List<String> ids = new ArrayList<>();
        ids.add("goospreadrecipe");
        ids.add("goospreadrecipe_tag");
        ids.add("fluiddroprecipe");
        ids.add("abilityrecipe");
        ids.add("paxelrecipe");
        return Collections.unmodifiableList(ids);
    }

    public static List<String> coreRecipeSerializerIds() {
        List<String> ids = new ArrayList<>();
        ids.add("goospread");
        ids.add("goospread_tag");
        ids.add("fluiddrop");
        ids.add("ability");
        ids.add("paxel");
        return Collections.unmodifiableList(ids);
    }

    public static Optional<UpgradeStationRecipe> findUpgradeStationRecipe(ItemStack template, ItemStack base, ItemStack addition) {
        for (UpgradeStationRecipe recipe : UPGRADE_STATION_RECIPES) {
            if (recipe.matches(template, base, addition)) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }

    public static ItemStack getUpgradeStationOutput(ItemStack template, ItemStack base, ItemStack addition) {
        Optional<UpgradeStationRecipe> recipe = findUpgradeStationRecipe(template, base, addition);
        return recipe.map(value -> value.createOutputStack(template, base, addition)).orElse(ItemStack.EMPTY);
    }

    private static List<UpgradeStationRecipe> createUpgradeStationRecipes() {
        List<UpgradeStationRecipe> recipes = new ArrayList<>();

        addToolTierRecipes(recipes, "blazegold", "template_blazegold", "ferricore", "blazegold_ingot", "blazegold");
        addToolTierRecipes(recipes, "celestigem", "template_celestigem", "blazegold", "celestigem", "celestigem");
        addToolTierRecipes(recipes, "eclipsealloy", "template_eclipsealloy", "celestigem", "eclipsealloy_ingot", "eclipsealloy");

        addBowTierRecipe(recipes, "blazegold", "template_blazegold", "bow_ferricore", "blazegold_ingot", "bow_blazegold");
        addBowTierRecipe(recipes, "celestigem", "template_celestigem", "bow_blazegold", "celestigem", "bow_celestigem");
        addBowTierRecipe(recipes, "eclipsealloy", "template_eclipsealloy", "bow_celestigem", "eclipsealloy_ingot", "bow_eclipsealloy");

        addArmorTierRecipes(recipes, "blazegold", "template_blazegold", "ferricore", "blazegold_ingot", "blazegold");
        addArmorTierRecipes(recipes, "celestigem", "template_celestigem", "blazegold", "celestigem", "celestigem");
        addArmorTierRecipes(recipes, "eclipsealloy", "template_eclipsealloy", "celestigem", "eclipsealloy_ingot", "eclipsealloy");

        addPaxelFusionRecipe(recipes, "celestigem", "celestigem_pickaxe", "celestigem_axe", "celestigem_shovel", "celestigem_paxel");
        addPaxelFusionRecipe(recipes, "eclipsealloy", "eclipsealloy_pickaxe", "eclipsealloy_axe", "eclipsealloy_shovel", "eclipsealloy_paxel");
        addTierRecipe(recipes, "paxel_eclipsealloy", "template_eclipsealloy", "celestigem_paxel", "eclipsealloy_ingot", "eclipsealloy_paxel");

        Collection<Item> equipmentItems = ModEquipmentItems.allItems();
        addAbilityRecipe(recipes, "upgrade_mobscanner", "mobscanner", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_oreminer", "oreminer", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_orescanner", "orescanner", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_lawnmower", "lawnmower", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_skysweeper", "skysweeper", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_treefeller", "treefeller", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_leafbreaker", "leafbreaker", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_runspeed", "runspeed", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_walkspeed", "walkspeed", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_stepheight", "stepheight", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_jumpboost", "jumpboost", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_mindfog", "mindfog", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_invulnerability", "invulnerability", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_potionarrow", "potionarrow", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_smelter", "smelter", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_smoker", "smoker", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_hammer", "hammer", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_cauterizewounds", "cauterizewounds", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_swimspeed", "swimspeed", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_groundstomp", "groundstomp", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_extinguish", "extinguish", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_stupefy", "stupefy", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_splash", "splash", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_elytra", "elytra", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_dropteleport", "dropteleport", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_negatefalldamage", "negatefalldamage", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_nightvision", "nightvision", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_decoy", "decoy", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_lingering", "lingering", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_homing", "homing", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_waterbreathing", "waterbreathing", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_orexray", "orexray", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_glowing", "glowing", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_instabreak", "instabreak", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_earthquake", "earthquake", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_noai", "noai", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_flight", "flight", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_lavaimmunity", "lavaimmunity", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_phase", "phase", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_deathprotection", "deathprotection", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_debuffremover", "debuffremover", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_epicarrow", "epicarrow", equipmentItems);
        addAbilityRecipe(recipes, "upgrade_time_protection", "timeprotection", equipmentItems);

        return Collections.unmodifiableList(recipes);
    }

    private static void addToolTierRecipes(List<UpgradeStationRecipe> recipes, String id, String templateId, String baseTier, String additionId, String resultTier) {
        addTierRecipe(recipes, id + "_sword", templateId, baseTier + "_sword", additionId, resultTier + "_sword");
        addTierRecipe(recipes, id + "_pickaxe", templateId, baseTier + "_pickaxe", additionId, resultTier + "_pickaxe");
        addTierRecipe(recipes, id + "_shovel", templateId, baseTier + "_shovel", additionId, resultTier + "_shovel");
        addTierRecipe(recipes, id + "_axe", templateId, baseTier + "_axe", additionId, resultTier + "_axe");
        addTierRecipe(recipes, id + "_hoe", templateId, baseTier + "_hoe", additionId, resultTier + "_hoe");
    }

    private static void addArmorTierRecipes(List<UpgradeStationRecipe> recipes, String id, String templateId, String baseTier, String additionId, String resultTier) {
        addTierRecipe(recipes, id + "_boots", templateId, baseTier + "_boots", additionId, resultTier + "_boots");
        addTierRecipe(recipes, id + "_leggings", templateId, baseTier + "_leggings", additionId, resultTier + "_leggings");
        addTierRecipe(recipes, id + "_chestplate", templateId, baseTier + "_chestplate", additionId, resultTier + "_chestplate");
        addTierRecipe(recipes, id + "_helmet", templateId, baseTier + "_helmet", additionId, resultTier + "_helmet");
    }

    private static void addBowTierRecipe(List<UpgradeStationRecipe> recipes, String id, String templateId, String baseId, String additionId, String resultId) {
        addTierRecipe(recipes, id + "_bow", templateId, baseId, additionId, resultId);
    }

    private static void addTierRecipe(List<UpgradeStationRecipe> recipes, String id, String templateId, String baseId, String additionId, String resultId) {
        Item templateItem = content(templateId);
        Item baseItem = equipment(baseId);
        Item additionItem = content(additionId);
        Item resultItem = equipment(resultId);
        if (templateItem != null && baseItem != null && additionItem != null && resultItem != null) {
            recipes.add(new TierUpgradeRecipe("tier_" + id, templateItem, baseItem, additionItem, resultItem));
        }
    }

    private static void addPaxelFusionRecipe(List<UpgradeStationRecipe> recipes, String id, String pickaxeId, String axeId, String shovelId, String resultId) {
        Item pickaxeItem = equipment(pickaxeId);
        Item axeItem = equipment(axeId);
        Item shovelItem = equipment(shovelId);
        Item resultItem = equipment(resultId);
        if (pickaxeItem != null && axeItem != null && shovelItem != null && resultItem != null) {
            recipes.add(new PaxelFusionRecipe("paxel_fusion_" + id, pickaxeItem, axeItem, shovelItem, resultItem));
        }
    }

    private static void addAbilityRecipe(List<UpgradeStationRecipe> recipes, String upgradeId, String abilityId, Collection<Item> equipmentItems) {
        Item upgradeItem = content(upgradeId);
        if (upgradeItem != null) {
            recipes.add(new AbilityInstallRecipe("ability_install_" + abilityId, upgradeItem, abilityId, equipmentItems));
        }
    }

    private static Item content(String id) {
        return ModContentItems.getItem(id);
    }

    private static Item equipment(String id) {
        return ModEquipmentItems.getItem(id);
    }
}
