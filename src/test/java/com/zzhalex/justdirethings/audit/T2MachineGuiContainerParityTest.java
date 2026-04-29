package com.zzhalex.justdirethings.audit;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class T2MachineGuiContainerParityTest {

    @Test
    void t2MachineContainersExposeAdvancedFilterSlotsBeforePlayerInventory() throws IOException {
        String base = read("src/main/java/com/zzhalex/justdirethings/common/container/base/ContainerMachineBase.java");
        String itemCollector = read("src/main/java/com/zzhalex/justdirethings/common/container/machine/ContainerItemCollector.java");
        String breaker = read("src/main/java/com/zzhalex/justdirethings/common/container/machine/ContainerBlockBreaker.java");
        String placer = read("src/main/java/com/zzhalex/justdirethings/common/container/machine/ContainerBlockPlacer.java");
        String clicker = read("src/main/java/com/zzhalex/justdirethings/common/container/machine/ContainerClicker.java");
        String dropper = read("src/main/java/com/zzhalex/justdirethings/common/container/machine/ContainerDropper.java");
        String fluidCollector = read("src/main/java/com/zzhalex/justdirethings/common/container/machine/ContainerFluidCollector.java");
        String fluidPlacer = read("src/main/java/com/zzhalex/justdirethings/common/container/machine/ContainerFluidPlacer.java");
        String swapper = read("src/main/java/com/zzhalex/justdirethings/common/container/machine/ContainerBlockSwapper.java");

        assertTrue(base.contains("addAdvancedFilterSlots"),
                "ContainerMachineBase should provide one shared T2 filter-slot bridge");
        assertTrue(base.contains("SlotFilterItemHandler"),
                "Advanced machine filter slots should use ghost/filter slot semantics");
        assertTrue(base.contains("slotClick") && base.contains("handleFilterSlotClick") && base.contains("addFilterCopy"),
                "Ghost filter click/shift behavior should live in ContainerMachineBase for every filterable machine");
        assertTrue(base.contains("filterSlotStart") && base.contains("filterSlotCount"),
                "Shared filter behavior should track the real filter-slot range instead of assuming Item Collector indices");
        assertFalse(itemCollector.contains("private boolean addFilterCopy"),
                "Item Collector should reuse the base ghost-filter implementation instead of carrying a second copy");
        assertTrue(itemCollector.contains("addFilterSlots(tile.getFilterHandler())"),
                "Item Collector should register its filter slots through the same shared bridge as advanced machines");
        for (String source : new String[] {breaker, placer, clicker, dropper, fluidCollector, fluidPlacer, swapper}) {
            assertTrue(source.contains("addAdvancedFilterSlots"),
                    "Every filterable T2 container should expose upstream advanced filter slots");
        }
        assertTrue(dropper.contains("addSlotBox(tile.getItemHandler(), 0, 62, -1, 3, 18, 3, 18)"),
                "DropperT2 should expose the original 3x3 dropping inventory layout");
    }

    @Test
    void t2MachineGuiBaseAddsAdvancedAreaFilterAndEnergyControls() throws IOException {
        String gui = read("src/main/java/com/zzhalex/justdirethings/client/gui/base/GuiMachineBase.java");
        String tooltipBase = read("src/main/java/com/zzhalex/justdirethings/client/gui/base/GuiTooltipContainer.java");

        assertTrue(gui.contains("TileAdvancedMachine"),
                "Machine GUI base should identify the shared T2 advanced-machine contract");
        assertTrue(gui.contains("addAdvancedMachineButtons"),
                "Advanced area/filter buttons should be wired once instead of per-machine hacks");
        assertTrue(gui.contains("advancedEnergyBar"),
                "Advanced machines should show their FE buffer like the upstream powered screens");
        assertTrue(gui.contains("machine instanceof TileAdvancedMachine"),
                "Area labels and wide top-section layout should apply to all T2 advanced machines");
        assertTrue(gui.contains("extraWidth = 60"),
                "T2 machine screens should widen the top panel by 60 pixels like the upstream advanced screens");
        assertTrue(gui.contains("this.xSize = BASE_X_SIZE + extraWidth"),
                "T2 machine screens should make the widened panel part of the real 1.12 GuiContainer width so JEI and slot hitboxes see it");
        assertTrue(gui.contains("applySlotDisplayOffset(extraWidth / 2, 0)"),
                "Widening the real GuiContainer should preserve upstream slot visuals by offsetting slots back under the original 176px inventory section");
        assertTrue(gui.contains("getBaseGuiLeft()"),
                "Machine GUI drawing should distinguish the widened real GUI left from the original 176px inventory-section left");
        assertTrue(gui.contains("extends GuiTooltipContainer") && tooltipBase.contains("drawDefaultBackground();"),
                "Machine GUI should inherit the vanilla dark overlay because 1.12 GuiContainer does not do it automatically");
        assertTrue(gui.contains("getEnergyBarOffset()"),
                "Energy bars should be placed through the upstream offset hook");
        assertTrue(gui.contains("getFluidBarOffset()"),
                "Fluid bars should be placed through the upstream offset hook so T2 fluid bars can move to the right side");
        assertTrue(gui.contains("return 204"),
                "T2 fluid machines should put the fluid bar on the right side of the widened panel like upstream");
        assertTrue(gui.contains("directionButton(116, 62"),
                "T2 directional machine buttons should use the upstream advanced-machine direction position");
        assertTrue(gui.contains("redstoneButton(134, 62"),
                "Advanced redstone controls should use the upstream wide top-section position");
    }

    @Test
    void specializedT2MachineGuisKeepOriginalWidePanelDetails() throws IOException {
        String gui = read("src/main/java/com/zzhalex/justdirethings/client/gui/base/GuiMachineBase.java");
        String factory = read("src/main/java/com/zzhalex/justdirethings/client/gui/button/MachineButtonFactory.java");
        String swapper = read("src/main/java/com/zzhalex/justdirethings/client/gui/machine/GuiBlockSwapper.java");
        String sensor = read("src/main/java/com/zzhalex/justdirethings/client/gui/machine/GuiSensor.java");

        assertTrue(gui.contains("addDropperAdvancedMachineButtons"),
                "Dropper T2 should have its upstream offset-only advanced control layout instead of the generic radius layout");
        assertTrue(gui.contains("drawOffsetOnlyAreaControlLabels"),
                "Dropper T2 labels should show the original Off row without the unused Rad row");
        assertTrue(factory.contains("compareNbtFilterButton") && gui.contains("compareNbtFilterButton(8, 62"),
                "Dropper T2 should expose only the upstream compare-NBT filter button");

        assertTrue(swapper.contains("addAdvancedMachineButtons"),
                "Block Swapper T2 should reuse the shared advanced area/filter controls");
        assertTrue(swapper.contains("swapBlocksButton(8, 44") && swapper.contains("swapEntityTypeButton(26, 44"),
                "Block Swapper T2 should use the upstream swap button positions on the widened panel");
        assertTrue(swapper.contains("redstoneButton(134, 62"),
                "Block Swapper T2 should not keep the T1 redstone position");
        assertTrue(swapper.contains("topSectionLeft + 156") && swapper.contains("topSectionTop + 38"),
                "Block Swapper T2 partner status icon should sit at the upstream widened-panel position");

        assertTrue(sensor.contains("sensorTargetButton(26, 62") && sensor.contains("strongWeakRedstoneButton(44, 62"),
                "Sensor T2 should use upstream target and strong/weak redstone positions");
        assertTrue(sensor.contains("equalityButton(104, 62") && sensor.contains("senseAmountButton(122, 64"),
                "Sensor T2 should keep the original comparison controls on the right side");
        assertTrue(sensor.indexOf("if (tile instanceof TileAdvancedMachine)") < sensor.indexOf("sensorTargetButton(26, 62"),
                "Sensor T2 controls should be inside the advanced-machine branch, separate from the T1 positions");
    }

    private static String read(String path) throws IOException {
        return Files.readString(path(path), StandardCharsets.UTF_8);
    }

    private static Path path(String path) {
        return Paths.get(path);
    }
}
