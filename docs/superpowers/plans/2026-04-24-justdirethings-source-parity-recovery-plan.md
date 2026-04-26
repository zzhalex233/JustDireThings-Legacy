# JustDireThings Source Parity Recovery Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bring JustDireThings-Legacy back onto a faithful 1.12.2 source-parity track. The original source in `source/JustDireThings-main` is the contract; placeholders, approximate GUIs, missing buttons, missing gameplay systems, and registry-name drift are defects, not acceptable simplifications.

**Global Source-Parity Rule:** Every upstream feature is blocking until its gameplay-visible traits are translated: registry identity, acquisition path, recipes/data, server behavior, client sync, GUI/buttons/tooltips, localization, models/textures, renderers/shaders, sounds, NBT persistence, optional integration behavior, and edge cases. Approximate shells, static stand-ins for dynamic renderers, missing buttons, missing sync, missing velocity/state inheritance, or "works enough" behavior are defects unless the project owner explicitly accepts that exact scoped omission.

**Current Portal Example:** Portal gun and portal entity work follows the global rule. Projectile collision/placement, portal linking and cleanup, teleport cooldowns, pre-teleport velocity inheritance, cross-dimensional motion sync, open/close sounds, chunk keeping, advanced portal data, and the shader/procedural portal surface renderer must all match upstream expectations.

**Architecture:** Clean the misleading placeholder foundation first, then build a parity audit gate and implement in dependency order: registry/resource identity, broken model and GUI foundations, machine framework, goo/gel gameplay, tools/abilities, entities, recipes/data, integrations, and verification. Modern-only APIs must be translated through explicit 1.12 adapters rather than silently dropping behavior.

**Tech Stack:** Java 8, Minecraft Forge/Cleanroom 1.12.2, Gradle, JUnit, Forge registries, `SimpleNetworkWrapper`, Forge capabilities, Forge fluids, JEI, Patchouli, optional FutureMC, optional BubblesEX/Baubles compatibility.

---

## Supersedes

This plan supersedes the broad master plan at `docs/superpowers/plans/2026-04-21-justdirethings-legacy-backport-master-plan.md` for all parity-critical work. The old plan is useful as historical context, but this plan is stricter: every original registration, screen, button, recipe system, model quirk, and gameplay subsystem must be accounted for.

## Current Audit Findings

- Original Java source contains about 526 files under `source/JustDireThings-main/src/main/java/com/direwolf20/justdirethings`.
- Current 1.12 implementation contains about 189 Java files under `src/main/java/com/zzhalex/justdirethings`.
- Original assets under `source/JustDireThings-main/src/main/resources/assets/justdirethings` contain about 888 files; current assets contain about 903 files, so the main gap is not only missing PNGs/models, but missing Java systems and wrong 1.12 rendering usage.
- Original core packages include `blockentities`, `blocks`, `capabilities`, `containers`, `entities`, `events`, `fluids`, `items`, `network`, `recipes`, and full client screens/renderers.
- Current implementation has a much smaller `block`, `container`, `tile`, `item`, `fluid`, `portal`, `paradox`, `recipe`, and `util` set, with many original systems stubbed or absent.
- Original GUI screens include T1/T2-specific machine screens, `BaseScreen`, `BaseMachineScreen`, button/widget classes, `ToolSettingScreen`, `MachineSettingsCopierScreen`, portal menus, canister screens, `ParadoxMachineScreen`, `InventoryHolderScreen`, and `ExperienceHolderScreen`.
- Current GUI screens are simplified and mostly use `GuiMachineBase`; they lack the original button framework, localized text, top-section logic, per-machine button placement, and manual handling for the original nine-slice background.
- Original `background.png.mcmeta` declares modern GUI nine-slice metadata. Minecraft 1.12 does not consume this metadata, so stretching `background.png` directly causes jagged top/bottom artifacts.
- `BlockItemCollector` currently behaves like a full machine cube. This reproduces the same visual class of bug as the old `raw_*` model blocks: a custom model is rendered with an extra transparent 1x1x1 shell/shape.
- `GuiUpgradeStation` still uses old template GUI texture/logic and is not acting as the 1.12 replacement for the original smithing/upgrade flow.
- `zh_cn.lang` is incomplete/corrupted compared with original `zh_cn.json`, and GUI strings are not consistently localized.
- Original machine IDs include `itemcollector`, `blockbreakert1`, `blockbreakert2`, `blockplacert1`, `blockplacert2`, `clickert1`, `clickert2`, `sensort1`, `sensort2`, `droppert1`, `droppert2`, `blockswappert1`, `blockswappert2`, `fluidplacert1`, `fluidplacert2`, `fluidcollectort1`, `fluidcollectort2`, `generatort1`, `generatorfluidt1`, `energytransmitter`, `playeraccessor`, `experienceholder`, `inventory_holder`, and `paradoxmachine`.
- Current machine IDs use simplified or drifted names such as `item_collector`, `generator`, `fluid_generator`, `energy_transmitter`, `player_accessor`, and single non-tiered machine blocks. These must be reconciled with original names.
- Original content includes goo/gel blocks, goo soils, goo spread recipes, fluid drop recipes, advanced machines, tool abilities, machine settings copier, portal guns, creature catcher, canisters, armor/tool upgrade systems, entities, JEI categories, and Patchouli documentation. Many of these are absent or only skeletal.

## Ground Rules For Implementation

