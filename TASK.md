# TASK.md — Modern Companions Port (for Codex)

## Overview

You are assisting with **porting and rebranding an existing Minecraft mod** into a new project called **Modern Companions** by **MajorBonghits**.

Modern Companions must:

* Mimic **all gameplay features and functionality** of the original **Companions** mod.
* Reuse/port **all necessary assets and data** (models, textures, lang, loot tables, etc.) so that the new mod fully replaces the original in supported environments.
* Be updated to run on **Minecraft 1.21.1** using **NeoForge**.
* Ship as a **standalone, buildable mod project** with a complete Gradle setup and a clear README.

If an `AGENTS.md` file exists, its rules and directory policies are **authoritative**. This TASK.md describes *what* to build; `AGENTS.md` (if present) constrains *where and how* you may edit.

---

## High-Level Goal

Create a new NeoForge 1.21.1 mod called **Modern Companions** that:

1. **Preserves feature parity** with the original Companions mod.
2. **Builds successfully** via Gradle on a modern toolchain.
3. **Loads and runs** in a Minecraft 1.21.1 + NeoForge dev environment.
4. Includes **all required metadata and documentation** (README, mods.toml or neoforge metadata, license references, etc.).

The final result should be suitable to publish as a standalone mod under the new name and author identity.

---

## Golden Rules

1. **Do not modify read-only or upstream directories.**  
   If `AGENTS.md` defines read-only regions (e.g. `Original*`, `MinecraftReferences`, etc.), treat them as *reference only*.

2. **No speculative behavior.**  
   Derive behaviors and features by **reading existing Companions code and assets**. Do not invent new gameplay unless required to replace obsolete APIs; when in doubt, match existing behavior.

3. **Preserve licensing and attribution.**
   * Do **not** change the existing `LICENSE` file unless explicitly instructed elsewhere in the repo.
   * Keep original authors’ copyright notices in ported files; add new notices/comments for Modern Companions and MajorBonghits where appropriate.
   * If no license exists, do **not** add one on your own.

4. **Keep changes organized and incremental.**  
   Prefer small, focused edits with clear intent over massive rewrites.

5. **Always keep the project buildable.**  
   After any series of structural changes (registries, Gradle setup, package renames), ensure that `./gradlew build` (or the equivalent) can succeed.

---

## Phase 0 – Repository & Context Discovery

**Goal:** Understand how the existing Companions mod is laid out and where you are allowed to edit.

1. **Scan for guidance files**
   * Look for:
     * `AGENTS.md`
     * Existing `TASK.md`
     * `README.md`
     * `LICENSE`
     * Any notes about “Companions”, “Modern Companions”, or “porting”.
   * Respect any directory/editing constraints described there.

2. **Identify the original Companions source**
   * Find the module or directory containing the original mod:
     * Typical names: `Companions`, `OriginalCompanions`, `CompanionsMod`, or similar.
     * Look for `build.gradle`, `mods.toml`, `neoforge.mods.toml`, `META-INF`, or a `src/main/java` layout with classes referencing “Companions”.
   * Confirm:
     * The original **mod id** (e.g. `companions`).
     * The original **package namespace** (e.g. `com.example.companions`).
     * The **Minecraft + Forge/NeoForge version** it was built for.

3. **Inspect existing build system**
   * Determine if this repository is:
     * A single Gradle project, or
     * A multi-project Gradle build with multiple submodules.
   * Identify:
     * Root `settings.gradle` or `settings.gradle.kts`
     * Root `build.gradle` or `build.gradle.kts`
     * How the original Companions module is included (if at all).

4. **Document findings in comments**
   * In this TASK’s working notes (or a separate `NOTES.md`), record:
     * Location of original Companions code.
     * Its mod id and package structure.
     * Current Forge/NeoForge + MC version.
     * Any constraints from `AGENTS.md`.

---

## Phase 1 – Define Modern Companions Project Identity

