package com.zzhalex.justdirethings.registry;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UpgradeStationResourcesTest {

    private static final Path RESOURCE_ROOT = Paths.get("src/main/resources/assets/justdirethings");

    @Test
    void upgradeStationBlockModelUsesJustDireThingsTextures() throws Exception {
        String model = Files.readString(RESOURCE_ROOT.resolve(Paths.get("models", "block", "upgrade_station.json")), StandardCharsets.UTF_8);

        assertTrue(!model.contains("minecraft:blocks/iron_block"), "Upgrade Station must not keep the old iron-block placeholder texture");
        assertTrue(model.contains("justdirethings:block/"), "Upgrade Station should use Just Dire Things block textures");
        assertTrue(model.contains("paradoxmachine_side") || model.contains("ferricore_block"),
                "Upgrade Station should visually belong to the Just Dire Things machine/material set");
    }

    @Test
    void upgradeStationItemModelUsesBlockModel() throws Exception {
        String model = Files.readString(RESOURCE_ROOT.resolve(Paths.get("models", "item", "upgrade_station.json")), StandardCharsets.UTF_8);

        assertTrue(model.contains("justdirethings:block/upgrade_station"),
                "Upgrade Station item should render from the fixed block model");
    }
}
