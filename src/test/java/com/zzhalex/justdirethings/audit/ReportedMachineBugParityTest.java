package com.zzhalex.justdirethings.audit;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportedMachineBugParityTest {

    private static final Pattern BLOCKSTATE_MODEL = Pattern.compile("\"model\"\\s*:\\s*\"justdirethings:([^\"]+)\"");

    @Test
    void fluidPlacerUsesARealFluidHandlerWhenPlacingFluid() throws IOException {
        String contents = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileFluidPlacer.java");

        assertFalse(contents.contains("tryPlaceFluid(null, world, targetPos, ItemStack.EMPTY"),
                "Fluid Placer cannot use the ItemStack overload with ItemStack.EMPTY; Forge 1.12 requires a real fluid handler source");
        assertTrue(contents.contains("IFluidHandler"),
                "Fluid Placer should expose its internal tank as an IFluidHandler when placing fluid into the world");
    }

    @Test
    void fluidPlacerMatchesUpstreamPartialDrainAndSourceValidation() throws IOException {
        String contents = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileFluidPlacer.java");

        assertTrue(contents.contains("handler.drain(Math.min(1000, room), false)"),
                "Fluid Placer should simulate only the accepted tank room, matching upstream oversized-container handling");
        assertTrue(contents.contains("handler.drain(accepted, true)"),
                "Fluid Placer should execute-drain only the amount accepted by the machine tank instead of requiring the whole container to fit");
        assertFalse(contents.contains("getFluidState().getAmount() + contained.amount > getFluidState().getCapacity()"),
                "Fluid Placer must not reject oversized fluid containers just because their total contents exceed remaining capacity");
        assertTrue(contents.contains("canPlaceFluidAt(targetPos)"),
                "Fluid Placer should share the upstream source-fluid placement guard for T1 and T2 targets");
        assertTrue(contents.contains("IFluidBlock") && contents.contains("BlockLiquid.LEVEL"),
                "Fluid Placer should reject already-placed source fluids in both Forge-fluid and vanilla-liquid forms");
    }

    @Test
    void fluidBarsExposeOriginalHoverTooltip() throws IOException {
        String baseGui = read("src/main/java/com/zzhalex/justdirethings/client/gui/base/GuiMachineBase.java");
        String widget = read("src/main/java/com/zzhalex/justdirethings/client/gui/widget/WidgetFluidBar.java");
        String enUs = read("src/main/resources/assets/justdirethings/lang/en_us.lang");
        String zhCn = read("src/main/resources/assets/justdirethings/lang/zh_cn.lang");

        assertTrue(baseGui.contains("drawFluidBarTooltip"),
                "Machine GUI base should ask fluid bars to render the upstream hover tooltip");
        assertTrue(widget.contains("justdirethings.screen.fluid"),
                "Fluid bar widget should use the upstream fluid tooltip localization key");
        assertTrue(enUs.contains("justdirethings.screen.fluid="), "Missing English fluid tooltip localization");
        assertTrue(zhCn.contains("justdirethings.screen.fluid="), "Missing Chinese fluid tooltip localization");
    }

    @Test
    void energyBarsExposeOriginalHoverTooltip() throws IOException {
        String baseGui = read("src/main/java/com/zzhalex/justdirethings/client/gui/base/GuiMachineBase.java");
        String widget = read("src/main/java/com/zzhalex/justdirethings/client/gui/widget/WidgetEnergyBar.java");
        String enUs = read("src/main/resources/assets/justdirethings/lang/en_us.lang");
        String zhCn = read("src/main/resources/assets/justdirethings/lang/zh_cn.lang");

        assertTrue(baseGui.contains("drawEnergyBarTooltip"),
                "Machine GUI base should ask energy bars to render the upstream hover tooltip");
        assertTrue(widget.contains("justdirethings.screen.energy"),
                "Energy bar widget should use the upstream energy tooltip localization key");
        assertTrue(enUs.contains("justdirethings.screen.energy="), "Missing English energy tooltip localization");
        assertTrue(zhCn.contains("justdirethings.screen.energy="), "Missing Chinese energy tooltip localization");
    }

    @Test
    void machineFilterSlotsSupportOriginalJeiGhostDragging() throws IOException {
        String jeiPlugin = read("src/main/java/com/zzhalex/justdirethings/client/jei/JDTJeiPlugin.java");
        String ghostHandler = read("src/main/java/com/zzhalex/justdirethings/client/jei/GhostFilterBasic.java");
        String network = read("src/main/java/com/zzhalex/justdirethings/network/JDTNetwork.java");
        String message = read("src/main/java/com/zzhalex/justdirethings/network/message/MessageGhostSlot.java");
        String container = read("src/main/java/com/zzhalex/justdirethings/common/container/base/ContainerMachineBase.java");

        assertTrue(jeiPlugin.contains("addGhostIngredientHandler(GuiMachineBase.class, new GhostFilterBasic())"),
                "JEI plugin should register one shared ghost-ingredient handler for machine filter slots");
        assertTrue(ghostHandler.contains("SlotFilterItemHandler") && ghostHandler.contains("MessageGhostSlot"),
                "Ghost ingredient handler should target filter slots and forward the marked stack to the server");
        assertTrue(network.contains("MessageGhostSlot.Handler.class"),
                "Network registration should expose the shared ghost-slot sync packet");
        assertTrue(message.contains("applyGhostSlot") && container.contains("applyGhostSlot"),
                "Ghost slot packets should land in ContainerMachineBase so every machine filter slot shares the same server-side path");
    }

    @Test
    void sensorBlockStatePanelMatchesOriginalFixedGrayScrollablePanel() throws IOException {
        String gui = read("src/main/java/com/zzhalex/justdirethings/client/gui/machine/GuiSensor.java");
        String list = read("src/main/java/com/zzhalex/justdirethings/client/gui/widget/SensorBlockStateScrollList.java");
        String layout = read("src/main/java/com/zzhalex/justdirethings/client/gui/SensorBlockStatePanelLayout.java");
        String message = read("src/main/java/com/zzhalex/justdirethings/network/message/MessageBlockStateFilter.java");

        assertTrue(gui.contains("topSectionLeft - SensorBlockStatePanelLayout.PANEL_WIDTH") && gui.contains("topSectionTop"),
                "Sensor block-state panel should open at the upstream left-side gray panel position");
        assertTrue(layout.contains("PANEL_CLICK_LEFT_OFFSET = -101"),
                "Sensor block-state panel outside-click bounds should preserve the upstream one-pixel left tolerance");
        assertTrue(list.contains("Gui.drawRect(listLeft, top, listLeft + listWidth, bottom, 0xD0101010)"),
                "Sensor block-state list should draw the original dark gray list background rather than dirt/slot textures");
        assertTrue(list.contains("getScrollBarX()") && list.contains("listLeft + listWidth - SensorBlockStatePanelLayout.SCROLLBAR_RIGHT_PADDING"),
                "Sensor block-state list should keep the upstream fixed scrollbar position");
        assertTrue(list.contains("trimToPixelWidth"),
                "Sensor block-state list should clip long property/value labels inside the fixed panel width");
        assertTrue(gui.contains("justdirethings.screen.rightclicksettings"),
                "Sensor filter slots should expose the upstream right-click settings tooltip");
        assertTrue(gui.contains("MessageBlockStateFilter(getWindowId(), slot, \"\", \"\")"),
                "Changing a marked filter stack should clear stale block-state settings on the server");
        assertTrue(message.contains("message.propertyName.isEmpty()") && message.contains("sensor.clearSensorProperties(message.slot)"),
                "The block-state settings packet should support clearing all properties for a slot");
    }

    @Test
    void sensorEntityFiltersSupportOriginalSpawnEggAndCreatureCatcherPaths() throws IOException {
        String sensor = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileSensor.java");

        assertTrue(sensor.contains("ItemMonsterPlacer") && sensor.contains("ItemMonsterPlacer.getNamedIdFrom(filter)"),
                "Sensor entity filters should support vanilla 1.12 spawn eggs like upstream SpawnEggItem filters");
        assertTrue(sensor.contains("ItemCreatureCatcher") && sensor.contains("EntityCreatureCatcher.createCapturedEntity"),
                "Sensor entity filters should keep the upstream Creature Catcher matching path");
        assertTrue(sensor.contains("normalizedEntityTag(captured)") && sensor.contains("normalizedEntityTag(entity)"),
                "Creature Catcher compare-NBT mode should compare normalized entity tags like upstream");
    }

    @Test
    void itemCollectorBlockstatesUseUpstreamAttachmentRotation() throws IOException {
        String upstream = read("src/main/resources/assets/justdirethings/blockstates/itemcollector.json");

        assertTrue(upstream.contains("\"facing=down\": { \"model\": \"justdirethings:itemcollector\" }"),
                "Item Collector down-facing variant should not be upside down");
        assertTrue(upstream.contains("\"facing=up\": { \"model\": \"justdirethings:itemcollector\", \"x\": 180, \"y\": 180 }"),
                "Item Collector up-facing variant should use the upstream inverted ceiling attachment");
    }

    @Test
    void itemCollectorGuiAndContainerExposeOriginalFilterControls() throws IOException {
        String baseGui = read("src/main/java/com/zzhalex/justdirethings/client/gui/base/GuiMachineBase.java");
        String container = read("src/main/java/com/zzhalex/justdirethings/common/container/machine/ContainerItemCollector.java");
        String tile = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileItemCollector.java");

        assertTrue(baseGui.contains("MachineButtonFactory.filterButtons"),
                "Item Collector GUI should include the upstream allow/deny and compare-NBT filter buttons");
        assertTrue(baseGui.contains("showParticlesButton(98, 62"),
                "Item Collector GUI should include the upstream Show Particles button at 98,62");
        assertTrue(container.contains("FILTER_SLOT_COUNT = 9"),
                "Item Collector should expose the upstream 9 ghost filter slots");
        assertTrue(tile.contains("setAllowList(false)"),
                "Item Collector should default to denylist mode so empty filters collect everything like upstream");
        assertTrue(tile.contains("matchesFilter"),
                "Item Collector should apply its GUI filter slots before collecting item entities");
    }

    @Test
    void energyTransmitterUsesOriginalFilterSlotsForChargeTargets() throws IOException {
        String tile = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileEnergyTransmitter.java");
        String container = read("src/main/java/com/zzhalex/justdirethings/common/container/machine/ContainerEnergyTransmitter.java");

        assertTrue(tile.contains("new FilterItemHandler(9)"),
                "Energy Transmitter should expose the upstream 9-slot basic filter handler");
        assertTrue(tile.contains("getFilterState().setAllowList(false)"),
                "Energy Transmitter should default to denylist mode like upstream FilterData");
        assertTrue(tile.contains("matchesFilter(blockStack)"),
                "Energy Transmitter should apply its filter before adding charge targets/transmitters");
        assertTrue(container.contains("addFilterSlots(tile.getFilterHandler())"),
                "Energy Transmitter container should expose the upstream ghost filter slots");
    }

    @Test
    void energyTransmitterTransfersEveryTickAndUsesTickSpeedOnlyForSourceScanCadence() throws IOException {
        String tile = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileEnergyTransmitter.java");

        assertTrue(tile.contains("setTickSpeed(50)"),
                "Energy Transmitter should use the upstream 50-tick scan cadence");
        assertFalse(tile.contains("!shouldRunTimedMachine()"),
                "Energy Transmitter must not gate every FE transfer behind the timed machine cadence");
        assertTrue(tile.contains("evaluateRedstoneControl()") && tile.contains("!isRedstoneActive()"),
                "Energy Transmitter should still obey redstone modes while allowing per-tick FE transfer");
    }

    @Test
    void machinesGateWorkThroughOriginalRedstoneModes() throws IOException {
        assertTrue(read("src/main/java/com/zzhalex/justdirethings/common/tile/base/TileTimedMachineBase.java").contains("shouldRunTimedMachine()"),
                "Timed machine base should gate work through the configured redstone mode");
        for (String tile : new String[] {
                "TileGenerator",
                "TileFluidGenerator",
                "TileEnergyTransmitter",
                "TileExperienceHolder",
                "TileBlockSwapper",
                "TileItemCollector"
        }) {
            String contents = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/" + tile + ".java");
            assertTrue(contents.contains("isRedstoneActive()") || contents.contains("shouldRunTimedMachine()"),
                    tile + " should gate its manual update loop through the configured redstone mode");
        }
    }

    @Test
    void itemCollectorGuiDropsLegacyPreviewAndUsesOriginalAreaControls() throws IOException {
        String itemCollectorGui = read("src/main/java/com/zzhalex/justdirethings/client/gui/machine/GuiItemCollector.java");
        String baseGui = read("src/main/java/com/zzhalex/justdirethings/client/gui/base/GuiMachineBase.java");
        String factory = read("src/main/java/com/zzhalex/justdirethings/client/gui/button/MachineButtonFactory.java");
        String itemCollectorTile = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileItemCollector.java");

        assertFalse(itemCollectorGui.contains("WidgetAreaPreview"),
                "Item Collector GUI should not draw the legacy yellow area preview box");
        assertFalse(itemCollectorGui.contains("addAreaPreview"),
                "Item Collector GUI should rely on world area rendering, not the temporary 2D preview widget");
        assertTrue(baseGui.contains("\"Rad\"") && baseGui.contains("\"Off\"") && baseGui.contains("\"X\"") && baseGui.contains("\"Y\"") && baseGui.contains("\"Z\""),
                "Area controls should draw the original Rad/Off and X/Y/Z labels");
        assertTrue(factory.contains("areaValueButtons"),
                "Area controls should use original add/remove value button pairs");
        assertTrue(factory.contains("state(\"remove.png\"") && factory.contains("state(\"add.png\""),
                "Area controls should render the original remove/add button icons");
        assertFalse(factory.contains("ButtonDefinition.number(25, 12"),
                "Area controls should not use the temporary click-the-number widgets");
        assertTrue(itemCollectorTile.contains("getAreaState().createArea(pos)"),
                "Item Collector should apply the original offset-aware area when collecting items");
    }

    @Test
    void itemCollectorRenderAreaUsesOriginalWorldOverlayRenderer() throws IOException {
        String clientRegistration = read("src/main/java/com/zzhalex/justdirethings/client/ClientRegistration.java");
        String renderer = read("src/main/java/com/zzhalex/justdirethings/client/render/tile/RenderMachineArea.java");
        String eventHandler = read("src/main/java/com/zzhalex/justdirethings/client/event/MachineAreaRenderHandler.java");
        String areaState = read("src/main/java/com/zzhalex/justdirethings/common/tile/base/MachineAreaState.java");

        assertFalse(clientRegistration.contains("RenderItemCollectorArea"),
                "Render-area overlays must not be hard-wired to Item Collector only");
        assertFalse(clientRegistration.contains("bindTileEntitySpecialRenderer(TileItemCollector")
                        || clientRegistration.contains("bindTileEntitySpecialRenderer(TileMachine")
                        || clientRegistration.contains("RenderMachineArea"),
                "Area overlays should not be registered as per-machine TESRs; that path caused duplicated one-off render behavior and renderer conflicts");
        assertTrue(clientRegistration.contains("bindTileEntitySpecialRenderer(TileGooBlock.Tier1.class, new RenderGooBlock())"),
                "Non-area dynamic tile renderers such as Goo blocks should still be allowed to use TESRs");
        assertTrue(clientRegistration.contains("MachineAreaRenderHandler.INSTANCE"),
                "Client setup should register one shared world render event for every area-capable machine");
        assertTrue(eventHandler.contains("RenderWorldLastEvent"),
                "Area overlays should render from the shared 1.12 world-last event instead of per-machine TESRs");
        assertTrue(eventHandler.contains("loadedTileEntityList") && eventHandler.contains("instanceof TileMachineBase"),
                "The shared event should scan loaded machine tiles and reuse the same overlay renderer for T1 area machines, T2 machines, and special machines");
        assertFalse(renderer.contains("extends TileEntitySpecialRenderer"),
                "RenderMachineArea should be a reusable drawing helper, not another tile-class renderer");
        assertTrue(renderer.contains("isRenderArea()"),
                "Render-area overlay should obey the GUI Render Area toggle");
        assertFalse(renderer.contains("RenderGlobal.drawSelectionBoundingBox"),
                "Render-area overlay should not delegate wire colors to RenderGlobal because it can inherit black GL state");
        assertTrue(renderer.contains("drawWireBox") && renderer.contains("GL11.GL_LINES"),
                "Render-area overlay should draw the original wireframe boxes through the shared 1.12 color-controlled helper");
        assertTrue(renderer.contains("drawSolidBox"),
                "Render-area overlay should draw the original translucent filled boxes");
        assertTrue(renderer.indexOf("drawSolidBox(area") < renderer.indexOf("drawWireBox(area"),
                "Render-area overlay should draw translucent fill first and wireframe last so original colors stay visible");
        assertTrue(renderer.contains("GlStateManager.tryBlendFuncSeparate") && renderer.contains("GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL"),
                "Render-area overlay should use explicit translucent GL state and polygon offset to reduce z-fighting where it overlaps blocks");
        assertTrue(renderer.contains("PRIMARY_FILL_RED = 255")
                        && renderer.contains("OFFSET_FILL_BLUE = 255"),
                "Render-area overlay should use the original red main area and blue offset area fills");
        assertTrue(renderer.contains("PRIMARY_LINE_GREEN = 255")
                        && renderer.contains("OFFSET_LINE_RED = 255")
                        && renderer.contains("OFFSET_LINE_GREEN = 255")
                        && renderer.contains("OFFSET_LINE_BLUE = 255"),
                "Render-area overlay should use the original green main area and white offset wireframe colors");
        assertTrue(renderer.contains(".color(red, green, blue, alpha)"),
                "Render-area overlay should write integer RGBA values directly to the buffer so GL color state cannot turn advanced ranges black");
        assertTrue(areaState.contains("createOffsetOnlyArea"),
                "Area state should expose the original offset-only helper box for the renderer");
    }

    @Test
    void powerMachineBlockstatesKeepUpstreamAttachmentOrientation() throws IOException {
        String generator = read("src/main/resources/assets/justdirethings/blockstates/generatort1.json");
        String fluidGenerator = read("src/main/resources/assets/justdirethings/blockstates/generatorfluidt1.json");
        String energyTransmitter = read("src/main/resources/assets/justdirethings/blockstates/energytransmitter.json");
        String experienceHolder = read("src/main/resources/assets/justdirethings/blockstates/experienceholder.json");

        assertTrue(generator.contains("\"normal\":") && generator.contains("\"model\": \"justdirethings:generatort1\""),
                "Coal Generator T1 should use the 1.12 normal variant for its upstream upright non-facing blockstate");
        assertFalse(generator.contains("\"facing="),
                "Coal Generator T1 must not rotate the whole model and move top/bottom textures to the side");
        assertTrue(fluidGenerator.contains("\"normal\":") && fluidGenerator.contains("\"model\": \"justdirethings:generatorfluidt1\""),
                "Fluid Generator T1 should use the 1.12 normal variant for its upstream upright non-facing blockstate");
        assertFalse(fluidGenerator.contains("\"facing="),
                "Fluid Generator T1 must not rotate the whole model and move top/bottom textures to the side");
        assertTrue(energyTransmitter.contains("\"facing=down\": { \"model\": \"justdirethings:energytransmitter\" }"),
                "Energy Transmitter down-facing variant should match the upstream attachment base orientation");
        assertTrue(energyTransmitter.contains("\"facing=north\": { \"model\": \"justdirethings:energytransmitter\", \"x\": 270 }"),
                "Energy Transmitter side-facing variants should use upstream -90-degree rotation, represented as 270 for 1.12");
        assertTrue(experienceHolder.contains("\"facing=down\": { \"model\": \"justdirethings:experienceholder\" }"),
                "Experience Holder should share the same upstream attachment orientation as the custom machine models");
        assertTrue(experienceHolder.contains("\"facing=north\": { \"model\": \"justdirethings:experienceholder\", \"x\": 270 }"),
                "Experience Holder side-facing variants should use upstream -90-degree rotation, represented as 270 for 1.12");
    }

    @Test
    void uprightGeneratorsRemoveFacingStateLikeOriginal() throws IOException {
        String blockMachineBase = read("src/main/java/com/zzhalex/justdirethings/common/block/machine/BlockMachineBase.java");
        String generator = read("src/main/java/com/zzhalex/justdirethings/common/block/machine/BlockGenerator.java");
        String fluidGenerator = read("src/main/java/com/zzhalex/justdirethings/common/block/machine/BlockFluidGenerator.java");

        assertTrue(generator.contains("new BlockStateContainer(this)"),
                "Coal Generator T1 should remove the inherited FACING property because its blockstate has only the upstream upright variant");
        assertTrue(fluidGenerator.contains("new BlockStateContainer(this)"),
                "Fluid Generator T1 should remove the inherited FACING property because its blockstate has only the upstream upright variant");
        assertTrue(blockMachineBase.contains("getPropertyKeys().contains(FACING)"),
                "Base machine placement/meta code must guard FACING access so non-rotatable upstream machines do not create missing 1.12 variants");
    }

    @Test
    void itemCollectorDoesNotDefaultRenderAreaOnLikeOriginal() throws IOException {
        String itemCollector = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileItemCollector.java");

        assertFalse(itemCollector.contains("setRenderArea(true)"),
                "Item Collector should not enable Render Area by default; upstream AreaAffectingData defaults renderArea to false");
    }

    @Test
    void attachmentMachinesShareClickedFacePlacementAndNonFullShapeBase() throws IOException {
        Path basePath = path("src/main/java/com/zzhalex/justdirethings/common/block/machine/BlockAttachedMachineBase.java");
        assertTrue(Files.exists(basePath),
                "Attachment-style custom machine models should share a base instead of fixing each transparent-shell bug one at a time");

        String base = read(basePath.toString());
        String itemCollector = read("src/main/java/com/zzhalex/justdirethings/common/block/machine/BlockItemCollector.java");
        String energyTransmitter = read("src/main/java/com/zzhalex/justdirethings/common/block/machine/BlockEnergyTransmitter.java");
        String experienceHolder = read("src/main/java/com/zzhalex/justdirethings/common/block/machine/BlockExperienceHolder.java");

        assertTrue(base.contains("facing.getOpposite()"),
                "Attachment machines should mimic upstream clicked-face placement, not player-horizontal placement");
        assertTrue(base.contains("isFullCube") && base.contains("isOpaqueCube") && base.contains("getBlockFaceShape"),
                "Attachment machines should centrally opt out of the 1x1x1 full-cube shell");
        assertTrue(base.contains("SHAPES") && base.contains("getBoundingBox") && base.contains("getCollisionBoundingBox"),
                "Attachment machines should centrally expose model-shaped selection/collision boxes");
        assertTrue(itemCollector.contains("extends BlockAttachedMachineBase"),
                "Item Collector should use the shared attachment base");
        assertTrue(energyTransmitter.contains("extends BlockAttachedMachineBase"),
                "Energy Transmitter should use the shared attachment base");
        assertTrue(experienceHolder.contains("extends BlockAttachedMachineBase"),
                "Experience Holder should use the shared attachment base");
    }

    @Test
    void regularMachinesUseNearestLookingDirectionPlacementLikeOriginal() throws IOException {
        String blockMachineBase = read("src/main/java/com/zzhalex/justdirethings/common/block/machine/BlockMachineBase.java");

        assertTrue(blockMachineBase.contains("EnumFacing.getDirectionFromEntityLiving(pos, placer)"),
                "Regular machines should use the 1.12 equivalent of upstream getNearestLookingDirection().getOpposite() placement");
        assertFalse(blockMachineBase.contains("placer.getHorizontalFacing().getOpposite()"),
                "Regular machines should not be locked to horizontal placement");
    }

    @Test
    void blockstateModelReferencesResolveUnderMinecraft112Rules() throws IOException {
        Path blockstates = path("src/main/resources/assets/justdirethings/blockstates");
        Path modelRoot = path("src/main/resources/assets/justdirethings/models/block");

        try (java.util.stream.Stream<Path> files = Files.list(blockstates)) {
            files.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .forEach(path -> assertBlockstateModelsResolve(path, modelRoot));
        }
    }

    @Test
    void itemCollectorAreaButtonsUseGuiLeftLikeOriginalScreen() throws IOException {
        String baseGui = read("src/main/java/com/zzhalex/justdirethings/client/gui/base/GuiMachineBase.java");

        assertTrue(baseGui.contains("new MachineGuiButton(buttonList.size(), getButtonBaseLeft(), topSectionTop, definition)"),
                "Machine buttons should be placed from guiLeft like upstream, not from the widened topSectionLeft");
        assertTrue(baseGui.contains("int valueLeft = getAreaButtonBaseLeft() + x + 12"),
                "Area value labels should align with the upstream ValueButtons display based on guiLeft");
        assertFalse(baseGui.contains("int valueLeft = topSectionLeft - guiLeft + x + 12"),
                "Area value labels should not inherit the widened panel's left shift");
    }

    private static String read(String path) throws IOException {
        return Files.readString(path(path), StandardCharsets.UTF_8);
    }

    private static Path path(String path) {
        return Paths.get(path);
    }

    private static void assertBlockstateModelsResolve(Path blockstate, Path modelRoot) {
        try {
            Matcher matcher = BLOCKSTATE_MODEL.matcher(Files.readString(blockstate, StandardCharsets.UTF_8));
            while (matcher.find()) {
                String model = matcher.group(1);
                assertFalse(model.startsWith("block/"),
                        blockstate + " uses a 1.20-style blockstate model path that resolves to models/block/block/* in 1.12: " + model);
                assertTrue(Files.exists(modelRoot.resolve(model + ".json")),
                        blockstate + " references missing 1.12 block model " + model);
            }
        } catch (IOException e) {
            throw new AssertionError("Could not inspect blockstate " + blockstate, e);
        }
    }
}
