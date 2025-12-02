# JOBS.md – Companion Jobs System

## 1. Summary

Design and implement a **Companion Jobs System** that allows players to assign each companion a specific “job” (e.g., Lumberjack, Hunter, Miner, Fisher, Chef). Companions will then autonomously perform tasks related to their job while respecting existing mod behavior, performance, and configuration.

This document defines the **task for Codex**, the **intended player experience**, and a **high‑level implementation plan** within the constraints of this repository (see `AGENTS.md`).


---

## 2. Goals

- Allow players to assign, change, and clear a **job** for each companion.
- Implement concrete behavior for at least the following jobs:
  - **Lumberjack** – Find trees, chop logs, collect wood, and optionally replant.
  - **Hunter** – Seek and attack valid targets (mobs/animals), collect drops.
  - **Miner** – Mine ores and stone blocks in a controlled radius, collect drops.
  - **Fisher** – Fish in nearby water sources, collect fish and related loot.
  - **Chef** – Cook raw food in nearby furnaces/campfires, manage input/output.
- Persist job assignments across world saves.
- Keep behavior configurable and non-destructive (no griefing player builds).
- Integrate cleanly with the existing companion AI and data model.

Non-goals (for this task):

- No global job-management UI across all companions.
- No economy or payment system.
- No complex town/building AI (just job-focused behaviors).


---

## 3. Repository & Constraints (for Codex)

- **Only edit**:
  - `src/**`
  - Root docs: `README.md`, `CONTRIBUTING.md`, `.gitignore`, `.editorconfig`, `SUGGESTIONS.md`, `TRACELOG.md`
  - Build configs: `gradle.properties`, `build.gradle`, `settings.gradle`
- **Do not modify**:
  - `ModernCompanions/`, `ModDevGradle-main/`, `NeoForge-1.21.x/`, `Minecraft_Client_Source_1.21.1/`, `OriginalCompanions/` (read-only).
  - `AGENTS.md`, `TASK.md`.
- For any code changes:
  - Add concise comments describing the purpose and behavior.
  - Increment `build.gradle` version.
  - Ensure the project still builds (no errors).
  - When actually committing, update `TRACELOG.md` and `SUGGESTIONS.md` as per `AGENTS.md` (document steps & suggestions).


---

## 4. Player Experience

### 4.1 Job Assignment

- **Where**:
  - Either via a companion **interaction GUI**, **context menu**, or **command** (whichever is consistent with existing mod patterns).
- **Actions**:
  - `Set Job: <JobName>` (Lumberjack, Hunter, Miner, Fisher, Chef, None).
  - `Clear Job` to revert to a default/non-working state.
- **Feedback**:
  - Job is shown in the companion’s UI (nameplate suffix, tooltip, or dedicated field).
  - Optionally show a simple icon/text in the companion screen.

### 4.2 Job Behavior Basics

- Companions only work when:
  - They are **active** (not sitting, not disabled).
  - They are within a configurable distance from the owner or a designated “work area”.
- Companions respect:
  - Protected regions (if the mod already considers any).
  - A “do not break player-placed blocks” rule where possible (or restrict to natural blocks).
- Inventory usage:
  - Companions collect items they generate.
  - If full, they can either:
    - Drop items near the player, or
    - Stop working until inventory is cleared (configurable / simplest behavior is fine).


---

## 5. Job Definitions (High-Level Behavior)

### 5.1 Lumberjack

- **Behavior**:
  - Search within radius for **natural trees** (logs with leaves above/beside).
  - Path to tree, break logs from bottom upwards.
  - Collect logs and saplings.
  - Optionally replant saplings on nearby dirt/grass blocks.
- **Constraints**:
  - Avoid cutting obviously player-shaped structures (e.g., logs without leaves nearby, or logs placed above certain Y height).
  - Limit search radius and number of trees per “work cycle” to avoid performance issues.

### 5.2 Hunter

- **Behavior**:
  - Scan for **valid targets** (hostile mobs and/or configured animals).
  - Move to targets, attack them, and collect drops.
  - Return towards the player if they move too far away.
- **Constraints**:
  - Do not attack:
    - The player or their tamed pets.
    - Neutral mobs unless configured (e.g., villagers, golems).
  - Allow configuration of target categories.

### 5.3 Miner

- **Behavior**:
  - Focus on mining **exposed ores** and stone within a small radius.
  - Prefer ore blocks, then stone, then gravel/andesite/etc as fallback.
  - Mine in a controlled pattern to avoid chaotic tunnels.
- **Constraints**:
  - Hard limit on how far below/away from the player mining is allowed.
  - Avoid mining under the player or creating large holes in bases.
  - Clearly document that mining is limited to avoid griefing/performance issues.

### 5.4 Fisher

- **Behavior**:
  - Find a water block or simple water patch within range.
  - Simulate fishing:
    - Wait near the water.
    - Periodically “fish” and generate fish loot as if using a fishing rod, or integrate with actual fishing mechanics if straightforward.
- **Constraints**:
  - Only works when water is nearby.
  - Rate-limited to avoid excessive item generation.

### 5.5 Chef