**Goal:** Establish consistent identifiers for the new mod.

1. **Set naming and identity**
   * **Mod name:** `Modern Companions`
   * **Mod id:** Prefer a lower_snake or lowerCamel version, e.g.:
     * `modern_companions` or `moderncompanions`
   * **Author:** `MajorBonghits`
   * **Root Java package:** Choose one consistent namespace, e.g.:
     * `com.majorbonghits.moderncompanions`

2. **Plan resource namespaces**
   * All assets under `src/main/resources` should be namespaced with the **new mod id**:
     * `assets/modern_companions/...`
   * Data packs and tags:
     * `data/modern_companions/...`

3. **Determine compatibility strategy**
   * If the original Companions mod id is different (e.g. `companions`):
     * Decide whether Modern Companions will:
       * Replace it completely (new mod id, fresh install), or
       * Attempt some cross-compatibility (e.g., migration data from the old mod id).
     * Default: **Treat Modern Companions as a new mod id** unless the repository explicitly requires compatibility.

Record all final choices (mod id, package, etc.) in this TASK.md or a `NOTES.md` so they remain consistent.

---

## Phase 2 – Bootstrap NeoForge 1.21.1 Project

**Goal:** Create a clean, modern NeoForge 1.21.1 skeleton for Modern Companions.

1. **Create or select the Modern Companions module**
   * If the repo already has a modern NeoForge 1.21.1 module template:
     * Reuse it as the base for Modern Companions.
   * Otherwise:
     * Create a **new Gradle module** or repurpose the existing Companions module by upgrading it, depending on AGENTS/directory rules.

2. **Gradle configuration**
   * Ensure the following exist and are correctly configured for NeoForge 1.21.1:
     * `settings.gradle` – includes the Modern Companions module.
     * `build.gradle` (Groovy or Kotlin DSL) – configured for:
       * Minecraft 1.21.1
       * NeoForge 1.21.1
       * Java toolchain (version per NeoForge requirements; e.g., Java 21).
     * `gradle.properties` – includes standard properties (group, version, mappings, etc.).
   * Set the group / Maven coordinates, e.g.:
     * `group = "com.majorbonghits.moderncompanions"`
     * `version = "1.0.0"` (or as specified elsewhere in the repo)

3. **Mod metadata**
   * Configure NeoForge metadata (e.g. `META-INF/neoforge.mods.toml` or equivalent):
     * Set `modId` to the chosen mod id (e.g. `modern_companions`).
     * Set `displayName` to `Modern Companions`.
     * Set `authors` to include `MajorBonghits` (and original authors, where appropriate).
     * Set `description` to briefly explain the mod (Companions functionality for modern NeoForge).
     * Configure correct Minecraft and NeoForge dependency versions (1.21.1).

4. **Entry point**
   * Create a main mod class in the chosen package, e.g.:
     * `com.majorbonghits.moderncompanions.ModernCompanions`
   * Wire it to NeoForge’s initialization flow:
     * Registration events
     * Client/server event handlers as needed
   * Add minimal logging so that you can confirm the mod loads in a dev environment.

5. **Verification**
   * Run `./gradlew build` to ensure the basic skeleton compiles.
   * Optionally run the dev client task (e.g. `runClient`) to confirm the mod appears in the mod list.

---

## Phase 3 – Inventory and Map Original Companions Features

**Goal:** Understand exactly what the original mod does so you can systematically port it.

1. **Feature inventory**
   * Read original Companions code and docs to identify:
     * All custom entities (companions, pets, mounts, etc.).
     * Any custom AI behaviors.
     * Custom items (summoning items, food, equipment, etc.).
     * GUIs / screens / HUD elements.
     * Config systems (server/client configs, JSON configs).
     * Worldgen (structures, biome features) if present.
     * Networking (packets, sync logic).
     * Data (loot tables, recipes, advancements, tags).
   * Record this as a concise bullet list in a doc (e.g. `NOTES.md#CompanionsFeatureMap`).

