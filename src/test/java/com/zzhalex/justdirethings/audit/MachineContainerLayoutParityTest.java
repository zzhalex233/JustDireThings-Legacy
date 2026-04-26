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
        for (String container : singleSlotContainers()) {
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
    void t1SingleSlotMachineTilesExposeOneSlotNotNine() throws IOException {
        for (String tile : Arrays.asList("TileBlockBreaker", "TileBlockPlacer", "TileDropper", "TileFluidCollector")) {
            String contents = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/" + tile + ".java");

            assertTrue(contents.contains("super(1);"), tile + " should expose exactly one machine slot");
            assertFalse(contents.contains("super(9);"), tile + " should not expose the old 3x3 temporary inventory");
            assertFalse(contents.contains("super(0);"), tile + " should keep the original visible bucket/tool slot");
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

        assertTrue(baseGui.contains("widget.draw(topSectionLeft, topSectionTop)"),
                "Machine bars should be anchored to the upstream top section, not the inventory panel");
        assertTrue(fluidPlacerGui.contains("new WidgetFluidBar(5, 5, 18, 72)"),
                "Fluid Placer should use the upstream fluid bar position and dimensions");
    }

    private static List<String> singleSlotContainers() {
        return Arrays.asList(
                "ContainerBlockBreaker",
                "ContainerBlockPlacer",
                "ContainerClicker",
                "ContainerDropper",
                "ContainerFluidCollector",
                "ContainerFluidPlacer",
                "ContainerGenerator",
                "ContainerFluidGenerator"
        );
    }

    private static String read(String path) throws IOException {
        return Files.readString(path(path), StandardCharsets.UTF_8);
    }

    private static Path path(String path) {
        return Paths.get(path);
    }
}