- [ ] Treat `source/JustDireThings-main` as the feature contract.
- [ ] Apply source parity to every feature, not only portals or high-risk systems.
- [ ] Do not introduce "temporary approximate" gameplay without adding an explicit failing parity test or checklist item.
- [ ] Do not mark a feature complete until behavior, resources, GUI/network, localization, data/recipes, rendering, and persistence are accounted for or explicitly accepted as omitted by the project owner.
- [ ] Prefer original registry IDs where possible. If a current WIP world has drifted IDs, add temporary aliases/migration notes instead of making drifted IDs the final API.
- [ ] Preserve original GUI layout intent and assets. In 1.12, reimplement the rendering mechanics; do not redesign the GUI.
- [ ] Every screen-visible string must use localization.
- [ ] Every model-only or non-full block must explicitly opt out of opaque/full-cube behavior in 1.12.
- [ ] Every optional integration must be isolated behind runtime detection. FutureMC, BubblesEX, and Baubles may be absent.
- [ ] Biome/mob worldgen content remains intentionally ignored unless a later requirement changes that decision.

---

## Phase 0: Clean Placeholder Foundation Before Any New Content

This phase comes before recipes/data work. Its purpose is to stop the port from treating "registered by name" as "implemented." After this phase, shells may still exist temporarily for compile safety, but they must be explicit, tested, listed in the parity matrix, and impossible to mistake for completed systems.

### Chunk 0.1: Mark every silent shell as a blocking parity defect

**Files:**
- Modify: `docs/audit/justdirethings-source-parity-matrix.md`
- Modify: `docs/audit/historical-legacy-issues.md`
- Create or modify: `src/test/java/com/zzhalex/justdirethings/audit/NoSilentPlaceholderTest.java`
- Inspect: `src/main/java/com/zzhalex/justdirethings/registry/ModItems.java`
- Inspect: `src/main/java/com/zzhalex/justdirethings/common/item/ability/AbilityMethods.java`
- Inspect: `src/main/java/com/zzhalex/justdirethings/client/gui/upstream`
- Inspect: `src/main/java/com/zzhalex/justdirethings/common/tile/machine`
- Inspect: `src/main/java/com/zzhalex/justdirethings/common/entity`

- [x] List every `ItemSimpleContent` special item as `stub`, not `partial`.
- [x] List every `AbilityMethods::notYetImplemented` ability as `stub`.
- [x] List empty upstream GUI anchors such as `ToolSettingScreen`, `MachineSettingsCopierScreen`, `AdvPortalRadialMenu`, and `AdvPortalEditMenu` as `stub`.
- [x] List every empty T2 tile subclass as `stub`, especially advanced machines that should add energy, area, and filter behavior.
- [x] List empty placeholder entities such as creature catcher, JustDire arrow, decoy, and area effect cloud as `stub`.
- [x] List placeholder/custom recipe type IDs separately from implemented recipe loading.
- [x] Add `NoSilentPlaceholderTest` that fails when a parity-critical class is empty or delegates to a known placeholder method without an allowlist entry.

Run:

```powershell
.\gradlew.bat test --tests "com.zzhalex.justdirethings.audit.NoSilentPlaceholderTest"
```

Expected: FAIL until the plan's later phases replace or explicitly allow each shell.

### Chunk 0.2: Replace misleading "done-looking" wrappers with explicit work items

