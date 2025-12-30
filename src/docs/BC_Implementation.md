# Optional Better Combat Integration (Soft-Dependency) — Codex Task

This task adds **optional** support so **Modern Companions** can use the **Better Combat** melee combat system when it is present, while remaining fully functional when Better Combat is not installed.

Key requirement: companions should follow the **same systems / functions / rules that players do** under Better Combat (attack cadence, reach handling, swing/upswing timing, weapon-defined attacks, and target selection rules), but **Modern Companions must not depend on Better Combat** to load.

---

## 0) Constraints & Repo Rules (Must Follow)

- **DO NOT** edit `TASK.md` or any read-only top-level directories.
- Allowed edits for this task should stay within:
  - `src/**` (code + this document)
  - and, if needed for optional metadata/config, `build.gradle`, `gradle.properties`, `settings.gradle`
- Any integration must be a **soft dependency**:
  - No required dependency declaration that prevents the mod from launching without Better Combat.
  - No unconditional classloading of Better Combat types from core code paths.

---

## 1) Goal

When Better Combat is installed:
- Companions resolve melee attacks using Better Combat’s combat logic **the same way a player does** (weapon-defined attacks, reach rules, upswing timing, cooldown logic, and multi-target/sweep rules where applicable).

When Better Combat is not installed:
- Companions use Modern Companions’ existing melee behavior with **no errors**, **no log spam**, and **no missing-class crashes**.

---

## 2) Success Criteria (Definition of Done)

- A single release jar runs in both environments:
  - With Better Combat installed
  - Without Better Combat installed
- Better Combat integration can be controlled via config:
  - `AUTO` (default): enabled only when Better Combat is present
  - `ON`: enabled if Better Combat is present, otherwise behaves like `AUTO` with a warning
  - `OFF`: always disabled
- No compile-time or runtime hard dependency is introduced:
  - **No required dependency** in `src/main/resources/META-INF/neoforge.mods.toml`
  - No unconditional imports/references to Better Combat types in classes that load even when Better Combat is absent
- Combat parity (server authoritative):
  - Attack timing / windup (upswing) and cooldown mirror Better Combat.
  - Range/reach is not double-applied (Modern Companions’ extra reach modifiers remain suppressed when Better Combat is active; this already exists for weapon items).

---

## 3) Design Overview (How to Keep It Optional)

### 3.1 Runtime Detection

- Detect Better Combat using NeoForge runtime metadata:
  - `ModList.get().isLoaded("bettercombat")`
- Gate *all* Better Combat integration behind this check **and** the new config toggle.

### 3.2 Avoiding Hard Dependencies

Prefer one of these approaches:

**Option A (recommended): “Bridge Interface + Reflection Implementation”**
- Core combat code calls a small internal interface (e.g., `BetterCombatBridge`).
- The Better Combat implementation is in `compat/bettercombat/` and uses reflection to call Better Combat methods.
- If reflection fails (API changed), the bridge disables itself and falls back safely.

**Option B: `compileOnly` API + optional runtime**
- Add Better Combat as a `compileOnly` dependency for development.
- Still ensure runtime loading is gated so the mod works without Better Combat installed.
- Use `@OnlyIn(Dist.CLIENT)` separation for animation-only pieces.

Do **not** directly reference Better Combat classes from entity base classes that are loaded regardless of mod presence.

---

## 4) Where to Integrate in Modern Companions

Identify the main melee attack entry point(s) and route them through a single decision point:

- Companion melee behavior currently uses vanilla AI goals like `MeleeAttackGoal` and entity overrides like `doHurtTarget(...)`.
- Create a dedicated “combat resolver” abstraction (server-side) used by all melee companions:
  - Example: `CombatResolver.resolveMeleeAttack(attackerCompanion, primaryTarget)`
  - Resolver chooses either Better Combat pipeline or vanilla pipeline.

Primary integration point candidates:
- `AbstractHumanCompanionEntity` melee attack method(s)
- Overrides of `doHurtTarget(Entity)` in individual classes
- Any custom “sweep/aoe” logic (e.g., Berserker) should also route through the resolver when Better Combat is active.

---

## 5) Better Combat Feature Parity Targets

### 5.1 Timing & Cooldowns

