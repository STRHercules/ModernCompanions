# Modern Companions (NeoForge 1.21.1)

Modern Companions is a NeoForge 1.21.1 port and rebrand of the Human Companions mod by justinwon777, maintained by MajorBonghits. The goal is full feature parity under the new mod id `modern_companions`, with updated APIs and attribution intact.

## Status
- Core code now compiles/builds on NeoForge 1.21.1 after swapping over to the new registry, SynchedEntityData, and networking APIs (gameplay is still incomplete but the project builds).
- Worldgen JSON/template/tag data and all companion structure NBTs/textures/models/lang/sounds have been migrated into the new namespace.
- Rendering + advanced gameplay behavior remain placeholder-only until the remaining AI/entity/renderer work is finished.

## Gameplay Overview
- **Finding companions:** Companion houses generate across the Overworld in houses/buildings (spacing is config-driven, default ~20 chunks). Residents spawn untamed with random name, sex, skin, base health variance, and RPG stats (STR/DEX/INT/END; a rare “specialist” rolls +5 in one stat).
- **Taming & upkeep:** Right-click an untamed companion with the exact items they request (two food/resource stacks chosen at spawn); once both reach zero they tame, follow, and unlock their GUI. Tamed companions heal themselves by eating any food in their 54-slot inventory and will ping the owner for food when low.
- **Commands & stances:** Shift + right-click toggles sit. Right-click opens the companion screen: follow/patrol/guard cycle, alert (hostile blacklist focus), hunt (farm animals), stationary, auto-pickup toggle, clear target, release back to the wild, and patrol-radius +/- (2–32, saved per companion). Patrol/guard anchor at your current block.
- **Inventory & gear:** 6×9 personal inventory, item-magnet pickup when enabled, automatic best-armor selection, and class-aware weapon selection each tick. Friendly-fire and fall damage respect config toggles.
- **Progression:** Companions earn XP from kills; an MMO-style curve gates levels. Health scales with level and END; STR boosts damage/knockback, DEX boosts move/attack speed + light KB resist, INT speeds XP gain, END adds health + physical reduction. Kill count and XP bar show in the GUI.
- **Resurrection:** Tamed companions drop a Resurrection Scroll containing full NBT/gear instead of loose items. Activate it by right-clicking with a nether star in off-hand; then use on a block or fluid face to respawn the companion exactly at that spot with inventory intact (pet cleared for Beastmasters). Scrolls are ignored by the auto-loot magnet.
- **Spawn Gems:** All companion spawn eggs are reskinned as class-colored gems. They still behave like eggs but visually match the new branding; find them on the creative tab.
- **Custom weapons & recipes:** Modern Companions bundles a BasicWeapons-style arsenal (dagger, club, hammer, spear, quarterstaff, glaive) in every vanilla material plus optional bronze when that mod is loaded. Each weapon has a standard crafting recipe in the data pack (JEI-compatible) matching its material tier; companions auto-prefer their class weapons.

## Companion Classes
- **Knight:** Balanced melee with swords/clubs/spears; standard tanky frontliner.
- **Vanguard:** Knight variant with shield; extra max health + knockback resist, 30% projectile reduction, periodic resistance aura to allies, and taunts monsters off the owner.
- **Axeguard:** Axe-first bruiser; auto-equips axes and closes to melee.
- **Berserker:** High-risk DPS; lighter armor, high KB resist, bonus damage ramps as health drops, cleaves nearby foes on hit.
- **Scout:** Fast hit-and-run; passive speed+jump buffs, reduced fall damage, extra damage when backstabbing or target is focused elsewhere.
- **Archer:** Bow user with ranged attack goal; auto-equips bows and arrows.
- **Arbalist:** Crossbow specialist using 1.21 charge/cooldown behavior; prefers crossbows and fires standard arrows.
- **Beastmaster:** Ranged companion with a bound pet (wolves, foxes, cats, pandas, camels, hoglins, etc.) that respawns automatically, won’t be friendly-fired, and scales with the master’s stats; buffs nearby tamed animals and deals bonus damage to beasts.
- **Cleric:** Support melee; carries golden/quarterstaff + totem, periodically heals/regen allies under 65% HP, grants brief resistance/regen pulses, and deals extra damage to undead.
- **Alchemist:** Splash-potion support; throws regen/heal at allies and weakness/slow at enemies, occasionally upgrading effects; uses daggers/staves if no potions in hand.
- **Stormcaller:** Trident brawler that calls lightning on hit (shorter cooldown in rain/thunder), gaining brief strength after striking.

## Worldgen & Structures
- **Structure set:** All buildings are injected via `data/modern_companions/worldgen/structure_set/companion_house.json` (random_spread placement). Use `/locate structure #modern_companions:companion_houses` to find the nearest; `/place structure modern_companions:<id>` to force-generate.
- **Biome theming (examples):** acacia→savanna, sandstone→desert/badlands, terracotta→badlands only, spruce→cold taiga, dark_oak→dark forest, windmill/tower→windswept+meadow, lumber→forest/taiga mix, watermill→temperate rivers, birch/oak_birch→birch/temperate, general houses→temperate spread. See each file in `data/modern_companions/worldgen/structure/*.json` for the exact list.
- **Template pools:** Each structure JSON points to a simple pool (e.g., `raw_berserker`, `raw_house`) that drops in the matching NBT from `data/modern_companions/structure/`.

## Companion Auto-Spawning (no entities in NBT)
- Companions are spawned in code when their structure generates—nothing is baked into the NBT. `StructureCompanionSpawner` listens to chunk loads, detects our structures, and spawns exactly one resident with `MobSpawnType.STRUCTURE`.
- A SavedData guard (`StructureSpawnTracker`) records each structure placement (structure id + bounding-box center) to prevent dupes on chunk reloads.
- Structure → resident mapping:
  - Alchemist: alchemist_house, tower1
  - Beastmaster: beastmaster_house, watermill, spruce_house
  - Berserker: berserker_house, largehouse3
  - Cleric: cleric_house, church
  - Scout: scout_house, tower2, oak_birch_house
  - Stormcaller: stormcaller_house, windmill
  - Vanguard: vanguard_house, smith
  - Knight: house, oak_house, birch_house, sandstone_house
  - Archer: largehouse, acacia_house
  - Axeguard: largehouse2, dark_oak_house
  - Arbalist: lumber, terracotta_house

## Requirements
- Java 21 (JDK 21)
- Minecraft 1.21.1
- NeoForge 21.1.1 (see `gradle.properties` for exact versions)

## Build & Run
```bash
./gradlew build        # builds the mod jar
./gradlew runClient    # launches the NeoForge dev client
```

## Development Notes
- Source lives in `src/`; other top-level directories are read-only references.
- Version is managed in `gradle.properties` and must be bumped with each change (per AGENTS.md).
- Companion behavior toggles now use NeoForge's payload system (`ToggleFlagPayload` registered in `ModNetwork`, emitted by `CompanionScreen`)—follow this pattern for future GUI actions.
- Worldgen/structure data now resides under `data/modern_companions`; ensure future resources use the same namespace.

## Credits
- Original mod: Human Companions by justinwon777.
- Port & maintenance: MajorBonghits.
