package com.zzhalex.justdirethings.audit;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.registry.ModBlocks;
import com.zzhalex.justdirethings.registry.ModContentBlocks;
import com.zzhalex.justdirethings.registry.ModContentItems;
import com.zzhalex.justdirethings.registry.ModEntities;
import com.zzhalex.justdirethings.registry.ModEquipmentItems;
import com.zzhalex.justdirethings.registry.ModFluids;
import com.zzhalex.justdirethings.registry.ModItems;
import com.zzhalex.justdirethings.registry.ModRecipes;
import net.minecraft.block.Block;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistryParityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void currentBlocksCoverUpstreamSourceContract() throws IllegalAccessException {
        assertContainsAll("blocks", SourceParityCatalog.BLOCK_IDS, currentBlockIds());
    }

    @Test
    void currentItemsCoverUpstreamSourceContract() throws IllegalAccessException {
        assertContainsAll("items", SourceParityCatalog.ITEM_IDS, currentItemIds());
    }

    @Test
    void currentFluidsCoverUpstreamSourceContract() {
        assertContainsAll("fluids", SourceParityCatalog.FLUID_IDS, new LinkedHashSet<>(ModFluids.coreFluidIds()));
    }

    @Test
    void currentEntitiesCoverUpstreamSourceContract() {
        Set<String> current = new LinkedHashSet<>();
        for (ResourceLocation id : ModEntities.coreEntityIds()) {
            current.add(path(id));
        }

        assertContainsAll("entities", SourceParityCatalog.ENTITY_IDS, current);
    }

    @Test
    void currentScreensCoverUpstreamSourceContract() {
        Set<String> current = new LinkedHashSet<>();
        collectJavaClassNames(Paths.get("src/main/java/com/zzhalex/justdirethings/client/gui"), current);

        assertContainsAll("screens", SourceParityCatalog.SCREEN_CLASS_NAMES, current);
    }

    @Test
    void upstreamContainersAreImplementedOrDocumentedAsParityGaps() {
        Set<String> current = new LinkedHashSet<>();
        collectJavaClassNames(Paths.get("src/main/java/com/zzhalex/justdirethings/common/container"), current);

        assertCoveredByCurrentOrMatrix("containers", SourceParityCatalog.CONTAINER_CLASS_NAMES, current);
    }

    @Test
    void upstreamNetworkMessagesAreImplementedOrDocumentedAsParityGaps() {
        Set<String> current = new LinkedHashSet<>();
        collectJavaClassNames(Paths.get("src/main/java/com/zzhalex/justdirethings/network"), current);

        assertCoveredByCurrentOrMatrix("network classes", SourceParityCatalog.NETWORK_CLASS_NAMES, current);
    }

    @Test
    void upstreamCapabilitiesAreImplementedOrDocumentedAsParityGaps() {
        Set<String> current = new LinkedHashSet<>();
        collectJavaClassNames(Paths.get("src/main/java/com/zzhalex/justdirethings/capability"), current);

        assertCoveredByCurrentOrMatrix("capability classes", SourceParityCatalog.CAPABILITY_CLASS_NAMES, current);
    }

    @Test
    void currentRecipeAndSoundResourcesCoverUpstreamSourceContract() {
        Set<String> soundResources = readSoundIds();

        assertContainsAll("recipe types", SourceParityCatalog.RECIPE_TYPE_IDS, new LinkedHashSet<>(ModRecipes.coreRecipeTypeIds()));
        assertContainsAll("recipe serializers", SourceParityCatalog.RECIPE_SERIALIZER_IDS, new LinkedHashSet<>(ModRecipes.coreRecipeSerializerIds()));
        assertContainsAll("sounds", SourceParityCatalog.SOUND_IDS, soundResources);
    }

    private static Set<String> currentBlockIds() throws IllegalAccessException {
        Set<String> ids = new LinkedHashSet<>(ModContentBlocks.coreContentBlockIds());
        for (Field field : ModBlocks.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && Block.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                ids.add(((Block) field.get(null)).getRegistryName().getPath());
            }
        }
        return ids;
    }

    private static Set<String> currentItemIds() throws IllegalAccessException {
        Set<String> ids = new LinkedHashSet<>();
        ids.addAll(ModContentBlocks.coreContentBlockIds());
        ids.addAll(ModContentItems.resourceItemIds());
        ids.addAll(ModContentItems.templateItemIds());
        ids.addAll(ModContentItems.upgradeItemIds());
        ids.addAll(ModEquipmentItems.toolItemIds());
        ids.addAll(ModEquipmentItems.bowItemIds());
        ids.addAll(ModEquipmentItems.armorItemIds());

        for (Field field : ModItems.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && Item.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                ids.add(((Item) field.get(null)).getRegistryName().getPath());
            }
        }
        return ids;
    }

    private static Set<String> resourceJsonIds(Path root) {
        Set<String> ids = new LinkedHashSet<>();
        if (!Files.isDirectory(root)) {
            return ids;
        }
        try {
            Files.walk(root)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .map(root::relativize)
                    .map(Path::toString)
                    .map(path -> path.replace('\\', '/'))
                    .map(path -> path.substring(0, path.length() - ".json".length()))
                    .forEach(ids::add);
        } catch (Exception ignored) {
            return ids;
        }
        return ids;
    }

    private static Set<String> readSoundIds() {
        Path sounds = Paths.get("src/main/resources/assets", Reference.MOD_ID, "sounds.json");
        Set<String> ids = new LinkedHashSet<>();
        if (!Files.isRegularFile(sounds)) {
            return ids;
        }
        try {
            String contents = new String(Files.readAllBytes(sounds), StandardCharsets.UTF_8);
            for (String expected : SourceParityCatalog.SOUND_IDS) {
                if (contents.contains("\"" + expected + "\"")) {
                    ids.add(expected);
                }
            }
        } catch (Exception ignored) {
            return ids;
        }
        return ids;
    }

    private static void collectJavaClassNames(Path root, Set<String> classNames) {
        if (!Files.isDirectory(root)) {
            return;
        }
        try {
            Files.walk(root)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .map(path -> path.getFileName().toString())
                    .map(name -> name.substring(0, name.length() - ".java".length()))
                    .forEach(classNames::add);
        } catch (Exception ignored) {
            return;
        }
    }

    private static String path(ResourceLocation id) {
        return id.getPath();
    }

    private static void assertContainsAll(String category, Set<String> expected, Set<String> current) {
        Set<String> missing = new TreeSet<>(expected);
        missing.removeAll(current);
        assertTrue(missing.isEmpty(), "Missing upstream " + category + ": " + missing);
    }

    private static void assertCoveredByCurrentOrMatrix(String category, Set<String> expected, Set<String> current) {
        Set<String> missing = new TreeSet<>(expected);
        missing.removeAll(current);

        String matrix = readParityMatrix();
        missing.removeIf(matrix::contains);
        assertTrue(missing.isEmpty(), "Missing upstream " + category + " with no parity-matrix entry: " + missing);
    }

    private static String readParityMatrix() {
        Path matrix = Paths.get("docs/audit/justdirethings-source-parity-matrix.md");
        if (!Files.isRegularFile(matrix)) {
            return "";
        }
        try {
            return new String(Files.readAllBytes(matrix), StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            return "";
        }
    }
}