2. **Data & assets inventory**
   * List:
     * All textures, models, animations, sounds.
     * Lang keys and translations.
     * `data` entries: loot tables, recipes, tags, etc.
   * Note where they are stored (resource paths and namespaces).

3. **API / version assumptions**
   * Identify the original Minecraft + Forge/NeoForge version.
   * Observe old APIs:
     * Entity registration style (e.g. `GameRegistry` / `DeferredRegister` / annotations).
     * Event bus usage.
     * Config APIs.
     * Network and capabilities APIs.
   * For each major category (entities, items, configs, networking), note:
     * What API the original uses.
     * What the equivalent or updated NeoForge 1.21.1 API should be (in general terms).

---

## Phase 4 – Port Core Registries & Types to NeoForge 1.21.1

**Goal:** Get all core game objects (entities, items, etc.) registered and compiling under the new environment.

1. **Create modern registries**
   * Implement NeoForge-style registries using `DeferredRegister` or the recommended API for:
     * Entities
     * Items
     * Blocks (if any)
     * Menu types / screens (if any)
     * Sounds
     * Other custom registries (if applicable)

2. **Port entity types**
   * For each original companion entity:
     * Create a new `EntityType` registration under the Modern Companions mod id.
     * Port the entity class to:
       * Extend the appropriate base class for 1.21.1.
       * Update constructors, attributes, goals/AI, and pathfinding to new signatures.
   * Ensure attributes and goals are registered using modern patterns.

3. **Port items and blocks**
   * Register all items corresponding to companions, summoning items, or gear.
   * Port any blocks used by the mod (e.g., spawning blocks, altars, or decorative blocks).
   * Update item/block properties to match 1.21.1 conventions.

4. **Port data files**
   * Move / recreate resource files under the **new namespace**, e.g.:
     * `assets/modern_companions/textures/...`
     * `assets/modern_companions/models/...`
     * `data/modern_companions/loot_tables/...`
     * `data/modern_companions/recipes/...`
     * `data/modern_companions/tags/...`
   * Update references in code and JSON to use the new mod id.

5. **Compile & fix**
   * Run `./gradlew build`.
   * Resolve compile-time errors from API changes (method signatures, registry differences, etc.).
   * Do not alter gameplay logic beyond what is necessary to compile and function under the new version.

---

## Phase 5 – Port Behavior, AI, and Systems

**Goal:** Restore original functionality and behavior under 1.21.1 NeoForge.

1. **AI and behavior**
   * Port all custom AI goals / tasks for companion entities.
   * Use modern AI registration methods:
     * Register goals in `registerGoals()` or equivalent.
     * Ensure compatibility with current pathfinding and goal selectors.
   * Where old APIs are removed:
     * Replace them with equivalent 1.21.1 constructs.
     * Preserve behavior (movement, attack logic, following, guarding, etc.) as closely as possible.

2. **Companion interaction systems**
   * Port taming, summoning, following, commands, or GUI interactions.
   * Ensure right-click behavior, command items, or other triggers work in 1.21.1.
   * Maintain NBT or capability-style data storage for companions, adapting to modern APIs.

3. **Config & balance**
   * Port configuration options:
     * Use current NeoForge config APIs (e.g. TOML configs).
     * Map old config options to new ones so players can control the same settings.
   * Preserve default values as much as possible.

4. **Networking & sync**
   * Port networking code to the current NeoForge networking API.
   * Ensure all client/server sync (e.g. companion state, GUIs, effects) functions correctly.
   * Remove/replace deprecated packet handling patterns with modern equivalents.

5. **Worldgen / structures (if present)**
   * Port any structures or worldgen features using the 1.21.1 data-driven worldgen system.
   * Register features, placed features, and structure sets per modern standards.

6. **Client-side features**
   * Port:
     * Renderers for entities.
     * Client-only event handlers.
     * HUD elements or custom overlays.
   * Register client-side only code correctly to avoid running it on the server.

