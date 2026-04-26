package com.zzhalex.justdirethings.audit;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineGuiLegacyCleanupTest {

    private static final Path MACHINE_GUI_DIR = Paths.get("src/main/java/com/zzhalex/justdirethings/client/gui/machine");
    private static final Path MACHINE_BASE = Paths.get("src/main/java/com/zzhalex/justdirethings/client/gui/base/GuiMachineBase.java");

    @Test
    void machineScreensDoNotRenderLegacyInlineStatusText() throws IOException {
        List<String> forbiddenTokens = Arrays.asList(
                "fontRenderer.drawString",
                "MachineActionHelper.getFacing",
                "justdirethings.gui.facing",
                "justdirethings.gui.energy",
                "justdirethings.gui.burn",
                "justdirethings.gui.fluid",
                "justdirethings.gui.range",
                "justdirethings.gui.delay",
                "justdirethings.gui.signal",
                "justdirethings.gui.xp",
                "justdirethings.gui.moved",
                "justdirethings.gui.slots",
                "justdirethings.gui.target",
                "justdirethings.gui.inventory",
                "justdirethings.gui.player_inventory"
        );

        for (Path guiFile : machineGuiFiles()) {
            String contents = Files.readString(guiFile, StandardCharsets.UTF_8);
            for (String token : forbiddenTokens) {
                assertFalse(contents.contains(token),
                        guiFile.getFileName() + " still contains legacy inline GUI text token: " + token);
            }
        }
    }

    @Test
    void machineBaseDrawsOriginalBackgroundSectionsAndTitleStrip() throws IOException {
        String contents = Files.readString(MACHINE_BASE, StandardCharsets.UTF_8);

        assertTrue(contents.contains("topSectionLeft"), "Machine GUI base should track the upstream top section left edge");
        assertTrue(contents.contains("topSectionTop"), "Machine GUI base should track the upstream top section top edge");
        assertTrue(contents.contains("topSectionWidth"), "Machine GUI base should track the upstream top section width");
        assertTrue(contents.contains("topSectionHeight"), "Machine GUI base should track the upstream top section height");
        assertTrue(contents.contains("drawBackgroundPanel(topSectionLeft + 20"),
                "Machine GUI base should draw the upstream title strip with side padding");
        assertTrue(contents.contains("topSectionWidth - 40"),
                "Machine GUI title strip should preserve the upstream 20px side padding");
        assertTrue(contents.contains("drawBackgroundPanel(topSectionLeft, topSectionTop"),
                "Machine GUI base should draw a separate upstream top control panel");
        assertTrue(contents.contains("drawBackgroundPanel(left, top + 75"),
                "Machine GUI base should draw a separate upstream inventory panel");
        assertFalse(contents.contains("drawBackgroundPanel(left, top, xSize, ySize)"),
                "Machine GUI base should not draw one old full-size panel behind the whole screen");
    }

    @Test
    void dedicatedItemScreensKeepOriginalTextureFamilies() throws IOException {
        assertFileContains("GuiFuelCanister.java", "textures/gui/fuelcanister.png");
        assertFileContains("GuiPotionCanister.java", "textures/gui/fuelcanister.png");
        assertFileContains("GuiPocketGenerator.java", "textures/gui/pocketgenerator.png");
        assertFileContains("GuiUpgradeStation.java", "textures/gui/sprites/background.png");
    }

    private static List<Path> machineGuiFiles() throws IOException {
        try (java.util.stream.Stream<Path> files = Files.list(MACHINE_GUI_DIR)) {
            return files
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .collect(Collectors.toList());
        }
    }

    private static void assertFileContains(String fileName, String token) throws IOException {
        Path file = Paths.get("src/main/java/com/zzhalex/justdirethings/client/gui").resolve(fileName);
        String contents = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(contents.contains(token), fileName + " should use upstream texture " + token);
    }
}
