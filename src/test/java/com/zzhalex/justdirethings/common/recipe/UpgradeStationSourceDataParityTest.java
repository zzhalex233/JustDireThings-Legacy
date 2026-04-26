package com.zzhalex.justdirethings.common.recipe;

import com.zzhalex.justdirethings.registry.ModContentItems;
import com.zzhalex.justdirethings.registry.ModEquipmentItems;
import com.zzhalex.justdirethings.registry.ModItems;
import com.zzhalex.justdirethings.registry.ModRecipes;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpgradeStationSourceDataParityTest {

    private static final Path UPSTREAM_RECIPES = Paths.get("source/JustDireThings-main/src/generated/resources/data/justdirethings/recipe");
    private static final Pattern TYPE_PATTERN = Pattern.compile("\"type\"\\s*:\\s*\"([^\"]+)\"");

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void everyUpstreamSmithingTransformHasA112UpgradeStationRecipe() throws IOException {
        int checked = 0;
        for (Path recipePath : upstreamRecipeFiles()) {
            String json = read(recipePath);
            if (!"minecraft:smithing_transform".equals(extractType(json))) {
                continue;
            }

            ItemStack template = stack(extractNestedItem(json, "template"));
            ItemStack base = stack(extractNestedItem(json, "base"));
            ItemStack addition = stack(extractNestedItem(json, "addition"));
            Item expected = resolveJdtItem(pathOf(extractNestedItem(json, "result")));

            ItemStack output = ModRecipes.getUpgradeStationOutput(template, base, addition);

            assertFalse(output.isEmpty(), "Missing 1.12 upgrade station recipe for " + recipePath.getFileName());
            assertEquals(expected, output.getItem(), "Wrong upgrade output for " + recipePath.getFileName());
            checked++;
        }

        assertEquals(31, checked, "Upstream smithing transform recipe count changed");
    }

    @Test
    void everyUpstreamPaxelRecipeHasA112UpgradeStationRecipe() throws IOException {
        int checked = 0;
        for (Path recipePath : upstreamRecipeFiles()) {
            String json = read(recipePath);
            if (!"justdirethings:paxel".equals(extractType(json))) {
                continue;
            }

            ItemStack template = stack(extractNestedItem(json, "template"));
            ItemStack base = stack(extractNestedItem(json, "base"));
            ItemStack addition = stack(extractNestedItem(json, "addition"));
            Item expected = resolveJdtItem(pathOf(extractNestedItem(json, "result")));

            ItemStack output = ModRecipes.getUpgradeStationOutput(template, base, addition);

            assertFalse(output.isEmpty(), "Missing 1.12 paxel recipe for " + recipePath.getFileName());
            assertEquals(expected, output.getItem(), "Wrong paxel output for " + recipePath.getFileName());
            checked++;
        }

        assertEquals(2, checked, "Upstream paxel recipe count changed");
    }

    @Test
    void everyUpstreamAbilityRecipeBaseIsAcceptedByGeneric112AbilityRecipe() throws IOException {
        int checked = 0;
        for (Path recipePath : upstreamRecipeFiles()) {
            String json = read(recipePath);
            if (!"justdirethings:ability".equals(extractType(json))) {
                continue;
            }

            ItemStack base = stack(extractNestedItem(json, "base"));
            ItemStack addition = stack(extractNestedItem(json, "addition"));

            Optional<UpgradeStationRecipe> recipe = ModRecipes.findUpgradeStationRecipe(ItemStack.EMPTY, base, addition);

            assertTrue(recipe.isPresent(), "Missing 1.12 ability install recipe for " + recipePath.getFileName());
            assertFalse(recipe.get().createOutputStack(ItemStack.EMPTY, base, addition).isEmpty(),
                    "Ability recipe produced no output for " + recipePath.getFileName());
            checked++;
        }

        assertEquals(151, checked, "Upstream ability recipe count changed");
    }

    private static Path[] upstreamRecipeFiles() throws IOException {
        try (Stream<Path> files = Files.walk(UPSTREAM_RECIPES)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .toArray(Path[]::new);
        }
    }

    private static String read(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private static String extractType(String json) {
        Matcher matcher = TYPE_PATTERN.matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String extractNestedItem(String json, String field) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\\{.*?\"(?:item|id)\"\\s*:\\s*\"([^\"]+)\"", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(json);
        assertTrue(matcher.find(), "Missing item/id field in " + field + " block");
        return matcher.group(1);
    }

    private static ItemStack stack(String id) {
        Item item = resolveJdtItem(pathOf(id));
        assertNotNull(item, "Missing item referenced by upstream upgrade station data: " + id);
        return new ItemStack(item);
    }

    private static String pathOf(String id) {
        return id.startsWith("justdirethings:") ? id.substring("justdirethings:".length()) : id;
    }

    private static Item resolveJdtItem(String path) {
        Item item = ModEquipmentItems.getItem(path);
        if (item != null) {
            return item;
        }
        item = ModContentItems.getItem(path);
        if (item != null) {
            return item;
        }
        return ModItems.getItem(path);
    }
}
