package com.zzhalex.justdirethings.audit;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoricalMachineControlsParityTest {

    @Test
    void blockBreakerClickerAndDropperExposeOriginalSpecificControls() throws IOException {
        String breaker = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileBlockBreaker.java");
        String clicker = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileClicker.java");
        String dropper = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileDropper.java");
        String baseGui = read("src/main/java/com/zzhalex/justdirethings/client/gui/base/GuiMachineBase.java");
        String applier = read("src/main/java/com/zzhalex/justdirethings/common/tile/base/MachineSettingApplier.java");

        assertTrue(breaker.contains("sneaking") && breaker.contains("setSneaking"),
                "Block Breaker should persist the upstream sneak-click setting");
        assertTrue(clicker.contains("clickTarget") && clicker.contains("clickType") && clicker.contains("showFakePlayer"),
                "Clicker should persist target, click type, sneak, and fake-player display settings");
        assertTrue(dropper.contains("pickupDelay") && dropper.contains("dropCount"),
                "Dropper should persist upstream pickup delay and drop-count settings");
        assertTrue(baseGui.contains("CLICK_TARGET") && baseGui.contains("CLICK_TYPE") && baseGui.contains("DROP_COUNT") && baseGui.contains("PICKUP_DELAY"),
                "Machine GUI should expose original per-machine controls for clicker and dropper");
        assertTrue(applier.contains("TileClicker") && applier.contains("TileDropper") && applier.contains("TileBlockBreaker"),
                "Machine setting packets should apply the original per-machine settings");
    }

    @Test
    void generatorsUseValidatedFuelSlotsAndDrawBurnProgress() throws IOException {
        String generatorContainer = read("src/main/java/com/zzhalex/justdirethings/common/container/machine/ContainerGenerator.java");
        String fluidGeneratorContainer = read("src/main/java/com/zzhalex/justdirethings/common/container/machine/ContainerFluidGenerator.java");
        String generatorGui = read("src/main/java/com/zzhalex/justdirethings/client/gui/machine/GuiGenerator.java");

        assertTrue(generatorContainer.contains("new SlotFuel"),
                "Generator should use a fuel-validating slot instead of a generic item slot");
        assertTrue(fluidGeneratorContainer.contains("new SlotFluidFuel"),
                "Fluid Generator should use a fluid-fuel-validating slot instead of a generic item slot");
        assertTrue(generatorGui.contains("drawBurnProgress"),
                "Generator GUI should render the upstream burn-progress overlay");
    }

    private static String read(String path) throws IOException {
        return Files.readString(path(path), StandardCharsets.UTF_8);
    }

    private static Path path(String path) {
        return Paths.get(path);
    }
}
