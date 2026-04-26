# JustDireThings Legacy Historical Issues

This file tracks defects caused by early incomplete backport scaffolding. It is not the final source-parity matrix; use `docs/audit/justdirethings-source-parity-matrix.md` for the authoritative feature status.

## Current Phase 0 Rule

Early foundations may remain temporarily only when they are explicit. A class, registry entry, model, or recipe ID that exists only as an anchor must be marked as a parity stub and listed in the matrix.

Source parity applies globally. Portal is only the current visible example; machines, tools, armor, abilities, goo/gel systems, recipes, JEI/Patchouli, integrations, GUIs, renderers, localization, and assets must all be restored from upstream rather than approximated.

## Placeholder Foundation To Clean First

- Special items still using `ItemParityStub`: none currently; remaining special-item gaps are partial behavior work, not silent item stubs.
- Ability actions still routed to `notYetImplemented`: scanners, lawnmower, invulnerability, cauterize wounds, air burst, ground stomp, stupefy, polymorph actions, void shift, ore xray, glowing, debuff remover, earthquake, no AI, epic arrow, leaf breaker, and eclipse gate. Decoy now has a real summon path, but armor ability parameter and energy-cost parity remain partial.
- Advanced T2 machine classes that currently inherit T1/simple behavior instead of porting upstream powered area/filter behavior.
- GUI class-name anchors that are not source-parity screens yet, especially tool settings, machine settings copier, and advanced portal menus.
- Entity anchors that are registered but do not implement upstream behavior: none currently. Creature catcher, JustDire arrow, decoy, and JDT area effect cloud are promoted to partial behavior and still need the exact upstream edge cases documented in the source-parity matrix.
- Recipe type and serializer IDs that exist only for catalog parity without 1.12 loaders and data.
- Portal implementation has been promoted beyond its original historical approximation debt: projectile firing/placement, cross-dimensional transfer, velocity inheritance, shader/procedural surface, and frame rendering are now implemented. Advanced portal menus/favorites remain tracked separately under the global source-parity matrix.

## Status Rules

- `stub` means the feature is registered or named but behavior is absent or materially incomplete.
- `resource-only` means assets/models/textures exist but behavior is absent.
- `partial` requires at least one real behavior path that can be tested.
- `ported` requires registry, resources, behavior, localization, GUI/network where applicable, and acquisition path.

## Notes For Future Workers

- Do not treat class-name wrappers under `client/gui/upstream` as complete screens.
- Do not replace special-item behavior with `ItemSimpleContent`; use `ItemParityStub` until a real item class is ported.
- Do not treat `SourceParityCatalog` registration coverage as behavior parity.
- Do not move recipes/data ahead of this cleanup phase; otherwise missing systems will be hidden behind craftable but nonfunctional content.
