# JustDireThings Source Parity Matrix

Status values: `missing`, `stub`, `resource-only`, `wrong-id`, `visual-bug`, `partial`, `ported`, `intentional-omit`.

This matrix is the human companion to `RegistryParityTest`. It should be updated whenever a feature moves from missing/stubbed to implemented.

## Matrix Promotion Rules

- Source parity applies to every upstream feature, not just portal systems. A feature remains `missing`, `stub`, `resource-only`, `visual-bug`, `wrong-id`, or `partial` until all upstream-visible behavior is restored or the project owner explicitly accepts a scoped omission.
- No feature may move to `partial` unless at least one real behavior path exists and is testable in-game or by automated test.
- No feature may move to `ported` unless registry, resources, behavior, localization, GUI/network where applicable, and acquisition path are complete.
- Class-name anchors count as `stub`, not `ported`.
- Copied textures/models count as `resource-only`, not `ported`.
- Recipe type/serializer catalog entries count as `stub` until a 1.12 loader and data path exist.
- Dynamic upstream renderers, GUI layouts, buttons, sync packets, sounds, state inheritance, and persistence count as required feature behavior, not polish.

## Current High-Risk Gaps

| Area | Upstream Contract | Current Status | Next Implementation Phase |
| --- | --- | --- | --- |
| Registry audit gate | `Registration.java` defines the full block/item/fluid/entity/screen/recipe/sound contract | partial | Phase 0 |
| Item Collector model | Non-occluding directional shape, no 1x1x1 shell | visual-bug | Phase 1.1 |
| GUI background | Modern nine-slice `textures/gui/sprites/background.png.mcmeta` | visual-bug in 1.12 because metadata is ignored | Phase 1.2 |
| GUI localization | All screen text uses lang keys | partial | Phase 1.3 and Phase 3 |
| Machine GUI buttons | Original button framework with toggle/number/grayscale controls and server settings packets | partial | Phase 3.2 |
| Upgrade Station | 1.12 bridge for original upgrade/smithing/ability flow | partial | Phase 1.4 |
| T1/T2 machines | Original separate T1/T2 machine blocks, containers, screens, BEs | missing/partial/wrong-id | Phase 2 and Phase 4 |
| Goo/gel gameplay | Goo blocks, goo soils, goo recipes, fluid drops, eclipse gate | missing | Phase 5 |
| Charged items | Canisters, wands, portal guns, tools, armor, ability energy/fluid use | partial/stub | Phase 6 |
| Special item registration | Portal gun, creature catcher, machine settings copier, wands, death recall totem | partial | Phase 2.3 and Phase 6 |
| Portal/paradox/time entities | All upstream entities and renderers; portal renderer must be shader/procedural, velocity inheritance must match upstream | partial/missing | Phase 8 |
| Portal renderer | Upstream `PortalEntityRender` uses custom `portal_entity` shader render type with `portal_shader.png` as shader input and a flat colored frame | visual-bug | Phase 8.2 and Phase 10.2 |
| Portal velocity inheritance | Upstream `PortalEntity` samples entity positions in a forward velocity box before teleport and syncs transformed motion after teleport | missing/partial | Phase 8.2 |
| JEI/Patchouli | Custom recipe categories and guide content | missing | Phase 9 |

## Silent Placeholder Foundation

These rows are intentionally noisy. They mark code that exists for compile, registry, or resource parity but must not be treated as implemented gameplay.

### Special Item Shells

