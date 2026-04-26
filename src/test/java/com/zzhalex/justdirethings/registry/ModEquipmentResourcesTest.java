package com.zzhalex.justdirethings.registry;

import net.minecraft.init.Bootstrap;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ModEquipmentResourcesTest {

    private static final Path RESOURCE_ROOT = Paths.get("src/main/resources/assets/justdirethings");

    @Test
    void everyEquipmentItemHasAModelFile() {
        Bootstrap.register();

        for (String id : ModEquipmentItems.toolItemIds()) {
            assertHasItemModel(id);
        }
        for (String id : ModEquipmentItems.bowItemIds()) {
            assertHasItemModel(id);
        }
        for (String id : ModEquipmentItems.armorItemIds()) {
            assertHasItemModel(id);
        }
    }

    @Test
    void armorLayerTexturesExistForEveryMaterial() {
        assertHasArmorLayer("ferricore", 1);
        assertHasArmorLayer("ferricore", 2);
        assertHasArmorLayer("blazegold", 1);
        assertHasArmorLayer("blazegold", 2);
        assertHasArmorLayer("celestigem", 1);
        assertHasArmorLayer("celestigem", 2);
        assertHasArmorLayer("eclipsealloy", 1);
        assertHasArmorLayer("eclipsealloy", 2);
    }

    @Test
    void armorItemModelsStayTrimFreeIn112() throws IOException {
        for (String id : ModEquipmentItems.armorItemIds()) {
            Path model = RESOURCE_ROOT.resolve(Paths.get("models", "item", id + ".json"));
            String contents = Files.readString(model, StandardCharsets.UTF_8);
            assertTrue(!contents.contains("trim_type"), "Armor model still references trim_type: " + id);
            assertTrue(!contents.contains("trims/items"), "Armor model still references trim textures: " + id);
        }
    }

    private static void assertHasItemModel(String id) {
        Path model = RESOURCE_ROOT.resolve(Paths.get("models", "item", id + ".json"));
        assertTrue(Files.exists(model), "Missing equipment model " + id);
    }

    private static void assertHasArmorLayer(String material, int layer) {
        Path texture = RESOURCE_ROOT.resolve(Paths.get("textures", "models", "armor", material + "_layer_" + layer + ".png"));
        assertTrue(Files.exists(texture), "Missing armor texture " + texture);
    }
}
