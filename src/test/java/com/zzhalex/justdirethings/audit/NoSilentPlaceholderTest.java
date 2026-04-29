package com.zzhalex.justdirethings.audit;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NoSilentPlaceholderTest {

    private static final String MATRIX_PATH = "docs/audit/justdirethings-source-parity-matrix.md";

    private static final String[] SIMPLE_CONTENT_SPECIAL_ITEMS = {};

    private static final String[] NOT_IMPLEMENTED_ABILITIES = {
            "MOBSCANNER",
            "ORESCANNER",
            "LAWNMOWER",
            "INVULNERABILITY",
            "CAUTERIZEWOUNDS",
            "AIRBURST",
            "GROUNDSTOMP",
            "STUPEFY",
            "POLYMORPH_RANDOM",
            "VOIDSHIFT",
            "OREXRAY",
            "GLOWING",
            "DEBUFFREMOVER",
            "EARTHQUAKE",
            "NOAI",
            "POLYMORPH_TARGET",
            "EPICARROW",
            "LEAFBREAKER",
            "ECLIPSEGATE"
    };

    private static final String[][] T2_MACHINE_PARTIALS = {
            {"TileBlockBreaker.java", "BlockBreakerT2BE"},
            {"TileBlockPlacer.java", "BlockPlacerT2BE"},
            {"TileBlockSwapper.java", "BlockSwapperT2BE"},
            {"TileClicker.java", "ClickerT2BE"},
            {"TileDropper.java", "DropperT2BE"},
            {"TileFluidCollector.java", "FluidCollectorT2BE"},
            {"TileFluidPlacer.java", "FluidPlacerT2BE"},
            {"TileSensor.java", "SensorT2BE"}
    };

    private static final String[][] ENTITY_STUBS = {};

    private static final String[] EMPTY_GUI_ANCHORS = {
            "ToolSettingScreen.java",
            "MachineSettingsCopierScreen.java",
            "AdvPortalRadialMenu.java",
            "AdvPortalEditMenu.java"
    };

    private static final String[] RECIPE_TYPE_STUBS = {
            "abilityrecipe",
            "paxelrecipe"
    };

    private static final String[] RECIPE_TYPE_PARTIALS = {
            "goospreadrecipe",
            "goospreadrecipe_tag",
            "fluiddroprecipe"
    };

    @Test
    void specialItemShellsUseDedicatedParityStubAndAreDocumented() throws IOException {
        String modItems = read("src/main/java/com/zzhalex/justdirethings/registry/ModItems.java");
        String itemParityStub = read("src/main/java/com/zzhalex/justdirethings/common/item/ItemParityStub.java");
        String matrix = read(MATRIX_PATH);

        assertTrue(itemParityStub.contains("PARITY STUB"),
                "ItemParityStub must advertise that it is an audit-visible placeholder, not a behavior implementation");
        assertTrue(!modItems.contains("createParityStub("),
                "Special items should now be real or partial item classes, not ItemParityStub anchors");
        for (String itemId : SIMPLE_CONTENT_SPECIAL_ITEMS) {
            assertTrue(modItems.contains("createParityStub(\"" + itemId + "\""),
                    "Expected " + itemId + " to use a dedicated ItemParityStub until ported");
            assertMatrixStub(matrix, itemId);
        }

        assertTrue(!modItems.contains("new ItemSimpleContent()"),
                "ModItems special items should not use the generic content placeholder");
    }

    @Test
    void notYetImplementedAbilitiesAreExplicitlyDocumentedAsStubs() throws IOException {
        String abilityMethods = read("src/main/java/com/zzhalex/justdirethings/common/item/ability/AbilityMethods.java");
        String matrix = read(MATRIX_PATH);

        assertTrue(abilityMethods.contains("PARITY STUB"),
                "AbilityMethods placeholder actions must carry a parity-stub marker");
        for (String ability : NOT_IMPLEMENTED_ABILITIES) {
            assertTrue(abilityMethods.contains("Ability." + ability),
                    "AbilityMethods should keep " + ability + " visible until the real upstream behavior is ported");
            assertMatrixMentions(matrix, ability);
        }
    }

    @Test
    void t2MachinePartialsUseSharedAdvancedContractAndAreDocumented() throws IOException {
        String matrix = read(MATRIX_PATH);
        for (String[] partial : T2_MACHINE_PARTIALS) {
            String source = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/" + partial[0]);
            assertTrue(source.contains("implements TileAdvancedMachine") || source.contains("extends TileAdvanced"),
                    partial[0] + " should use the shared advanced-machine contract instead of remaining a silent T2 shell");
            assertMatrixPartial(matrix, partial[1]);
        }
    }

    @Test
    void emptyGuiAnchorsAreExplicitlyDocumentedAsStubs() throws IOException {
        String matrix = read(MATRIX_PATH);
        for (String fileName : EMPTY_GUI_ANCHORS) {
            String source = read("src/main/java/com/zzhalex/justdirethings/client/gui/upstream/" + fileName);
            assertTrue(source.contains("PARITY STUB"),
                    fileName + " is only a class-name anchor and must say so until the real screen is ported");
            assertMatrixMentions(matrix, fileName.replace(".java", ""));
        }
    }

    @Test
    void placeholderEntitiesAreExplicitlyDocumentedAsStubs() throws IOException {
        String matrix = read(MATRIX_PATH);
        for (String[] stub : ENTITY_STUBS) {
            String source = read("src/main/java/com/zzhalex/justdirethings/common/entity/" + stub[0]);
            assertTrue(source.contains("PARITY STUB: Upstream " + stub[1]),
                    stub[0] + " must point future workers to the missing upstream entity implementation");
            assertMatrixMentions(matrix, stub[1]);
        }
    }

    @Test
    void recipeCatalogIdsCannotBeMistakenForImplementedLoaders() throws IOException {
        String recipes = read("src/main/java/com/zzhalex/justdirethings/registry/ModRecipes.java");
        String matrix = read(MATRIX_PATH);

        assertTrue(recipes.contains("PARITY STUB"),
                "Recipe catalog IDs must be marked as placeholders until 1.12 loaders and data are ported");
        for (String recipeType : RECIPE_TYPE_STUBS) {
            assertTrue(recipes.contains("\"" + recipeType + "\""),
                    "ModRecipes should keep the upstream recipe type visible for audit: " + recipeType);
            assertMatrixStub(matrix, recipeType);
        }
        for (String recipeType : RECIPE_TYPE_PARTIALS) {
            assertTrue(recipes.contains("\"" + recipeType + "\""),
                    "ModRecipes should keep the upstream recipe type visible for audit: " + recipeType);
            assertMatrixPartial(matrix, recipeType);
        }
    }

    @Test
    void matrixDefinesStubRulesBeforeLaterPhasesUseIt() throws IOException {
        String matrix = read(MATRIX_PATH);

        assertTrue(matrix.contains("No feature may move to `partial`"),
                "Matrix must define the threshold for partial status");
        assertTrue(matrix.contains("No feature may move to `ported`"),
                "Matrix must define the threshold for ported status");
        assertTrue(matrix.contains("Class-name anchors count as `stub`"),
                "Matrix must prevent screen/container anchors from being counted as complete");
        assertTrue(matrix.contains("Copied textures/models count as `resource-only`"),
                "Matrix must prevent resource-only parity from being counted as behavior parity");
    }

    private static void assertMatrixStub(String matrix, String id) {
        assertTrue(matrix.contains("`" + id + "`") && matrix.contains("| stub |"),
                "Parity matrix should list " + id + " as a stub");
    }

    private static void assertMatrixPartial(String matrix, String id) {
        assertTrue(matrix.contains("`" + id + "`") && matrix.contains("| partial |"),
                "Parity matrix should list " + id + " as partial once data/factories exist but runtime behavior is pending");
    }

    private static void assertMatrixMentions(String matrix, String token) {
        assertTrue(matrix.contains(token), "Parity matrix should mention " + token);
    }

    private static String read(String path) throws IOException {
        return Files.readString(path(path), StandardCharsets.UTF_8);
    }

    private static Path path(String path) {
        return Paths.get(path);
    }
}
