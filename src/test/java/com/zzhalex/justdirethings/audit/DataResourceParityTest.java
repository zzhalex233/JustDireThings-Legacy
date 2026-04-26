package com.zzhalex.justdirethings.audit;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataResourceParityTest {

    private static final Path UPSTREAM_DATA = Paths.get("source/JustDireThings-main/src/generated/resources/data/justdirethings");
    private static final Path CURRENT_112_RECIPES = Paths.get("src/main/resources/assets/justdirethings/recipes");
    private static final Path CURRENT_DATA = Paths.get("src/main/resources/data/justdirethings");
    private static final Path MATRIX = Paths.get("docs/audit/justdirethings-source-parity-matrix.md");

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

        assertEquals(184, currentRecipes,
                "The vanilla crafting/smelting subset converted from upstream should stay stable until the next data phase");
        assertTrue(currentDataFiles > 0 || matrix.contains("current data/justdirethings: 0"),
                "Missing current data resources must stay visible in the parity matrix until ported");
        assertTrue(matrix.contains("assets/justdirethings/recipes"),
                "Matrix should document the 1.12 recipe target location");
        assertTrue(matrix.contains("converted vanilla 1.12 recipes: 184"),
                "Matrix should record the generated 1.12 vanilla recipe subset");
        assertTrue(matrix.contains("custom/modern recipes still pending: 208"),
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
}
