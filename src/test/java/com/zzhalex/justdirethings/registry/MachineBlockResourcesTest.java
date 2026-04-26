package com.zzhalex.justdirethings.registry;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineBlockResourcesTest {

    private static final Path RESOURCE_ROOT = Paths.get("src/main/resources/assets/justdirethings");

    @Test
    void keyMachineBlockTexturesExist() {
        assertExists(Paths.get("textures", "block", "blockbreakert1_top.png"));
        assertExists(Paths.get("textures", "block", "blockplacert1_side.png"));
        assertExists(Paths.get("textures", "block", "blockswappert1_top.png"));
        assertExists(Paths.get("textures", "block", "clickert1_top.png"));
        assertExists(Paths.get("textures", "block", "droppert1_side.png"));
        assertExists(Paths.get("textures", "block", "fluidcollectort1_top.png"));
        assertExists(Paths.get("textures", "block", "fluidplacert1_top.png"));
        assertExists(Paths.get("textures", "block", "generatort1_top.png"));
        assertExists(Paths.get("textures", "block", "generatorfluidt1_top.png"));
        assertExists(Paths.get("textures", "block", "sensort1_top.png"));
        assertExists(Paths.get("textures", "block", "itemcollector.png"));
        assertExists(Paths.get("textures", "block", "energytransmitter.png"));
        assertExists(Paths.get("textures", "block", "experienceholder.png"));
        assertExists(Paths.get("textures", "block", "inventory_holder.png"));
        assertExists(Paths.get("textures", "block", "playeraccessor.png"));
    }

    @Test
    void keyMachineModelsUseUpstreamStyleDefinitions() throws IOException {
        assertFileContains(Paths.get("models", "block", "blockbreakert1.json"), "justdirethings:block/orientable_with_bottom");
        assertFileContains(Paths.get("models", "block", "generatort1.json"), "generatort1_top");
        assertFileContains(Paths.get("models", "block", "generatorfluidt1.json"), "generatorfluidt1_top");
        assertFileContains(Paths.get("models", "block", "itemcollector.json"), "itemcollector");
        assertFileContains(Paths.get("models", "block", "energytransmitter.json"), "energytransmitter_animation");
        assertFileContains(Paths.get("models", "block", "experienceholder.json"), "experienceholder_animation");
        assertFileContains(Paths.get("models", "block", "orientable_with_bottom.json"), "\"north\": { \"texture\": \"#front\"");
        assertFileContains(Paths.get("models", "block", "orientable_with_bottom.json"), "\"elements\"");
        assertFileContains(Paths.get("models", "block", "orientable_with_bottom.json"), "\"from\": [0, 0, 0]");
        assertFileContains(Paths.get("models", "block", "orientable_with_bottom.json"), "\"to\": [16, 16, 16]");
        assertFileContains(Paths.get("models", "block", "orientable_with_bottom.json"), "\"texture\": \"#bottom\"");
        assertFileContains(Paths.get("models", "block", "orientable_with_bottom.json"), "\"texture\": \"#front\"");
    }

    @Test
    void directionalMachineBlockstatesExposeFacingVariantsAndInventoryModels() throws IOException {
        assertFileContains(Paths.get("blockstates", "blockbreakert1.json"), "\"facing=north\"");
        assertFileContains(Paths.get("blockstates", "blockbreakert1.json"), "\"inventory\"");
        assertFileContains(Paths.get("blockstates", "generatort1.json"), "\"model\": \"justdirethings:generatort1\"");
        assertFileContains(Paths.get("blockstates", "generatorfluidt1.json"), "\"model\": \"justdirethings:generatorfluidt1\"");
        assertFileContains(Paths.get("blockstates", "sensort1.json"), "\"facing=up\"");
        assertFileContains(Paths.get("blockstates", "playeraccessor.json"), "\"inventory\"");
    }

    private static void assertExists(Path relativePath) {
        Path path = RESOURCE_ROOT.resolve(relativePath);
        assertTrue(Files.exists(path), "Missing machine resource " + path);
    }

    private static void assertFileContains(Path relativePath, String expectedSnippet) throws IOException {
        Path path = RESOURCE_ROOT.resolve(relativePath);
        String contents = Files.readString(path, StandardCharsets.UTF_8);
        assertTrue(contents.contains(expectedSnippet), "Expected " + expectedSnippet + " in " + path);
    }
}
