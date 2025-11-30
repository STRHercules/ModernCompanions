# TASK: Companion Personality, Bond, Lore & Morale Systems
_For the Modern Companions NeoForge 1.21.1 mod_

This TASK file defines how to implement a lightweight, systemic **personality layer** for companions in Modern Companions:
- Birth **traits**
- A parallel **Bond / Loyalty** track
- **Lore & narrative hooks** (backstories + memory journal)
- A hidden **Morale** value that nudges performance and dialogue

The goal is to add flavor, replayability, and attachment **without** turning this into a heavy mood sim or micro‑management system.

---

## 0. High-Level Constraints

- Keep systems **lightweight**:
  - Small numeric nudges only (no massive stat multipliers).
  - Limit per-tick work; prefer event-driven updates (kills, feeds, resurrections).
- All new data must be saved/loaded through the companion entity's **NBT**/**data components**.
- All features must work in **singleplayer** and **integrated/server** contexts.
- Systems should be **opt-in configurable** where reasonable (e.g. enable/disable morale/traits via config).

Where the TASK says **Codex MUST**, treat it as a hard requirement.

---

## 1. Data Model & Storage

Codex MUST introduce a central data model (class or component) representing a companion's personality state, with at least:

- `PrimaryTrait: String` (may be empty)
- `SecondaryTrait: String` (may be empty)
- `BondLevel: int` (e.g. 0–5)
- `BondXP: int` (0+)
- `BackstoryId: String` (e.g. `backstory_village_guard`)
- `Morale: float` (range -1.0 to +1.0, clamped)
- `MemoryJournal` fields (see section 4):
  - `FirstTamedGameTime: long` (or -1 if not set)
  - `TotalKills: int`
  - `MajorKills: int` (bosses / elites)
  - `TimesDowned: int` (if applicable)
  - `TimesResurrected: int`
  - Optionally: `DistanceTraveledWithOwner: long` (if easy to track)

### 1.1 NBT / Data Components

Codex MUST:
- Implement **save/load** for all of the above on the companion entity (NBT or data component).
- Use stable string keys to allow extensions later, e.g.:
  - `"PrimaryTrait"`, `"SecondaryTrait"`, `"BondXP"`, `"BondLevel"`, `"BackstoryId"`, `"Morale"`, `"Mem_FirstTamedTime"`, `"Mem_TotalKills"`, etc.

---

## 2. Birth Traits System

Each companion can roll **1–2 traits at spawn**. Traits are intended as:
- **Identity tags** (for dialogue/barks + GUIs).
- **Small behavior tweaks** (AI and stats).

### 2.1 Trait Assignment

Codex MUST:
- On **initial spawn / creation** of a Modern Companion (not on load):
  - Roll a **PrimaryTrait** from the list below.
  - With a configurable chance (config), roll a **SecondaryTrait** that is **distinct** from the first.
- Ensure traits are **only rolled once** and persisted via NBT.
- Provide a simple config to:
  - Enable/disable trait system entirely.
  - Adjust **secondary trait chance** (0–100%).

### 2.2 Trait List & Effects

Implement traits as **string IDs** with light mechanical effects and flavor hooks. Tag names below are canonical and MUST be used consistently:

#### 1. Brave
- **Tag:** `trait_brave`
- **Effect:**
  - Slightly reduced retreat behavior at low HP.
  - More willing to close to melee range rather than kiting.
- **Usage:**
  - AI: lower “flee at low HP” threshold.
  - Barks: more bold lines in combat.

#### 2. Cautious
- **Tag:** `trait_cautious`
- **Effect:**
  - Keeps a slightly larger follow distance from the player.
  - Prefers kiting and staying just outside melee when possible.
- **Usage:**
  - AI: increase ideal follow distance; favor ranged/kite where applicable.

#### 3. Guardian
- **Tag:** `trait_guardian`
- **Effect:**
  - Prioritizes mobs that are targeting the player.
  - Slightly tighter follow distance; tends to body‑block for the player.
