package com.zzhalex.justdirethings.registry;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GuiAndLangResourcesTest {

    private static final Path RESOURCE_ROOT = Paths.get("src/main/resources/assets/justdirethings");

    @Test
    void keyGuiTexturesExist() {
        assertExists(Paths.get("textures", "gui", "justinv.png"));
        assertExists(Paths.get("textures", "gui", "justslot.png"));
        assertExists(Paths.get("textures", "gui", "powerbar.png"));
        assertExists(Paths.get("textures", "gui", "fluidbar.png"));
        assertExists(Paths.get("textures", "gui", "blockbreakert1.png"));
        assertExists(Paths.get("textures", "gui", "fuelcanister.png"));
        assertExists(Paths.get("textures", "gui", "itemcollector.png"));
        assertExists(Paths.get("textures", "gui", "pocketgenerator.png"));
        assertExists(Paths.get("textures", "gui", "settings.png"));
        assertExists(Paths.get("textures", "gui", "buttons", "add.png"));
    }

    @Test
    void machineGuiCodeUsesJustDireThingsSlotResources() throws IOException {
        Path guiMachineBase = Paths.get("src/main/java/com/zzhalex/justdirethings/client/gui/base/GuiMachineBase.java");
        String guiMachineBaseContents = Files.readString(guiMachineBase, StandardCharsets.UTF_8);

        assertTrue(guiMachineBaseContents.contains("textures/gui/sprites/background.png"),
                "Machine GUI base should use the upstream shared background panel");
        assertTrue(guiMachineBaseContents.contains("textures/gui/justslot.png"),
                "Machine GUI base should draw upstream-style Just Dire Things slot backgrounds");
    }

    @Test
    void playerAccessorGuiUsesJustDireThingsResourcesInsteadOfVanillaChest() throws IOException {
        Path playerAccessorGui = Paths.get("src/main/java/com/zzhalex/justdirethings/client/gui/machine/GuiPlayerAccessor.java");
        String playerAccessorContents = Files.readString(playerAccessorGui, StandardCharsets.UTF_8);

        assertTrue(playerAccessorContents.contains("extends GuiMachineBase"),
                "Player Accessor should use the shared Just Dire Things machine GUI base");
        assertTrue(!playerAccessorContents.contains("textures/gui/container/generic_54.png"),
                "Player Accessor should not use the vanilla chest GUI texture");
    }

    @Test
    void migratedMachineScreensDoNotUseOldFullBackgroundTextures() throws IOException {
        Path blockBreakerGui = Paths.get("src/main/java/com/zzhalex/justdirethings/client/gui/machine/GuiBlockBreaker.java");
        Path itemCollectorGui = Paths.get("src/main/java/com/zzhalex/justdirethings/client/gui/machine/GuiItemCollector.java");

        String blockBreakerContents = Files.readString(blockBreakerGui, StandardCharsets.UTF_8);
        String itemCollectorContents = Files.readString(itemCollectorGui, StandardCharsets.UTF_8);

        assertTrue(!blockBreakerContents.contains("textures/gui/blockbreakert1.png"),
                "Block Breaker screen should use the shared upstream machine background");
        assertTrue(!itemCollectorContents.contains("textures/gui/itemcollector.png"),
                "Item Collector screen should use the shared upstream machine background");
    }

    @Test
    void zhCnLanguageFileContainsRealChineseTranslations() throws IOException {
        Path zhCn = RESOURCE_ROOT.resolve(Paths.get("lang", "zh_cn.lang"));
        String contents = Files.readString(zhCn, StandardCharsets.UTF_8);

        assertTrue(contents.contains("item.justdirethings.portal_gun_v2.name=高级传送枪"),
                "Expected translated portal gun entry in zh_cn.lang");
        assertTrue(contents.contains("item.justdirethings.fuel_canister.name=燃料罐"),
                "Expected translated fuel canister entry in zh_cn.lang");
        assertTrue(contents.contains("tile.justdirethings.raw_eclipsealloy_ore.name=粗蚀空合金矿簇"),
                "Expected translated raw eclipse alloy ore entry in zh_cn.lang");
    }

    private static void assertExists(Path relativePath) {
        Path path = RESOURCE_ROOT.resolve(relativePath);
        assertTrue(Files.exists(path), "Missing GUI resource " + path);
    }
}
