package com.zzhalex.justdirethings.client.gui.button;

import com.zzhalex.justdirethings.common.tile.base.MachineSettingKeys;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineButtonFactoryTest {

    @Test
    void redstoneButtonUsesOriginalFourStateTexturesAndLocalization() {
        List<ButtonDefinition.State> states = MachineButtonFactory.redstoneStates();

        assertEquals(4, states.size());
        assertEquals("textures/gui/buttons/redstoneignore.png", states.get(0).getTexturePath());
        assertEquals("justdirethings.screen.ignored", states.get(0).getLocalizationKey());
        assertEquals("textures/gui/buttons/redstonelow.png", states.get(1).getTexturePath());
        assertEquals("textures/gui/buttons/redstonehigh.png", states.get(2).getTexturePath());
        assertEquals("textures/gui/buttons/redstonepulse.png", states.get(3).getTexturePath());
    }

    @Test
    void directionButtonUsesOriginalSixFacingTextures() {
        List<ButtonDefinition.State> states = MachineButtonFactory.directionStates();

        assertEquals(6, states.size());
        assertEquals("textures/gui/buttons/direction-down.png", states.get(0).getTexturePath());
        assertEquals("textures/gui/buttons/direction-up.png", states.get(1).getTexturePath());
        assertEquals("textures/gui/buttons/direction-north.png", states.get(2).getTexturePath());
        assertEquals("textures/gui/buttons/direction-south.png", states.get(3).getTexturePath());
        assertEquals("textures/gui/buttons/direction-west.png", states.get(4).getTexturePath());
        assertEquals("textures/gui/buttons/direction-east.png", states.get(5).getTexturePath());
    }

    @Test
    void wideTimedMachineButtonsMatchOriginalBaseRelativeLayout() {
        List<ButtonDefinition> buttons = MachineButtonFactory.wideTimedMachineButtons(20, 0);

        assertTrue(buttons.stream().anyMatch(button ->
                button.getKind() == ButtonDefinition.Kind.NUMBER
                        && button.getSettingKey().equals(MachineSettingKeys.TICK_SPEED)
                        && button.getX() == 144
                        && button.getY() == 40
                        && button.getWidth() == 24
                        && button.getHeight() == 12
                        && button.getLocalizationKey().equals("justdirethings.screen.tickspeed")));
        assertTrue(buttons.stream().anyMatch(button ->
                button.getKind() == ButtonDefinition.Kind.TOGGLE
                        && button.getSettingKey().equals(MachineSettingKeys.REDSTONE_MODE)
                        && button.getX() == 134
                        && button.getY() == 62));
        assertTrue(buttons.stream().noneMatch(button ->
                button.getSettingKey().equals(MachineSettingKeys.DIRECTION)));
    }

    @Test
    void compactDirectionalMachineButtonsMatchT1ScreenLayout() {
        List<ButtonDefinition> buttons = MachineButtonFactory.compactTimedDirectionalMachineButtons(20, 0, 2);

        assertTrue(buttons.stream().anyMatch(button ->
                button.getKind() == ButtonDefinition.Kind.TOGGLE
                        && button.getSettingKey().equals(MachineSettingKeys.DIRECTION)
                        && button.getX() == 122
                        && button.getY() == 38));
        assertTrue(buttons.stream().anyMatch(button ->
                button.getKind() == ButtonDefinition.Kind.TOGGLE
                        && button.getSettingKey().equals(MachineSettingKeys.REDSTONE_MODE)
                        && button.getX() == 104
                        && button.getY() == 38));
    }

    @Test
    void itemCollectorRespectPickupDelayUsesOriginalRelativeLayout() {
        ButtonDefinition button = MachineButtonFactory.respectPickupDelayButton(false);

        assertEquals(MachineSettingKeys.RESPECT_PICKUP_DELAY, button.getSettingKey());
        assertEquals(116, button.getX());
        assertEquals(62, button.getY());
    }
}
