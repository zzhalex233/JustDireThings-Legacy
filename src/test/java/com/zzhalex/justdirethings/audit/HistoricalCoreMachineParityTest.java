package com.zzhalex.justdirethings.audit;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoricalCoreMachineParityTest {

    private static final String[] LEGACY_UNNUMBERED_MACHINE_CONSTANTS = {
            "public static final BlockGenerator GENERATOR =",
            "public static final BlockFluidGenerator FLUID_GENERATOR =",
            "public static final BlockItemCollector ITEM_COLLECTOR =",
            "public static final BlockBlockBreaker BLOCK_BREAKER =",
            "public static final BlockBlockPlacer BLOCK_PLACER =",
            "public static final BlockClicker CLICKER =",
            "public static final BlockDropper DROPPER =",
            "public static final BlockSensor SENSOR =",
            "public static final BlockBlockSwapper BLOCK_SWAPPER =",
            "public static final BlockFluidCollector FLUID_COLLECTOR =",
            "public static final BlockFluidPlacer FLUID_PLACER =",
            "public static final BlockExperienceHolder EXPERIENCE_HOLDER =",
            "public static final BlockEnergyTransmitter ENERGY_TRANSMITTER =",
            "public static final BlockPlayerAccessor PLAYER_ACCESSOR ="
    };

    private static final String[] ORIGINAL_MACHINE_IDS = {
            "generatort1",
            "generatorfluidt1",
            "itemcollector",
            "blockbreakert1",
            "blockbreakert2",
            "blockplacert1",
            "blockplacert2",
            "clickert1",
            "clickert2",
            "sensort1",
            "sensort2",
            "droppert1",
            "droppert2",
            "blockswappert1",
            "blockswappert2",
            "fluidplacert1",
            "fluidplacert2",
            "fluidcollectort1",
            "fluidcollectort2",
            "experienceholder",
            "energytransmitter",
            "playeraccessor",
            "paradoxmachine"
    };

    private static final String[] LEGACY_UNNUMBERED_MACHINE_IDS = {
            "generator",
            "fluid_generator",
            "item_collector",
            "block_breaker",
            "block_placer",
            "clicker",
            "dropper",
            "sensor",
            "block_swapper",
            "fluid_collector",
            "fluid_placer",
            "experience_holder",
            "energy_transmitter",
            "player_accessor"
    };

    private static final String[] ORIGINAL_MACHINE_BLOCK_CONSTANTS = {
            "GENERATOR_T1",
            "GENERATOR_FLUID_T1",
            "ITEMCOLLECTOR",
            "BLOCK_BREAKER_T1",
            "BLOCK_BREAKER_T2",
            "BLOCK_PLACER_T1",
            "BLOCK_PLACER_T2",
            "CLICKER_T1",
            "CLICKER_T2",
            "SENSOR_T1",
            "SENSOR_T2",
            "DROPPER_T1",
            "DROPPER_T2",
            "BLOCK_SWAPPER_T1",
            "BLOCK_SWAPPER_T2",
            "FLUID_PLACER_T1",
            "FLUID_PLACER_T2",
            "FLUID_COLLECTOR_T1",
            "FLUID_COLLECTOR_T2",
            "INVENTORY_HOLDER",
            "EXPERIENCEHOLDER",
            "ENERGYTRANSMITTER",
            "PLAYERACCESSOR",
            "PARADOX_MACHINE"
    };

    private static final String[] ORIGINAL_MACHINE_TILE_IDS = {
            "generatort1",
            "generatorfluidt1",
            "itemcollectorbe",
            "blockbreakert1",
            "blockbreakert2",
            "blockplacert1",
            "blockplacert2",
            "clickert1",
            "clickert2",
            "sensort1be",
            "sensort2be",
            "droppert1",
            "droppert2",
            "blockswappert1",
            "blockswappert2",
            "fluidplacert1",
            "fluidplacert2",
            "fluidcollectort1",
            "fluidcollectort2",
            "inventory_holder",
            "experienceholder",
            "energytransmitter",
            "playeraccessorbe",
            "paradoxmachine"
    };

    @Test
    void machineRegistrationsFollowUpstreamTieredIdsOnly() throws IOException {
        String blocks = read("src/main/java/com/zzhalex/justdirethings/registry/ModBlocks.java");
        String items = read("src/main/java/com/zzhalex/justdirethings/registry/ModItems.java");
        String tiles = read("src/main/java/com/zzhalex/justdirethings/registry/ModTileEntities.java");

        for (String constant : LEGACY_UNNUMBERED_MACHINE_CONSTANTS) {
            assertFalse(blocks.contains(constant), constant + " should not remain as a registered legacy machine");
        }

        for (String id : ORIGINAL_MACHINE_IDS) {
            assertTrue(blocks.contains("(\"" + id + "\")") || blocks.contains("\"" + id + "\""),
                    "ModBlocks should register upstream machine id " + id);
        }

        for (String constant : ORIGINAL_MACHINE_BLOCK_CONSTANTS) {
            assertTrue(items.contains("createBlockItem(ModBlocks." + constant + ")"),
                    "ModItems should register an ItemBlock for upstream machine constant " + constant);
        }

        assertFalse(tiles.contains("\"generator\""));
        assertFalse(tiles.contains("\"fluid_generator\""));
        assertFalse(tiles.contains("\"item_collector\""));
        assertFalse(tiles.contains("\"energy_transmitter\""));
        for (String id : ORIGINAL_MACHINE_TILE_IDS) {
            assertTrue(tiles.contains("\"" + id + "\""), "ModTileEntities should register upstream tile id " + id);
        }
    }

    @Test
    void machineBlockDefaultsCannotReintroduceLegacyIds() throws IOException {
        Path machineBlocks = path("src/main/java/com/zzhalex/justdirethings/common/block/machine");

        try (java.util.stream.Stream<Path> files = Files.list(machineBlocks)) {
            String contents = files.filter(path -> path.getFileName().toString().endsWith(".java"))
                    .map(HistoricalCoreMachineParityTest::readUnchecked)
                    .reduce("", String::concat);
            for (String id : LEGACY_UNNUMBERED_MACHINE_IDS) {
                assertFalse(contents.contains("this(\"" + id + "\")"),
                        "Machine block no-arg constructors should not default back to legacy id " + id);
            }
        }
    }

    @Test
    void machineContainerInventoryNamesCannotReintroduceLegacyIds() throws IOException {
        Path machineContainers = path("src/main/java/com/zzhalex/justdirethings/common/container/machine");

        try (java.util.stream.Stream<Path> files = Files.list(machineContainers)) {
            String contents = files.filter(path -> path.getFileName().toString().endsWith(".java"))
                    .map(HistoricalCoreMachineParityTest::readUnchecked)
                    .reduce("", String::concat);
            for (String id : LEGACY_UNNUMBERED_MACHINE_IDS) {
                assertFalse(contents.contains("new InventoryBasic(\"" + id + "\""),
                        "Machine container inventory names should not default back to legacy id " + id);
            }
        }
    }

    @Test
    void resourcePackDoesNotExposeUnregisteredLegacyMachineAliases() {
        for (String id : LEGACY_UNNUMBERED_MACHINE_IDS) {
            assertFalse(Files.exists(path("src/main/resources/assets/justdirethings/blockstates/" + id + ".json")),
                    "Unregistered legacy machine blockstate should not remain in assets: " + id);
            assertFalse(Files.exists(path("src/main/resources/assets/justdirethings/models/item/" + id + ".json")),
                    "Unregistered legacy machine item model should not remain in assets: " + id);
            assertFalse(Files.exists(path("src/main/resources/assets/justdirethings/models/block/" + id + ".json")),
                    "Unregistered legacy machine block model should not remain in assets: " + id);
        }
    }

    @Test
    void chineseMachineTranslationsUseUpstreamNames() throws IOException {
        String zhCn = read("src/main/resources/assets/justdirethings/lang/zh_cn.lang");

        assertTrue(zhCn.contains("tile.justdirethings.generatort1.name=初级煤炭发电器"));
        assertTrue(zhCn.contains("tile.justdirethings.generatorfluidt1.name=初级燃液发电器"));
        assertTrue(zhCn.contains("tile.justdirethings.itemcollector.name=物品拾取器"));
        assertTrue(zhCn.contains("tile.justdirethings.blockbreakert2.name=高级方块破坏器"));
        assertTrue(zhCn.contains("tile.justdirethings.inventory_holder.name=物品栏存储器"));
        assertTrue(zhCn.contains("tile.justdirethings.experienceholder.name=经验存储器"));
        assertTrue(zhCn.contains("justdirethings.screen.renderarea=显示区域"));
        assertFalse(zhCn.contains("tile.justdirethings.generator.name="),
                "Legacy unnumbered generator translation should not mask the upstream id");
        assertFalse(zhCn.contains("tile.justdirethings.energy_transmitter.name="),
                "Legacy unnumbered energy transmitter translation should not mask the upstream id");
    }

    @Test
    void englishMachineTranslationsUseUpstreamNames() throws IOException {
        String enUs = read("src/main/resources/assets/justdirethings/lang/en_us.lang");

        assertTrue(enUs.contains("tile.justdirethings.generatort1.name=Simple Coal Generator"));
        assertTrue(enUs.contains("tile.justdirethings.generatorfluidt1.name=Simple Fuel Generator"));
        assertTrue(enUs.contains("tile.justdirethings.blockbreakert1.name=Simple Block Breaker"));
        assertTrue(enUs.contains("tile.justdirethings.blockbreakert2.name=Advanced Block Breaker"));
        assertTrue(enUs.contains("tile.justdirethings.blockswappert1.name=Simple Swapper"));
        assertTrue(enUs.contains("tile.justdirethings.fluidcollectort2.name=Advanced Fluid Collector"));
        assertTrue(enUs.contains("tile.justdirethings.inventory_holder.name=Inventory Holder"));
        for (String id : LEGACY_UNNUMBERED_MACHINE_IDS) {
            assertFalse(enUs.contains("tile.justdirethings." + id + ".name="),
                    "Legacy unnumbered English translation should not mask upstream id " + id);
        }
    }

    @Test
    void blockSwapperUsesPartnerBindingInsteadOfTemporaryInventory() throws IOException {
        String container = read("src/main/java/com/zzhalex/justdirethings/common/container/machine/ContainerBlockSwapper.java");
        String tile = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileBlockSwapper.java");
        String gui = read("src/main/java/com/zzhalex/justdirethings/client/gui/machine/GuiBlockSwapper.java");

        assertTrue(container.contains("new InventoryBasic(\"blockswappert1\", false, 0)"),
                "Block Swapper should not declare a visible machine inventory");
        assertFalse(container.contains("SlotItemHandler"),
                "Block Swapper should not expose the old 3x3 temporary inventory");
        assertFalse(tile.contains("super(9);"),
                "Block Swapper tile should not own a 9-slot temporary inventory");
        assertTrue(tile.contains("BlockPos boundTo"),
                "Block Swapper should persist a bound partner position");
        assertTrue(tile.contains("handleConnection"),
                "Block Swapper should have upstream-style wrench binding semantics");
        assertTrue(tile.contains("swapBlocks"),
                "Block Swapper should expose the upstream swap-blocks setting");
        assertTrue(tile.contains("swapEntityType"),
                "Block Swapper should expose the upstream entity swap setting");
        assertTrue(gui.contains("SWAP_BLOCKS") && gui.contains("SWAP_ENTITY_TYPE"),
                "Block Swapper GUI should expose upstream swap buttons");
    }

    @Test
    void experienceHolderUsesExperienceStateInsteadOfItemSlotPlaceholder() throws IOException {
        String container = read("src/main/java/com/zzhalex/justdirethings/common/container/machine/ContainerExperienceHolder.java");
        String tile = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileExperienceHolder.java");
        String gui = read("src/main/java/com/zzhalex/justdirethings/client/gui/machine/GuiExperienceHolder.java");

        assertTrue(container.contains("new InventoryBasic(\"experienceholder\", false, 0)"),
                "Experience Holder should not declare a machine item slot");
        assertFalse(container.contains("SlotItemHandler"),
                "Experience Holder should not show the old bottle input slot");
        assertTrue(tile.contains("extends TileMachineBase implements ITickable"),
                "Experience Holder should be a stateful machine, not a 1-slot timed inventory");
        assertTrue(tile.contains("targetExperience") && tile.contains("ownerOnly") && tile.contains("collectExperience"),
                "Experience Holder should persist target/owner/collect settings");
        assertTrue(tile.contains("storeExperience") && tile.contains("extractExperience"),
                "Experience Holder should expose manual store/extract behavior");
        assertTrue(gui.contains("STORE_EXPERIENCE") && gui.contains("EXTRACT_EXPERIENCE") && gui.contains("TARGET_EXPERIENCE"),
                "Experience Holder GUI should expose upstream experience controls");
    }

    @Test
    void inventoryHolderUsesPlayerMirrorSlotModelAndControls() throws IOException {
        String container = read("src/main/java/com/zzhalex/justdirethings/common/container/machine/ContainerInventoryHolder.java");
        String tile = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileInventoryHolder.java");
        String gui = read("src/main/java/com/zzhalex/justdirethings/client/gui/machine/GuiInventoryHolder.java");

        assertTrue(tile.contains("super(41);"),
                "Inventory Holder should reserve upstream player inventory, armor, and offhand mirror slots");
        assertTrue(container.contains("SLOT_COUNT = 41"),
                "Inventory Holder container should lay out all 41 upstream machine slots");
        assertTrue(gui.contains("FILTER_ONLY") && gui.contains("COMPARE_COUNTS") && gui.contains("SHOW_FAKE_PLAYER"),
                "Inventory Holder GUI should expose upstream mirror/filter/action controls");
    }

    @Test
    void playerAccessorPersistsSidedInventoryConnectionModes() throws IOException {
        String tile = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/TilePlayerAccessor.java");
        String gui = read("src/main/java/com/zzhalex/justdirethings/client/gui/machine/GuiPlayerAccessor.java");

        assertTrue(tile.contains("EnumMap<EnumFacing, InventoryConnectionType>"),
                "Player Accessor should persist per-side inventory connection modes");
        assertTrue(tile.contains("setInventoryConnectionType") && tile.contains("getInventoryConnectionType"),
                "Player Accessor should expose sided inventory connection accessors");
        assertTrue(gui.contains("INVENTORY_CONNECTION_"),
                "Player Accessor GUI should expose the six upstream side-connection buttons");
    }

    @Test
    void sensorHasFilterSlotAndOriginalSignalControls() throws IOException {
        String container = read("src/main/java/com/zzhalex/justdirethings/common/container/machine/ContainerSensor.java");
        String tile = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileSensor.java");
        String gui = read("src/main/java/com/zzhalex/justdirethings/client/gui/machine/GuiSensor.java");

        assertTrue(tile.contains("implements ITickable, TileFilteredMachine"),
                "Sensor should expose the upstream ghost filter handler instead of a real item container");
        assertTrue(tile.contains("FILTER_SLOT_COUNT = 9") && tile.contains("new FilterItemHandler(FILTER_SLOT_COUNT)"),
                "Sensor should reserve the upstream 9 marked filter slots");
        assertTrue(container.contains("addFilterSlots(tile.getFilterHandler(), tile instanceof TileSensor.T2 ? 8 : 80, tile instanceof TileSensor.T2 ? 54 : 13, 9)"),
                "Sensor T1/T2 filter slots should use the upstream T1 row and shared T2 lower filter row positions");
        assertTrue(tile.contains("senseTarget") && tile.contains("strongSignal"),
                "Sensor should persist upstream target and strong/weak settings");
        assertTrue(gui.contains("sensorTargetButton") && gui.contains("strongWeakRedstoneButton"),
                "Sensor GUI should expose upstream signal controls");
    }

    @Test
    void energyTransmitterHasMachineSlotAndAreaFilterControls() throws IOException {
        String container = read("src/main/java/com/zzhalex/justdirethings/common/container/machine/ContainerEnergyTransmitter.java");
        String tile = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileEnergyTransmitter.java");
        String gui = read("src/main/java/com/zzhalex/justdirethings/client/gui/machine/GuiEnergyTransmitter.java");

        assertTrue(tile.contains("extends TileInventoryMachineBase implements ITickable"),
                "Energy Transmitter should reserve the upstream one-slot machine inventory");
        assertTrue(tile.contains("super(1);"),
                "Energy Transmitter should expose one machine slot");
        assertTrue(container.contains("addSlotToContainer(new SlotItemHandler(tile.getItemHandler(), 0, 80, 35))"),
                "Energy Transmitter machine slot should use the upstream default machine-slot position");
        assertTrue(tile.contains("showParticles"),
                "Energy Transmitter should persist the upstream show-particles setting");
        assertTrue(gui.contains("SHOW_PARTICLES") && gui.contains("filterButtons") && gui.contains("areaButtons"),
                "Energy Transmitter GUI should expose upstream particle, filter, and area controls");
    }

    private static String read(String path) throws IOException {
        return Files.readString(path(path), StandardCharsets.UTF_8);
    }

    private static Path path(String path) {
        return Paths.get(path);
    }

    private static String readUnchecked(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError("Could not read " + path, e);
        }
    }
}
