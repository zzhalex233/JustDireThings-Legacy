# Recipe Data Migration

The upstream generated recipe contract is `source/JustDireThings-main/src/generated/resources/data/justdirethings/recipe`.

Current 1.12 output lives in `src/main/resources/assets/justdirethings/recipes`.

## Converted

- `184` recipes were converted by `scripts/Convert-UpstreamRecipes.ps1`.
- Converted types are `minecraft:crafting_shaped`, `minecraft:crafting_shapeless`, and `minecraft:smelting`.
- Modern recipe result objects using `id` are translated to 1.12 result objects using `item`.
- Supported modern tags are translated to 1.12 Forge OreDictionary ingredients:
- `c:dusts/redstone` -> `dustRedstone`
- `minecraft:leaves` -> `treeLeaves`
- `minecraft:wool` -> `blockWool`

## Covered By Current Upgrade Station Logic

- `151` `justdirethings:ability` recipes are represented by generic 1.12 ability install recipes in `ModRecipes`.
- `2` `justdirethings:paxel` recipes are represented by 1.12 paxel fusion recipes in `ModRecipes`.
- `31` `minecraft:smithing_transform` recipes are represented by 1.12 tier upgrade recipes in `ModRecipes`.
- `UpgradeStationSourceDataParityTest` checks these upstream generated recipes against the current 1.12 upgrade station recipe table.

These are not counted as fully ported data yet because the JSON loader/datapack path is not implemented; they are covered behaviorally by the current in-memory bridge.

## Still Pending

- `14` `justdirethings:goospread` recipes and `1` `goospread_tag` recipe need the goo spread loader.
- `6` `justdirethings:fluiddrop` recipes need the fluid drop loader.
- `3` `minecraft:blasting` recipes have matching smelting coverage and need an explicit 1.12 decision before adding duplicate furnace recipes.