**Files:**
- Modify: `src/main/java/com/zzhalex/justdirethings/client/gui/upstream/*.java`
- Modify: `src/main/java/com/zzhalex/justdirethings/common/item/ItemSimpleContent.java`
- Modify: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/*.java`
- Modify: `docs/audit/justdirethings-source-parity-matrix.md`

- [x] For empty GUI wrapper classes, either replace them with real screens in the relevant GUI phase or add a clear source comment: `PARITY STUB: class-name anchor only; original screen logic not ported`.
- [x] Move special items off generic `ItemSimpleContent` and onto explicit `ItemParityStub` anchors so future workers know the item is not behavior-complete.
- [x] For empty T2 tile subclasses, add explicit TODO/audit markers naming the original source class that must be ported.
- [x] For placeholder entities, add explicit TODO/audit markers naming the original entity class and missing behavior.
- [x] Do not remove registry anchors required for compile/resource parity unless the replacement implementation lands in the same task.

Run:

```powershell
.\gradlew.bat test --tests "com.zzhalex.justdirethings.audit.*"
.\gradlew.bat classes
```

Expected: existing classes still compile, but audit output clearly distinguishes real implementations from anchors.

### Chunk 0.3: Delete or quarantine obsolete template-era foundation

**Files:**
- Inspect: `src/main/java/com/example`
- Inspect: `src/main/java-templates`
- Inspect: `src/main/resource-templates`
- Inspect: `src/main/resources/modid_at.cfg`
- Inspect: `src/main/java/com/zzhalex/justdirethings/client/gui`
- Inspect: `src/main/resources/assets/justdirethings/textures/gui`

- [x] Confirm old template packages and resource templates are deleted or intentionally absent.
- [x] Confirm no code path still references `com.example`, `modid`, old template GUI IDs, or old template textures.
- [x] Confirm `GuiUpgradeStation` is tracked as a parity defect until it becomes the original upgrade/smithing bridge.
- [x] Confirm old approximate machine GUI layouts are tracked as parity defects when they differ from original source screens.
- [x] Confirm no placeholder asset is hiding a missing original model/texture.

Run:

```powershell
rg -n "com\\.example|modid|ExampleMod|template|TODO|notYetImplemented|PARITY STUB" src/main docs
.\gradlew.bat classes
```

Expected: only intentional audit markers remain; no template-era package/resource references remain in active code.

### Chunk 0.4: Define the execution rule for future content

**Files:**
- Modify: `docs/audit/justdirethings-source-parity-matrix.md`
- Modify: `docs/superpowers/plans/2026-04-24-justdirethings-source-parity-recovery-plan.md`

- [x] Add a matrix rule: no feature may move to `partial` unless at least one behavior path exists and is testable in-game or by automated test.
- [x] Add a matrix rule: no feature may move to `ported` unless registry, resource, behavior, localization, GUI/network where applicable, and acquisition path are complete.
- [x] Add a matrix rule: class-name anchors count as `stub`, not `ported`.
- [x] Add a matrix rule: copied textures/models count as `resource-only`, not `ported`.

Checkpoint:

- [x] Every known fake foundation item is visible in audit output.
- [x] No worker can accidentally treat empty wrappers, empty T2 classes, `ItemParityStub` special items, or dummy recipe IDs as completed parity.
- [ ] Commit message: `Expose placeholder foundation before parity work`

---

## Phase 0.5: Build The Source-Parity Gate

### Chunk 0.1: Add a machine-readable parity catalog

- [x] Create `src/test/java/com/zzhalex/justdirethings/audit/SourceParityCatalog.java`.
- [x] Record expected original registry IDs from `source/JustDireThings-main/src/main/java/com/direwolf20/justdirethings/setup/Registration.java`.
- [x] Include categories for blocks, items, fluids, entities, sounds, containers, recipe serializers/types, capabilities, screens, and network packets.
- [x] Keep comments next to each category pointing to the original source class and rough line area.
- [x] Do not include ignored biome/mob worldgen entries unless they affect item/block recipes.

Expected first test result: fail, because the current port is missing T1/T2 machines, classic portal gun, machine settings copier, goo systems, several entities, many screens, and recipe systems.

### Chunk 0.2: Add current registry snapshot tests

- [x] Create `src/test/java/com/zzhalex/justdirethings/audit/RegistryParityTest.java`.
- [x] Assert all original block IDs that should exist in 1.12 are registered or intentionally mapped.
- [x] Assert all original item IDs that should exist in 1.12 are registered or intentionally mapped.
- [x] Assert all original fluid IDs are registered with fluid, block, and bucket coverage.
- [x] Assert all machine/container IDs have a matching block, tile entity, container, GUI handler entry, screen implementation, or explicit parity-matrix gap entry.
- [x] Assert all original GUI localization keys used by source screens exist in both `en_us.lang` and `zh_cn.lang`.
- [x] Add an allowlist file for deliberate omissions. Initially allow only biome/mob worldgen, Mekanism integration, and direct Curios API usage.

Run:

```powershell
.\gradlew.bat test --tests "com.zzhalex.justdirethings.audit.RegistryParityTest"
```

### Chunk 0.3: Generate a human parity matrix

- [x] Create `docs/audit/justdirethings-source-parity-matrix.md`.
- [x] Add sections for blocks, items, fluids, machines, GUIs, entities, recipes, integrations, resources, localization, and network packets.
- [x] For each original feature, record status: `missing`, `stub`, `resource-only`, `wrong-id`, `visual-bug`, `partial`, or `ported`.
- [x] Link each row to original source path and current 1.12 path where available; keep unresolved package-level gaps named until detailed rows are added.
- [x] Update this matrix at the end of every later phase.

Checkpoint:

- [x] `RegistryParityTest` exists and requires known missing systems to be implemented or explicitly documented.
- [x] `justdirethings-source-parity-matrix.md` lists at least all original registrations and package-level container/network/capability gaps.
- [ ] Commit message: `Add JustDireThings source parity audit gate`.

---

## Phase 1: Fix Current Visual Blockers Before Adding More Content

### Chunk 1.1: Fix Item Collector extra transparent shell

Root cause: current `BlockItemCollector` extends a full-cube machine block path. Original `ItemCollector.java` defines directional shapes and non-occluding behavior.

- [ ] Update `src/main/java/com/zzhalex/justdirethings/block/BlockItemCollector.java`.
- [ ] Port original directional shape bounds to 1.12 `AxisAlignedBB` arrays for all six facings.
- [ ] Override `getBoundingBox`, `getCollisionBoundingBox`, `isFullCube`, `isOpaqueCube`, `isFullBlock`, `causesSuffocation`, and `getBlockFaceShape`.
- [ ] Ensure render layer is cutout/cutout-mipped where needed, matching the raw model-block fix pattern.
- [ ] Verify blockstate rotations match original `itemcollector.json` orientation.
- [ ] Decide final registry/model ID: original `itemcollector` is preferred. If keeping temporary `item_collector`, document and add an alias migration step.
- [ ] Add `ItemCollectorShapeTest` to assert it is not full cube and not opaque.

Run:

```powershell
.\gradlew.bat test --tests "com.zzhalex.justdirethings.block.ItemCollectorShapeTest"
.\gradlew.bat classes
```

Manual check:

- [ ] Place item collector on all six faces.
- [ ] Confirm no 1x1x1 transparent shell is visible.
- [ ] Confirm item collector model rotates exactly like the original high-version model.

### Chunk 1.2: Implement 1.12 GUI nine-slice rendering

Root cause: original GUI background relies on modern `.mcmeta` GUI scaling. 1.12 ignores that and current code stretches the whole texture.

- [ ] Add `src/main/java/com/zzhalex/justdirethings/client/gui/GuiNineSlice.java`.
- [ ] Render the original `textures/gui/background.png` as a manual nine-slice using border size 8 and source size 236x34.
- [ ] Support arbitrary panel heights without scaling the top and bottom borders.
- [ ] Replace direct `drawScaledCustomSizeModalRect` background usage in `GuiMachineBase`.
- [ ] Use the same renderer in `GuiUpgradeStation`, canister screens, and future base screens.
- [ ] Add a lightweight unit test for nine-slice segment math.

Manual check:

- [ ] Open every existing machine GUI.
- [ ] Confirm top and bottom borders are not jagged.
- [ ] Confirm background corners remain crisp.

### Chunk 1.3: Replace hardcoded GUI text with localization

- [ ] Audit `src/main/java/com/zzhalex/justdirethings/client/gui`.
- [ ] Replace hardcoded labels like Energy, Fluid, Area, Upgrade Station, Fuel, Potion, and machine names with `I18n.format(...)`.
- [ ] Add missing keys to `src/main/resources/assets/justdirethings/lang/en_us.lang`.
- [ ] Rebuild `src/main/resources/assets/justdirethings/lang/zh_cn.lang` from original `source/JustDireThings-main/src/main/resources/assets/justdirethings/lang/zh_cn.json`.
- [ ] Preserve valid 1.12 `.lang` encoding and avoid mojibake.
- [ ] Add `LocalizationParityTest` for GUI keys.

Run:

```powershell
.\gradlew.bat test --tests "com.zzhalex.justdirethings.audit.LocalizationParityTest"
```

### Chunk 1.4: Rebuild Upgrade Station as the 1.12 smithing/upgrade bridge

Root cause: original mod uses modern upgrade/smithing-like flows and custom recipes. Current upgrade station is visually and functionally a template stub.

- [ ] Inspect original upgrade-related classes, recipe serializers, smithing/upgrade datagen, and GUI flow.
- [ ] Replace `GuiUpgradeStation` old template texture path with the shared JDT GUI panel and original slot/button layout.
- [ ] Implement server-side `ContainerUpgradeStation` validation for upgrade template, target item, ingredients, energy/fluid requirements if present, and result.
- [ ] Implement `TileUpgradeStation` processing, progress sync, and result extraction.
- [ ] Wire recipe lookup to the ported `AbilityRecipe` and `PaxelRecipe` systems.
- [ ] Add localized title, slot labels, tooltip text, and error/missing-requirement text.
- [ ] Add tests for at least one tier upgrade, one ability install, and one paxel fusion recipe.

Run:

```powershell
.\gradlew.bat test --tests "com.zzhalex.justdirethings.recipe.UpgradeStationRecipeTest"
.\gradlew.bat classes
```

Checkpoint:

- [ ] Item collector visual shell fixed.
- [ ] Existing machine GUIs no longer have jagged top/bottom.
- [ ] GUI text uses localization.
- [ ] Upgrade Station is no longer the old template GUI/stub.
- [ ] Commit message: `Fix core model and GUI rendering blockers`.

---

## Phase 2: Restore Original Registry Identity And Resource Wiring

### Chunk 2.1: Normalize registry IDs to original mod IDs

- [ ] Compare current `ModBlocks`, `ModItems`, `ModContainers`, `ModTileEntities`, `ModFluids`, and `ModEntities` against original `Registration.java`.
- [ ] Rename final registry IDs to original names where feasible:
- [ ] `itemcollector`
- [ ] `generatort1`
- [ ] `generatorfluidt1`
- [ ] `energytransmitter`
- [ ] `experienceholder`
- [ ] `playeraccessor`
- [ ] `blockbreakert1`, `blockbreakert2`
- [ ] `blockplacert1`, `blockplacert2`
- [ ] `clickert1`, `clickert2`
- [ ] `sensort1`, `sensort2`
- [ ] `droppert1`, `droppert2`
- [ ] `blockswappert1`, `blockswappert2`
- [ ] `fluidplacert1`, `fluidplacert2`
- [ ] `fluidcollectort1`, `fluidcollectort2`
- [ ] `paradoxmachine`
- [ ] Add temporary compatibility aliases only for WIP names already used in local test worlds.
- [ ] Update blockstate/model/item model file names to match registry IDs or add explicit model loader mapping.
- [ ] Update language keys to original key names.

Expected outcome: registry names match original JustDireThings wherever 1.12 permits.

### Chunk 2.2: Restore missing block registrations

- [ ] Add/verify all original raw/resource blocks.
- [ ] Add/verify all `gooblock_tier1` through `gooblock_tier4`.
- [ ] Add/verify `goopatternblock`.
- [ ] Add/verify `goosoil_tier1` through `goosoil_tier4`.
- [ ] Add/verify `eclipsegateblock`.
- [ ] Add all T1/T2 machine blocks listed in Phase 2.1.
- [ ] Add block item registrations for every original block item.
- [ ] Add test coverage that every registered block has an item model and blockstate.

### Chunk 2.3: Restore missing item registrations

- [ ] Add classic `portalgun` alongside `portalgun_v2`.
- [ ] Add `machinesettingscopier`.
- [ ] Add `creaturecatcher`.
- [ ] Add `totem_of_death_recall`.
- [ ] Add `blazejet_wand`, `voidshift_wand`, and `eclipsegate_wand`.
- [ ] Verify `time_wand`, `polymorphic_wand`, and `polymorphic_wand_v2` are behavior-complete, not only registered.
- [ ] Add/verify `fuel_canister`, `fluid_canister`, `pocket_generator`, and `potion_canister`.
- [ ] Add/verify all bows, tools, armor, smithing templates, upgrade templates, and upgrade items from original registration.
- [ ] Add tests that every original item has a model, language key, and creative tab placement.

### Chunk 2.4: Restore creative tabs

- [ ] Match original creative tab grouping, ordering, and icon strategy.
- [ ] Ensure all blocks/items appear in the expected JDT creative tabs.
- [ ] Add an audit test for missing creative tab assignments.
- [ ] Manual check in `runClient`: creative tab shows all original blocks/items, not only the first few ported items.

Checkpoint:

- [ ] `RegistryParityTest` passes for block and item IDs or reports only planned later behavior gaps.
- [ ] Creative tabs are populated with all original content.
- [ ] Missing-model log is clean for registered blocks/items.
- [ ] Commit message: `Restore JustDireThings registry identity`.

---

## Phase 3: Port The Original GUI Framework

### Chunk 3.1: Port base screen architecture

- [ ] Port original `client/screens/basescreens/BaseScreen.java` to 1.12 `GuiContainer`/`GuiScreen` APIs.
- [ ] Port original `BaseMachineScreen.java` behavior: top section, slot drawing, power/fluid bars, fake slot rendering, tooltips, and button initialization.
- [ ] Keep original layout constants for panel width, border, slot texture, power bar, fluid bar, and social background.
- [ ] Translate modern `GuiGraphics`/pose stack calls to 1.12 `drawTexturedModalRect`, `GlStateManager`, and `FontRenderer` calls.
- [ ] Ensure screen scaling works at GUI scale 1, 2, 3, and Auto.

### Chunk 3.2: Port button and widget system

- [ ] Port `BaseButton`.
- [ ] Port `ToggleButton`.
- [ ] Port `GrayscaleButton`.
- [ ] Port `NumberButton`.
- [ ] Port `BlockStateScrollList`.
- [ ] Port `ValueButtons` and `ValueButtonsDouble`.
- [ ] Port `ToggleButtonFactory`.
- [ ] Use original `textures/gui/buttons/*.png` assets already present in resources.
- [ ] Wire every button to localized tooltip keys.
- [ ] Implement packet sends for every button action through 1.12 `SimpleNetworkWrapper`.

Buttons that must exist:

- [ ] Redstone ignore/low/high/pulse.
- [ ] Allowlist/blocklist.
- [ ] Item filter and block filter mode.
- [ ] Pull/push item and fluid toggles.
- [ ] Add/remove mode.
- [ ] Direction buttons for all six sides.
- [ ] Area adjustment and preview buttons.
- [ ] Hammer size buttons.
- [ ] Tick speed button.
- [ ] Target experience button.
- [ ] Pickup delay button.
- [ ] Click mode buttons.
- [ ] Target type buttons.
- [ ] Sneak-click button.
- [ ] Show fake player button.
- [ ] Player/mind fog/jump boost/owner-related buttons where original screens use them.

### Chunk 3.3: Restore every original screen mapping

- [ ] Match original `ClientSetup.registerScreens`.
- [ ] Register `FuelCanisterScreen`.
- [ ] Register `PocketGeneratorScreen`.
- [ ] Register `ToolSettingScreen`.
- [ ] Register `ItemCollectorScreen`.
- [ ] Register `BlockBreakerT1Screen`.
- [ ] Register `BlockBreakerT2Screen`.
- [ ] Register `BlockPlacerT1Screen`.
- [ ] Register `BlockPlacerT2Screen`.
- [ ] Register `ClickerT1Screen`.
- [ ] Register `ClickerT2Screen`.
- [ ] Register `SensorT1Screen`.
- [ ] Register `SensorT2Screen`.
- [ ] Register `DropperT1Screen`.
- [ ] Register `DropperT2Screen`.
- [ ] Register `GeneratorT1Screen`.
- [ ] Register `GeneratorFluidT1Screen`.
- [ ] Register `EnergyTransmitterScreen`.
- [ ] Register `BlockSwapperT1Screen`.
- [ ] Register `BlockSwapperT2Screen`.
- [ ] Register `PlayerAccessorScreen`.
- [ ] Register `FluidPlacerT1Screen`.
- [ ] Register `FluidPlacerT2Screen`.
- [ ] Register `FluidCollectorT1Screen`.
- [ ] Register `FluidCollectorT2Screen`.
- [ ] Register `PotionCanisterScreen`.
- [ ] Register `ParadoxMachineScreen`.
- [ ] Register `InventoryHolderScreen`.
- [ ] Register `ExperienceHolderScreen`.
- [ ] Register `MachineSettingsCopierScreen`.
- [ ] Register `AdvPortalRadialMenu` and `AdvPortalEditMenu` equivalents.

### Chunk 3.4: GUI parity verification

- [ ] Add a screenshot checklist under `docs/audit/gui-parity-checklist.md`.
- [ ] For each GUI, record original source screen class, current screen class, texture assets, buttons, localized labels, and packet actions.
- [ ] Run `runClient` and open every GUI.
- [ ] Confirm no GUI uses the old template texture unless the original source does.
- [ ] Confirm all expected buttons are present.
- [ ] Confirm every visible string has a Chinese translation.

Checkpoint:

- [ ] GUI base framework is source-derived.
- [ ] Upgrade Station, machine GUIs, canister GUIs, portal menus, and tool settings no longer use placeholder layouts.
- [ ] Commit message: `Port original JustDireThings GUI framework`.

---

## Phase 4: Restore Machine Framework And Advanced Machines

### Chunk 4.1: Port shared machine state and capabilities

- [ ] Port/translate original machine block entity base classes.
- [ ] Implement owner tracking, security, fake player identity, NBT save/load, and drops preserving settings.
- [ ] Implement area configuration and preview data.
- [ ] Implement redstone control modes.
- [ ] Implement filter inventory and filter matching.
- [ ] Implement tick speed settings.
- [ ] Implement Forge item handler, fluid handler, and energy capability exposure per side.
- [ ] Implement machine settings copy/import/export.
- [ ] Implement all machine sync packets required by GUI buttons.

### Chunk 4.2: Port T1/T2 item/block/fluid automation machines

- [ ] Port `ItemCollector` behavior, pickup delay, area filtering, and rendering.
- [ ] Port `BlockBreakerT1` and `BlockBreakerT2`.
- [ ] Port `BlockPlacerT1` and `BlockPlacerT2`.
- [ ] Port `ClickerT1` and `ClickerT2`, including fake-player click modes.
- [ ] Port `SensorT1` and `SensorT2`, including target modes and redstone output.
- [ ] Port `DropperT1` and `DropperT2`.
- [ ] Port `BlockSwapperT1` and `BlockSwapperT2`.
- [ ] Port `FluidPlacerT1` and `FluidPlacerT2`.
- [ ] Port `FluidCollectorT1` and `FluidCollectorT2`.
- [ ] Port all per-machine recipes, inventory layouts, energy/fluid costs, and upgrades.
- [ ] Use ModularRouters 1.12 code only as a translation reference for world interaction, fake player safety, fluid placement, and side effects.

### Chunk 4.3: Port storage/generator/transmitter machines

- [ ] Port `InventoryHolder` inventory behavior and GUI.
- [ ] Port `ExperienceHolder` XP storage, insertion/extraction, and GUI.
- [ ] Port `EnergyTransmitter` range/linking/transfer behavior and renderer.
- [ ] Port `GeneratorT1` fuel burn behavior and GUI.
- [ ] Port `GeneratorFluidT1` fluid fuel behavior and GUI.
- [ ] Port `PlayerAccessor` inventory/player access behavior and GUI.
- [ ] Port `ParadoxMachine` behavior, energy/fluid requirements, GUI, ambient sound, and renderer.

### Chunk 4.4: Machine behavior tests

- [ ] Add fake-world or integration tests for block breaking, block placing, clicking, dropping, swapping, fluid placing, and fluid collecting.
- [ ] Add tests for redstone modes.
- [ ] Add tests for filters.
- [ ] Add tests for area size/range boundaries.
- [ ] Add tests for NBT save/load of machine settings.
- [ ] Add tests for machine settings copier import/export.

Checkpoint:

- [ ] All original machines are registered, placeable, have GUIs, have buttons, and perform their core behaviors.
- [ ] Commit message: `Port JustDireThings machine framework and T1 T2 machines`.

---

## Phase 5: Restore Goo/Gel Gameplay

### Chunk 5.1: Port goo blocks and block entities

- [ ] Port `GooBlock_Tier1`.
- [ ] Port `GooBlock_Tier2`.
- [ ] Port `GooBlock_Tier3`.
- [ ] Port `GooBlock_Tier4`.
- [ ] Port corresponding goo block entities.
- [ ] Port `GooPatternBlock`.
- [ ] Port goo rendering, particles, and blockstate/model behavior.
- [ ] Ensure all goo blocks are non-buggy in 1.12 lighting, collision, and opacity.

### Chunk 5.2: Port goo soil and eclipse gate gameplay

- [ ] Port `GooSoil_Tier1` through `GooSoil_Tier4`.
- [ ] Port goo soil block entities.
- [ ] Port `EclipseGateBlock` and `EclipseGateBE`.
- [ ] Implement interaction with `eclipsegate_wand`.
- [ ] Implement recipe/resource dependencies for goo soil progression.

### Chunk 5.3: Port goo spread and fluid drop recipes

- [ ] Implement 1.12 custom recipe type/manager equivalent for `goospreadrecipe`.
- [ ] Implement tag-aware fallback for `goospreadrecipe_tag` using OreDictionary where appropriate.
- [ ] Implement `fluiddroprecipe`.
- [ ] Port recipe data generated by original datagen into 1.12-compatible JSON/custom loading.
- [ ] Add JEI category for goo spread.
- [ ] Add JEI category for fluid drop.

Checkpoint:

- [ ] Goo/gel progression is playable and recipe-driven.
- [ ] Goo blocks are not just decorative registered blocks.
- [ ] Commit message: `Port goo progression gameplay`.

---

## Phase 6: Restore Tools, Armor, Abilities, And Charged Items

### Chunk 6.1: Port ability data model

- [ ] Port original ability enums/classes, ability parameters, cooldowns, and upgrade requirements.
- [ ] Translate data components/modern item data into 1.12 NBT.
- [ ] Implement per-item installed abilities and tier restrictions.
- [ ] Implement localized tooltips for installed abilities, energy, fluid, cooldown, range, and mode.
- [ ] Add tests for NBT serialization and ability install/remove behavior.

### Chunk 6.2: Port charged item infrastructure

- [ ] Implement Forge energy capability on every item that needs charge.
- [ ] Implement fluid/potion storage capability on canisters.
- [ ] Implement pocket generator energy generation and transfer.
- [ ] Implement fuel canister, fluid canister, and potion canister GUI sync.
- [ ] Verify items visually show charge/fluid/potion state where original does.

### Chunk 6.3: Port tools, bows, and armor behavior

- [ ] Port Ferricore, Blazegold, Celestigem, and Eclipse Alloy tool classes.
- [ ] Port paxel behavior and recipes.
- [ ] Port bows and custom arrow/entity behavior.
- [ ] Port armor abilities.
- [ ] Port tool/armor ability activation through keybinds and GUI settings.
- [ ] Implement `ToolSettingScreen` exactly enough to configure all original modes.
- [ ] Add BubblesEX/Baubles adapter for trinket/curio-like behavior that the original Curios integration provided.

### Chunk 6.4: Port special items

- [ ] Port `ferricore_wrench`.
- [ ] Port `totem_of_death_recall`.
- [ ] Port `blazejet_wand`.
- [ ] Port `voidshift_wand`.
- [ ] Port `eclipsegate_wand`.
- [ ] Port `time_wand`, including visible multiplier/status marker.
- [ ] Port `creaturecatcher`.
- [ ] Port `machinesettingscopier`.
- [ ] Port `portalgun`.
- [ ] Port `portalgun_v2`.
- [ ] Port `polymorphic_wand`.
- [ ] Port `polymorphic_wand_v2`.

Checkpoint:

- [ ] Every original charged item actually stores/uses charge.
- [ ] Every original special item has behavior, model, localization, and recipes.
- [ ] Commit message: `Port tools armor abilities and charged items`.

---

## Phase 7: Restore Fluids, Fuel, And Fluid Rendering

### Chunk 7.1: Verify all original fluids

- [ ] Verify polymorphic fluid.
- [ ] Verify portal fluid.
- [ ] Verify time fluid.
- [ ] Verify unstable portal fluid.
- [ ] Verify unrefined T2/T3/T4 fuel.
- [ ] Verify refined T2/T3/T4 fuel.
- [ ] Verify XP fluid.
- [ ] Ensure every fluid has source/flowing texture, still/flowing registration, bucket, block, language key, and model.

### Chunk 7.2: Port fluid behaviors

- [ ] Port fuel refining and generator consumption behavior.
- [ ] Port fluid interactions used by machines and recipes.
- [ ] Port XP fluid conversion behavior.
- [ ] Port portal/time/polymorphic fluid special effects if present in original source.
- [ ] Add JEI fluid usage and production entries.

Checkpoint:

- [ ] All fluids render in-world, in buckets, in tanks, and in JEI without missing textures.
- [ ] Commit message: `Port JustDireThings fluid systems`.

---

## Phase 8: Restore Entities, Portal, Paradox, And Time Systems

### Chunk 8.1: Port missing entities

- [ ] Port `creature_catcher` entity.
- [ ] Port `justdirearrow`.
- [ ] Port `portal_projectile`.
- [ ] Port `portal_entity`.
- [ ] Port `decoy_entity`.
- [ ] Port `justdireareaeffectcloud`.
- [ ] Port `time_wand_entity`.
- [ ] Port `paradox_entity`.
- [ ] Register renderers, sounds, spawn data, network sync, and NBT for each entity.

Progress note 2026-04-25:

- Decoy is no longer an empty anchor: owner UUID, lifetime, periodic aggro, invulnerability, NBT, renderer, localization, and a basic summon ability hook are present.
- JustDireArrow is no longer an empty anchor: it extends the 1.12 tipped arrow, syncs/saves potion/splash/lingering/homing/phase/epic/hostile state, and has basic phase and homing behavior.
- JustDireAreaEffectCloud is no longer an empty anchor: it extends the 1.12 area-effect cloud and uses the vanilla cloud renderer/behavior as the compatibility base.
- Remaining parity debt: exact armor ability params/energy use, online decoy skin lookup, JustDireArrow splash/lingering owner filtering, epic multi-target pierce parity, and complete bow/ability launch integration.

### Chunk 8.2: Port portal systems

- [ ] Port classic portal gun behavior.
- [ ] Port portal gun v2 behavior.
- [ ] Port portal projectile collision and placement.
- [ ] Port portal entity linking and cleanup.
- [ ] Port original portal velocity sampling: capture entity positions in the portal's forward velocity box before collision, transform the sampled velocity through the linked portal, and sync the resulting motion to clients.
- [ ] Port original portal renderer semantics: animated shader/procedural surface plus flat colored frame, not a single static texture plane.
- [ ] Port exact floor, ceiling, and wall placement/bounding boxes from upstream `PortalEntity` and projectile collision rules, including ignoring non-colliding plants/grass.
- [ ] Port portal open/close sounds and lifecycle cleanup behavior.
- [ ] Port advanced portal radial menu.
- [ ] Port advanced portal edit menu.
- [ ] Port saved favorites/destinations using 1.12 world saved data or player NBT.

### Chunk 8.3: Port paradox and time systems

- [ ] Port paradox entity behavior and renderer.
- [ ] Port paradox machine interactions.
- [ ] Port time wand acceleration behavior.
- [ ] Add the missing time wand multiplier/status display.
- [ ] Verify server-side acceleration does not desync client display.

Checkpoint:

- [ ] Portal, paradox, and time systems are playable, not just registered.
- [ ] Commit message: `Port portal paradox and time entities`.

---

## Phase 9: Restore Recipes, JEI, Patchouli, And Data

### Chunk 9.1: Port recipe data from original datagen

- [ ] Audit original `JustDireRecipes.java` and related datagen providers.
- [ ] Generate or hand-port 1.12 recipes for every original item/block/fluid/machine.
- [ ] Implement custom 1.12 loaders for ability recipes.
- [ ] Implement custom 1.12 loaders for paxel recipes.
- [ ] Implement custom 1.12 loaders for goo spread recipes.
- [ ] Implement custom 1.12 loaders for fluid drop recipes.
- [ ] Add tests that every registered non-creative-only item has at least one intended acquisition path.

### Chunk 9.2: Port JEI integration

- [ ] Port JEI categories for ability recipes.
- [ ] Port JEI categories for paxel recipes.
- [ ] Port JEI categories for goo spread.
- [ ] Port JEI categories for fluid drop.
- [ ] Port JEI descriptions/usages for machines, canisters, fuels, and upgrade station.
- [ ] Ensure JEI works with FutureMC absent and present.

### Chunk 9.3: Port Patchouli book

- [ ] Move/translate original Patchouli assets and data into the correct 1.12 Patchouli layout.
- [ ] Preserve original book content where possible.
- [ ] Add Chinese book entries if original provides them.
- [ ] Add recipe for obtaining the book.
- [ ] Verify book opens in a 1.12 client with Patchouli installed.

Checkpoint:

- [ ] Recipes, JEI, and Patchouli explain and expose the restored content.
- [ ] Commit message: `Port recipes JEI and Patchouli docs`.

---

## Phase 10: Restore Client Renderers, Models, And Texture Parity

### Chunk 10.1: Audit every model/blockstate/texture path

- [ ] Compare original `assets/justdirethings/blockstates`.
- [ ] Compare original `assets/justdirethings/models/block`.
- [ ] Compare original `assets/justdirethings/models/item`.
- [ ] Compare original `assets/justdirethings/textures/block`.
- [ ] Compare original `assets/justdirethings/textures/item`.
- [ ] Compare original `assets/justdirethings/textures/gui`.
- [ ] Compare original `assets/justdirethings/textures/entity`.
- [ ] Fix every missing, renamed, or unreferenced asset.
- [ ] Add a log scan step for missing models/textures after `runClient`.

### Chunk 10.2: Port special renderers

- [ ] Port item collector renderer if original uses one beyond static model.
- [ ] Port energy transmitter renderer.
- [ ] Port experience holder renderer.
- [ ] Port inventory holder renderer.
- [ ] Port goo and goo soil renderers.
- [ ] Port paradox/eclipsegate renderers.
- [ ] Port entity renderers for portal, projectile, paradox, catcher, arrow, decoy, area effect cloud, and time wand entity.
- [ ] Replace modern renderer APIs with 1.12 TESR/entity renderer equivalents.

### Chunk 10.3: Verify transparent/model-only blocks

- [ ] Add tests/checklist entries for every non-full block: raw resource blocks, item collector, goo blocks if non-full, energy transmitter if non-full, portal/eclipsegate visual blocks, and any decorative custom model blocks.
- [ ] Confirm each one disables full-cube/opaque behavior where needed.
- [ ] Confirm each one has correct collision and selection boxes.

Checkpoint:

- [ ] No machine block texture is transparent/missing.
- [ ] No custom model block renders an unwanted full cube shell.
- [ ] No item uses a flat placeholder when original has a model.
- [ ] Commit message: `Restore client render and model parity`.

---

## Phase 11: Integrations And Optional Dependencies

### Chunk 11.1: FutureMC optional fallback

- [ ] Keep FutureMC optional.
- [ ] Use FutureMC materials/items when loaded.
- [ ] Use vanilla 1.12 fallback materials when absent, such as sticks/leaves replacing bamboo-like recipe inputs.
- [ ] Add recipe condition/loader support for optional FutureMC variants.
- [ ] Test both with and without FutureMC.

### Chunk 11.2: BubblesEX/Baubles replacement for Curios

- [ ] Do not port direct Curios dependency.
- [ ] Add an adapter layer for BubblesEX/Baubles.
- [ ] Detect BubblesEX/Baubles at runtime.
- [ ] Register compatible slots/effects only when present.
- [ ] Ensure ordinary item behavior still works when absent.
- [ ] Add manual test with a pack containing BubblesEX and Baubles.

### Chunk 11.3: Deliberate integration omissions

- [ ] Do not port Mekanism integration unless a later requirement adds real gameplay value.
- [ ] Document Mekanism omission in `docs/audit/justdirethings-source-parity-matrix.md`.
- [ ] Keep worldgen/biome/mob omissions documented separately from gameplay omissions.

Checkpoint:

- [ ] Optional dependency behavior is explicit and tested.
- [ ] Commit message: `Port optional dependency adapters`.

---

## Phase 12: Final Verification Gates

### Automated checks

- [ ] `.\gradlew.bat test`
- [ ] `.\gradlew.bat classes`
- [ ] `.\gradlew.bat runClient`
- [ ] Scan latest client log for missing models.
- [ ] Scan latest client log for missing textures.
- [ ] Scan latest client log for unlocalized names.
- [ ] Scan latest client log for registry remap warnings.

Suggested log scan:

```powershell
rg -n "missing model|missing texture|Unable to load model|FileNotFoundException|unlocalized|Exception loading model|FML.TEXTURE_ERRORS" run/client/logs/latest.log
```

### Manual in-game parity checklist

- [ ] Creative tabs contain every original item/block.
- [ ] Every machine places with correct model, texture, collision, and orientation.
- [ ] Every machine GUI matches original layout and has all expected buttons.
- [ ] Upgrade Station can perform tier upgrade, ability install, and paxel fusion flows.
- [ ] All charged items display and consume stored energy/fluid correctly.
- [ ] Goo/gel progression works through recipes and in-world spreading.
- [ ] Portal guns create, edit, and use portals.
- [ ] Time wand acceleration works and displays multiplier/status.
- [ ] Machine settings copier copies and applies settings.
- [ ] Creature catcher captures and releases entities.
- [ ] BubblesEX/Baubles integration works when present and does not crash when absent.
- [ ] FutureMC recipe/material variants work when present and fallback recipes work when absent.
- [ ] JEI shows custom recipe categories.
- [ ] Patchouli book opens and references restored systems.
- [ ] Chinese localization is readable and covers GUI, items, blocks, tooltips, JEI, and Patchouli where applicable.

### Completion criteria

- [ ] Source parity matrix has no `missing`, `stub`, `wrong-id`, or undocumented `partial` rows except explicitly accepted omissions.
- [ ] All planned tests pass.
- [ ] `runClient` has no missing model/texture errors for JDT assets.
- [ ] The user can play through original JDT progression in 1.12.2 without relying on placeholder systems.

Final commit message:

```text
Complete JustDireThings source parity backport foundation
```
