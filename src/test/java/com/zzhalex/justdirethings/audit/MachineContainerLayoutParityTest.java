package com.zzhalex.justdirethings.audit;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineContainerLayoutParityTest {

    @Test
    void t1SingleSlotMachineContainersUseOriginalSlotPosition() throws IOException {
        for (String container : t1OnlySingleSlotContainers()) {
            String contents = read("src/main/java/com/zzhalex/justdirethings/common/container/machine/" + container + ".java");

            assertTrue(contents.contains("tile.getItemHandler(), 0, 80, 13)"),
                    container + " should place its single machine slot at the upstream T1 position 80,13");
            assertFalse(contents.contains("for (int row = 0; row < 3; row++)"),
                    container + " should not render the old 3x3 temporary machine inventory");
            assertFalse(contents.contains("62 + column * 18"),
                    container + " should not keep the old 3x3 temporary slot x layout");
            assertFalse(contents.contains("17 + row * 18"),
                    container + " should not keep the old 3x3 temporary slot y layout");
            assertFalse(contents.contains(", 80, 35)"),
                    container + " should not keep the old centered vanilla-container slot position");
        }
    }

    @Test
    void t2SingleSlotMachineContainersUseOriginalBaseMachinePosition() throws IOException {
        for (String container : sharedT1T2SingleSlotContainers()) {
            String contents = read("src/main/java/com/zzhalex/justdirethings/common/container/machine/" + container + ".java");

            assertTrue(contents.contains("machineSlotY(tile)") || contents.contains("tile instanceof TileAdvancedMachine ? 35 : 13"),
                    container + " should branch T2 single machine slots to upstream BaseMachineContainer position 80,35");
            assertTrue(contents.contains("new SlotItemHandler(tile.getItemHandler(), 0, 80, machineSlotY(tile))")
                            || contents.contains("new SlotItemHandler(tile.getItemHandler(), 0, 80, tile instanceof TileAdvancedMachine ? 35 : 13)"),
                    container + " should keep T1 at 80,13 while placing T2's one visible slot lower at 80,35");
            assertFalse(contents.contains("new SlotItemHandler(tile.getItemHandler(), 0, 80, 13);"),
                    container + " should not force T2 to inherit the T1 top slot position");
        }
    }

    @Test
    void t1SingleSlotMachineTilesExposeOneSlotNotNine() throws IOException {
        for (String tile : Arrays.asList("TileBlockBreaker", "TileBlockPlacer", "TileDropper", "TileFluidCollector")) {
            String contents = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/" + tile + ".java");
            String baseTileSection = contents.substring(0, contents.indexOf("public static class T1"));

            assertTrue(baseTileSection.contains("super(1);") || baseTileSection.contains("this(1);"),
                    tile + " should expose exactly one machine slot for the base/T1 machine");
            assertFalse(baseTileSection.contains("super(9);"),
                    tile + " should not expose the old 3x3 temporary inventory for the base/T1 machine");
            assertFalse(baseTileSection.contains("super(0);"),
                    tile + " should keep the original visible bucket/tool slot for the base/T1 machine");
        }
    }

    @Test
    void itemCollectorDoesNotExposeTemporaryInternalStorageSlots() throws IOException {
        String container = read("src/main/java/com/zzhalex/justdirethings/common/container/machine/ContainerItemCollector.java");
        String tile = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileItemCollector.java");

        assertFalse(container.contains("SlotItemHandler(tile.getItemHandler()"),
                "Item Collector should use the upstream adjacent-inventory behavior, not a visible internal 3x3 buffer");
        assertFalse(container.contains("new InventoryBasic(\"item_collector\", false, 9)"),
                "Item Collector container should not declare a visible 9-slot inventory");
        assertFalse(tile.contains("new InternalItemHandler(9)"),
                "Item Collector tile should not own the old temporary 9-slot item buffer");
    }

    @Test
    void machineBarsAreDrawnFromTopSectionWithOriginalDimensions() throws IOException {
        String baseGui = read("src/main/java/com/zzhalex/justdirethings/client/gui/base/GuiMachineBase.java");
        String fluidPlacerGui = read("src/main/java/com/zzhalex/justdirethings/client/gui/machine/GuiFluidPlacer.java");
        String fluidCollectorGui = read("src/main/java/com/zzhalex/justdirethings/client/gui/machine/GuiFluidCollector.java");

        assertTrue(baseGui.contains("widget.draw(topSectionLeft, topSectionTop)"),
                "Machine bars should be anchored to the upstream top section, not the inventory panel");
        assertTrue(baseGui.contains("protected int getFluidBarOffset()"),
                "Fluid bar position should use the same upstream offset hook as BaseMachineScreen");
        assertTrue(fluidPlacerGui.contains("new WidgetFluidBar(getFluidBarOffset(), 5, 18, 72)"),
                "Fluid Placer should use the upstream fluid bar dimensions with the T1/T2 offset hook");
        assertTrue(fluidCollectorGui.contains("new WidgetFluidBar(getFluidBarOffset(), 5, 18, 72)"),
                "Fluid Collector should use the upstream fluid bar dimensions with the T1/T2 offset hook");
    }

    private static List<String> t1OnlySingleSlotContainers() {
        return Arrays.asList(
                "ContainerGenerator",
                "ContainerFluidGenerator"
        );
    }

    private static List<String> sharedT1T2SingleSlotContainers() {
        return Arrays.asList(
                "ContainerBlockBreaker",
                "ContainerBlockPlacer",
                "ContainerClicker",
                "ContainerFluidCollector",
                "ContainerFluidPlacer"
        );
    }

    private static String read(String path) throws IOException {
        return Files.readString(path(path), StandardCharsets.UTF_8);
    }

    private static Path path(String path) {
        return Paths.get(path);
    }
}