- **Usage:**
  - Target selection: weight targets currently attacking the owner higher.

#### 4. Reckless
- **Tag:** `trait_reckless`
- **Effect:**
  - More likely to hard‑chase low‑health enemies.
  - Slightly shorter preferred combat distance.
- **Usage:**
  - AI: allow longer chase radius vs fleeing enemies, within sane limits.

#### 5. Stalwart
- **Tag:** `trait_stalwart`
- **Effect:**
  - Small knockback resistance.
  - Less likely to retreat when badly hurt.
- **Usage:**
  - Stats: small knockback modifier if feasible; adjust flee threshold.

#### 6. Quickstep
- **Tag:** `trait_quickstep`
- **Effect:**
  - Slight movement speed bump.
  - Prefers strafing and kiting over standing still.
- **Usage:**
  - Stats: small speed modifier; AI: encourage lateral movement.

#### 7. Glutton
- **Tag:** `trait_glutton`
- **Effect:**
  - Requests food slightly more often (if food‑asking system exists).
  - Gains slightly increased healing from food items fed by the owner.
- **Usage:**
  - Healing: +X% healing from food.
  - Barks: more frequent food‑related lines.

#### 8. Disciplined
- **Tag:** `trait_disciplined`
- **Effect:**
  - Modest bonus to XP gained from kills.
  - Reduced idle wandering when following the player.
- **Usage:**
  - XP: multiplier for companion XP from kill events.
  - AI: reduce idle/random walk when in follow mode.

#### 9. Lucky
- **Tag:** `trait_lucky`
- **Effect:**
  - Small chance to improve loot when they’re nearby or land the killing blow.
- **Usage:**
  - Hook into kill/loot generation with a tiny extra roll or chance for extra drop.

#### 10. Night Owl
- **Tag:** `trait_night_owl`
- **Effect:**
  - Modest buff to perception/accuracy at night.
  - Extra barks when traveling or fighting after dark.
- **Usage:**
  - Minor stat buff when world time indicates night.

#### 11. Sun‑Blessed
- **Tag:** `trait_sun_blessed`
- **Effect:**
  - Modest buff to regen or accuracy in daylight.
- **Usage:**
  - Opposite of Night Owl: small buff during daytime.

#### 12. Jokester
- **Tag:** `trait_jokester`
- **Effect:**
  - Higher chance to use humorous barks in combat and low‑health situations.
  - No direct stat effect; pure flavor.
- **Usage:**
  - Bark selection: pick from “joke” lines more often.

#### 13. Melancholic
- **Tag:** `trait_melancholic`
- **Effect:**
  - Occasional reflective lines about the past, ruins, or graveyards.
  - No direct combat bonus.
- **Usage:**
  - Barks around ruins, grave‑like structures, or after deaths.

#### 14. Devoted
- **Tag:** `trait_devoted`
- **Effect:**
  - Gains slightly more **Bond XP** over time while following the same player.
  - **Resurrection discount:** when using a Resurrection Scroll on a Devoted companion, the extra reagent cost is reduced:
    - Design target: allow using any flower item (tag‑based) instead of the usual rare reagent, or consume fewer reagents overall.
- **Usage:**
  - Bond XP: multiplier when awarding Bond XP from qualifying events.
  - Resurrection logic: branch on `trait_devoted` to allow the “flower discount” behavior, with a config toggle.

Codex MUST ensure trait effects are **small and additive**, not game‑breaking. All multipliers and thresholds should be backed by constants/config values (not magic numbers scattered in code).

---

## 3. Bond / Loyalty Track

The Bond system is a **parallel progression** to combat XP/levels. It rewards **keeping a companion alive and cared for** over time.

### 3.1 Bond Data

- `BondXP: int` — total bond experience.
- `BondLevel: int` — derived from XP (e.g. 0–5 or 0–10).

