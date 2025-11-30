# Modern Companions (NeoForge 1.21.1)

![Header](https://i.imgur.com/V29Cq8E.jpeg)

Modern Companions is a faithful NeoForge 1.21.1 port/rebrand of [*Human Companions*](https://www.curseforge.com/minecraft/mc-mods/human-companions) with new branding, soul gems, summoning wand, a bundled BasicWeapons-inspired arsenal, and expanded class kit flavor. Hire RPG-flavored human followers, gear them up, and command them with a clear stance UI.

## What You Get

![Inventory/Curio](https://i.imgur.com/NRLqCWk.gif)


- **Fourteen Recruitable Roles:** Knight, Vanguard, Axeguard, Berserker, Scout, Archer, Arbalist, Beastmaster, Cleric, Alchemist, Stormcaller, Fire Mage, Lightning Mage, Necromancer—each with tailored AI, weapon prefs, and perks.
- **Spawn Gems:** All spawn eggs are class-colored gems on the Modern Companions creative tab; they still behave like eggs but match the new branding.
- **Soul Gems:** Soul Gems are obtained by using the **Companion Mover** on a recruited Companion, converting them into an item preserving their stats and inventories, which can then be used to convert them back into an entity.
- **Custom Weapons:** Daggers, clubs, hammers, spears, quarterstaves, and glaives in every vanilla material (plus bronze if that mod is present). Standard crafting recipes (JEI/REI friendly); companions auto-prefer their class weapon type.
- **RPG Stats & Leveling:** STR/DEX/INT/END shape damage, speed, XP gain, and bulk. A superlinear XP curve makes high levels meaningful; health scales with END and level.
- **Custom Names:** Use a nametag on a Companion to change their name!
- **Limits:** There is **no level cap** and **no hard limit on party size**—you can level companions indefinitely and command as many as you can gather; only performance/server rules apply.
- **Commands in the GUI:** Follow ↔ Patrol ↔ Guard cycle, alert/hunt toggles, **sprint toggle** (on = sprint, off = jog), auto-pickup, patrol radius +/- (2–32), clear target, release. Shift+right-click to sit/stand, right-click to open the screen.
- **Deep Taming Loop:** Each untamed companion demands two specific foods/resources; supply both stacks to tame. They self-heal from food in their 6×9 inventory and politely ping you when hurt and empty.
- **Resurrection Built-in:** Tamed companions drop a Resurrection Scroll containing full stats/inventory. Hold a nether star in off-hand to activate (adds a glint), then use the scroll on a block/face to respawn them exactly there; Beastmaster pets are cleared on revival to avoid duplicates.
- **Beastmaster Pets:** Permanent pet (wolf/fox/cat/panda/camel/hoglin/etc.) that auto-respawns, inherits STR/DEX/END scaling, and ignores friendly fire from its master.
- **Self-care:** Along with regular veggies and cooked foods, Companions also eat enchanted golden foods/honey and will drink beneficial potions (regen/instant health, etc.), applying the effects and holding onto empty bottles when they can, dropping them when they can't.
- **Staying close:** Follow AI recalls companions that drift ~35 blocks away on the same dimension to a safe spot near you, when set to follow.
- **Custom Skins:** You can assign specific companions any skin you want! Using the command `/companionskin "NAME" URL` you can assign skins to your companions like so; `/companionskin "Daniel George" https://i.imgur.com/FWADR65.png`
- **Curios + Sophisticated Backpacks (optional):** Curio rendering is toggleable per companion. If a companion wears a Sophisticated Backpack in the Curios back slot, picked-up items fill the backpack before the companion inventory.
- **Preferred weapons & shields:** Preferred weapon types grant +2 damage; companions fall back to any available weapon if preferred gear is missing; Vanguard shield handling improved (modded shields, no double-shield bug).
- **Personality & Journal:** Companions roll 1–2 traits, a backstory, morale, and a Bond track (XP from travel/feeding/resurrection). A Journal/Bio button shows traits with effect blurbs, backstory, morale, bond, kills/major kills, resurrections, distance traveled, and age. Companions start age 18–35 and age +1 year every ~90 in-game days (visual only). Legacy companions with missing traits/backstory/age are backfilled once.
- **Morale & Bond:** Morale [-1,1] gives tiny buffs (+0.5 dmg/armor) when high and matching penalties when low; feeds, bond level-ups, and traits can raise/soften it; near-death/resurrection lowers it. Bond XP comes from time-with-owner ticks, feeding, and resurrection; Devoted/Glutton boost bond XP. Bond levels raise the morale floor.
- **Trait effects:** Brave (+dmg, closer follow); Cautious (farther follow, slower); Guardian (+armor); Reckless (+speed, closer); Stalwart (KB resist); Quickstep (+speed/follow speed); Glutton (bonus bond XP from feeding); Disciplined (+XP gain, softer morale loss); Lucky (duplicate drop chance); Night Owl (small night dmg/speed); Sun-Blessed (small day dmg/speed); Jokester (softer morale loss); Melancholic (minor dmg penalty when morale low); Devoted (+armor, bonus bond XP).

  ![Bio Page](https://i.imgur.com/oo3xrUR.png)

## Worldgen & Spawns

![](https://i.imgur.com/ERYQEPk.jpeg)

- Companion buildings generate across the Overworld (random-spread set); each spawns exactly **one** resident when generated.
- Certain structures will house certain Companions.

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

## How It Plays
1. **Find:** Companion houses generate across the Overworld (spacing ~20 chunks by default; configurable).
2. **Hire:** Right-click untamed companions with the exact items they request (shown in chat and the GUI). When both requested stacks hit zero, they tame and begin following.
3. **Command:** Use the GUI to set stance, toggle pickup, and set patrol radius; Shift+right-click to sit/stand on the spot.
4. **Equip:** Drop weapons/armor in their 6×9 inventory; they auto-equip the best armor and a class-appropriate weapon each tick.
5. **Progress:** Gain XP from kills; MMO-style curve. Level ups increase health; STR/DEX/INT/END modify combat/XP/defense.
6. **Recover:** If a tamed companion dies, grab the Resurrection Scroll they drop. With an off-hand nether star, activate, then right-click a surface to respawn them exactly there.
7. **Regroup fast:** If they drift out ~35 blocks, built-in recall snaps them to you; the Summoning Wand can pull every companion (and Beastmaster pet) in the dimension instantly.

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

## Technical Notes
- **Recipes & tags:** All custom weapons use vanilla-style JSON recipes and standard tool/block tags; JEI/REI will list them automatically. Spawn eggs are items with gem textures; survival acquisition is left to pack makers/datapacks.
- **Resurrection:** Activation is purely item logic (nether star off-hand). Auto-loot blacklist prevents companions from grabbing their own scroll.
- **XP & health scaling:** Companions gain XP when they land killing blows (server-side event). INT raises XP gain (≈ +3% per INT over 4). A superlinear curve governs level costs: XP needed ≈ 20 + 10·(level+1)^1.35. Each level raises max health by +⅓ heart (via attribute modifier), and END grants +1 HP per point above 4, plus up to 35% physical mitigation via END-based reduction. Current health is clamped to new max on level-up; the GUI shows level, XP bar, and kills.
- **Beastmaster pets:** Pets are permanently bound, inherit STR/DEX/END scaling, and automatically respawn after a short timer if they die or unload. Respawn is suppressed while the Beastmaster is dying/dropped to a scroll to prevent dupes. Pets also avoid friendly fire from their master.
- **Random names:** Companions roll from a large male/female name pool on spawn, and Beastmaster pets get their own sizable pet-name list. Names are saved, shown on hover (not always-on), and carried through resurrection/pet respawn.

## FAQ
- **Do companions take fall damage?** Toggle in config (default on).
- **Can they hurt me/each other?** Player FF off by default; companion FF off by default; both configurable.
- **Where are recipes?** Standard JSON recipes; view with JEI/REI. Resurrection uses nether star activation.

***Take a squad with you, keep them fed, and enjoy the extra firepower. Feedback and balance reports are welcome!***


## Credits

- [Human Companions - justinwon777](https://www.curseforge.com/minecraft/mc-mods/human-companions)
- [Basic Weapons - Khazoda](https://www.curseforge.com/minecraft/mc-mods/basicweapons)
