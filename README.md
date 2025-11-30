# Modern Companions (NeoForge 1.21.1)

![](https://i.imgur.com/V29Cq8E.jpeg)

Modern Companions is a NeoForge 1.21.1 port and rebrand of the Human Companions mod by justinwon777, maintained by MajorBonghits. The goal is full feature parity under the new mod id `modern_companions`, with updated APIs and attribution intact.

## Status
- Core code now compiles/builds on NeoForge 1.21.1 after swapping over to the new registry, SynchedEntityData, and networking APIs (gameplay is still incomplete but the project builds).
- Worldgen JSON/template/tag data and all companion structure NBTs/textures/models/lang/sounds have been migrated into the new namespace.
- Rendering + advanced gameplay behavior remain placeholder-only until the remaining AI/entity/renderer work is finished.

## Curio / Backpack Support
- **Curios (optional)**: If Curios is installed, companions expose Curio slots and a render toggle so you can hide/show equipped curios per companion. Metadata marks Curios as optional; the mod runs fine without it.
- **Sophisticated Backpacks (optional)**: When a companion wears a sophisticated backpack in the Curios back slot, all picked-up items are inserted into the backpack before the companion’s own 6×9 inventory (uses SB’s backpack IO wrapper with capability fallback).

## Morale & Bond (lightweight mood/progression)
- **Morale:** Hidden value [-1, 1]. High morale (>0.5) grants small buffs (+0.5 dmg, +0.5 armor); low morale (<-0.5) applies equal penalties. Morale rises from feeding and bond level-ups; drops on near-death and resurrection. Traits can soften loss (Jokester, Disciplined) or add situational penalties (Melancholic when morale is low). Configurable deltas in `common` config.
- **Bond:** Parallel XP track with a simple tier curve; gains XP while alive near owner (config interval), when fed, and on resurrection. Devoted/Glutton traits boost bond XP. Bond levels raise morale floor (harder to dip very low) and surface in the Journal.

## Traits (current effects)
- **Brave:** Small damage boost; follows slightly closer.  
- **Cautious:** Keeps extra follow distance; slightly slower follow speed.  
- **Guardian:** Small armor bonus; follows slightly closer.  
- **Reckless:** Small move-speed bonus; follows closer.  
- **Stalwart:** Knockback resistance.  
- **Quickstep:** Move-speed bonus; faster follow speed.  
- **Glutton:** Bonus bond XP from feeding.  
- **Disciplined:** Bonus XP gain; softer morale loss.  
- **Lucky:** Chance to duplicate one drop on kills (configurable).  
- **Night Owl:** Small damage + speed buff at night.  
- **Sun-Blessed:** Small damage + speed buff during day.  
- **Jokester:** Softer morale loss.  
- **Melancholic:** Minor damage penalty when morale is low.  
- **Devoted:** Small armor bonus; bonus bond XP (including resurrection).  
- (Legacy companions with zero traits are backfilled once; no rerolls on subsequent loads.)

## Journal / Biography Page (Traits, Backstory, Age)

![Bio Page](https://i.imgur.com/oo3xrUR.png)

- New **Journal button** on the companion GUI opens a dedicated Bio screen showing:
  - Traits (1–2 rolled at spawn, or backfilled once for legacy companions with no traits), each with a short effect blurb.
  - Backstory tag (rolled at spawn or backfilled if missing).
  - Morale descriptor and Bond level/XP.
  - Journey stats: kills, major kills, resurrections, distance traveled with owner, first hired day.
  - Age: rolled 18–35 at spawn; ages +1 year every ~90 in-game days (visual only).
- Legacy companions (zero traits) are backfilled **once** on load with traits/backstory/age—no rerolls after the initial backfill.

## Gameplay Overview

![Inventory/Curio](https://i.imgur.com/NRLqCWk.gif)

- **Finding companions:** Companion houses generate across the Overworld in houses/buildings (spacing is config-driven, default ~20 chunks). Residents spawn untamed with random name, sex, skin, base health variance, and RPG stats (STR/DEX/INT/END; a rare “specialist” rolls +5 in one stat).
- **Taming & upkeep:** Right-click an untamed companion with the exact items they request (two food/resource stacks chosen at spawn); once both reach zero they tame, follow, and unlock their GUI. Tamed companions heal with a wide pantry — cooked foods, veggies, fruits, enchanted golden foods, honey, and beneficial potions (regen/instant health, etc.), applying the effects and returning empty bottles when possible—plus they still ping the owner for food when low.
- **Commands & stances:** Shift + right-click toggles sit. Right-click opens the companion screen: follow/patrol/guard cycle, alert (hostile blacklist focus), hunt (farm animals), **sprint toggle** (on = sprint with you; off = normal run), auto-pickup toggle, clear target, release back to the wild, and patrol-radius +/- (2–32, saved per companion). Patrol/guard anchor at your current block.
- **Staying close:** Follow AI now mirrors vanilla pet recall—companions on the same dimension teleport to the nearest safe spot around you when they drift ~35 blocks away, with navigation fallback if no space is open.
- **Inventory & gear:** 6×9 personal inventory, item-magnet pickup when enabled, automatic best-armor selection, and class-aware weapon selection each tick. Friendly-fire and fall damage respect config toggles.
- **Progression:** Companions earn XP from kills; an MMO-style curve gates levels. Health scales with level and END; STR boosts damage/knockback, DEX boosts move/attack speed + light KB resist, INT speeds XP gain, END adds health + physical reduction. Kill count and XP bar show in the GUI.
- **Progression:** Companions earn XP from kills; an MMO-style curve gates levels. Health scales with level and END; STR boosts damage/knockback, DEX boosts move/attack speed + light KB resist, INT speeds XP gain, END adds health + physical reduction. Kill count and XP bar show in the GUI.
- **Limits:** There is **no level cap** and **no hard party-size limit**—you can keep leveling companions and control as many as you can recruit; practical limits are only your hardware/server performance.
- **Resurrection:** Tamed companions drop a Resurrection Scroll containing full NBT/gear instead of loose items. Activate it by right-clicking with a nether star in off-hand; then use on a block or fluid face to respawn the companion exactly at that spot with inventory intact (pet cleared for Beastmasters). Scrolls are ignored by the auto-loot magnet.
- **Spawn Gems:** All companion spawn eggs are reskinned as class-colored gems. They still behave like eggs but visually match the new branding; find them on the creative tab.
- **Custom weapons & recipes:** Modern Companions bundles a BasicWeapons-style arsenal (dagger, club, hammer, spear, quarterstaff, glaive) in every vanilla material plus optional bronze when that mod is loaded. Each weapon has a standard crafting recipe in the data pack (JEI-compatible) matching its material tier; companions auto-prefer their class weapons.
- **Custom Skins:** You can assign specific companions any skin you want! Using the command `/companionskin "NAME" URL` you can assign skins to your companions like so; `/companionskin "Daniel George" https://i.imgur.com/FWADR65.png`



## Worldgen & Spawns

![](https://i.imgur.com/ERYQEPk.jpeg)

- Companion buildings generate across the Overworld (random-spread set); each spawns exactly **one** resident when generated.
- Certain structures will house certain Companions.
- **Structure set:** All buildings are injected via `data/modern_companions/worldgen/structure_set/companion_house.json` (random_spread placement). Use `/locate structure #modern_companions:companion_houses` to find the nearest; `/place structure modern_companions:<id>` to force-generate.
- **Biome theming (examples):** acacia→savanna, sandstone→desert/badlands, terracotta→badlands only, spruce→cold taiga, dark_oak→dark forest, windmill/tower→windswept+meadow, lumber→forest/taiga mix, watermill→temperate rivers, birch/oak_birch→birch/temperate, general houses→temperate spread. See each file in `data/modern_companions/worldgen/structure/*.json` for the exact list.
- **Template pools:** Each structure JSON points to a simple pool (e.g., `raw_berserker`, `raw_house`) that drops in the matching NBT from `data/modern_companions/structure/`.

## Companion Auto-Spawning (no entities in NBT)
- Companions are spawned in code when their structure generates—nothing is baked into the NBT. `StructureCompanionSpawner` listens to chunk loads, detects our structures, and spawns exactly one resident with `MobSpawnType.STRUCTURE`.
- A SavedData guard (`StructureSpawnTracker`) records each structure placement (structure id + bounding-box center) to prevent dupes on chunk reloads.
- Structure → resident mapping:
  - Alchemist: alchemist_house
  - Beastmaster: beastmaster_house, watermill, spruce_house
  - Berserker: berserker_house, largehouse3
  - Cleric: cleric_house, church
  - Scout: scout_house, oak_birch_house
  - Stormcaller: stormcaller_house, windmill
  - Vanguard: vanguard_house, smith
  - Knight: house, oak_house, birch_house, sandstone_house
  - Archer: largehouse, acacia_house
  - Axeguard: largehouse2, dark_oak_house
  - Arbalist: lumber, terracotta_house
  - Fire Mage: tower1
  - Lightning Mage: tower1
  - Necromancer: tower2

## Class Details

![](https://i.imgur.com/HW0Zk9y.png)

- **Knight:** Balanced melee with swords/clubs/spears; reliable frontliner.
- **Vanguard:** Shielded tank; **actively raises/lowers shields against ranged threats (respecting axe breaks/cooldowns)**, +HP/KB resist, 30% projectile DR, resistance aura ticks, and taunts monsters off the owner.
- **Axeguard:** Axe bruiser; heavy hits, closes to melee.
- **Berserker:** Ramps damage as health drops, cleaves nearby foes, high KB resist, lighter armor mitigation.
- **Scout:** Fast skirmisher; speed+jump buffs, reduced fall damage, bonus damage on backstab or distracted targets.
- **Archer:** Classic bow user with ranged AI; auto-equips bows/arrows.
- **Arbalist:** 1.21 crossbow behavior (charge/cooldown/LOS); favors crossbows.
- **Beastmaster:** Bow + scaling pet; buffs nearby tamed animals and deals bonus damage to beasts.
- **Cleric:** Support melee; periodic heals/regen/resistance to allies under 65% HP, extra damage vs undead, carries totem/golden gear theme.
- **Alchemist:** Potion support; throws regen/heal at allies and weakness/slow at enemies, sometimes upgraded potency; uses dagger/staff if no potions in hand.
- **Stormcaller:** Trident brawler; calls lightning on hit (shorter cooldown in rain/thunder) and gains brief strength after striking.
- **Fire Mage:** Caster that peppers foes with precise, non-igniting blaze fireballs and occasionally fires a heavier ghast-style blast.
- **Lightning Mage:** Precision caster dropping single-target lightning and 4-target chain bursts that hit harder during storms.
- **Necromancer:** Wither-skull artillery that can raise short-lived wither skeleton allies; other companions treat the summons as friendly.

## Items & Crafting
**Weapons:** Vanilla-style recipes for every weapon/material combo; JEI/REI will show them. Bronze variants appear only when a bronze mod is loaded.

  ![](https://i.imgur.com/vJeU7FG.png)
  ![](https://i.imgur.com/wuyhvYn.png)
  ![](https://i.imgur.com/yfRNaFM.png)

  ![](https://i.imgur.com/dAwVUHw.png)
  ![](https://i.imgur.com/ebEzs9s.png)
  ![](https://i.imgur.com/kK0xqiX.png)

 **Companion Mover:** Owner-only tool that captures a companion into a glinting stored item (preserves full NBT/UUID/inventory) for redeployment.

  ![](https://i.imgur.com/wKsYkiP.png)

**Soul Gems:** A Companion's soul transferred into a Soul Gem using the **Companion Mover**.

  ![](https://i.imgur.com/1FrL94k.png)

**Summoning Wand:** Teleports all of your living companions (and Beastmaster pets) in the dimension to a safe spot near you on a short cooldown.

  ![](https://i.imgur.com/OClm2Fj.png)

**Gem Eggs:** Retextured spawn eggs available on the Modern Companions creative tab; survival acquisition is up to pack makers/datapacks.

  ![](https://i.imgur.com/nHlP3mX.png)
  ![](https://i.imgur.com/Ddy3yEk.png)

**Resurrection Scroll:** Drops only from *tamed* companions. Activation consumes one nether star (off-hand).

  ![](https://i.imgur.com/NV1urK6.png)

  ![](https://i.imgur.com/K8Zl7ka.png)

## Attribute Enchantments
**Empower, Nimbility, Enlightenment, Vitality (armor-only):** Add STR/DEX/INT/END bonuses from companion armor; stats recalc live as gear changes. Levels I-III available.

  ![](https://i.imgur.com/NDqdrXP.png)

**Availability:** Custom-textured enchanted books live on the Modern Companions creative tab and can drop from dungeon/mineshaft/stronghold library/temple/buried treasure/shipwreck loot tables.

## Config & Packmaker Notes
- Friendly fire, fall damage, spawn armor/weapon, and house spacing are configurable.
- Data pack uses pack_format 48; loot injections use NeoForge global loot modifiers for compatibility.
- Better Combat detected: reach modifiers are skipped to avoid stacking with that mod’s reach.

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

## Technical Notes
- **Recipes & tags:** All custom weapons use vanilla-style JSON recipes and standard tool/block tags; JEI/REI will list them automatically. Spawn eggs are items with gem textures; survival acquisition is left to pack makers/datapacks.
- **Resurrection:** Activation is purely item logic (nether star off-hand). Auto-loot blacklist prevents companions from grabbing their own scroll.
- **XP & health scaling:** Companions gain XP when they land killing blows (server-side event). INT raises XP gain (≈ +3% per INT over 4). A superlinear curve governs level costs: XP needed ≈ 20 + 10·(level+1)^1.35. Each level raises max health by +⅓ heart (via attribute modifier), and END grants +1 HP per point above 4, plus up to 35% physical mitigation via END-based reduction. Current health is clamped to new max on level-up; the GUI shows level, XP bar, and kills.
- **Beastmaster pets:** Pets are permanently bound, inherit STR/DEX/END scaling, and automatically respawn after a short timer if they die or unload. Respawn is suppressed while the Beastmaster is dying/dropped to a scroll to prevent dupes. Pets also avoid friendly fire from their master.
- **Random names:** Companions roll from a large male/female name pool on spawn, and Beastmaster pets get their own sizable pet-name list. Names are saved, shown on hover (not always-on), and carried through resurrection/pet respawn.

## Credits
- Original mod: Human Companions by justinwon777.
- Port & maintenance: MajorBonghits.
