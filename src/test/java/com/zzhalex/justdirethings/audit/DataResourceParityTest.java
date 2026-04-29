package com.zzhalex.justdirethings.audit;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataResourceParityTest {

    private static final Path UPSTREAM_DATA = Paths.get("source/JustDireThings-main/src/generated/resources/data/justdirethings");
    private static final Path CURRENT_112_RECIPES = Paths.get("src/main/resources/assets/justdirethings/recipes");
    private static final Path CURRENT_DATA = Paths.get("src/main/resources/data/justdirethings");
    private static final Path MATRIX = Paths.get("docs/audit/justdirethings-source-parity-matrix.md");
    private static final Set<String> MODERN_ONLY_MINECRAFT_ITEMS = new TreeSet<>(Arrays.asList(
            "minecraft:amethyst_shard",
            "minecraft:blast_furnace",
            "minecraft:calibrated_sculk_sensor",
            "minecraft:echo_shard",
            "minecraft:honey_bottle",
            "minecraft:kelp",
            "minecraft:netherite_block",
            "minecraft:netherite_pickaxe",
            "minecraft:netherite_scrap",
            "minecraft:phantom_membrane",
            "minecraft:sculk",
            "minecraft:sculk_shrieker",
            "minecraft:short_grass",
            "minecraft:smoker",
            "minecraft:target"
    ));

    @Test
    void upstreamGeneratedDataCountsAreKnownAndStableForMigration() throws IOException {
        assertEquals(392, countFiles(UPSTREAM_DATA.resolve("recipe")),
                "Upstream generated recipe count changed; update the data migration plan before porting");
        assertEquals(392, countFiles(UPSTREAM_DATA.resolve("advancement")),
                "Upstream generated advancement count changed; update the data migration plan before porting");
        assertEquals(57, countFiles(UPSTREAM_DATA.resolve("loot_table")),
                "Upstream generated loot table count changed; update the data migration plan before porting");
        assertEquals(29, countFiles(UPSTREAM_DATA.resolve("tags")),
                "Upstream generated tag count changed; update the data migration plan before porting");
    }

    @Test
    void missingCurrentDataResourcesAreExplicitlyTracked() throws IOException {
        String matrix = read(MATRIX);
        long currentRecipes = countFiles(CURRENT_112_RECIPES);
        long currentDataFiles = countFiles(CURRENT_DATA);

        assertEquals(214, currentRecipes,
                "The 1.12 recipe folder should include vanilla/fallback recipes, custom data recipes, FutureMC variants, and _factories.json");
        assertTrue(currentDataFiles > 0 || matrix.contains("current data/justdirethings: 0"),
                "Missing current data resources must stay visible in the parity matrix until ported");
        assertTrue(matrix.contains("assets/justdirethings/recipes"),
                "Matrix should document the 1.12 recipe target location");
        assertTrue(matrix.contains("converted vanilla/fallback 1.12 recipes: 184"),
                "Matrix should record the generated 1.12 vanilla/fallback recipe subset");
        assertTrue(matrix.contains("custom goospread/fluiddrop data recipes: 21"),
                "Matrix should keep custom data recipe coverage visible");
        assertTrue(matrix.contains("FutureMC optional recipe variants: 8"),
                "Matrix should keep FutureMC conditional recipe variants visible");
        assertTrue(matrix.contains("custom/modern recipes still pending: 187"),
                "Matrix should keep unsupported custom/modern recipes visible");
    }

    @Test
    void convertedRecipesUse112RecipeShapes() throws IOException {
        String machineSettingsCopier = read(CURRENT_112_RECIPES.resolve("machinesettingscopier.json"));
        String leafBreaker = read(CURRENT_112_RECIPES.resolve("upgrade_leafbreaker.json"));
        String ferricoreSmelting = read(CURRENT_112_RECIPES.resolve("ferricore_ingot_smelted.json"));

        assertTrue(machineSettingsCopier.contains("\"type\":  \"minecraft:crafting_shaped\""));
        assertTrue(machineSettingsCopier.contains("\"item\":  \"justdirethings:machinesettingscopier\""));
        assertTrue(!machineSettingsCopier.contains("\"id\":"),
                "1.12 recipe results should use item, not modern id");

        assertTrue(leafBreaker.contains("\"type\":  \"forge:ore_shaped\""));
        assertTrue(leafBreaker.contains("\"ore\":  \"treeLeaves\""),
                "Modern leaf tags should become a 1.12 OreDictionary ingredient");

        assertTrue(ferricoreSmelting.contains("\"type\":  \"minecraft:smelting\""));
        assertTrue(ferricoreSmelting.contains("\"result\":  \"justdirethings:ferricore_ingot\""));
    }

    @Test
    void convertedRecipesDoNotReferenceModernOnlyMinecraftItemsDirectly() throws IOException {
        for (Path recipePath : currentRecipeFiles()) {
            String json = read(recipePath);
            for (String modernItem : MODERN_ONLY_MINECRAFT_ITEMS) {
                assertTrue(!json.contains(modernItem),
                        recipePath.getFileName() + " still references modern-only item " + modernItem
                                + "; use a FutureMC conditional variant or a 1.12 fallback");
            }
        }
    }

    @Test
    void futureMcSupportedIngredientsHaveConditionalVariants() throws IOException {
        assertFutureMcVariant("generatort1", "futuremc:blast_furnace", "minecraft:furnace");
        assertFutureMcVariant("generatorfluidt1", "futuremc:blast_furnace", "minecraft:furnace");
        assertFutureMcVariant("upgrade_smelter", "futuremc:blast_furnace", "minecraft:furnace");
        assertFutureMcVariant("upgrade_smoker", "futuremc:smoker", "minecraft:furnace");
        assertFutureMcVariant("upgrade_debuffremover", "futuremc:honey_bottle", "minecraft:sugar");
        assertFutureMcVariant("upgrade_instabreak", "futuremc:netherite_pickaxe", "minecraft:diamond_pickaxe");
        assertFutureMcVariant("upgrade_lavaimmunity", "futuremc:netherite_scrap", "minecraft:obsidian");
        assertFutureMcBlockStateVariant("raw_eclipsealloy_ore-goospread", "futuremc:netherite_block", "minecraft:obsidian");
    }

    private static void assertFutureMcVariant(String recipeId, String futureMcItem, String fallbackItem) throws IOException {
        String fallback = normalized(read(CURRENT_112_RECIPES.resolve(recipeId + ".json")));
        String future = normalized(read(CURRENT_112_RECIPES.resolve(recipeId + "_futuremc.json")));

        assertTrue(fallback.contains("\"type\":\"forge:not\""),
                recipeId + " fallback recipe should be disabled when the FutureMC item exists");
        assertTrue(fallback.contains("\"type\":\"minecraft:item_exists\""),
                recipeId + " fallback recipe should test the FutureMC item registry entry");
        assertTrue(fallback.contains("\"item\":\"" + futureMcItem + "\""),
                recipeId + " fallback recipe should test " + futureMcItem);
        assertTrue(fallback.contains("\"item\":\"" + fallbackItem + "\""),
                recipeId + " fallback recipe should use " + fallbackItem);

        assertTrue(future.contains("\"type\":\"minecraft:item_exists\""),
                recipeId + " FutureMC variant should be gated by item_exists");
        assertTrue(future.contains("\"item\":\"" + futureMcItem + "\""),
                recipeId + " FutureMC variant should use " + futureMcItem);
    }

    private static void assertFutureMcBlockStateVariant(String recipeId, String futureMcBlock, String fallbackBlock) throws IOException {
        String fallback = normalized(read(CURRENT_112_RECIPES.resolve(recipeId + ".json")));
        String future = normalized(read(CURRENT_112_RECIPES.resolve(recipeId + "_futuremc.json")));

        assertTrue(fallback.contains("\"type\":\"forge:not\""),
                recipeId + " fallback data recipe should be disabled when the FutureMC block exists");
        assertTrue(fallback.contains("\"type\":\"minecraft:item_exists\""),
                recipeId + " fallback data recipe should test the FutureMC item registry entry");
        assertTrue(fallback.contains("\"item\":\"" + futureMcBlock + "\""),
                recipeId + " fallback data recipe should test " + futureMcBlock);
        assertTrue(fallback.contains("\"Name\":\"" + fallbackBlock + "\""),
                recipeId + " fallback data recipe should use " + fallbackBlock);

        assertTrue(future.contains("\"type\":\"minecraft:item_exists\""),
                recipeId + " FutureMC data variant should be gated by item_exists");
        assertTrue(future.contains("\"Name\":\"" + futureMcBlock + "\""),
                recipeId + " FutureMC data variant should use " + futureMcBlock);
    }

    private static long countFiles(Path root) throws IOException {
        if (!Files.isDirectory(root)) {
            return 0;
        }
        try (java.util.stream.Stream<Path> files = Files.walk(root)) {
            return files.filter(Files::isRegularFile).count();
        }
    }

    private static String read(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private static List<Path> currentRecipeFiles() throws IOException {
        try (java.util.stream.Stream<Path> files = Files.walk(CURRENT_112_RECIPES)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .collect(java.util.stream.Collectors.toList());
        }
    }

    private static String normalized(String json) {
        return json.replaceAll("\\s+", "");
    }
}