| Feature | Upstream Contract | Current Status | Next Implementation Phase |
| --- | --- | --- | --- |
| `totem_of_death_recall` | `TotemOfDeathRecall` death/recall behavior | partial | Phase 6.4 |
| `blazejet_wand` | `BlazejetWand` durability and ability registration present; ability actions still stubbed | partial | Phase 6.3 and Phase 6.4 |
| `voidshift_wand` | `VoidshiftWand` durability, FE storage, and ability registration present; ability actions still stubbed | partial | Phase 6.3 and Phase 6.4 |
| `eclipsegate_wand` | `EclipsegateWand` durability, FE storage, and ability registration present; eclipse gate action still stubbed | partial | Phase 5.2, Phase 6.3, and Phase 6.4 |
| `creaturecatcher` | `CreatureCatcher` item is throwable, preserves captured-entity NBT, captures 1.12 `EntityLiving` mobs, releases stored mobs on block impact, and drops the correct return item; upstream shrinking/growing mob render animation still needs full renderer parity | partial | Phase 8.1 and Phase 10.2 |
| `machinesettingscopier` | `MachineSettingsCopier` copy/paste settings flow implemented; GUI still a stub anchor | partial | Phase 3.3 and Phase 6.4 |
| `portalgun` | Classic `PortalGun` FE/UUID, shift-close, projectile firing, and left-click packet path present; source parity still requires sounds, exact collision/placement, velocity inheritance, and shader portal renderer | partial | Phase 8.2 |
| `polymorphic_wand` | `PolymorphicWand` durability, polymorphic-fluid storage, and ability registration present; polymorph action still stubbed | partial | Phase 6.3 and Phase 6.4 |

### Ability Action Shells

| Feature | Upstream Contract | Current Status | Next Implementation Phase |
| --- | --- | --- | --- |
| `AbilityMethods` use actions | `MOBSCANNER`, `ORESCANNER`, `LAWNMOWER`, `INVULNERABILITY`, `CAUTERIZEWOUNDS`, `AIRBURST`, `GROUNDSTOMP`, `STUPEFY`, `POLYMORPH_RANDOM`, `VOIDSHIFT`, `OREXRAY`, `GLOWING`, `DEBUFFREMOVER`, `EARTHQUAKE`, `NOAI`, `POLYMORPH_TARGET`, `EPICARROW` | stub | Phase 6.1 and Phase 6.3 |
| `AbilityMethods.DECOY` | Spawns a decoy, assigns owner/name, plays summon sound, and writes an active cooldown; full armor ability parameter and energy-cost parity still pending | partial | Phase 6.1 and Phase 6.3 |
| `AbilityMethods` use-on actions | `LEAFBREAKER`, `ECLIPSEGATE` | stub | Phase 5.2 and Phase 6.3 |

### Advanced Machine Shells

| Feature | Upstream Contract | Current Status | Next Implementation Phase |
| --- | --- | --- | --- |
| `BlockBreakerT2BE` | Powered area/filter block breaking | stub | Phase 4.2 |
| `BlockPlacerT2BE` | Powered area/filter block placement | stub | Phase 4.2 |
| `BlockSwapperT2BE` | Powered area/filter swap behavior | stub | Phase 4.2 |
| `ClickerT2BE` | Powered area/filter fake-player click behavior | stub | Phase 4.2 |
| `DropperT2BE` | Powered area/filter dropping behavior | stub | Phase 4.2 |
| `FluidCollectorT2BE` | Powered area/filter fluid collection | stub | Phase 4.2 |
| `FluidPlacerT2BE` | Powered area/filter fluid placement | stub | Phase 4.2 |
| `SensorT2BE` | Powered area sensing | stub | Phase 4.2 |

### GUI Anchor Shells

| Feature | Upstream Contract | Current Status | Next Implementation Phase |
| --- | --- | --- | --- |
| `ToolSettingScreen` | Full tool/armor ability configuration screen | stub | Phase 3.3 and Phase 6.3 |
| `MachineSettingsCopierScreen` | Full copy/paste machine settings screen | stub | Phase 3.3 and Phase 6.4 |
| `AdvPortalRadialMenu` | Advanced portal radial favorite/select menu | stub | Phase 8.2 |
| `AdvPortalEditMenu` | Advanced portal destination edit screen | stub | Phase 8.2 |

### Entity Shells