- **Behavior**:
  - Detect nearby furnaces/smokers/campfires.
  - Move raw food items from companion inventory (or optionally player inventory) into available cooking slots.
  - Retrieve cooked food and store it back in companion inventory.
- **Constraints**:
  - Do not steal items from arbitrary inventories that aren’t part of the “cooking” process (no chest looting unless explicitly designed).
  - Only handle simple raw→cooked recipes.


---

## 6. Technical Plan (for Codex)

### 6.1 Data Model & Persistence

- Introduce a **Job type** for companions:
  - Likely an enum (e.g., `Lumberjack`, `Hunter`, `Miner`, `Fisher`, `Chef`, `None`).
- Store job data on the companion entity:
  - Field for current job.
  - Optional per-job settings (e.g., work radius) if needed.
- Ensure job data:
  - Is **saved/loaded** with the companion.
  - Is correctly synced to clients for UI display.

### 6.2 AI / Behavior Integration

- For each job, add one or more **AI tasks/goals** attached to the companion entity:
  - Jobs should be mutually exclusive or have clear priority ordering with existing AI.
- Implement, per job:
  - Target acquisition (blocks or entities).
  - Pathfinding to target.
  - Action execution (breaking blocks, attacking, etc.).
  - Cooldowns and throttling to prevent CPU spikes.
- Respect existing behavior:
  - Don’t break core movement/follow/defend behaviors.
  - Keep job tasks at appropriate priority so companions still follow/defend player as designed.

### 6.3 UI / UX Wiring

- Decide primary interaction:
  - Reuse existing companion GUI and add a **“Job”** selection component, or
  - Add a command (e.g., `/companion job <job>`), or both.
- Add:
  - Display of current job.
  - Simple tooltips or descriptions for each job.
- Keep UI changes minimal and consistent with the mod’s current style.

### 6.4 Configurability

- Provide configuration where it makes sense:
  - Job enable/disable flags (e.g., allow disabling Miner entirely).
  - Work radius per job (Lumberjack, Hunter, etc.).
  - Target lists (for Hunter) and block lists (for Lumberjack/Miner).
- Use existing config patterns in the mod (e.g., `*.toml` or similar) and only modify them under `src/**`.

### 6.5 Performance & Safety

- Limit:
  - Maximum blocks broken per tick / per time interval.
  - Scan radii and frequency.
- Ensure:
  - No infinite loops (e.g., searching for unreachable targets forever).
  - Jobs automatically “idle” when there’s nothing to do.


---

## 7. Detailed Task List for Codex

**Phase 1 – Discovery & Planning**

1. Inspect `src/**` to find:
   - Companion entity class(es).
   - Existing AI/goal registration.
   - Existing GUI or commands for managing companions.
2. Document findings in a short comment block or dev notes (in code comments where relevant).

**Phase 2 – Job Data & Enum**

3. Introduce a **Job enum/type** in `src/**` describing the supported jobs.
4. Add a **job field** to the companion entity or its data model.
5. Implement **save/load** and **network sync** of the job field.
6. Add comments explaining the purpose and usage of the job data.

**Phase 3 – UI / Command for Assignment**

7. Add a job assignment mechanism:
   - Either extend companion GUI or add a job command as per existing patterns.
8. Show the current job in the UI and/or in the companion’s display name.
9. Validate that setting/changing/clearing the job updates both server and client.

**Phase 4 – Job AI Implementations**

10. Implement **Lumberjack** AI:
    - Tree detection, log breaking, log/sapling collection.
    - Basic protection against obvious player-built structures.
11. Implement **Hunter** AI:
    - Target selection, attack behavior, drop collection.
    - Respect exclusions like tamed pets and villagers.
12. Implement **Miner** AI:
    - Basic ore/stone mining, radius limits, safety controls.
13. Implement **Fisher** AI:
    - Water detection, periodic fishing, loot generation/collection.
14. Implement **Chef** AI:
    - Detect cooking blocks, move/cook/retrieve food items.
15. Integrate job AI with existing AI goals and priorities.

**Phase 5 – Config & Polishing**

16. Add configuration entries for toggling jobs and tuning radii/targets if appropriate.
17. Add/adjust comments to clearly outline how the job system works and how to extend it.
18. Run the build (`./gradlew build`) when ready, ensuring no errors.
19. If this change is committed:
    - Increment `build.gradle` version.
    - Add an entry in `TRACELOG.md` summarizing the prompt, steps, and rationale.
    - Add an entry in `SUGGESTIONS.md` with potential future enhancements (new jobs, better AI, etc.).


---

## 8. Acceptance Criteria

- A companion can have **exactly one job** (or None) at a time.
- The player can **assign** and **clear** jobs via GUI or command.
- Each of the five initial jobs:
  - Performs a visible, job-appropriate action in-game.
  - Respects basic safety constraints (no rampant griefing).
  - Collects items it generates in a predictable way.
- Job assignment persists across world saves and reloads.
- The project builds successfully with no compilation errors.


---

## 9. Future Extensions (Optional Ideas)

- Additional jobs:
  - Farmer, Builder, Guard, Healer, Courier, etc.
- Shared job presets per companion type.
- More advanced AI behaviors (e.g., path networks, work shifts).
- Integration with other mods (e.g., storage mods, town-building mods).

