package com.zzhalex.justdirethings.audit;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortableItemGuiDetailParityTest {

    @Test
    void fuelCanisterGuiKeepsUpstreamFuelTooltipsAndInvalidSlotOverlay() throws IOException {
        String gui = read("src/main/java/com/zzhalex/justdirethings/client/gui/GuiFuelCanister.java");

        assertTrue(gui.contains("renderHoveredToolTip"),
                "Fuel Canister should customize the vanilla slot tooltip like upstream FuelCanisterScreen.renderTooltip");
        assertTrue(gui.contains("FuelBurnHelper.getBurnTime") && gui.contains("fuelcanisteritemsamt") && gui.contains("fuelcanisteramtstack"),
                "Fuel Canister fuel stack tooltips should include per-item and stack fuel/burn-time lines");
        assertTrue(gui.contains("GuiScreen.isShiftKeyDown()"),
                "Fuel Canister should switch tooltip detail when Shift is held like upstream");
        assertTrue(gui.contains("drawInvalidSlotOverlays") && gui.contains("0x7FFF0000"),
                "Invalid fuel items should get the upstream translucent red slot overlay");
        assertFalse(gui.contains("item.justdirethings.fuel_canister.name") || gui.contains("justdirethings.gui.items") || gui.contains("justdirethings.gui.fuel"),
                "Fuel Canister should not keep old title/items/fuel foreground labels; upstream draws only the two centered fuel amount lines");
    }

    @Test
    void pocketGeneratorGuiRestoresEnergyAndFuelHoverDetails() throws IOException {
        String gui = read("src/main/java/com/zzhalex/justdirethings/client/gui/GuiPocketGenerator.java");

        assertTrue(gui.contains("drawEnergyMeter") && gui.contains("drawBurnMeter"),
                "Pocket Generator should render the upstream energy and burn progress overlays from pocketgenerator.png");
        assertTrue(gui.contains("drawEnergyTooltip") && gui.contains("screen.burn_time") && gui.contains("screen.fepertick"),
                "Hovering the energy bar should show energy, burn time and FE/t details");
        assertTrue(gui.contains("drawFuelSlotTooltip") && gui.contains("screen.burnspeedmultiplier"),
                "Fuel stack tooltips should include the upstream burn speed multiplier line");
        assertTrue(gui.contains("FuelBurnHelper.getBurnSpeedMultiplier"),
                "Fuel Canister stacks should contribute their stored burn-speed multiplier to Pocket Generator tooltips");
        assertFalse(gui.contains("item.justdirethings.pocket_generator.name") || gui.contains("justdirethings.gui.energy") || gui.contains("justdirethings.gui.burn"),
                "Pocket Generator should not keep old foreground text labels; upstream uses bars and hover tooltips");
    }

    @Test
    void potionCanisterGuiUsesTexturedPotionFluidAndFluidTooltip() throws IOException {
        String gui = read("src/main/java/com/zzhalex/justdirethings/client/gui/GuiPotionCanister.java");

        assertTrue(gui.contains("TextureAtlasSprite") && gui.contains("TextureMap.LOCATION_BLOCKS_TEXTURE"),
                "Potion Canister should render the tinted water still texture, not a flat rectangle");
        assertTrue(gui.contains("drawPotionFluid") && gui.contains("PotionUtils.addPotionTooltip"),
                "Potion Canister fluid tooltip should include potion effect details like upstream");
        assertTrue(gui.contains("drawFluidBarTooltip") && gui.contains("justdirethings.screen.fluid"),
                "Potion Canister should show a dedicated fluid-bar tooltip on hover");
        assertFalse(gui.contains("Gui.drawRect"),
                "The old solid-color potion fill was a placeholder and should not remain");
    }

    @Test
    void toolSettingGuiRestoresUpstreamRightClickSettingsAndBindingControls() throws IOException {
        String gui = read("src/main/java/com/zzhalex/justdirethings/client/gui/GuiToolSettings.java");

        assertTrue(gui.contains("shownAbilityButton"),
                "Tool settings should track the right-click-expanded ability button like upstream");
        assertTrue(gui.contains("bindingButtons") && gui.contains("requireEquippedButtons") && gui.contains("customSettingsButtons"),
                "Tool settings should expose keybind, require-equipped and custom-setting child buttons");
        assertTrue(gui.contains("MessageToolBindingSetting"),
                "Tool settings should send the dedicated binding packet instead of only slot-setting packets");
        assertTrue(gui.contains("rightclicksettings") && gui.contains("unbound-screen") && gui.contains("bound-key") && gui.contains("bound-mouse"),
                "Tool settings button tooltips should include upstream binding/help text");
        assertTrue(gui.contains("keyTyped") && gui.contains("bindingEnabled"),
                "Tool settings should capture keyboard input while the keybind button is active");
    }

    private static String read(String path) throws IOException {
        return Files.readString(path(path), StandardCharsets.UTF_8);
    }

    private static Path path(String path) {
        return Paths.get(path);
    }
}