Codex SHOULD implement a simple progression curve, e.g.:
- Bond 0 → 1: 100 XP
- Bond 1 → 2: 250 XP
- Bond 2 → 3: 500 XP
- Bond 3 → 4: 1000 XP
- Bond 4 → 5: 2000 XP

Put these values into a config or a small static helper so they can be tuned.

### 3.2 Bond XP Sources

Codex MUST award BondXP from the following events (values configurable):

- **Time alive with owner**:
  - Periodically (e.g. once per in‑game minute) award a small BondXP if:
    - Companion is alive.
    - Within reasonable distance of the owner.
- **Feeding**:
  - When the owner gives valid food to the companion and it heals:
    - Award BondXP based on heal or flat value.
    - `trait_glutton` and `trait_devoted` can both apply multipliers.
- **Resurrection**:
  - When the player successfully uses a Resurrection Scroll to bring this companion back:
    - Award a significant chunk of BondXP with reasonable cooldown so players cannot just murder spam.
- Optional hooks (nice‑to‑have, not mandatory at first pass):
  - Major kill (boss/elite).
  - Surviving a near‑death encounter (see morale section for “nearly died” detection).

### 3.3 Bond Milestones & Rewards

For each Bond level up, Codex MUST implement:

- A **small** stat reward, such as:
  - +1 to a core stat (e.g. +1 damage, +1 armor, +small HP increase) per milestone, configurable.
- A **cosmetic** reward:
  - Example: subtle particle effect, a faint aura, or tiny sparkle around bonded companions.
- Optional: unlock special **“bond emote”** or unique lines:
  - Additional bark lines that only unlock after certain Bond levels.

Bond level changes should:
- Trigger a **client‑visible notification** (chat message, toast, or GUI popup).
- Be reflected in the companion GUI as a simple **Bond bar** or level indicator.

---

## 4. Lore & Narrative: Backstory + Memory Journal

### 4.1 Backstory System

Backstories are simple flavor tags plus small bark pools.

Codex MUST:
- Generate a `BackstoryId` on first spawn from a predefined set (e.g. `backstory_village_guard`, `backstory_runaway_mage`, `backstory_exiled_noble`, etc.).
- Store it in NBT as `BackstoryId`.
- Use that ID to:
  - Select **intro lines** when first hired.
  - Provide occasional idle barks that reference their past.

The initial set can be small (4–8 backstories) but must be:
- **Data‑driven** where possible (e.g. json/lang‑driven text), so new backstories can be added later without editing code.

### 4.2 Memory Journal

Memory Journal is a **read‑only summary** of that companion’s journey, surfaced in the GUI.

Codex MUST track at minimum:

- `FirstTamedGameTime`: set when the player first recruits the companion.
- `TotalKills`: increment each time the companion kills an enemy.
- `MajorKills`: increment for bosses/elites (if detection is easy; otherwise stub this out for later).
- `TimesDowned`: if a “downed”/non‑permanent death state exists.
- `TimesResurrected`: each time a Resurrection Scroll successfully revives them.

Codex MUST:
- Add a **Memory Journal** button or panel to the companion GUI:
  - Shows “first hired” date/time (converted to a readable format if possible).
  - Shows total kills, major kills, times resurrected.
  - Optionally shows “time together” or “days served” if derived from game time.

The Memory Journal is **not** an editable log; it’s just metrics and a couple of short generated lines, e.g.:
- “First joined you on Day 12.”
- “Has felled 243 foes and survived 3 resurrections.”

---

## 5. Morale System (Hidden)

Morale is a hidden float in the range [-1.0, +1.0] that gently nudges performance and dialogue. It should **never** cause extreme penalties.

### 5.1 Morale Storage

- `Morale: float` stored on the companion.
- Clamp at all writes to [-1.0, +1.0].
- Default: `0.0` (neutral) at spawn.

### 5.2 Morale Influences

Codex MUST implement these **events that change morale** (values configurable):

**Increase Morale on:**
- Winning fights without taking heavy damage.
- Being well‑fed (recent feeding event, especially when not at 1 HP).
- Gaining Bond levels.
- Getting upgraded gear (detect significant armor/weapon improvements if practical).

