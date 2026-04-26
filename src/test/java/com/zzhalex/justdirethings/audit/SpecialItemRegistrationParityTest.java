package com.zzhalex.justdirethings.audit;

import com.zzhalex.justdirethings.registry.ModEquipmentItems;
import com.zzhalex.justdirethings.registry.ModItems;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecialItemRegistrationParityTest {

    private static final List<String> SPECIAL_ITEM_IDS = Arrays.asList(
            "fuel_canister",
            "pocket_generator",
            "ferricore_wrench",
            "totem_of_death_recall",
            "blazejet_wand",
            "voidshift_wand",
            "eclipsegate_wand",
            "time_wand",
            "creaturecatcher",
            "machinesettingscopier",
            "portalgun",
            "portalgun_v2",
            "fluid_canister",
            "potion_canister",
            "polymorphic_wand",
            "polymorphic_wand_v2"
    );

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void upstreamSpecialItemsAreRegisteredWithOriginalIds() {
        for (String id : SPECIAL_ITEM_IDS) {
            Item item = resolveItem(id);

            assertNotNull(item, "Missing special item registration: " + id);
            assertNotNull(item.getRegistryName(), "Missing registry name for: " + id);
            assertTrue(id.equals(item.getRegistryName().getPath()),
                    "Special item should keep upstream id " + id + " but was " + item.getRegistryName());
        }
    }

    @Test
    void upstreamSpecialItemsHaveModelsAndLangKeys() throws Exception {
        String enUs = read("src/main/resources/assets/justdirethings/lang/en_us.lang");
        String zhCn = read("src/main/resources/assets/justdirethings/lang/zh_cn.lang");

        for (String id : SPECIAL_ITEM_IDS) {
            assertTrue(Files.exists(Paths.get("src/main/resources/assets/justdirethings/models/item/" + id + ".json")),
                    "Missing item model for: " + id);
            assertTrue(enUs.contains("item.justdirethings." + id + ".name="),
                    "Missing en_us item key for: " + id);
            assertTrue(zhCn.contains("item.justdirethings." + id + ".name="),
                    "Missing zh_cn item key for: " + id);
        }
    }

    @Test
    void upstreamSpecialItemsDoNotExposeMinecraftNullTranslationKey() {
        for (String id : SPECIAL_ITEM_IDS) {
            Item item = resolveItem(id);

            assertNotNull(item, "Missing special item registration: " + id);
            assertFalse(item.getTranslationKey().contains("null"),
                    "Special item " + id + " should not render as item.null.name");
            assertTrue(item.getTranslationKey().startsWith("item.justdirethings."),
                    "Special item " + id + " should use the JustDireThings translation namespace");
        }
    }

    private static Item resolveItem(String id) {
        Item item = ModEquipmentItems.getItem(id);
        return item != null ? item : ModItems.getItem(id);
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
