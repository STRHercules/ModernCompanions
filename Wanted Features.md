# Wanted Features

## Class Additions
* Sneaky Stealth Rogue Class

## Weapon Usage Expansion
* Classes will PREFER their preferred weapons, but will use anything given to them - using their preferred weapons first.

## New Races
* Orcs
* Elves
    * Strictly skin based, maybe specific names if rolled with a unique skin

## Companion Depth & Personality
* Personality Traits System
    * Give each companion 1–2 lightweight traits rolled at spawn:
    * Examples:
        * Brave (+small KB resist, more likely to push into melee)
        * Cautious (keeps slightly larger follow distance, kites more)
        * Glutton (asks for food more often, gets bonus healing)
        * Disciplined (+XP gain from kills, less idle wandering)
    * Traits could just be NBT flags + flavor lines in the GUI; no need for full mood simulation.
    * Synergy with your random names + stat rolls, adds replay value and “this one is special” feeling.
    * Bond / Loyalty Track
        * We already have XP and levels—this would be parallel, very light:
    * Bond XP gained from:
        * Keeping them alive a long time
        * Feeding them when they ask
        * Bringing them back via resurrection scroll
    * Small rewards at bond milestones:
        * +1 to a core stat
        * Unique cosmetic particle on them
        * Unlock a special “bond emote” or voice line
* This plays nicely with the Resurrection Scroll system and reinforces long-term attachment.

## Lore & Narrative
* Personal Quests
    * High-level companions occasionally offer personal quests (find their lost sibling, retrieve a family heirloom, defeat a specific boss). Completion grants permanent bonuses and deepens attachment.
* Backstory System
    * Companions generate procedural backstories at spawn affecting their personality and occasional dialogue. A former soldier, exiled noble, or wandering scholar.
* Memory Journal
    * Auto-logged book tracking each companion's journey—first tame, major kills, death/resurrections, milestones. Readable lore item you can keep.

## Crafting & Professions
* Profession System
    * Assign secondary roles: Blacksmith (repairs gear faster), Cook (creates buffed food), Tailor (crafts companion-specific armor), Scribe (copies enchanted books). Levels separately from combat.
* Resource Gathering
    * Companions on gathering stance will mine, chop wood, or farm crops while following. Efficiency based on their tool and stats.
* Automated Crafting
    * High-INT companions can craft from their inventory when given a recipe book item. Useful for making arrows, potions, or food during long expeditions.

## Wandering Champions
* Rare named companions spawn randomly in the world with unique gear and elevated stats. Defeating them in honorable combat (specific challenge) lets you recruit them.

## Skill Trees
* At level milestones (10, 20, 30, etc.), let players choose passive bonuses or unlock alternative abilities. For example, Archers could specialize in power shots vs. rapid fire, or Clerics could focus healing vs. offensive holy damage.

## Companion Relationships
* Companions who fight together build affinity, unlocking cooperative buffs when near each other. Could add light personality traits (brave, cautious, loyal) that affect relationship gains.

## Home Base System
* Let players designate a structure as a companion barracks. Companions left there could generate passive resources, train to gain slow XP, or unlock a vendor NPC who trades based on your party's total level.

## Achievements & Titles
* Track milestones (first level 50, defeated X enemies, etc.) that unlock cosmetic titles displayed under companion names. Gives long-term goals without affecting balance.
* Examples:
    * “Slayer of Endermen” (X endermen kills)
    * “Defender of the Village” (repelled multiple raids)
    * “Stormborn” (Stormcaller with Y lightning kills during storms)
* Titles could:
    * Show in GUI & hover text
    * Grant tiny passive bonuses or just be cosmetic.

## Elite Variants
* Rare "Ancient" or "Veteran" companions with unique skins and +2 to all base stats. Spawn in special rare structures or require difficult summoning rituals.

## Raid Bosses
* Add a few massive optional bosses designed for full companion parties, dropping unique gear or enchantments that only companions can use.

## Companion Expeditions
* Send companions on timed automated missions (like WoW's garrison system). They return with loot based on their levels/stats, with small risk of injury requiring extra healing.

## Consumable "Tomes"
* Rare loot found in Dungeons/Strongholds.
* Usage: A companion reads the tome to instantly gain a chunk of XP or a permanent +1 to a specific stat (capped at +5 total per companion to prevent overpowered scaling).

## Weather & Biome Affinities
* We already do storms boosting lightning—expand that idea:
    * Fire Mage:
        * Slightly weaker in rain, slightly stronger in Nether.
    * Stormcaller:
        * Movement speed or attack speed buff during thunder.
    * Necromancer:
        * Minor buff in soul sand valleys or deep dark.
    * Cleric:
        * Tiny bonus in villages or near altars/chapels you define.
* All super small percentages so it’s mostly flavor, but it rewards paying attention to environment.

## Environmental Hazards Awareness
* Companions call out dangers
    * Lines + subtle behavior tweaks when:
        * Near lava lakes
        * At the edge of ravines
        * Entering the deep dark
* Maybe a config: “Cautious Mode” where they avoid walking close to obvious hazards.

## Idle Chatter & Context Lines
* Biome/structure lines:
    * “These mountains remind me of my training days…”
    * “Ugh, it’s so humid in this jungle.”
* Class-based comments when fighting certain mobs:
    * Cleric: extra lines vs undead
    * Beastmaster: lines when near wolves, horses, etc.
* Kill celebrations, ambient chatter when idle
* Weather Reactions - Companions seek shelter during storms, dry off near fires, or comment on environmental conditions. Stormcaller could gain visible energy crackling in thunderstorms.
* Simple timed random line system with cooldown so it doesn’t spam.

## Barracks / Banner Anchor
* A dedicated block or item that acts as a home base:
    * Place a Companion Banner / Barracks Block:
        * “Set as home” for selected companions via GUI.
        * When set to “off duty,” they return there and enter idle routines.
    * Idle behaviors:
        * Patrol small radius
        * Sit at a chair/bed
        * Interact with training dummies (see below)
    * This complements your companion houses and worldgen; lets players build a “party HQ.”

## The "Tavern" & Mercenary System
* Currently, we have to find specific houses to get specific classes. A hub system would solve bad RNG.
    * Structure: The Wayfarer's Inn:
        * A rare, larger structure (Village style) that spawns in Plains or Meadows.
        * Contains 3-5 random untamed companions relaxing inside.
    * The Contract Item:
        * Instead of taming with random items, Tavern companions require a Mercenary Contract (crafted with Paper, Gold, and Feather).
        * Gold Upkeep: Higher-level mercenaries found here might require a daily "wage" of 1 Gold Nugget (automatically taken from their inventory), or they stop following.

## The "Runner" Behavior (Auto-Sell):
* Give a companion a Bundle/backpack
    * Action: When full, the companion can be commanded to "Return to Town." They teleport to the nearest Village (or the Rally Banner), dump their inventory into a linked chest, and teleport back.

## Morale System
* Companions have hidden morale affected by wins/losses, gear quality, hunger, and how often they nearly die. High morale = small stat buffs and cheerful dialogue; low morale = performance penalties and complaints.

## Transformation Items 
* Ultra-rare items that permanently transform a companion into a new class (Knight → Paladin, Scout → Assassin). Maintains level but changes abilities and appearance.

## Dedicated area for equipped gear 
* Minecraft Player style paperdoll

## Cosmetic Armor Slots
* Set Armors to show instead of what they are actually wearing.

## Mule Companion
* Larger inventory?
* Avoids combat?

## Resource gathering
* Self explanitory.

## War
* FTB Teams integration - opposing team companions will fight eachother as if they are hostile mobs

## Invaders
