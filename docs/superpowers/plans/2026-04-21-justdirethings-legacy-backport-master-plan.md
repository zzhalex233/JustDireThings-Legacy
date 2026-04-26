# JustDireThings-Legacy Backport Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild `JustDireThings` for `Minecraft 1.12.2` with preserved core gameplay, optional `FutureMC` material support, and optional `BubblesEX/Baubles` accessory support.

**Current parity override:** This broad master plan is historical. The stricter source-parity recovery plan at `docs/superpowers/plans/2026-04-24-justdirethings-source-parity-recovery-plan.md` supersedes it for implementation details. Every upstream feature must match original behavior and presentation unless the project owner explicitly accepts a scoped omission. Portal entities are only the current example: exact placement/collision, velocity inheritance, lifecycle sounds, chunk keeping, and shader/procedural portal rendering are mandatory, not optional polish.

**Architecture:** This backport is implemented as a 1.12.2-native mod, not a mechanical code port. Build the shared 1.12 platform first, then migrate content and systems into stable abstractions for item state, machine state, networking, compatibility content lookup, and high-risk gameplay features.

**Tech Stack:** Java, Forge/Cleanroom 1.12.2, JUnit 5, JEI, Patchouli, optional FutureMC, optional BubblesEX/Baubles

---

## Scope Decision

The approved design spans several independent subsystems. The safest execution model would be separate plans per subsystem, but this master plan keeps everything in one document because the project owner explicitly requested a single total plan. To preserve sanity, the plan is chunked into independently reviewable workstreams.

## Planned File Structure

### Core bootstrap and config

- Modify: `build.gradle`
- Modify: `gradle/scripts/dependencies.gradle`
- Modify: `src/main/java/com/zzhalex/justdirethings/JustDireThingsLegacy.java`
- Modify: `src/main/java/com/zzhalex/justdirethings/Reference.java`
- Create: `src/main/java/com/zzhalex/justdirethings/CommonProxy.java`
- Create: `src/main/java/com/zzhalex/justdirethings/ClientProxy.java`
- Create: `src/main/java/com/zzhalex/justdirethings/config/JDTConfig.java`

### Registries and compatibility

- Create: `src/main/java/com/zzhalex/justdirethings/registry/ModBlocks.java`
- Create: `src/main/java/com/zzhalex/justdirethings/registry/ModItems.java`
- Create: `src/main/java/com/zzhalex/justdirethings/registry/ModFluids.java`
- Create: `src/main/java/com/zzhalex/justdirethings/registry/ModEntities.java`
- Create: `src/main/java/com/zzhalex/justdirethings/registry/ModTileEntities.java`
- Create: `src/main/java/com/zzhalex/justdirethings/registry/ModContainers.java`
- Create: `src/main/java/com/zzhalex/justdirethings/registry/ModSounds.java`
- Create: `src/main/java/com/zzhalex/justdirethings/registry/ModRecipes.java`
- Create: `src/main/java/com/zzhalex/justdirethings/compat/content/CompatContentKey.java`
- Create: `src/main/java/com/zzhalex/justdirethings/compat/content/CompatContentResolver.java`
- Create: `src/main/java/com/zzhalex/justdirethings/compat/content/FutureMcCompat.java`
- Test: `src/test/java/com/zzhalex/justdirethings/compat/content/CompatContentResolverTest.java`

### Shared data and capabilities

- Create: `src/main/java/com/zzhalex/justdirethings/data/JDTDataKeys.java`
- Create: `src/main/java/com/zzhalex/justdirethings/data/tool/ToolState.java`
- Create: `src/main/java/com/zzhalex/justdirethings/data/tool/ToolStateIO.java`
- Create: `src/main/java/com/zzhalex/justdirethings/data/tool/AbilityBinding.java`
- Create: `src/main/java/com/zzhalex/justdirethings/data/tool/AbilityCooldown.java`
- Create: `src/main/java/com/zzhalex/justdirethings/capability/ModCapabilities.java`
- Create: `src/main/java/com/zzhalex/justdirethings/capability/energy/ItemEnergyStorage.java`
- Create: `src/main/java/com/zzhalex/justdirethings/capability/fluid/ItemFluidTank.java`
- Create: `src/main/java/com/zzhalex/justdirethings/capability/inventory/InternalItemHandler.java`
- Test: `src/test/java/com/zzhalex/justdirethings/data/tool/ToolStateIOTest.java`

### Networking

- Create: `src/main/java/com/zzhalex/justdirethings/network/JDTNetwork.java`
- Create: `src/main/java/com/zzhalex/justdirethings/network/message/MessageSyncToolState.java`
- Create: `src/main/java/com/zzhalex/justdirethings/network/message/MessageMachineSetting.java`
- Create: `src/main/java/com/zzhalex/justdirethings/network/message/MessageParadoxState.java`
- Create: `src/main/java/com/zzhalex/justdirethings/network/message/MessagePortalFavorite.java`
- Test: `src/test/java/com/zzhalex/justdirethings/network/MessageRoundTripTest.java`

### Upgrade station and recipes

- Create: `src/main/java/com/zzhalex/justdirethings/common/block/BlockUpgradeStation.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/TileUpgradeStation.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/container/ContainerUpgradeStation.java`
- Create: `src/main/java/com/zzhalex/justdirethings/client/gui/GuiUpgradeStation.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/recipe/UpgradeStationRecipe.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/recipe/TierUpgradeRecipe.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/recipe/AbilityInstallRecipe.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/recipe/PaxelFusionRecipe.java`
- Test: `src/test/java/com/zzhalex/justdirethings/common/recipe/UpgradeRecipeLogicTest.java`

### Tools, armor, items, fluids

- Create: `src/main/java/com/zzhalex/justdirethings/common/item/base/ItemToggleableTool.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/base/ItemPoweredTool.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/base/ItemFluidPoweredTool.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/ability/Ability.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/ability/AbilityMethods.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/material/JDTToolTier.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/material/JDTArmorMaterial.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/misc/PocketGeneratorItem.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/misc/FluidCanisterItem.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/misc/PotionCanisterItem.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/misc/FuelCanisterItem.java`
- Test: `src/test/java/com/zzhalex/justdirethings/common/item/ability/AbilityStateTest.java`

