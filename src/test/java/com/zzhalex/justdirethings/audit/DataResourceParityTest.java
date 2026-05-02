package com.zzhalex.justdirethings.audit;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final Map<String, String> LEGACY_112_ITEM_REPLACEMENTS = new HashMap<>();

    static {
        LEGACY_112_ITEM_REPLACEMENTS.put("justdirethings:time_fluid_bucket", "forge:bucketfilled with time_fluid NBT");
        LEGACY_112_ITEM_REPLACEMENTS.put("minecraft:charcoal", "minecraft:coal data 1");
        LEGACY_112_ITEM_REPLACEMENTS.put("minecraft:lapis_lazuli", "minecraft:dye data 4");
        LEGACY_112_ITEM_REPLACEMENTS.put("minecraft:popped_chorus_fruit", "minecraft:chorus_fruit_popped");
        LEGACY_112_ITEM_REPLACEMENTS.put("minecraft:pufferfish", "minecraft:fish data 3");
        LEGACY_112_ITEM_REPLACEMENTS.put("minecraft:slime_block", "minecraft:slime");
        LEGACY_112_ITEM_REPLACEMENTS.put("minecraft:wither_skeleton_skull", "minecraft:skull data 1");
    }

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

        assertEquals(211, currentRecipes,
                "The 1.12 recipe folder should include vanilla/fallback recipes, custom data recipes, FutureMC variants, and _factories.json");
        assertTrue(currentDataFiles > 0 || matrix.contains("current data/justdirethings: 0"),
                "Missing current data resources must stay visible in the parity matrix until ported");
        assertTrue(matrix.contains("assets/justdirethings/recipes"),
                "Matrix should document the 1.12 recipe target location");
        assertTrue(matrix.contains("converted vanilla/fallback 1.12 recipes: 181"),
                "Matrix should record the generated 1.12 vanilla/fallback recipe subset");
        assertTrue(matrix.contains("custom goospread/fluiddrop data recipes: 21"),
                "Matrix should keep custom data recipe coverage visible");
        assertTrue(matrix.contains("FutureMC optional recipe variants: 8"),
                "Matrix should keep FutureMC conditional recipe variants visible");
        assertTrue(matrix.contains("custom/modern recipes still pending: 184"),
                "Matrix should keep unsupported custom/modern recipes visible");
    }

    @Test
    void convertedRecipesUse112RecipeShapes() throws IOException {
        String machineSettingsCopier = read(CURRENT_112_RECIPES.resolve("machinesettingscopier.json"));
        String leafBreaker = read(CURRENT_112_RECIPES.resolve("upgrade_leafbreaker.json"));
        String modRecipes = normalized(read(Paths.get("src/main/java/com/zzhalex/justdirethings/registry/ModRecipes.java")));

        assertTrue(machineSettingsCopier.contains("\"type\":  \"minecraft:crafting_shaped\""));
        assertTrue(machineSettingsCopier.contains("\"item\":  \"justdirethings:machinesettingscopier\""));
        assertTrue(!machineSettingsCopier.contains("\"id\":"),
                "1.12 recipe results should use item, not modern id");

        assertTrue(leafBreaker.contains("\"type\":  \"forge:ore_shaped\""));
        assertTrue(leafBreaker.contains("\"ore\":  \"treeLeaves\""),
                "Modern leaf tags should become a 1.12 OreDictionary ingredient");

        assertTrue(modRecipes.contains("addSmelting(\"raw_ferricore\",\"ferricore_ingot\",1.0F)"),
                "1.12 furnace recipes should be registered through GameRegistry, not written as unsupported smelting JSON");
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
    void convertedRecipesDoNotReferenceRenamed112MetadataItemsDirectly() throws IOException {
        for (Path recipePath : currentRecipeFiles()) {
            String json = read(recipePath);
            for (Map.Entry<String, String> replacement : LEGACY_112_ITEM_REPLACEMENTS.entrySet()) {
                assertTrue(!json.contains(replacement.getKey()),
                        recipePath.getFileName() + " still references " + replacement.getKey()
                                + "; use " + replacement.getValue() + " for 1.12.2");
            }
        }
    }

    @Test
    void convertedRecipeFolderDoesNotUseUnsupported112RecipeTypes() throws IOException {
        for (Path recipePath : currentRecipeFiles()) {
            String json = normalized(read(recipePath));
            assertTrue(!json.contains("\"type\":\"minecraft:smelting\""),
                    recipePath.getFileName() + " uses minecraft:smelting, which Forge 1.12's crafting recipe loader rejects");
        }
    }

    @Test
    void metadataSensitiveVanillaRecipesDeclareDataValues() throws IOException {
        String charcoalToBlock = normalized(read(CURRENT_112_RECIPES.resolve("charcoal_9x9.json")));
        String blockToCharcoal = normalized(read(CURRENT_112_RECIPES.resolve("charcoal_block_9x9.json")));
        String fuelCanister = normalized(read(CURRENT_112_RECIPES.resolve("fuel_canister.json")));
        String generator = normalized(read(CURRENT_112_RECIPES.resolve("generatort1.json")));
        String generatorFutureMc = normalized(read(CURRENT_112_RECIPES.resolve("generatort1_futuremc.json")));
        String gooTier1 = normalized(read(CURRENT_112_RECIPES.resolve("gooblock_tier1.json")));
        String pocketGenerator = normalized(read(CURRENT_112_RECIPES.resolve("pocket_generator.json")));
        String cauterizeWounds = normalized(read(CURRENT_112_RECIPES.resolve("upgrade_cauterizewounds.json")));
        String debuffRemover = normalized(read(CURRENT_112_RECIPES.resolve("upgrade_debuffremover.json")));
        String debuffRemoverFutureMc = normalized(read(CURRENT_112_RECIPES.resolve("upgrade_debuffremover_futuremc.json")));
        String paradoxMachine = normalized(read(CURRENT_112_RECIPES.resolve("paradoxmachine.json")));
        String skysweeper = normalized(read(CURRENT_112_RECIPES.resolve("upgrade_skysweeper.json")));

        assertTrue(charcoalToBlock.contains("\"item\":\"minecraft:coal\",\"data\":1"),
                "1.12 charcoal ingredients should use coal metadata 1");
        assertTrue(blockToCharcoal.contains("\"item\":\"minecraft:coal\",\"count\":9,\"data\":1"),
                "1.12 charcoal recipe outputs should use coal metadata 1");
        assertTrue(fuelCanister.contains("\"item\":\"minecraft:coal\",\"data\":0"),
                "1.12 coal ingredients should declare data 0 so Forge does not reject the recipe as ambiguous");
        assertTrue(generator.contains("\"item\":\"minecraft:coal\",\"data\":0"),
                "1.12 coal ingredients should declare data 0 so Forge does not reject the recipe as ambiguous");
        assertTrue(generatorFutureMc.contains("\"item\":\"minecraft:coal\",\"data\":0"),
                "1.12 coal ingredients should declare data 0 so Forge does not reject the FutureMC variant");
        assertTrue(gooTier1.contains("\"item\":\"minecraft:dirt\",\"data\":0"),
                "1.12 dirt ingredients should declare data 0 so Forge does not reject the recipe as ambiguous");
        assertTrue(pocketGenerator.contains("\"item\":\"minecraft:coal\",\"data\":0"),
                "1.12 coal ingredients should declare data 0 so Forge does not reject the recipe as ambiguous");
        assertTrue(cauterizeWounds.contains("\"item\":\"minecraft:golden_apple\",\"data\":0"),
                "1.12 golden apple ingredients should declare data 0 to avoid enchanted apple ambiguity");
        assertTrue(debuffRemover.contains("\"item\":\"minecraft:golden_apple\",\"data\":0"),
                "1.12 golden apple ingredients should declare data 0 to avoid enchanted apple ambiguity");
        assertTrue(debuffRemoverFutureMc.contains("\"item\":\"minecraft:golden_apple\",\"data\":0"),
                "1.12 golden apple ingredients should declare data 0 in the FutureMC variant too");
        assertTrue(paradoxMachine.contains("\"type\":\"minecraft:item_nbt\"")
                        && paradoxMachine.contains("\"item\":\"forge:bucketfilled\"")
                        && paradoxMachine.contains("\"FluidName\":\"time_fluid\"")
                        && paradoxMachine.contains("\"Amount\":1000"),
                "The paradox machine should use Forge's universal bucket item with time_fluid NBT in 1.12");
        assertTrue(skysweeper.contains("\"item\":\"minecraft:sand\",\"data\":0"),
                "1.12 sand ingredients should declare data 0 so Forge does not reject the recipe as ambiguous");
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

    @Test
    void upstreamBlastingRecipesAreCoveredBy112SmeltingDecision() throws IOException {
        Set<String> coveredByRuntimeSmelting = new TreeSet<>(Arrays.asList(
                "blazegold_ingot_blasted.json",
                "eclipsealloy_ingot_blasted.json",
                "ferricore_ingot_blasted.json"
        ));

        List<Path> upstreamBlastingRecipes;
        try (java.util.stream.Stream<Path> files = Files.walk(UPSTREAM_DATA.resolve("recipe"))) {
            upstreamBlastingRecipes = files.filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            return normalized(read(path)).contains("\"type\":\"minecraft:blasting\"");
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .sorted()
                    .collect(java.util.stream.Collectors.toList());
        }

        assertEquals(coveredByRuntimeSmelting,
                upstreamBlastingRecipes.stream()
                        .map(path -> path.getFileName().toString())
                        .collect(java.util.stream.Collectors.toCollection(TreeSet::new)),
                "New upstream blasting recipes need an explicit 1.12 conversion decision");

        String migration = read(Paths.get("docs/audit/recipe-data-migration.md"));
        String modRecipes = normalized(read(Paths.get("src/main/java/com/zzhalex/justdirethings/registry/ModRecipes.java")));
        for (Path upstreamRecipe : upstreamBlastingRecipes) {
            String upstreamName = upstreamRecipe.getFileName().toString();
            String upstream = normalized(read(upstreamRecipe));
            String input = extractJsonString(upstream, "\"item\":\"justdirethings:", "\"");
            String output = extractJsonString(upstream, "\"id\":\"justdirethings:", "\"");

            assertTrue(modRecipes.contains("addSmelting(\"" + input + "\",\"" + output + "\","),
                    upstreamName + " should be registered as a 1.12 GameRegistry furnace recipe");
            assertTrue(migration.contains(upstreamName.replace(".json", ""))
                            && migration.contains("ModRecipes.addSmelting"),
                    "Blasting-to-smelting decision for " + upstreamName + " should be documented");
        }
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

    private static String extractJsonString(String json, String prefix, String suffix) {
        int start = json.indexOf(prefix);
        assertTrue(start >= 0, "Missing " + prefix + " in " + json);
        int valueStart = start + prefix.length();
        int valueEnd = json.indexOf(suffix, valueStart);
        assertTrue(valueEnd > valueStart, "Missing value after " + prefix + " in " + json);
        return json.substring(valueStart, valueEnd);
    }
}
