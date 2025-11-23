# Modern Companions (NeoForge 1.21.1)

Modern Companions is a faithful NeoForge 1.21.1 port/rebrand of *Human Companions* with new branding, gem-style spawn eggs, a bundled BasicWeapons-inspired arsenal, and expanded class kit flavor. Hire RPG-flavored human followers, gear them up, and command them with a clear stance UI.

## What You Get
- **Eleven Recruitable Roles:** Knight, Vanguard, Axeguard, Berserker, Scout, Archer, Arbalist, Beastmaster, Cleric, Alchemist, Stormcaller —each with tailored AI, weapon prefs, and perks.
- **Spawn Gems:** All spawn eggs are class-colored gems on the Modern Companions creative tab; they still behave like eggs but match the new branding.
- **Custom Weapons:** Daggers, clubs, hammers, spears, quarterstaves, and glaives in every vanilla material (plus bronze if that mod is present). Standard crafting recipes (JEI/REI friendly); companions auto-prefer their class weapon type.
- **RPG Stats & Leveling:** STR/DEX/INT/END shape damage, speed, XP gain, and bulk. A superlinear XP curve makes high levels meaningful; health scales with END and level.
- **Commands in the GUI:** Follow ↔ Patrol ↔ Guard cycle, alert/hunt toggles, stationary mode, auto-pickup, patrol radius +/- (2–32), clear target, release. Shift+right-click to sit/stand, right-click to open the screen.
- **Deep Taming Loop:** Each untamed companion demands two specific foods/resources; supply both stacks to tame. They self-heal from food in their 6×9 inventory and politely ping you when hurt and empty.
- **Resurrection Built-in:** Tamed companions drop a Resurrection Scroll containing full NBT/gear. Hold a nether star in off-hand to activate (adds a glint), then use the scroll on a block/face to respawn them exactly there; Beastmaster pets are cleared on revival to avoid duplicates.
- **Beastmaster Pets:** Permanent pet (wolf/fox/cat/panda/camel/hoglin/etc.) that auto-respawns, inherits STR/DEX/END scaling, and ignores friendly fire from its master.

## Worldgen & Spawns
- Companion buildings generate across the Overworld (random-spread set); each spawns exactly **one** resident when generated.
- Class-by-structure:
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

## Class Details
- **Knight:** Balanced melee with swords/clubs/spears; reliable frontliner.
- **Vanguard:** Shielded tank; +HP/KB resist, 30% projectile DR, resistance aura ticks, and taunts monsters off the owner.
- **Axeguard:** Axe bruiser; heavy hits, closes to melee.
- **Berserker:** Ramps damage as health drops, cleaves nearby foes, high KB resist, lighter armor mitigation.
- **Scout:** Fast skirmisher; speed+jump buffs, reduced fall damage, bonus damage on backstab or distracted targets.
- **Archer:** Classic bow user with ranged AI; auto-equips bows/arrows.
- **Arbalist:** 1.21 crossbow behavior (charge/cooldown/LOS); favors crossbows.
- **Beastmaster:** Bow + scaling pet; buffs nearby tamed animals and deals bonus damage to beasts.
- **Cleric:** Support melee; periodic heals/regen/resistance to allies under 65% HP, extra damage vs undead, carries totem/golden gear theme.
- **Alchemist:** Potion support; throws regen/heal at allies and weakness/slow at enemies, sometimes upgraded potency; uses dagger/staff if no potions in hand.
- **Stormcaller:** Trident brawler; calls lightning on hit (shorter cooldown in rain/thunder) and gains brief strength after striking.

## How It Plays
1. **Find:** Companion houses generate across the Overworld (spacing ~20 chunks by default; configurable).
2. **Hire:** Right-click untamed companions with the exact items they request (shown in chat and the GUI). When both requested stacks hit zero, they tame and begin following.
3. **Command:** Use the GUI to set stance, toggle pickup, and set patrol radius; Shift+right-click to sit/stand on the spot.
4. **Equip:** Drop weapons/armor in their 6×9 inventory; they auto-equip the best armor and a class-appropriate weapon each tick.
5. **Progress:** Gain XP from kills; MMO-style curve. Level ups increase health; STR/DEX/INT/END modify combat/XP/defense.
6. **Recover:** If a tamed companion dies, grab the Resurrection Scroll they drop. With an off-hand nether star, activate, then right-click a surface to respawn them exactly there.

## Items & Crafting
- **Weapons:** Vanilla-style recipes for every weapon/material combo; JEI/REI will show them. Bronze variants appear only when a bronze mod is loaded.
- **Gem eggs:** Available on the Modern Companions creative tab; survival acquisition is up to pack makers/datapacks.
- **Resurrection Scroll:** Drops only from *tamed* companions. Activation consumes one nether star (off-hand).

## Config & Packmaker Notes
- Friendly fire, fall damage, spawn armor/weapon, and house spacing are configurable.
- Data pack uses pack_format 48; loot injections use NeoForge global loot modifiers for compatibility.
- Better Combat detected: reach modifiers are skipped to avoid stacking with that mod’s reach.

## Requirements
- Minecraft 1.21.1
- NeoForge 21.1.1

## Technical Notes
- **Configs:** `config/modern_companions-common.toml` — worldgen spacing (`averageHouseSeparation`, default 20 chunks), friendly fire toggles (`friendlyFirePlayer`, `friendlyFireCompanions`), fall damage, spawn armor/weapon, base health, low-health food requests, creeper warning.
- **Compatibility hooks:** If Better Combat is loaded, weapon reach modifiers are skipped to avoid double-stacking. Bronze weapon set registers only when a bronze mod is present (checked via `ModList`).
- **Recipes & tags:** All custom weapons use vanilla-style JSON recipes and standard tool/block tags; JEI/REI will list them automatically. Spawn eggs are items with gem textures; survival acquisition is left to pack makers/datapacks.
- **Resurrection:** Activation is purely item logic (nether star off-hand). Auto-loot blacklist prevents companions from grabbing their own scroll.
- **XP & health scaling:** Companions gain XP when they land killing blows (server-side event). INT raises XP gain (≈ +3% per INT over 4). A superlinear curve governs level costs: XP needed ≈ 20 + 10·(level+1)^1.35. Each level raises max health by +⅓ heart (via attribute modifier), and END grants +1 HP per point above 4, plus up to 35% physical mitigation via END-based reduction. Current health is clamped to new max on level-up; the GUI shows level, XP bar, and kills.
- **Beastmaster pets:** Pets are permanently bound, inherit STR/DEX/END scaling, and automatically respawn after a short timer if they die or unload. Respawn is suppressed while the Beastmaster is dying/dropped to a scroll to prevent dupes. Pets also avoid friendly fire from their master.
- **Random names:** Companions roll from a large male/female name pool on spawn, and Beastmaster pets get their own sizable pet-name list. Names are saved, shown on hover (not always-on), and carried through resurrection/pet respawn.

## FAQ
- **Do companions take fall damage?** Toggle in config (default on).
- **Can they hurt me/each other?** Player FF off by default; companion FF off by default; both configurable.
- **Where are recipes?** Standard JSON recipes; view with JEI/REI. Resurrection uses nether star activation—no smithing template needed.

Take a squad with you, keep them fed, and enjoy the extra firepower. Feedback and balance reports are welcome!***
