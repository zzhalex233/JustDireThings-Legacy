package com.zzhalex.justdirethings.registry;

import net.minecraft.init.Bootstrap;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ModContentResourcesTest {

    private static final Path RESOURCE_ROOT = Paths.get("src/main/resources/assets/justdirethings");
    private static final List<String> ORE_BLOCK_IDS = List.of(
            "raw_ferricore_ore",
            "raw_blazegold_ore",
            "raw_celestigem_ore",
            "raw_eclipsealloy_ore",
            "raw_coal_t1_ore",
            "raw_coal_t2_ore",
            "raw_coal_t3_ore",
            "raw_coal_t4_ore"
    );

    @Test
    void everyContentBlockHasAnInventoryModel() {
        Bootstrap.register();
        for (String blockId : ModContentBlocks.coreContentBlockIds()) {
            Path itemModel = RESOURCE_ROOT.resolve(Paths.get("models", "item", blockId + ".json"));
            assertTrue(Files.exists(itemModel), "Missing item model for block item " + blockId);
        }
    }

    @Test
    void oreBlockstatesKeepFacingVariantsForSculptedRawOreModels() throws IOException {
        for (String blockId : ORE_BLOCK_IDS) {
            Path blockstate = RESOURCE_ROOT.resolve(Paths.get("blockstates", blockId + ".json"));
            String contents = Files.readString(blockstate, StandardCharsets.UTF_8);

            assertTrue(contents.contains("\"facing=up\""), "Expected facing variants for " + blockId);
            assertTrue(contents.contains("\"facing=north\""), "Expected rotated north variant for " + blockId);
            assertTrue(!contents.contains("justdirethings:block/" + blockId),
                    "Expected 1.12 blockstate model path format for " + blockId);
        }
    }
}
