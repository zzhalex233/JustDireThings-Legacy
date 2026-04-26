package com.zzhalex.justdirethings.client.gui.button;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineButtonLocalizationTest {

    @Test
    void machineButtonTooltipKeysExistInBothLanguages() throws Exception {
        String enUs = read("src/main/resources/assets/justdirethings/lang/en_us.lang");
        String zhCn = read("src/main/resources/assets/justdirethings/lang/zh_cn.lang");

        for (String key : buttonKeys()) {
            assertTrue(enUs.contains(key + "="), "Missing en_us button key: " + key);
            assertTrue(zhCn.contains(key + "="), "Missing zh_cn button key: " + key);
        }
    }

    private static Set<String> buttonKeys() {
        Set<String> keys = new LinkedHashSet<>();
        for (ButtonDefinition.State state : MachineButtonFactory.redstoneStates()) {
            keys.add(state.getLocalizationKey());
        }
        for (ButtonDefinition.State state : MachineButtonFactory.directionStates()) {
            keys.add(state.getLocalizationKey());
        }
        for (ButtonDefinition definition : MachineButtonFactory.baseMachineButtons(20, 0, 2)) {
            addDefinitionKeys(keys, definition);
        }
        for (ButtonDefinition definition : MachineButtonFactory.filterButtons(true, false, 0)) {
            addDefinitionKeys(keys, definition);
        }
        for (ButtonDefinition definition : MachineButtonFactory.areaButtons(true, 2.0D, 2.0D, 2.0D, 0, 0, 0)) {
            addDefinitionKeys(keys, definition);
        }
        addDefinitionKeys(keys, MachineButtonFactory.respectPickupDelayButton(false));
        return keys;
    }

    private static void addDefinitionKeys(Set<String> keys, ButtonDefinition definition) {
        if (!definition.getLocalizationKey().isEmpty()) {
            keys.add(definition.getLocalizationKey());
        }
        for (ButtonDefinition.State state : definition.getStates()) {
            keys.add(state.getLocalizationKey());
        }
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
