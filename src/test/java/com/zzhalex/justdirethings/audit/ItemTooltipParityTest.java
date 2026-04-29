package com.zzhalex.justdirethings.audit;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemTooltipParityTest {

    @Test
    void sharedTooltipHelperPortsUpstreamItemHoverBuildingBlocks() throws IOException {
        String helper = read("src/main/java/com/zzhalex/justdirethings/common/item/tooltip/TooltipHelper.java");

        for (String token : Arrays.asList(
                "appendFEText",
                "appendToolEnabled",
                "appendAbilityList",
                "appendShiftForInfo",
                "appendUpgradeDetails",
                "appendGeneratorDetails",
                "justdirethings.festored",
                "justdirethings.missingupgrade",
                "justdirethings.shiftmoreinfo"
        )) {
            assertTrue(helper.contains(token), "TooltipHelper should contain upstream helper behavior: " + token);
        }
    }

    @Test
    void poweredAndToggleableBasesExposeUpstreamHoverText() throws IOException {
        String toggleable = read("src/main/java/com/zzhalex/justdirethings/common/item/base/ItemToggleableTool.java");
        String powered = read("src/main/java/com/zzhalex/justdirethings/common/item/base/ItemPoweredTool.java");
        String support = read("src/main/java/com/zzhalex/justdirethings/common/item/equipment/EquipmentItemSupport.java");

        assertTrue(toggleable.contains("addInformation") && toggleable.contains("appendToolEnabled") && toggleable.contains("appendAbilityList"),
                "ItemToggleableTool should restore enabled/disabled and ability list hover text");
        assertTrue(powered.contains("addInformation") && powered.contains("appendFEText"),
                "ItemPoweredTool should restore FE hover text for powered items");
        assertTrue(support.contains("appendEquipmentTooltip") && support.contains("appendFEText") && support.contains("appendAbilityList"),
                "Equipment items should share the same upstream FE/enabled/ability tooltip path");
    }

    @Test
    void portableAndSpecialItemsExposeTheirUpstreamHoverText() throws IOException {
        for (String path : Arrays.asList(
                "src/main/java/com/zzhalex/justdirethings/common/item/misc/FuelCanisterItem.java",
                "src/main/java/com/zzhalex/justdirethings/common/item/misc/FluidCanisterItem.java",
                "src/main/java/com/zzhalex/justdirethings/common/item/misc/PotionCanisterItem.java",
                "src/main/java/com/zzhalex/justdirethings/common/item/misc/PocketGeneratorItem.java",
                "src/main/java/com/zzhalex/justdirethings/common/item/tool/ItemPortalGunV2.java",
                "src/main/java/com/zzhalex/justdirethings/common/item/tool/ItemTimeWand.java",
                "src/main/java/com/zzhalex/justdirethings/common/item/tool/ItemPolymorphicWand.java",
                "src/main/java/com/zzhalex/justdirethings/common/item/tool/ItemPolymorphicWandV2.java"
        )) {
            assertTrue(read(path).contains("addInformation"), path + " should implement its upstream item hover tooltip");
        }
    }

    @Test
    void contentRegistrationUsesTooltipAwareItemsForUpgradesAndSpecialResources() throws IOException {
        String contentItems = read("src/main/java/com/zzhalex/justdirethings/registry/ModContentItems.java");

        assertTrue(contentItems.contains("ItemUpgradeContent"),
                "Ability upgrade items should not be plain ItemSimpleContent; they need Shift detail/flavor tooltips");
        assertTrue(contentItems.contains("ItemPolymorphicCatalyst") && contentItems.contains("ItemTimeCrystal"),
                "Special resource items should expose their upstream tooltip behavior");
    }

    @Test
    void tooltipLocalizationKeysExistInBothLanguages() throws IOException {
        String en = read("src/main/resources/assets/justdirethings/lang/en_us.lang");
        String zh = read("src/main/resources/assets/justdirethings/lang/zh_cn.lang");

        for (String key : requiredTooltipKeys()) {
            assertTrue(en.contains(key + "="), "Missing English tooltip key: " + key);
            assertTrue(zh.contains(key + "="), "Missing Chinese tooltip key: " + key);
        }
    }

    private static List<String> requiredTooltipKeys() {
        return Arrays.asList(
                "justdirethings.festored",
                "justdirethings.shiftmoreinfo",
                "justdirethings.presshotkey",
                "justdirethings.enabled",
                "justdirethings.disabled",
                "justdirethings.pocketgeneratorburntime",
                "justdirethings.pocketgeneratorfuelstack",
                "justdirethings.pocketgeneratornofuel",
                "justdirethings.timefluidamt",
                "justdirethings.polymorphicfluidamt",
                "justdirethings.polymorphset",
                "justdirethings.unbound",
                "justdirethings.boundside",
                "justdirethings.fillmode",
                "justdirethings.fillmode.none",
                "justdirethings.fillmode.jdtonly",
                "justdirethings.fillmode.all",
                "justdirethings.hint.dropinwater",
                "justdirethings.missingupgrade",
                "justdirethings.requiresfeeding",
                "justdirethings.timecrystaltooltip",
                "justdirethings.timecrystaltooltiptwo",
                "justdirethings.decoy.detailtext"
        );
    }

    private static String read(String path) throws IOException {
        return Files.readString(path(path), StandardCharsets.UTF_8);
    }

    private static Path path(String path) {
        return Paths.get(path);
    }
}
