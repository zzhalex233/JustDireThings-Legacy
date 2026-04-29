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

class GuiTooltipParityTest {

    @Test
    void containerGuisExplicitlyRenderVanillaSlotTooltipsLikeUpstreamBaseScreen() throws IOException {
        String tooltipBase = read("src/main/java/com/zzhalex/justdirethings/client/gui/base/GuiTooltipContainer.java");

        assertTrue(tooltipBase.contains("extends GuiContainer"),
                "Shared GUI base should wrap the 1.12 GuiContainer behavior");
        assertTrue(tooltipBase.contains("drawDefaultBackground();"),
                "Shared GUI base should render the normal dim background before container contents");
        assertTrue(tooltipBase.contains("renderHoveredToolTip(mouseX, mouseY);"),
                "Cleanroom/1.12 GuiContainer does not call renderHoveredToolTip from drawScreen, so JDT screens must do it explicitly");
    }

    @Test
    void allJustDireContainerScreensUseSharedTooltipBase() throws IOException {
        for (String gui : containerGuis()) {
            String contents = read("src/main/java/com/zzhalex/justdirethings/client/gui/" + gui + ".java");
            assertTrue(contents.contains("extends GuiTooltipContainer") || contents.contains("extends GuiMachineBase"),
                    gui + " should inherit the explicit slot-tooltip draw path instead of raw GuiContainer");
        }
    }

    @Test
    void machineGuiKeepsCustomTooltipsAfterSlotTooltipPass() throws IOException {
        String machineGui = read("src/main/java/com/zzhalex/justdirethings/client/gui/base/GuiMachineBase.java");

        assertTrue(machineGui.contains("extends GuiTooltipContainer"),
                "Machine GUI base should inherit vanilla slot tooltip rendering");
        assertTrue(machineGui.contains("super.drawScreen(mouseX, mouseY, partialTicks);"),
                "Machine GUI should let the shared base render slot tooltips before energy/fluid/button tooltips");
        assertTrue(machineGui.indexOf("super.drawScreen(mouseX, mouseY, partialTicks);")
                        < machineGui.indexOf("drawEnergyBarTooltip(mouseX, mouseY)"),
                "Machine-specific tooltips should stay layered after the upstream slot tooltip pass");
    }

    private static List<String> containerGuis() {
        return Arrays.asList(
                "GuiFuelCanister",
                "GuiPotionCanister",
                "GuiPocketGenerator",
                "GuiToolSettings",
                "GuiUpgradeStation",
                "base/GuiMachineBase"
        );
    }

    private static String read(String path) throws IOException {
        return Files.readString(path(path), StandardCharsets.UTF_8);
    }

    private static Path path(String path) {
        return Paths.get(path);
    }
}
