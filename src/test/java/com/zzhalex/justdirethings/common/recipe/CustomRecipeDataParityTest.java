package com.zzhalex.justdirethings.common.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zzhalex.justdirethings.common.recipe.custom.FluidDropDataRecipe;
import com.zzhalex.justdirethings.common.recipe.custom.GooSpreadDataRecipe;
import com.zzhalex.justdirethings.common.recipe.custom.GooSpreadTagDataRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.JsonContext;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomRecipeDataParityTest {

    private static final Path CURRENT_112_RECIPES = Paths.get("src/main/resources/assets/justdirethings/recipes");
    private static final JsonContext CONTEXT = new JsonContext("justdirethings");

    @Test
    void forgeRecipeFactoriesExposeJdtCustomRecipeTypes() throws IOException {
        String factories = read(CURRENT_112_RECIPES.resolve("_factories.json"));

        assertTrue(factories.contains("\"goospread\""));
        assertTrue(factories.contains("GooSpreadDataRecipe$Factory"));
        assertTrue(factories.contains("\"goospread_tag\""));
        assertTrue(factories.contains("GooSpreadTagDataRecipe$Factory"));
        assertTrue(factories.contains("\"fluiddrop\""));
        assertTrue(factories.contains("FluidDropDataRecipe$Factory"));
    }

    @Test
    void upstreamCustomRecipeDataIsPresentIn112RecipeFolder() throws IOException {
        Map<String, Long> counts = Files.list(CURRENT_112_RECIPES)
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().endsWith(".json"))
                .filter(path -> !path.getFileName().toString().equals("_factories.json"))
                .filter(path -> !path.getFileName().toString().endsWith("_futuremc.json"))
                .map(CustomRecipeDataParityTest::readUnchecked)
                .map(CustomRecipeDataParityTest::typeOf)
                .collect(Collectors.groupingBy(type -> type, Collectors.counting()));

        assertEquals(14L, counts.get("justdirethings:goospread"));
        assertEquals(1L, counts.get("justdirethings:goospread_tag"));
        assertEquals(6L, counts.get("justdirethings:fluiddrop"));
    }

    @Test
    void gooSpreadFactoryParsesBlockStateAndTimingData() throws IOException {
        JsonObject json = parse(CURRENT_112_RECIPES.resolve("raw_ferricore_ore-goospread.json"));
        GooSpreadDataRecipe recipe = (GooSpreadDataRecipe) new GooSpreadDataRecipe.Factory().parse(CONTEXT, json);

        assertEquals(new ResourceLocation("justdirethings", "dire_iron_block"), recipe.getSourceId());
        assertEquals(new ResourceLocation("minecraft", "iron_block"), recipe.getInput().getBlockId());
        assertEquals(new ResourceLocation("justdirethings", "raw_ferricore_ore"), recipe.getOutput().getBlockId());
        assertEquals("north", recipe.getOutput().getProperties().get("facing"));
        assertEquals(1, recipe.getTierRequirement());
        assertEquals(2400, recipe.getCraftingDuration());
    }

    @Test
    void gooSpreadTagFactoryParsesTagData() throws IOException {
        JsonObject json = parse(CURRENT_112_RECIPES.resolve("raw_coal_t1_ore-goospread_tag.json"));
        GooSpreadTagDataRecipe recipe = (GooSpreadTagDataRecipe) new GooSpreadTagDataRecipe.Factory().parse(CONTEXT, json);

        assertEquals("c:storage_blocks/charcoal", recipe.getInputTag());
        assertEquals(new ResourceLocation("justdirethings", "raw_coal_t1_ore"), recipe.getOutput().getBlockId());
        assertEquals(1, recipe.getTierRequirement());
        assertEquals(2400, recipe.getCraftingDuration());
    }

    @Test
    void fluidDropFactoryParsesCatalystAndFluidStates() throws IOException {
        JsonObject json = parse(CURRENT_112_RECIPES.resolve("time_fluid_block-fluiddrop.json"));
        FluidDropDataRecipe recipe = (FluidDropDataRecipe) new FluidDropDataRecipe.Factory().parse(CONTEXT, json);

        assertEquals(new ResourceLocation("justdirethings", "time_fluid"), recipe.getSourceId());
        assertEquals(new ResourceLocation("justdirethings", "polymorphic_fluid_block"), recipe.getInput().getBlockId());
        assertEquals("0", recipe.getInput().getProperties().get("level"));
        assertEquals(new ResourceLocation("justdirethings", "time_fluid_block"), recipe.getOutput().getBlockId());
        assertEquals(new ResourceLocation("justdirethings", "time_crystal"), recipe.getCatalystId());
    }

    private static JsonObject parse(Path path) throws IOException {
        return new JsonParser().parse(read(path)).getAsJsonObject();
    }

    private static String typeOf(String json) {
        return new JsonParser().parse(json).getAsJsonObject().get("type").getAsString();
    }

    private static String read(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private static String readUnchecked(Path path) {
        try {
            return read(path);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