| Feature | Upstream Contract | Current Status | Next Implementation Phase |
| --- | --- | --- | --- |
| `CreatureCatcherEntity` | Throwable capture/release projectile behavior present; mob shrinking/growing render parity still pending | partial | Phase 8.1 and Phase 10.2 |
| `JustDireArrow` | Ability-aware custom arrow now extends 1.12 tipped arrow with synced potion/splash/lingering/homing/phase/epic/hostile state, NBT, phase block bypass, and basic homing; full upstream splash/lingering filtering, multi-target pierce, and ability launch integration still pending | partial | Phase 8.1 and Phase 6.3 |
| `DecoyEntity` | Decoy/bait entity has owner UUID, lifetime, periodic mob aggro, invulnerability, NBT, renderer, and summon ability hook; exact online skin resolution and armor ability parameter parity still pending | partial | Phase 8.1, Phase 6.3, and Phase 10.2 |
| `JustDireAreaEffectCloud` | JDT lingering area-effect cloud now delegates to 1.12 vanilla area-effect cloud behavior with registered renderer; owner-based beneficial/harmful filtering parity still pending | partial | Phase 8.1 |

### Recipe Loader Shells

| Feature | Upstream Contract | Current Status | Next Implementation Phase |
| --- | --- | --- | --- |
| `goospreadrecipe` | Goo spread recipe loader and data | stub | Phase 5.3 and Phase 9.1 |
| `goospreadrecipe_tag` | Tag-aware goo spread recipe loader and data | stub | Phase 5.3 and Phase 9.1 |
| `fluiddroprecipe` | Fluid drop recipe loader and data | stub | Phase 5.3 and Phase 9.1 |
| `abilityrecipe` | Ability install recipe loader and data | stub | Phase 6.1 and Phase 9.1 |
| `paxelrecipe` | Paxel recipe loader and data | stub | Phase 6.3 and Phase 9.1 |

### Generated Data Resource Gap

| Feature | Upstream Contract | Current Status | Next Implementation Phase |
| --- | --- | --- | --- |
| upstream generated recipes: 392 | Convert modern `data/justdirethings/recipe` JSONs/datagen output to 1.12 `assets/justdirethings/recipes` and custom loaders where needed | missing | Phase 9.1 |
| converted vanilla 1.12 recipes: 184 | `minecraft:crafting_shaped`, `minecraft:crafting_shapeless`, and `minecraft:smelting` subset generated by `scripts/Convert-UpstreamRecipes.ps1` into `assets/justdirethings/recipes` | partial | Phase 9.1 |
| custom/modern recipes still pending: 208 | `ability` 151, `paxel` 2, and `smithing_transform` 31 are covered by current upgrade-station logic tests but still need source-derived loaders/data; `fluiddrop` 6, `goospread` 14, `goospread_tag` 1, and `blasting` 3 still require 1.12-specific handling | partial | Phase 5.3, Phase 6.1, and Phase 9.1 |
| upstream generated advancements: 392 | Decide whether to port, omit, or translate to 1.12-compatible advancements | missing | Phase 9.1 |
| upstream generated loot tables: 57 | Port block/entity loot into 1.12-compatible loot resources | missing | Phase 9.1 |
| upstream generated tags: 29 | Translate item/block/fluid tags to OreDictionary, Forge tags where available, or explicit recipe fallback lists | missing | Phase 9.1 and Phase 11.1 |
| current data/justdirethings: 0 | Current tree has no modern data resources; 1.12 recipes should target `assets/justdirethings/recipes` | missing | Phase 9.1 |

## Phase 0.5 Audit Gate Coverage

The automated parity gate now requires every upstream container, network class, and capability class to be either implemented in current source or named here as a tracked gap.

### Upstream Containers

Status: `stub`/`partial` until each original container layout and server validation path is ported.