### Machine framework

- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/base/TileMachineBase.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/base/MachineRedstoneState.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/base/MachineAreaState.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/base/MachineFilterState.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/base/MachineEnergyState.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/base/MachineFluidState.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/container/base/ContainerMachineBase.java`
- Create: `src/main/java/com/zzhalex/justdirethings/client/gui/base/GuiMachineBase.java`
- Test: `src/test/java/com/zzhalex/justdirethings/common/tile/base/MachineStateSerializationTest.java`

### Machine content

- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileGenerator.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileFluidGenerator.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileItemCollector.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileBlockBreaker.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileBlockPlacer.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileClicker.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileDropper.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileSensor.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileBlockSwapper.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileFluidCollector.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileFluidPlacer.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileInventoryHolder.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileExperienceHolder.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileEnergyTransmitter.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TilePlayerAccessor.java`

### High-risk systems

- Create: `src/main/java/com/zzhalex/justdirethings/common/entity/EntityPortal.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/entity/EntityPortalProjectile.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/entity/EntityTimeWand.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/entity/EntityParadox.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/tool/ItemPortalGunV2.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/tool/ItemTimeWand.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/tool/ItemPolymorphicWandV2.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileParadoxMachine.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/world/PortalChunkKeeper.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/entity/group/JDTEntityGroups.java`
- Test: `src/test/java/com/zzhalex/justdirethings/common/tile/machine/ParadoxSanitizerTest.java`

### Client and integrations

- Create: `src/main/java/com/zzhalex/justdirethings/client/ClientRegistration.java`
- Create: `src/main/java/com/zzhalex/justdirethings/client/render/RenderPortal.java`
- Create: `src/main/java/com/zzhalex/justdirethings/client/render/RenderPortalProjectile.java`
- Create: `src/main/java/com/zzhalex/justdirethings/client/render/RenderParadox.java`
- Create: `src/main/java/com/zzhalex/justdirethings/client/render/RenderTimeWand.java`
- Create: `src/main/java/com/zzhalex/justdirethings/client/overlay/AbilityCooldownOverlay.java`
- Create: `src/main/java/com/zzhalex/justdirethings/compat/jei/JDTJeiPlugin.java`
- Create: `src/main/java/com/zzhalex/justdirethings/compat/patchouli/JDTPatchouliBook.java`
- Create: `src/main/java/com/zzhalex/justdirethings/compat/accessory/AccessoryInventoryBridge.java`
- Create: `src/main/java/com/zzhalex/justdirethings/compat/accessory/BubblesCompatBridge.java`
- Create: `src/main/java/com/zzhalex/justdirethings/compat/accessory/BaublesCompatBridge.java`

### Documentation and assets

- Modify: `README.md`
- Modify: `src/main/resources/mcmod.info`
- Create: `src/main/resources/assets/justdirethings/lang/en_us.lang`
- Create: `src/main/resources/assets/justdirethings/lang/zh_cn.lang`
- Create: `src/main/resources/assets/justdirethings/patchouli_books/justdirethingsbook/...`
- Create: `src/main/resources/assets/justdirethings/textures/...`
- Create: `src/main/resources/assets/justdirethings/models/...`

## Chunk 1: Foundation and Compatibility

### Task 1: Add optional dependency plumbing and bootstrap proxies

**Files:**
- Modify: `build.gradle`
- Modify: `gradle/scripts/dependencies.gradle`
- Modify: `src/main/java/com/zzhalex/justdirethings/JustDireThingsLegacy.java`
- Create: `src/main/java/com/zzhalex/justdirethings/CommonProxy.java`
- Create: `src/main/java/com/zzhalex/justdirethings/ClientProxy.java`
- Modify: `src/main/java/com/zzhalex/justdirethings/Reference.java`

- [ ] **Step 1: Write the failing bootstrap test**

```java
class ReferenceTest {
    @Test
    void modMetadataIsStable() {
        assertEquals("justdirethings", Reference.MOD_ID);
    }
}
```

- [ ] **Step 2: Run the test to verify the harness is alive**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.ReferenceTest" -q`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Add optional dependency declarations**

Add dependency hooks for:
- JEI
- Patchouli
- BubblesEX/Baubles compile-only support
- optional FutureMC runtime support

- [ ] **Step 4: Split mod bootstrap into common and client proxies**

Create proxy entrypoints and move client-only registration out of the root mod class.

- [ ] **Step 5: Run compile verification**

Run: `.\gradlew.bat classes`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Commit**

```bash
git add build.gradle gradle/scripts/dependencies.gradle src/main/java/com/zzhalex/justdirethings
git commit -m "build: add optional compat hooks and mod bootstrap proxies"
```

### Task 2: Implement the compatibility content resolver

**Files:**
- Create: `src/main/java/com/zzhalex/justdirethings/compat/content/CompatContentKey.java`
- Create: `src/main/java/com/zzhalex/justdirethings/compat/content/CompatContentResolver.java`
- Create: `src/main/java/com/zzhalex/justdirethings/compat/content/FutureMcCompat.java`
- Test: `src/test/java/com/zzhalex/justdirethings/compat/content/CompatContentResolverTest.java`

- [ ] **Step 1: Write the failing resolver test**

```java
@Test
void bambooFallsBackToReedsWhenFutureMcIsMissing() {
    assertEquals("minecraft:reeds", CompatContentResolver.fallbackId(CompatContentKey.BAMBOO).toString());
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.compat.content.CompatContentResolverTest" -q`
Expected: FAIL with missing resolver classes

- [ ] **Step 3: Implement the fixed mapping table**

Use the approved mappings from the spec and expose:
- fallback id lookup
- futuremc id lookup
- resolution priority helper

- [ ] **Step 4: Add soft FutureMC detection**

