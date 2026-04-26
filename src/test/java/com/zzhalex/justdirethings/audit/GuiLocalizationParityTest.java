package com.zzhalex.justdirethings.audit;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GuiLocalizationParityTest {

    private static final Path GUI_ROOT = Paths.get("src/main/java/com/zzhalex/justdirethings/client/gui");
    private static final Path LANG_ROOT = Paths.get("src/main/resources/assets/justdirethings/lang");

    @Test
    void guiScreensDoNotDrawHardcodedEnglishText() throws Exception {
        Set<String> offenders = new LinkedHashSet<>();
        Files.walk(GUI_ROOT)
                .filter(path -> path.getFileName().toString().endsWith(".java"))
                .forEach(path -> collectHardcodedDrawStrings(path, offenders));

        assertTrue(offenders.isEmpty(), "GUI screens still draw hardcoded strings: " + offenders);
    }

    @Test
    void commonGuiLocalizationKeysExistInEnglishAndChinese() throws Exception {
        String enUs = readLang("en_us.lang");
        String zhCn = readLang("zh_cn.lang");

        assertKey(enUs, "justdirethings.gui.energy");
        assertKey(enUs, "justdirethings.gui.fluid");
        assertKey(enUs, "justdirethings.gui.facing");
        assertKey(enUs, "justdirethings.gui.burn");
        assertKey(enUs, "justdirethings.gui.range");
        assertKey(enUs, "justdirethings.gui.delay");
        assertKey(enUs, "justdirethings.gui.player_inventory");

        assertKey(zhCn, "justdirethings.gui.energy");
        assertKey(zhCn, "justdirethings.gui.fluid");
        assertKey(zhCn, "justdirethings.gui.facing");
        assertKey(zhCn, "justdirethings.gui.burn");
        assertKey(zhCn, "justdirethings.gui.range");
        assertKey(zhCn, "justdirethings.gui.delay");
        assertKey(zhCn, "justdirethings.gui.player_inventory");
    }

    private static void collectHardcodedDrawStrings(Path path, Set<String> offenders) {
        try {
            int lineNumber = 0;
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                lineNumber++;
                if (line.contains("drawString(\"")) {
                    offenders.add(GUI_ROOT.relativize(path) + ":" + lineNumber);
                }
            }
        } catch (Exception e) {
            offenders.add(path + " (" + e.getMessage() + ")");
        }
    }

    private static String readLang(String fileName) throws Exception {
        return new String(Files.readAllBytes(LANG_ROOT.resolve(fileName)), StandardCharsets.UTF_8);
    }

    private static void assertKey(String contents, String key) {
        assertTrue(contents.contains(key + "="), "Missing localization key " + key);
    }
}
