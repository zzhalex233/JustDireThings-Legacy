# JustDireThings-Legacy Backport Design

**Project:** `JustDireThings-Legacy`

**Goal:** Backport the modern `JustDireThings` mod to `Minecraft 1.12.2`, preserving core gameplay behavior while rebuilding the implementation on top of the 1.12.2 Forge/Cleanroom stack.

**Target parity:** `L3` behavior parity is the baseline for every upstream feature, and gameplay-visible `L4` presentation parity is required wherever the original mod exposes custom GUI, model, texture, sound, renderer, shader, tooltip, animation, or overlay behavior. No feature may be simplified into placeholder visuals or approximate mechanics unless the project owner explicitly accepts that exact scoped omission.

## Approved Constraints

- The backport is a rebuild, not a compiler-driven port of the 1.21 codebase.
- `JustDireThings-main` is the authoritative behavior reference.
- `PortalGunClassic-master` and `Torcherino` are 1.12.2 platform references only.
- `JEI` and `Patchouli` stay.
- `Mekanism` integration is dropped.
- `Curios` support is replaced by `BubblesEX/Baubles` support.
- `FutureMC` is an optional dependency, not a core API dependency.
- High-version new mobs are not remapped to 1.12.2 stand-ins.
- High-version new items/blocks may be resolved through `FutureMC` or fixed fallbacks.

## Architecture

The port is divided into four layers:

1. `1.12 core platform`
   Rebuild mod entrypoints, registries, proxies, networking, config, containers, GUI registration, capabilities, and save/sync pathways using 1.12.2 Forge conventions.
2. `Shared gameplay abstractions`
   Introduce stable, backport-native abstractions for tool state, machine state, compatibility content lookups, packet routing, fake-player operations, and recipe execution.
3. `Feature migration`
   Port blocks, items, machines, fluids, recipes, tools, armor, and high-risk gameplay systems into the shared abstractions.
4. `Client and integration`
   Rebuild GUI, renderers, overlays, JEI, Patchouli, and optional mod bridges after server-side systems are stable.

## Platform Migration Rules

Modern APIs are not emulated directly. They are replaced by native 1.12.2 equivalents:

- `DeferredRegister` and `DeferredHolder` -> `RegistryEvent.Register` + static registry holders
- `DataComponentType` -> `ItemStack NBT` + item capabilities + explicit sync
- `AttachmentType` -> TE fields or Forge capabilities
- `PayloadRegistrar` and `StreamCodec` -> `SimpleNetworkWrapper`
- `MenuType` and `Screen` -> `Container` and `GuiContainer`
- `FluidType` -> 1.12 Forge fluid registration and custom block/item render handling
- Java `record` types -> plain Java 8-compatible classes
- Modern chunk tickets -> 1.12 chunk-loading tickets or equivalent custom chunk keeping

## Compatibility Content Resolver

All high-version content references flow through a single compatibility layer. The resolver uses this priority:

1. Native `1.12.2` vanilla content if it already exists
2. Optional `FutureMC` content if installed and registered
3. A fixed project-owned fallback mapping

Core mod logic does not import `FutureMC` classes. It only soft-resolves `futuremc:*` identifiers through `Loader.isModLoaded("futuremc")` and Forge registries.

### Fixed fallback mapping

- `bamboo` -> `futuremc:bamboo` or `minecraft:reeds`
- `honey_bottle` -> `futuremc:honey_bottle` or `minecraft:sugar`
- `netherite_pickaxe` -> `futuremc:netherite_pickaxe` or `minecraft:diamond_pickaxe`
- `netherite_scrap` -> `futuremc:netherite_scrap` or `minecraft:obsidian`
- `netherite_block` -> `futuremc:netherite_block` or `minecraft:obsidian`
- `amethyst_shard` -> `minecraft:quartz`
- `phantom_membrane` -> `minecraft:ghast_tear`
- `target` -> `minecraft:compass`
- `sculk` -> `minecraft:obsidian`
- `sculk_catalyst` -> `minecraft:nether_star`
- `sculk_shrieker` -> `minecraft:dragon_breath`
- `calibrated_sculk_sensor` -> `minecraft:observer`
- `echo_shard` -> `minecraft:popped_chorus_fruit`

The same mapping must be used consistently in recipes, JEI displays, Patchouli pages, tooltip text, and data-driven helper code.

## Entity Rules

High-version new mobs do not receive replacement stand-ins.

- Keep entity classification and deny-list mechanics
- Remove entities that do not exist in 1.12.2
- Do not map `frog -> chicken`, `warden -> wither skeleton`, or any similar substitutions

Implications:

- `Polymorphic`, `NOAI`, `Earthquake`, `Paradox`, and `Creature Catcher` still keep target-group and deny logic
- Their entity groups are rebuilt from 1.12.2-available entities only
- Boss and special-case deny lists stay conservative

## Upgrade and Recipe Flow