---

## Phase 6 – Documentation & Metadata

**Goal:** Provide clear documentation so humans can build and understand Modern Companions.

1. **Create/Update README.md**
   * Include at least:
     * **Title:** Modern Companions
     * **Author:** MajorBonghits (+ original authors credited in a “Credits” section).
     * **Description:** One–two paragraphs summarizing the mod and that it is a modern port of the original Companions mod.
     * **Requirements:**
       * Minecraft 1.21.1
       * NeoForge version (exact version used in Gradle)
       * Java version
     * **Installation:**
       * Where to place the built JAR.
     * **Development Setup:**
       * How to import the project into an IDE.
       * How to run the dev client (`./gradlew runClient` or equivalent).
       * How to build the mod (`./gradlew build`).
     * **License:**
       * Reference existing `LICENSE` file and its terms (e.g. GPL-3.0, if that is what this repo uses).
     * **Credits & Attribution:**
       * Acknowledge original Companions authors and contributors.

2. **Gradle & metadata comments**
   * Add concise comments in `build.gradle` and `mods.toml` / metadata where helpful:
     * Explain critical version numbers.
     * Explain non-obvious configuration choices.

3. **Changelog / Migration Notes (optional but helpful)**
   * Add a basic `CHANGELOG.md` or “Port Notes” section in README:
     * State that this is a port of the original Companions mod to 1.21.1 using NeoForge.
     * Mention major technical changes (e.g., updated registries, new config system).

---

## Phase 7 – Testing & Parity Verification

**Goal:** Confirm that Modern Companions behaves like the original Companions mod.

1. **Functional checklist**
   * For each feature identified in **Phase 3**:
     * Verify that the same behavior exists in Modern Companions under 1.21.1.
   * Examples (adapt to the actual feature list):
     * Companions can be summoned/spawned.
     * Companions follow the player correctly.
     * Combat, guarding, or utility behaviors work.
     * Any companion-related GUIs/opening screens function as expected.
     * Config options toggle or adjust behaviors correctly.

2. **Dev environment testing**
   * Run the dev client:
     * Confirm that the mod loads without errors.
     * Check logs for warnings or errors related to Modern Companions.
   * Fix issues iteratively while keeping behavior aligned to the original.

3. **Build artifact**
   * Ensure `./gradlew build` produces a JAR in the standard location (e.g. `build/libs`).
   * Confirm that the JAR includes:
     * Correct metadata (mod id, name, version).
     * Required assets.
     * No obvious missing resources (no missing texture errors for core content).

---

## Non-Goals (What NOT to Do)

* Do **not**:
  * Change the overall design of the mod or add major new mechanics unless absolutely required to satisfy API changes.
  * Rename the mod id in a way that contradicts decisions recorded earlier in this TASK or in `AGENTS.md`.
  * Remove or alter licensing terms in `LICENSE`.
  * Hardcode environment-specific paths, usernames, or secrets.

* Only refactor or simplify code when it:
  * Helps resolve compatibility issues, or
  * Makes it easier to maintain without changing behavior.

---

## Definition of Done

The Modern Companions port is considered **complete** when:

1. **Build passes:**  
   `./gradlew build` completes successfully for the Modern Companions project/module.

2. **Loads in dev environment:**  
   The mod:
   * Appears in the NeoForge mod list for Minecraft 1.21.1, and
   * Does not crash at startup or during basic usage.

3. **Feature parity achieved:**  
   All core features identified from the original Companions mod are present and functional in Modern Companions.

4. **Documentation exists and is accurate:**
   * `README.md` clearly explains how to build, run, and install the mod.
   * Metadata files correctly describe the mod and its requirements.

5. **Licensing and attribution are preserved:**
   * Original authors are credited.
   * The codebase respects the existing license terms.

Once these conditions are met, the Modern Companions port is ready for human review and playtesting.