**Decrease Morale on:**
- Frequently dropping to very low HP (“near‑death” threshold).
- Going very long without being fed or healed while frequently fighting.
- Repeated deaths / resurrections in short time windows.

Morale updates should be **event-based**, not every tick. Use convenient hooks like:
- OnKill
- OnDamaged (with thresholds)
- OnFeed
- OnResurrect
- Periodic “state check” (e.g. once every X seconds) for hunger/gear conditions.

### 5.3 Morale Effects

Codex MUST ensure morale effects are **small**. Suggested defaults:

- High morale (> +0.5):
  - +5–10% damage and/or small defense bonus.
  - More cheerful bark lines.
- Low morale (< -0.5):
  - -5–10% damage and/or small defense penalty.
  - More complaining / tired lines.

All percentages must be configurable. At `Morale` in (-0.5, +0.5), do not apply any stat changes.

### 5.4 Integration with Traits & Bond

- Certain traits can influence morale drift:
  - `trait_disciplined`: less morale loss from frequent battles.
  - `trait_jokester`: less morale loss from near‑death, more “humorous coping” lines.
  - `trait_melancholic`: more frequent reflective lines when morale is low.
- Bond level can provide:
  - A small **floor** to morale (e.g. high‑Bond companions don’t drop below -0.3).

Codex SHOULD implement these ties as simple modifiers and keep them easy to tune.

---

## 6. GUI Integration

Codex MUST extend the existing companion GUI to surface the new systems:

- **Traits Display:**
  - Show Primary/Secondary traits as icons or labeled text, including a short hover tooltip.
- **Bond Display:**
  - Bond level (e.g. hearts, pips, or numeric “Bond 3/5”).
  - A simple progress bar for current BondXP → next level.
- **Backstory & Memory Journal:**
  - A dedicated tab/panel or a toggleable section.
  - Show backstory short description and key journal stats.
- **Morale:**
  - Morale is hidden numerically, but Codex SHOULD show vague descriptors in the journal (e.g. “In good spirits”, “Worn down”, etc.).

All new text must be localized via lang entries, not hard‑coded strings.

---

## 7. Implementation Order (Recommended)

Codex SHOULD implement in this order:

1. **Data model & NBT** (Section 1):
   - Create fields, save/load logic, and add basic debug logging if needed.
2. **Trait system** (Section 2):
   - Rolling traits at spawn.
   - Wiring traits into AI/stats in a minimal way.
   - Showing traits in GUI.
3. **Bond system** (Section 3):
   - BondXP sources and level progression.
   - Rewards and GUI indications.
4. **Backstory & Memory Journal** (Section 4):
   - Backstory ID + some sample lines.
   - Basic stats tracking and a simple journal view.
5. **Morale system** (Section 5):
   - Add Morale field, events, and small stat nudges.
   - Tie into traits and Bond where straightforward.
6. **Balance & Config**:
   - Add config options for multipliers, thresholds, enabling/disabling subsystems.

At each major step, Codex MUST ensure:
- No crashes if config disables a subsystem.
- Missing data (old worlds) default to sane values and do not break loading.

---

## 8. Done Criteria

This TASK is considered **complete** when:

- [ ] New companions reliably spawn with 1–2 traits saved to NBT and visible in the GUI.
- [ ] Traits cause small but noticeable behavior or stat differences.
- [ ] Companions accumulate BondXP from time together, feeding, and resurrection.
- [ ] Bond levels grant small stat bonuses and at least one cosmetic effect.
- [ ] Each companion gets a backstory ID with at least 4–8 unique backstory text variants in lang.
- [ ] Memory Journal displays first tame time and core lifetime stats.
- [ ] Morale exists, updates on key events, and gently nudges stats and dialogue.
- [ ] All values (multipliers, thresholds, XP amounts) are configurable.
- [ ] Old worlds load without data loss or crashes, defaulting new fields safely.