The modern smithing path is replaced by a mod-owned `Upgrade Station`.

It handles three recipe families:

1. `Tier Upgrade`
   Template + old-tier gear + upgrade material -> next-tier gear
2. `Ability Install`
   Tool or armor + upgrade item -> same item with installed ability state
3. `Paxel Fusion`
   Pickaxe + axe + shovel -> paxel

The station is independent from `FutureMC`. `FutureMC` may provide inputs, but not upgrade logic.

### State inheritance rules

- `Tier Upgrade` preserves enchantments, energy, fluids, toggles, installed abilities, bindings, cooldowns, and custom values
- `Ability Install` preserves the full item state and only adds the installed ability flag
- `Paxel Fusion` preserves the union of installed upgrades and compatible enchantments, but outputs a fresh paxel rather than copying live durability from all inputs

JEI and Patchouli must be updated to describe the `Upgrade Station`, not smithing tables.

## Shared State Model

All item state is normalized into a shared backport data model.

The shared state must cover:

- enabled/disabled toggle
- installed abilities
- ability slider values
- ability custom settings
- ability bindings
- cooldown lists
- left-click ability lists
- portal data
- bound inventory or bound positions
- fuel counters
- energy storage
- fluid storage
- potion container state
- copied machine settings

The backport may expose this through helper classes such as `ToolState`, `ToolStateIO`, `JDTDataKeys`, and item capability providers, but it must remain a single project-wide convention.

## Machine Framework

Machines are rebuilt on top of a shared TE framework inspired by the high-version base classes. Internal implementation may use 1.12-native abstractions, but original machine behavior, GUI, sync, rendering, inventory/filter/redstone semantics, persistence, and user-visible quirks must not be simplified away.

The base framework must cover:

- machine inventory
- filter inventory
- redstone control mode
- area settings
- tick interval
- fake-player ownership
- energy storage
- fluid storage
- packet-driven configuration changes

Priority order:

1. `Generator` and `GeneratorFluid`
2. `ItemCollector`
3. `BlockBreaker`, `BlockPlacer`, `Clicker`, `Dropper`
4. `Sensor`, `BlockSwapper`
5. `FluidCollector`, `FluidPlacer`
6. `InventoryHolder`, `ExperienceHolder`
7. `EnergyTransmitter`
8. `PlayerAccessor`
9. `ParadoxMachine`

## High-Risk Systems

### Portal Gun V2

Required behavior:

- portal pairing
- saved favorites
- stay-open mode
- linked destination persistence
- teleport cooldown handling
- cross-dimension support
- forced chunk loading while a portal exists
- original portal entity placement and collision rules, including ignoring non-colliding plants/grass and correct floor/ceiling alignment
- original velocity inheritance from pre-teleport movement sampling, not only transforming the entity's current motion vector at collision time
- original animated portal renderer behavior, including shader/procedural portal surface and flat colored frame, translated to a 1.12 renderer rather than replaced with a static texture plane
- original portal lifecycle sounds and cleanup behavior

### Time Wand

Required behavior:

- FE + Time Fluid consumption
- repeated use increases acceleration tier
- maximum configured multiplier
- one accelerator entity per target block
- target validation for tick-acceleratable blocks

### Paradox Machine

Required behavior:

- area snapshot
- block snapshot and restore
- living entity snapshot and restore
- deny filtering
- entity NBT sanitization
- configurable restrictive mob mode
- FE and fluid consumption during runtime
- paradox side effects and associated visuals/sounds where practical

### PlayerAccessor

Required behavior:

- machine access to player inventory slices
- online-player targeting
- optional accessory inventory exposure through `BubblesEX/Baubles`

### Polymorphic and NOAI-style abilities

Required behavior:

- 1.12-only entity target grouping
- no high-version stand-ins
- preserved deny behavior

## Client and Integrations

Client migration is staged:

1. required GUI and model state
2. TESR and entity renderer essentials
3. overlays and advanced visuals
4. shader and specialty rendering polish

Renderer and GUI parity are not polish. The upstream portal surface is the current concrete example: it uses a custom shader render type with `portal_shader.png` as an input texture, so a flat textured quad is a known defect, not an acceptable 1.12 simplification. The same rule applies to every original custom renderer, model, GUI, button, overlay, and visible effect.

Integrations:

- `JEI`: required
- `Patchouli`: required
- `BubblesEX/Baubles`: optional compat bridge
- `FutureMC`: optional content provider
- `Mekanism`: intentionally omitted

## Verification Standard

The backport is considered complete only when:

- The mod runs without `FutureMC`
- The mod also runs with `FutureMC`
- Upgrade progression works in both environments
- Core machines sync correctly in multiplayer
- Portal, Time Wand, and Paradox preserve their core gameplay loops
- `BubblesEX/Baubles` accessory routing is functional for intended items
- JEI and Patchouli describe the actual 1.12.2 implementation rather than the 1.21 implementation