Match Better Combat’s effective cadence:
- “Upswing” (windup) before damage is applied
- Cooldown/attack speed rules
- Combo progression (if Better Combat uses combos per weapon)

Implementation guidance:
- Store a small per-companion “attack state”:
  - current combo index
  - swing start tick
  - when to apply damage
  - when combo resets (timeout)

### 5.2 Reach / Range

- Better Combat modifies reach handling.
- Modern Companions must avoid stacking reach rules:
  - Keep the existing behavior that skips Modern Companions’ reach modifiers when Better Combat is loaded.
  - Ensure the companion melee resolver uses Better Combat’s range logic when integration is active.

### 5.3 Multi-Target (Cleave/Sweep)

If Better Combat’s rules allow multi-target hits:
- Use the same target acquisition rules:
  - hitbox shape / angle restrictions
  - maximum targets / diminishing damage (if configured)
  - relation checks (teammates, neutral entities, pets, etc.)

### 5.4 Rules About Who Can Be Hit

Modern Companions must not break existing friendly-fire options:
- When Better Combat is active, target filtering must still respect:
  - owner-friendly-fire config
  - companion-companion friendly fire config
  - “pet/owner/team” relationships

If Better Combat provides its own relation/teammate rules, prefer them while still enforcing Modern Companions’ explicit friendly-fire settings as an additional guardrail (never allow hits that MC explicitly forbids).

---

## 6) Concrete Implementation Steps (Codex Checklist)

### Step 1 — Add Config Toggle

- Add a new common config entry (string enum) under a new `compat` category:
  - `compat.betterCombat = "AUTO" | "ON" | "OFF"`
- Add a small helper:
  - `BetterCombatCompat.isEnabled()` → returns true only if config allows and mod is loaded.

### Step 2 — Add a Compat Bridge Layer

Create a new package:
- `com.majorbonghits.moderncompanions.compat.bettercombat`

Add:
- `BetterCombatCompat` (detection + safety)
- `BetterCombatBridge` (interface describing operations MC needs)
- `BetterCombatBridgeImpl` (reflection-based calls into Better Combat)

The bridge should expose only what MC needs, e.g.:
- “Compute attack plan” (upswing ticks, cooldown ticks, range)
- “Find entities hit by swing”
- “Apply Better Combat damage rules / multipliers”

### Step 3 — Route Companion Melee Attacks Through the Resolver

Implement:
- `CombatResolver` (or similarly named class) in MC that decides:
  - If Better Combat enabled → use bridge
  - Else → use current logic

Update melee companions so they call the resolver from one place:
- Prefer updating base-class behavior rather than per-entity overrides (unless special cases require it).

### Step 4 — Server-Authoritative Damage

- Ensure all damage application happens server-side.
- Client should only:
  - play swing animations / particles (optional)
  - keep visuals consistent, but never be authoritative

### Step 5 — Logging & Failure Modes

- If Better Combat is installed but the bridge fails (API mismatch):
  - Log **one** warning (once per session) and disable the integration.
  - Fall back to vanilla MC combat path.
- No repeated per-tick warnings.

### Step 6 — Optional Metadata

- Decide whether to add an **optional** dependency entry in `src/main/resources/META-INF/neoforge.mods.toml`:
  - This is not required for runtime detection, but can improve modloader UI.
  - If added, it must be **optional**, never required.

---

## 7) Local Validation Steps (Required Before Shipping)

Run the mod in both configurations:

1) **Without Better Combat installed**
   - Spawn companions and verify melee still functions normally.
   - Verify no `ClassNotFoundException` / missing class crashes.

2) **With Better Combat installed**
   - Verify companions:
     - attack cadence mirrors Better Combat (upswing + timing)
     - respect Better Combat range logic (no duplicate reach)
     - can cleave/multi-hit when the weapon rules allow it
   - Confirm the config toggle works (`AUTO`, `ON`, `OFF`).

Keep a quick smoke-test list:
- Vanguard / Knight (basic melee)
- Berserker (any custom sweep behavior)
- Beastmaster pets (owner/team checks)

---

## 8) Notes / References (Repo-Local)

- Better Combat mod id is `bettercombat`.
- Modern Companions already conditionally disables its own reach modifiers when Better Combat is loaded (see weapon item attribute code under `src/main/java/.../item/`).