```text
BaseContainer
BaseMachineContainer
BlockBreakerT1Container
BlockBreakerT2Container
BlockPlacerT1Container
BlockPlacerT2Container
BlockSwapperT1Container
BlockSwapperT2Container
ClickerT1Container
ClickerT2Container
DropperT1Container
DropperT2Container
EnergyTransmitterContainer
ExperienceHolderContainer
FilterBasicHandler
FilterBasicSlot
FluidCollectorT1Container
FluidCollectorT2Container
FluidPlacerT1Container
FluidPlacerT2Container
FuelCanisterContainer
FuelCanisterHandler
FuelSlot
GeneratorFluidT1Container
GeneratorT1Container
InventoryHolderContainer
InventoryHolderSlot
ItemCollectorContainer
ParadoxMachineContainer
PlayerAccessorContainer
PlayerHandler
PocketGeneratorContainer
PotionCanisterContainer
PotionCanisterHandler
RefinedFuelSlot
SensorT1Container
SensorT2Container
ToolSettingContainer
```

### Upstream Network Classes

Status: `stub`/`missing` until each packet has a 1.12 `SimpleNetworkWrapper` equivalent and the related GUI/server behavior is wired.

```text
PacketHandler
AreaAffectingPacket
AreaAffectingPayload
BlockStateFilterPacket
BlockStateFilterPayload
BreakerPacket
BreakerPayload
ClickerPacket
ClickerPayload
ClientSoundPacket
ClientSoundPayload
CopyMachineSettingsPacket
CopyMachineSettingsPayload
DirectionSettingPacket
DirectionSettingPayload
DropperSettingPacket
DropperSettingPayload
EnergyTransmitterPacket
EnergyTransmitterSettingPayload
ExperienceHolderPacket
ExperienceHolderPayload
ExperienceHolderSettingsPacket
ExperienceHolderSettingsPayload
FilterSettingPacket
FilterSettingPayload
GhostSlotPacket
GhostSlotPayload
InventoryHolderMoveItemsPacket
InventoryHolderMoveItemsPayload
InventoryHolderSaveSlotPacket
InventoryHolderSaveSlotPayload
InventoryHolderSettingsPacket
InventoryHolderSettingsPayload
ItemCollectorSettingsPacket
ItemCollectorSettingsPayload
LeftClickPacket
LeftClickPayload
ParadoxMachineSnapshotPayload
ParadoxRenderPacket
ParadoxRenderPayload
ParadoxSnapshotPacket
ParadoxSyncPacket
ParadoxSyncPayload
PlayerAccessorPacket
PlayerAccessorPayload
PortalGunFavoriteChangePacket
PortalGunFavoriteChangePayload
PortalGunFavoritePacket
PortalGunFavoritePayload
PortalGunLeftClickPacket
PortalGunLeftClickPayload
RedstoneSettingPacket
RedstoneSettingPayload
SensorPacket
SensorPayload
SwapperPacket
SwapperPayload
TickSpeedPacket
TickSpeedPayload
ToggleToolLeftRightClickPacket
ToggleToolLeftRightClickPayload
ToggleToolPacket
ToggleToolPayload
ToggleToolRefreshSlots
ToggleToolRefreshSlotsPacket
ToggleToolSlotPacket
ToggleToolSlotPayload
ToolSettingsGUIPacket
ToolSettingsGUIPayload
```

### Upstream Capability Classes

Status: `stub`/`partial` until 1.12 Forge capability wrappers match the original item, fluid, energy, and machine semantics.

```text
EnergyStorageItemstack
EnergyStorageItemStackNoReceive
EnergyStorageNoReceive
ExperienceHolderFluidTank
GeneratorFluidItemHandler
GeneratorItemHandler
InventoryHolderItemHandler
JustDireFluidTank
MachineEnergyStorage
TransmitterEnergyStorage
```

## Deliberate Omissions

| Area | Reason |
| --- | --- |
| Mekanism integration | User chose to drop it because upstream has no meaningful content there. |
| Direct Curios integration | Replaced by BubblesEX/Baubles adapter for 1.12 packs. |
| Biome/mob worldgen | User chose to ignore creature/worldgen side unless item/block recipes need them. |