Use `Loader.isModLoaded("futuremc")` and registry lookups only.

- [ ] **Step 5: Re-run the test**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.compat.content.CompatContentResolverTest" -q`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/zzhalex/justdirethings/compat/content src/test/java/com/zzhalex/justdirethings/compat/content
git commit -m "feat: add compatibility content resolver"
```

### Task 3: Add project configuration scaffolding

**Files:**
- Create: `src/main/java/com/zzhalex/justdirethings/config/JDTConfig.java`
- Modify: `src/main/java/com/zzhalex/justdirethings/JustDireThingsLegacy.java`
- Test: `src/test/java/com/zzhalex/justdirethings/config/JDTConfigDefaultsTest.java`

- [ ] **Step 1: Write a config defaults test**

```java
@Test
void fallbackCompatCanBeEnabledByDefault() {
    assertTrue(JDTConfig.DEFAULT_ENABLE_FALLBACK_COMPAT);
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.config.JDTConfigDefaultsTest" -q`
Expected: FAIL

- [ ] **Step 3: Implement config categories**

Create categories for:
- ability toggles
- pocket generator
- time wand
- paradox
- compat behavior

- [ ] **Step 4: Wire config registration into mod startup**

- [ ] **Step 5: Re-run the test**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.config.JDTConfigDefaultsTest" -q`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/zzhalex/justdirethings/config/JDTConfig.java src/main/java/com/zzhalex/justdirethings/JustDireThingsLegacy.java src/test/java/com/zzhalex/justdirethings/config/JDTConfigDefaultsTest.java
git commit -m "feat: add backport configuration scaffolding"
```

## Chunk 2: Shared Data, Networking, and Upgrade Station

### Task 4: Introduce shared tool state data classes

**Files:**
- Create: `src/main/java/com/zzhalex/justdirethings/data/JDTDataKeys.java`
- Create: `src/main/java/com/zzhalex/justdirethings/data/tool/ToolState.java`
- Create: `src/main/java/com/zzhalex/justdirethings/data/tool/ToolStateIO.java`
- Create: `src/main/java/com/zzhalex/justdirethings/data/tool/AbilityBinding.java`
- Create: `src/main/java/com/zzhalex/justdirethings/data/tool/AbilityCooldown.java`
- Test: `src/test/java/com/zzhalex/justdirethings/data/tool/ToolStateIOTest.java`

- [ ] **Step 1: Write the failing round-trip test**

```java
@Test
void toolStateRoundTripsThroughNbt() {
    ToolState original = new ToolState();
    original.setEnabled(false);
    NBTTagCompound tag = ToolStateIO.write(original);
    ToolState restored = ToolStateIO.read(tag);
    assertFalse(restored.isEnabled());
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.data.tool.ToolStateIOTest" -q`
Expected: FAIL

- [ ] **Step 3: Implement the shared data model**

Model the approved shared state fields only. Do not add speculative fields.

- [ ] **Step 4: Re-run the round-trip test**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.data.tool.ToolStateIOTest" -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/zzhalex/justdirethings/data src/test/java/com/zzhalex/justdirethings/data/tool/ToolStateIOTest.java
git commit -m "feat: add shared tool state model"
```

### Task 5: Add shared item capabilities

**Files:**
- Create: `src/main/java/com/zzhalex/justdirethings/capability/ModCapabilities.java`
- Create: `src/main/java/com/zzhalex/justdirethings/capability/energy/ItemEnergyStorage.java`
- Create: `src/main/java/com/zzhalex/justdirethings/capability/fluid/ItemFluidTank.java`
- Create: `src/main/java/com/zzhalex/justdirethings/capability/inventory/InternalItemHandler.java`
- Test: `src/test/java/com/zzhalex/justdirethings/capability/InternalItemHandlerTest.java`

- [ ] **Step 1: Write the failing inventory capability test**

```java
@Test
void handlerPersistsSingleSlotItem() {
    InternalItemHandler handler = new InternalItemHandler(1);
    handler.setStackInSlot(0, new ItemStack(Items.COAL));
    assertFalse(handler.getStackInSlot(0).isEmpty());
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.capability.InternalItemHandlerTest" -q`
Expected: FAIL

- [ ] **Step 3: Implement energy, fluid, and internal-inventory capability wrappers**

- [ ] **Step 4: Re-run the test**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.capability.InternalItemHandlerTest" -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/zzhalex/justdirethings/capability src/test/java/com/zzhalex/justdirethings/capability/InternalItemHandlerTest.java
git commit -m "feat: add shared item capabilities"
```

### Task 6: Build the network channel and core messages

**Files:**
- Create: `src/main/java/com/zzhalex/justdirethings/network/JDTNetwork.java`
- Create: `src/main/java/com/zzhalex/justdirethings/network/message/MessageSyncToolState.java`
- Create: `src/main/java/com/zzhalex/justdirethings/network/message/MessageMachineSetting.java`
- Create: `src/main/java/com/zzhalex/justdirethings/network/message/MessagePortalFavorite.java`
- Create: `src/main/java/com/zzhalex/justdirethings/network/message/MessageParadoxState.java`
- Test: `src/test/java/com/zzhalex/justdirethings/network/MessageRoundTripTest.java`

- [ ] **Step 1: Write the failing message round-trip test**

```java
@Test
void syncToolStateMessageEncodesAndDecodes() {
    MessageSyncToolState original = new MessageSyncToolState(3, new NBTTagCompound());
    ByteBuf buf = Unpooled.buffer();
    original.toBytes(buf);
    MessageSyncToolState copy = new MessageSyncToolState();
    copy.fromBytes(buf);
    assertEquals(3, copy.slot);
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.network.MessageRoundTripTest" -q`
Expected: FAIL

- [ ] **Step 3: Implement `SimpleNetworkWrapper` registration and message skeletons**

- [ ] **Step 4: Re-run the test**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.network.MessageRoundTripTest" -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/zzhalex/justdirethings/network src/test/java/com/zzhalex/justdirethings/network/MessageRoundTripTest.java
git commit -m "feat: add core network channel and message scaffolding"
```

### Task 7: Implement the upgrade station shell

**Files:**
- Create: `src/main/java/com/zzhalex/justdirethings/common/block/BlockUpgradeStation.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/TileUpgradeStation.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/container/ContainerUpgradeStation.java`
- Create: `src/main/java/com/zzhalex/justdirethings/client/gui/GuiUpgradeStation.java`
- Modify: `src/main/java/com/zzhalex/justdirethings/registry/ModBlocks.java`
- Modify: `src/main/java/com/zzhalex/justdirethings/registry/ModItems.java`
- Modify: `src/main/java/com/zzhalex/justdirethings/registry/ModTileEntities.java`
- Modify: `src/main/java/com/zzhalex/justdirethings/registry/ModContainers.java`

- [ ] **Step 1: Write the failing container test**

```java
@Test
void upgradeStationExposesThreeInputSlotsAndOneOutput() {
    assertEquals(4, ContainerUpgradeStation.SLOT_COUNT);
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.container.ContainerUpgradeStationTest" -q`
Expected: FAIL

- [ ] **Step 3: Implement the block, tile, container, and GUI shell**

- [ ] **Step 4: Re-run the test**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.container.ContainerUpgradeStationTest" -q`
Expected: PASS

- [ ] **Step 5: Launch the client and verify registration**

Run: `.\gradlew.bat runClient`
Expected: game launches and the upgrade station appears in the creative tab without missing model or registration crashes

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/zzhalex/justdirethings/common/block/BlockUpgradeStation.java src/main/java/com/zzhalex/justdirethings/common/tile/TileUpgradeStation.java src/main/java/com/zzhalex/justdirethings/common/container/ContainerUpgradeStation.java src/main/java/com/zzhalex/justdirethings/client/gui/GuiUpgradeStation.java src/main/java/com/zzhalex/justdirethings/registry
git commit -m "feat: add upgrade station shell"
```

### Task 8: Implement upgrade station recipe logic

**Files:**
- Create: `src/main/java/com/zzhalex/justdirethings/common/recipe/UpgradeStationRecipe.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/recipe/TierUpgradeRecipe.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/recipe/AbilityInstallRecipe.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/recipe/PaxelFusionRecipe.java`
- Modify: `src/main/java/com/zzhalex/justdirethings/registry/ModRecipes.java`
- Test: `src/test/java/com/zzhalex/justdirethings/common/recipe/UpgradeRecipeLogicTest.java`

- [ ] **Step 1: Write failing recipe inheritance tests**

```java
@Test
void abilityInstallPreservesExistingFlags() {
    ToolState state = new ToolState();
    state.setEnabled(false);
    ToolState output = UpgradeRecipeLogic.installAbility(state, "flight");
    assertFalse(output.isEnabled());
    assertTrue(output.hasInstalledAbility("flight"));
}
```

- [ ] **Step 2: Run the tests and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.recipe.UpgradeRecipeLogicTest" -q`
Expected: FAIL

- [ ] **Step 3: Implement the three recipe families and state inheritance helpers**

- [ ] **Step 4: Re-run the tests**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.recipe.UpgradeRecipeLogicTest" -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/zzhalex/justdirethings/common/recipe src/main/java/com/zzhalex/justdirethings/registry/ModRecipes.java src/test/java/com/zzhalex/justdirethings/common/recipe/UpgradeRecipeLogicTest.java
git commit -m "feat: add upgrade station recipe families"
```

## Chunk 3: Tools, Armor, and Item Systems

### Task 9: Add ability enum and execution seam

**Files:**
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/ability/Ability.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/ability/AbilityMethods.java`
- Test: `src/test/java/com/zzhalex/justdirethings/common/item/ability/AbilityAvailabilityTest.java`

- [ ] **Step 1: Write the failing availability test**

```java
@Test
void everyDeclaredAbilityHasAStableStringId() {
    assertNotNull(Ability.FLIGHT.getId());
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.item.ability.AbilityAvailabilityTest" -q`
Expected: FAIL

- [ ] **Step 3: Implement the ability catalog and method dispatch seam**

- [ ] **Step 4: Re-run the test**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.item.ability.AbilityAvailabilityTest" -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/zzhalex/justdirethings/common/item/ability src/test/java/com/zzhalex/justdirethings/common/item/ability/AbilityAvailabilityTest.java
git commit -m "feat: add ability catalog and execution seam"
```

### Task 10: Implement base tool and armor classes

**Files:**
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/base/ItemToggleableTool.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/base/ItemPoweredTool.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/base/ItemFluidPoweredTool.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/material/JDTToolTier.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/material/JDTArmorMaterial.java`
- Test: `src/test/java/com/zzhalex/justdirethings/common/item/base/ToggleableToolTest.java`

- [ ] **Step 1: Write the failing base item state test**

```java
@Test
void toolsDefaultToEnabled() {
    ToolState state = new ToolState();
    assertTrue(state.isEnabled());
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.item.base.ToggleableToolTest" -q`
Expected: FAIL

- [ ] **Step 3: Implement the backport base item classes and tiers**

- [ ] **Step 4: Re-run the test**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.item.base.ToggleableToolTest" -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/zzhalex/justdirethings/common/item/base src/main/java/com/zzhalex/justdirethings/common/item/material src/test/java/com/zzhalex/justdirethings/common/item/base/ToggleableToolTest.java
git commit -m "feat: add base tool and armor classes"
```

### Task 11: Implement canisters and pocket generator

**Files:**
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/misc/PocketGeneratorItem.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/misc/FluidCanisterItem.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/misc/PotionCanisterItem.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/misc/FuelCanisterItem.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/container/ContainerPocketGenerator.java`
- Create: `src/main/java/com/zzhalex/justdirethings/client/gui/GuiPocketGenerator.java`
- Test: `src/test/java/com/zzhalex/justdirethings/common/item/misc/PocketGeneratorFuelMathTest.java`

- [ ] **Step 1: Write the failing generator math test**

```java
@Test
void burnMultiplierScalesOutputLinearly() {
    assertEquals(40, PocketGeneratorMath.fePerTick(10, 4));
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.item.misc.PocketGeneratorFuelMathTest" -q`
Expected: FAIL

- [ ] **Step 3: Implement the math seam and then wire the actual items to it**

- [ ] **Step 4: Add accessory inventory bridge calls for energy distribution**

- [ ] **Step 5: Re-run the tests**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.item.misc.PocketGeneratorFuelMathTest" -q`
Expected: PASS

- [ ] **Step 6: Launch the client and manually verify GUI**

Run: `.\gradlew.bat runClient`
Expected: canisters and pocket generator load, render, and open without GUI crashes

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/zzhalex/justdirethings/common/item/misc src/main/java/com/zzhalex/justdirethings/common/container/ContainerPocketGenerator.java src/main/java/com/zzhalex/justdirethings/client/gui/GuiPocketGenerator.java src/test/java/com/zzhalex/justdirethings/common/item/misc/PocketGeneratorFuelMathTest.java
git commit -m "feat: add generator and canister items"
```

## Chunk 4: Machine Base Framework and Core Automation

### Task 12: Implement the machine base state classes

**Files:**
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/base/TileMachineBase.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/base/MachineRedstoneState.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/base/MachineAreaState.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/base/MachineFilterState.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/base/MachineEnergyState.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/base/MachineFluidState.java`
- Test: `src/test/java/com/zzhalex/justdirethings/common/tile/base/MachineStateSerializationTest.java`

- [ ] **Step 1: Write the failing serialization test**

```java
@Test
void machineTickSpeedPersistsToNbt() {
    MachineRedstoneState state = new MachineRedstoneState();
    NBTTagCompound tag = state.writeToNbt(new NBTTagCompound());
    assertNotNull(tag);
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.tile.base.MachineStateSerializationTest" -q`
Expected: FAIL

- [ ] **Step 3: Implement shared machine state objects and TE base class**

- [ ] **Step 4: Re-run the tests**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.tile.base.MachineStateSerializationTest" -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/zzhalex/justdirethings/common/tile/base src/test/java/com/zzhalex/justdirethings/common/tile/base/MachineStateSerializationTest.java
git commit -m "feat: add machine base state framework"
```

### Task 13: Implement the base container and GUI framework

**Files:**
- Create: `src/main/java/com/zzhalex/justdirethings/common/container/base/ContainerMachineBase.java`
- Create: `src/main/java/com/zzhalex/justdirethings/client/gui/base/GuiMachineBase.java`
- Create: `src/main/java/com/zzhalex/justdirethings/client/gui/widget/WidgetEnergyBar.java`
- Create: `src/main/java/com/zzhalex/justdirethings/client/gui/widget/WidgetFluidBar.java`
- Create: `src/main/java/com/zzhalex/justdirethings/client/gui/widget/WidgetAreaPreview.java`

- [ ] **Step 1: Write a minimal GUI smoke test seam**

```java
@Test
void machineContainerUsesStableSlotConstants() {
    assertTrue(ContainerMachineBase.PLAYER_INV_START >= 0);
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.container.base.ContainerMachineBaseTest" -q`
Expected: FAIL

- [ ] **Step 3: Implement the base container and GUI shell**

- [ ] **Step 4: Re-run the test**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.container.base.ContainerMachineBaseTest" -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/zzhalex/justdirethings/common/container/base src/main/java/com/zzhalex/justdirethings/client/gui/base src/main/java/com/zzhalex/justdirethings/client/gui/widget
git commit -m "feat: add machine base container and gui framework"
```

### Task 14: Implement generator and simple collection machines

**Files:**
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileGenerator.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileFluidGenerator.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileItemCollector.java`
- Create matching blocks, containers, and GUIs under:
  - `src/main/java/com/zzhalex/justdirethings/common/block/machine/`
  - `src/main/java/com/zzhalex/justdirethings/common/container/machine/`
  - `src/main/java/com/zzhalex/justdirethings/client/gui/machine/`

- [ ] **Step 1: Write a failing fuel-consumption unit test for generator math**

```java
@Test
void generatorConsumesFuelToFillEnergyBuffer() {
    assertTrue(GeneratorMath.canStartBurn(1600, 0, 10000));
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.tile.machine.GeneratorMathTest" -q`
Expected: FAIL

- [ ] **Step 3: Implement the generator math seam**

- [ ] **Step 4: Wire generator, fluid generator, and item collector tiles to the shared base**

- [ ] **Step 5: Re-run unit tests**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.tile.machine.GeneratorMathTest" -q`
Expected: PASS

- [ ] **Step 6: Launch the client and verify these machines tick, open, and sync**

Run: `.\gradlew.bat runClient`
Expected: generator family and collector place, tick, and update GUI values correctly

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/zzhalex/justdirethings/common/tile/machine src/main/java/com/zzhalex/justdirethings/common/block/machine src/main/java/com/zzhalex/justdirethings/common/container/machine src/main/java/com/zzhalex/justdirethings/client/gui/machine
git commit -m "feat: add generator and collector machines"
```

### Task 15: Implement placement, breaking, clicking, dropping, sensing, swapping, and fluid machines

**Files:**
- Create machine-specific block, tile, container, and GUI files for:
  - `BlockBreaker`
  - `BlockPlacer`
  - `Clicker`
  - `Dropper`
  - `Sensor`
  - `BlockSwapper`
  - `FluidCollector`
  - `FluidPlacer`

- [ ] **Step 1: Write a failing fake-player helper test seam**

```java
@Test
void fakePlayerFacingProducesDeterministicRotation() {
    assertEquals(90.0F, FakePlayerMath.pitchForFacing(EnumFacing.DOWN), 0.01F);
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.tile.machine.FakePlayerMathTest" -q`
Expected: FAIL

- [ ] **Step 3: Implement fake-player math and shared placement/breaking helpers**

- [ ] **Step 4: Implement the machine family on top of the helpers**

- [ ] **Step 5: Re-run unit tests**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.tile.machine.FakePlayerMathTest" -q`
Expected: PASS

- [ ] **Step 6: Launch the client and manually verify one full scenario per machine**

Run: `.\gradlew.bat runClient`
Expected: each machine executes its core action without desync or GUI crash

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/zzhalex/justdirethings/common/tile/machine src/main/java/com/zzhalex/justdirethings/common/block/machine src/main/java/com/zzhalex/justdirethings/common/container/machine src/main/java/com/zzhalex/justdirethings/client/gui/machine
git commit -m "feat: add core automation machine family"
```

### Task 16: Implement holder, transmitter, and player accessor machines

**Files:**
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileInventoryHolder.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileExperienceHolder.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileEnergyTransmitter.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TilePlayerAccessor.java`
- Create matching block/container/gui files
- Modify: `src/main/java/com/zzhalex/justdirethings/compat/accessory/AccessoryInventoryBridge.java`

- [ ] **Step 1: Write a failing accessory bridge test seam**

```java
@Test
void emptyAccessoryBridgeReturnsZeroSlots() {
    assertEquals(0, AccessoryInventoryBridge.empty().getSlotCount());
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.compat.accessory.AccessoryInventoryBridgeTest" -q`
Expected: FAIL

- [ ] **Step 3: Implement the accessory abstraction first**

- [ ] **Step 4: Implement holder, experience, transmitter, and player accessor machines**

- [ ] **Step 5: Re-run the test**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.compat.accessory.AccessoryInventoryBridgeTest" -q`
Expected: PASS

- [ ] **Step 6: Launch the client and manually verify inventory and accessory access**

Run: `.\gradlew.bat runClient`
Expected: player accessor can read inventory and accessory slots when compat mod is present

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/zzhalex/justdirethings/common/tile/machine src/main/java/com/zzhalex/justdirethings/compat/accessory
git commit -m "feat: add holder, transmitter, and player accessor machines"
```

## Chunk 5: High-Risk Gameplay Systems

### Task 17: Implement entity grouping and polymorphic helpers

**Files:**
- Create: `src/main/java/com/zzhalex/justdirethings/common/entity/group/JDTEntityGroups.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/tool/ItemPolymorphicWandV2.java`
- Modify: `src/main/java/com/zzhalex/justdirethings/common/item/ability/AbilityMethods.java`
- Test: `src/test/java/com/zzhalex/justdirethings/common/entity/group/JDTEntityGroupsTest.java`

- [ ] **Step 1: Write the failing entity grouping test**

```java
@Test
void dragonIsAlwaysDeniedAsPolymorphicTarget() {
    assertTrue(JDTEntityGroups.isPolymorphicTargetDenied("minecraft:ender_dragon"));
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.entity.group.JDTEntityGroupsTest" -q`
Expected: FAIL

- [ ] **Step 3: Implement 1.12-only group definitions**

- [ ] **Step 4: Re-run the test**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.entity.group.JDTEntityGroupsTest" -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/zzhalex/justdirethings/common/entity/group src/main/java/com/zzhalex/justdirethings/common/item/tool/ItemPolymorphicWandV2.java src/test/java/com/zzhalex/justdirethings/common/entity/group/JDTEntityGroupsTest.java
git commit -m "feat: add 1.12 entity grouping rules"
```

### Task 18: Implement the time wand and tick accelerator entity

**Files:**
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/tool/ItemTimeWand.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/entity/EntityTimeWand.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/util/TickAccelerationRules.java`
- Test: `src/test/java/com/zzhalex/justdirethings/common/util/TickAccelerationRulesTest.java`

- [ ] **Step 1: Write the failing acceleration test**

```java
@Test
void multiplierLevelMapsToExpectedRate() {
    assertEquals(2.0F, TickAccelerationRules.accelRateForLevel(1), 0.001F);
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.util.TickAccelerationRulesTest" -q`
Expected: FAIL

- [ ] **Step 3: Implement the pure acceleration math**

- [ ] **Step 4: Port the item and accelerator entity using the tested seam**

- [ ] **Step 5: Re-run the test**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.util.TickAccelerationRulesTest" -q`
Expected: PASS

- [ ] **Step 6: Launch the client and verify repeated clicks increase acceleration**

Run: `.\gradlew.bat runClient`
Expected: time wand consumes FE and fluid and updates acceleration tiers correctly

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/zzhalex/justdirethings/common/item/tool/ItemTimeWand.java src/main/java/com/zzhalex/justdirethings/common/entity/EntityTimeWand.java src/main/java/com/zzhalex/justdirethings/common/util/TickAccelerationRules.java src/test/java/com/zzhalex/justdirethings/common/util/TickAccelerationRulesTest.java
git commit -m "feat: add time wand acceleration system"
```

### Task 19: Implement portal gun items and projectile flow

**Files:**
- Create: `src/main/java/com/zzhalex/justdirethings/common/item/tool/ItemPortalGunV2.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/entity/EntityPortalProjectile.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/portal/PortalLinkData.java`
- Test: `src/test/java/com/zzhalex/justdirethings/common/portal/PortalLinkDataTest.java`

- [ ] **Step 1: Write the failing portal favorite round-trip test**

```java
@Test
void portalFavoritesRoundTripThroughNbt() {
    PortalLinkData data = new PortalLinkData();
    data.setFavoriteIndex(2);
    assertEquals(2, PortalLinkData.read(PortalLinkData.write(data)).getFavoriteIndex());
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.portal.PortalLinkDataTest" -q`
Expected: FAIL

- [ ] **Step 3: Implement the portal item data model**

- [ ] **Step 4: Implement projectile spawning and favorite mutation packets**

- [ ] **Step 5: Re-run the test**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.portal.PortalLinkDataTest" -q`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/zzhalex/justdirethings/common/item/tool/ItemPortalGunV2.java src/main/java/com/zzhalex/justdirethings/common/entity/EntityPortalProjectile.java src/main/java/com/zzhalex/justdirethings/common/portal/PortalLinkData.java src/test/java/com/zzhalex/justdirethings/common/portal/PortalLinkDataTest.java
git commit -m "feat: add portal gun item data and projectile flow"
```

### Task 20: Implement portal entities and chunk keeping

**Files:**
- Create: `src/main/java/com/zzhalex/justdirethings/common/entity/EntityPortal.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/world/PortalChunkKeeper.java`
- Create: `src/main/java/com/zzhalex/justdirethings/client/render/RenderPortal.java`
- Create: `src/main/java/com/zzhalex/justdirethings/client/render/RenderPortalProjectile.java`

Parity warning: this task must be implemented against upstream `PortalEntity` and `PortalEntityRender`, not as a simplified teleport plane. Required behavior includes pre-teleport velocity sampling/inheritance, exact floor/ceiling/wall geometry, projectile collision rules that ignore non-colliding blocks, client motion sync, portal open/close sounds, and shader/procedural surface rendering.

- [ ] **Step 1: Write a failing chunk-claim math test seam**

```java
@Test
void portalChunkKeyUsesBlockPositionChunk() {
    assertEquals("0,0", PortalChunkKeeper.chunkKey(new BlockPos(1, 64, 1)));
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.world.PortalChunkKeeperTest" -q`
Expected: FAIL

- [ ] **Step 3: Implement chunk-key helpers and chunk keeping layer**

- [ ] **Step 4: Implement portal entity lifecycle and teleport cooldown handling**

- [ ] **Step 5: Re-run the test**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.world.PortalChunkKeeperTest" -q`
Expected: PASS

- [ ] **Step 6: Launch the client and manually verify paired portal teleporting**

Run: `.\gradlew.bat runClient`
Expected: portals pair, teleport, and unload cleanly without leaving chunk tickets behind

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/zzhalex/justdirethings/common/entity/EntityPortal.java src/main/java/com/zzhalex/justdirethings/common/world/PortalChunkKeeper.java src/main/java/com/zzhalex/justdirethings/client/render/RenderPortal.java src/main/java/com/zzhalex/justdirethings/client/render/RenderPortalProjectile.java
git commit -m "feat: add portal entities and chunk keeping"
```

### Task 21: Implement paradox snapshot and restore logic

**Files:**
- Create: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileParadoxMachine.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/entity/EntityParadox.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/paradox/ParadoxSnapshot.java`
- Create: `src/main/java/com/zzhalex/justdirethings/common/paradox/ParadoxSanitizer.java`
- Test: `src/test/java/com/zzhalex/justdirethings/common/tile/machine/ParadoxSanitizerTest.java`

- [ ] **Step 1: Write the failing sanitizer test**

```java
@Test
void restrictiveSanitizerStripsInventoryKeys() {
    NBTTagCompound input = new NBTTagCompound();
    input.setTag("Inventory", new NBTTagList());
    NBTTagCompound output = ParadoxSanitizer.restrictive(input);
    assertFalse(output.hasKey("Inventory"));
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.tile.machine.ParadoxSanitizerTest" -q`
Expected: FAIL

- [ ] **Step 3: Implement the sanitizer and snapshot data classes first**

- [ ] **Step 4: Implement paradox tile runtime and entity interactions**

- [ ] **Step 5: Re-run the test**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.common.tile.machine.ParadoxSanitizerTest" -q`
Expected: PASS

- [ ] **Step 6: Launch the client and manually verify snapshot, restore, and stop behavior**

Run: `.\gradlew.bat runClient`
Expected: paradox machine snapshots an area, restores it, and obeys deny rules

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileParadoxMachine.java src/main/java/com/zzhalex/justdirethings/common/entity/EntityParadox.java src/main/java/com/zzhalex/justdirethings/common/paradox src/test/java/com/zzhalex/justdirethings/common/tile/machine/ParadoxSanitizerTest.java
git commit -m "feat: add paradox snapshot and restore system"
```

## Chunk 6: Client, JEI, Patchouli, and Accessory Compat

### Task 22: Centralize client registration

**Files:**
- Create: `src/main/java/com/zzhalex/justdirethings/client/ClientRegistration.java`
- Modify: `src/main/java/com/zzhalex/justdirethings/ClientProxy.java`
- Create renderer and overlay files under `src/main/java/com/zzhalex/justdirethings/client/render/` and `src/main/java/com/zzhalex/justdirethings/client/overlay/`

- [ ] **Step 1: Write a minimal registration smoke test seam**

```java
@Test
void clientRegistrationClassExists() {
    assertEquals("ClientRegistration", ClientRegistration.class.getSimpleName());
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.client.ClientRegistrationTest" -q`
Expected: FAIL

- [ ] **Step 3: Implement client item model predicates, overlays, screen registration, and renderer hooks**

- [ ] **Step 4: Re-run the test**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.client.ClientRegistrationTest" -q`
Expected: PASS

- [ ] **Step 5: Launch the client for a full render smoke test**

Run: `.\gradlew.bat runClient`
Expected: client boots without missing-class crashes and custom items render with state changes

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/zzhalex/justdirethings/client src/main/java/com/zzhalex/justdirethings/ClientProxy.java
git commit -m "feat: add client registration and renderer hooks"
```

### Task 23: Add JEI and Patchouli support

**Files:**
- Create: `src/main/java/com/zzhalex/justdirethings/compat/jei/JDTJeiPlugin.java`
- Create: `src/main/java/com/zzhalex/justdirethings/compat/jei/category/UpgradeStationCategory.java`
- Create: `src/main/java/com/zzhalex/justdirethings/compat/patchouli/JDTPatchouliBook.java`
- Create: `src/main/resources/assets/justdirethings/patchouli_books/justdirethingsbook/...`
- Modify: `src/main/resources/assets/justdirethings/lang/en_us.lang`
- Modify: `src/main/resources/assets/justdirethings/lang/zh_cn.lang`

- [ ] **Step 1: Write a failing upgrade station display helper test**

```java
@Test
void jeiCategoryIdIsStable() {
    assertEquals("justdirethings.upgrade_station", UpgradeStationCategory.UID);
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.compat.jei.UpgradeStationCategoryTest" -q`
Expected: FAIL

- [ ] **Step 3: Implement JEI category and recipe wrapper support**

- [ ] **Step 4: Rewrite Patchouli book text to describe the 1.12 backport implementation**

- [ ] **Step 5: Re-run the test**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.compat.jei.UpgradeStationCategoryTest" -q`
Expected: PASS

- [ ] **Step 6: Launch the client with JEI and Patchouli present**

Run: `.\gradlew.bat runClient`
Expected: JEI lists upgrade station recipes and the Patchouli book text references the upgrade station instead of smithing

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/zzhalex/justdirethings/compat/jei src/main/java/com/zzhalex/justdirethings/compat/patchouli src/main/resources/assets/justdirethings/patchouli_books src/main/resources/assets/justdirethings/lang
git commit -m "feat: add JEI and Patchouli support"
```

### Task 24: Add BubblesEX/Baubles compat bridge

**Files:**
- Create: `src/main/java/com/zzhalex/justdirethings/compat/accessory/AccessoryInventoryBridge.java`
- Create: `src/main/java/com/zzhalex/justdirethings/compat/accessory/BubblesCompatBridge.java`
- Create: `src/main/java/com/zzhalex/justdirethings/compat/accessory/BaublesCompatBridge.java`
- Modify: `src/main/java/com/zzhalex/justdirethings/common/item/misc/PocketGeneratorItem.java`
- Modify: `src/main/java/com/zzhalex/justdirethings/common/tile/machine/TilePlayerAccessor.java`

- [ ] **Step 1: Write the failing empty-bridge test**

```java
@Test
void noCompatBridgeBehavesAsEmptyInventory() {
    assertEquals(0, AccessoryInventoryBridge.none().getSlotCount());
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.compat.accessory.AccessoryInventoryBridgeTest" -q`
Expected: FAIL

- [ ] **Step 3: Implement the neutral bridge abstraction**

- [ ] **Step 4: Add BubblesEX and Baubles adapters behind soft dependency checks**

- [ ] **Step 5: Re-run the test**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.compat.accessory.AccessoryInventoryBridgeTest" -q`
Expected: PASS

- [ ] **Step 6: Launch the client with compat mods and verify pocket generator charging**

Run: `.\gradlew.bat runClient`
Expected: pocket generator and player accessor see accessory inventory when compat mods are loaded

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/zzhalex/justdirethings/compat/accessory src/main/java/com/zzhalex/justdirethings/common/item/misc/PocketGeneratorItem.java src/main/java/com/zzhalex/justdirethings/common/tile/machine/TilePlayerAccessor.java
git commit -m "feat: add BubblesEX and Baubles compat bridge"
```

## Chunk 7: Verification, Assets, and Release Readiness

### Task 25: Fill localization, resources, and documentation gaps

**Files:**
- Modify: `README.md`
- Modify: `src/main/resources/mcmod.info`
- Create or update asset files under:
  - `src/main/resources/assets/justdirethings/lang/`
  - `src/main/resources/assets/justdirethings/models/`
  - `src/main/resources/assets/justdirethings/textures/`

- [ ] **Step 1: Write a failing localization completeness test seam**

```java
@Test
void englishLangContainsUpgradeStationKey() throws Exception {
    String content = Files.readString(Paths.get("src/main/resources/assets/justdirethings/lang/en_us.lang"));
    assertTrue(content.contains("tile.justdirethings.upgrade_station.name"));
}
```

- [ ] **Step 2: Run the test and confirm failure**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.resources.LocalizationSmokeTest" -q`
Expected: FAIL

- [ ] **Step 3: Add the missing resource keys and baseline docs**

- [ ] **Step 4: Re-run the test**

Run: `.\gradlew.bat test --tests "com.zzhalex.justdirethings.resources.LocalizationSmokeTest" -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add README.md src/main/resources/mcmod.info src/main/resources/assets/justdirethings
git commit -m "docs: add backport resources and localization baseline"
```

### Task 26: Run the full automated test suite

**Files:**
- No code changes required unless failures appear

- [ ] **Step 1: Run the full unit test suite**

Run: `.\gradlew.bat test`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: Fix any failing tests before proceeding**

- [ ] **Step 3: Commit test fixes if needed**

```bash
git add .
git commit -m "test: stabilize full unit test suite"
```

### Task 27: Run the full manual verification matrix

**Files:**
- No code changes required unless failures appear

- [ ] **Step 1: Launch the client without FutureMC**

Run: `.\gradlew.bat runClient`
Expected:
- mod loads
- upgrade station works
- core machines place and sync
- portal, time wand, paradox, and pocket generator all function

- [ ] **Step 2: Launch the client with FutureMC**

Run: `.\gradlew.bat runClient`
Expected:
- FutureMC-backed materials resolve in recipes and displays
- no hard dependency crashes if FutureMC is missing

- [ ] **Step 3: Launch the client with BubblesEX/Baubles**

Run: `.\gradlew.bat runClient`
Expected:
- accessory inventory is exposed where intended
- pocket generator charges supported accessory items
- player accessor sees accessory slots according to bridge rules

- [ ] **Step 4: Fix defects found during manual verification**

- [ ] **Step 5: Commit final verification fixes**

```bash
git add .
git commit -m "fix: address final backport verification issues"
```

- [ ] **Step 6: Build the release artifact**

Run: `.\gradlew.bat build`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Record the tested matrix in the README or release notes**

## Notes for Executors

- Prefer introducing pure helper classes first when gameplay logic is too coupled to Minecraft internals to unit test directly.
- Keep item state, machine state, and compat resolution centralized. Do not recreate ad hoc NBT schemas per item or machine.
- Do not wire `FutureMC`, `BubblesEX`, or `Baubles` classes directly into core logic. All compat must stay behind soft checks and bridge layers.
- Preserve fake-player semantics for automation blocks.
- For high-risk systems, match behavior before matching visuals.
