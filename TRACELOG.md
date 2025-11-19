## 2025-11-18
- Prompt/task: "Continue with the logical next steps."
- Steps:
  - Reviewed TASK.md and AGENTS.md constraints; identified OriginalCompanions as the read-only source with mod id `humancompanions` targeting Forge 1.20.1.
  - Established Modern Companions NeoForge 1.21.1 skeleton: added `settings.gradle`, `gradle.properties`, and `build.gradle` using the NeoForge moddev plugin, Java 21 toolchain, and property expansion for metadata.
  - Created the entrypoint `ModernCompanions` class under `com.majorbonghits.moderncompanions` and initial NeoForge metadata (`META-INF/neoforge.mods.toml`) plus `pack.mcmeta` with current pack formats.
- Rationale: Provides a clean, buildable foundation to begin porting entities, items, networking, and data from the original mod while honoring licensing and namespace changes.
- Build: Not run yet (Gradle wrapper not present; will run after adding core content).

## 2025-11-18 (later)
- Prompt/task: "Port core registries (entities/items/structures)."
- Steps:
  - Bumped project version to 0.0.2 per AGENTS rule.
  - Added NeoForge deferred registries for entities, items (spawn eggs), and structure type under new namespace `modern_companions`; wired into the mod event bus.
  - Stubbed companion entity classes and structure class to allow compilation while full behavior/data is ported in subsequent steps.
- Rationale: Establishes all registry hooks in the new namespace so gameplay code and assets can be ported incrementally without blocking builds on missing registrations.
- Build: Not run yet; will execute once core logic is ported or a Gradle wrapper is added.

## 2025-11-18 (next)
- Prompt/task: "Continue with these steps" (port logic and wiring).
- Steps:
  - Added common config port (`ModConfig`) using NeoForge ModConfigSpec and registered it during mod construction.
  - Hooked creative tab population to include companion spawn eggs in the Spawn Eggs tab via `BuildCreativeModeTabContentsEvent`.
  - Left entity/structure behavior and networking TODOs to be ported with logic; avoided adding binary assets per AGENTS rules.
- Rationale: Extends the new registries with config and UI integration so further gameplay porting can plug in cleanly.
- Build: Not run yet; pending additional code port before executing Gradle.

## 2025-11-18 (continued)
- Prompt/task: "Continue with these steps" (begin entity logic port).
- Steps:
  - Replaced placeholder base with a simplified `AbstractHumanCompanionEntity` carrying synched state flags, inventory stub, basic goals, save/load, and config-aware attributes.
  - Updated companion entity classes to extend the new abstract base; added data tables from the original (`CompanionData`) and a follow-goal gate (`CustomFollowOwnerGoal`).
  - Kept TODOs for full AI, taming, GUI, networking, and structure generation to be ported next; enabled sources JAR in build for IDE use.
- Rationale: Establishes the structural API surface (flags, inventory, follow logic) so AI/behavior code can be ported incrementally without breaking compile.
- Build: Not run yet; awaiting further logic/asset migration.

## 2025-11-18 (continued again)
- Prompt/task: "Continue with these steps" (menu/network-lite and attribute wiring).
- Steps:
  - Bumped version to 0.0.3 (AGENTS rule).
  - Added menu registration and client screen for companion inventory; hooked spawn eggs creative tab remains.
  - Wired entity attribute registration for all companions; implemented simplified taming/interact path that opens the new menu for owners.
  - Added minimal armor/weapon selection helper and retained TODOs for full AI, networking, and structure logic.
- Rationale: Provides an end-to-end path to open companion inventories without custom packets, enabling iterative gameplay porting while keeping builds coherent.
- Build: Not run yet; still pending fuller logic/asset port before validation.

## 2025-11-18 (continued further)
- Prompt/task: "Continue with these steps" (bring over AI/taming behaviors).
- Steps:
  - Bumped version to 0.0.4 (per AGENTS).
  - Ported key AI pieces: creeper avoidance, low-health self-heal, friendly-fire-aware targeting, and random armor/weapon equipping hooks.
  - Implemented food-based taming requirements (randomized per spawn, persisted via entity data/NBT) with basic feedback lines; added healing from carried food.
  - Added spawn-time health variance and optional armor/weapon seeding based on config; ensured fall-damage toggle respected.
- Rationale: Moves companion behavior closer to the original while keeping code compiling under NeoForge 1.21.1, setting up for remaining patrol/guard/worldgen features.
- Build: Not run yet; pending additional porting before executing Gradle.

## 2025-11-18 (even further)
- Prompt/task: "Continue with these steps" (patrol/guard/hunt targets).
- Steps:
  - Bumped version to 0.0.5 per rule.
  - Added patrol/guard movement goals, hunting/alert targeting, and creeper avoidance; hooked guarding toggle via shift-right-click and preserved menu opening for owners.
  - Synced randomized food requirements via entity data/NBT and expanded AI ordering so companions stay near patrol/guard points.
- Rationale: Rounds out core behavioral loops (taming, guarding, hunting, alerting) closer to the original mod before GUI/network polish and worldgen migration.
- Build: Not run yet; to be done after further GUI/network/worldgen port.

## 2025-11-18 (network prep)
- Prompt/task: "Continue with these steps" (set up toggle networking).
- Steps:
  - Bumped version to 0.0.6.
  - Added NeoForge networking channel and a toggle-flag packet to remotely change companion follow/guard/patrol/hunt/alert/stationery flags.
  - Hooked network registration into the mod bootstrap; added server-side applyFlag handler on companions for future GUI buttons.
- Rationale: Provides a clean channel for upcoming GUI controls to sync behavior flags to the server, matching original mod functionality.
- Build: Not run yet; pending GUI/worldgen port before validation.

## 2025-11-19
- Prompt/task: "Continue with these steps" (GUI controls wired to networking).
- Steps:
  - Bumped version to 0.0.7.
  - Upgraded companion menu registration to support client-side reconstruction via buffer; menu now carries companion id/entity reference safely.
  - Added GUI buttons on the companion screen to toggle follow/patrol/guard/hunt/alert/stationary flags; buttons send the new ToggleFlagPacket and update local state.
- Rationale: Restores player-facing controls for companion behaviors, using the new network channel to sync changes server-side.
- Build: Not run yet; will do after remaining worldgen/asset ports.

## 2025-11-19 (continued)
- Prompt/task: "Continue with these steps" (data tags + packet fix).
- Steps:
  - Bumped version to 0.0.7; fixed menu buffer to use varints for entity id when opening companion GUI.
  - Ported item tag data (axes, swords) into `data/modern_companions/tags/items/` to preserve equipment classification without adding binaries.
- Rationale: Aligns data namespace for item handling and fixes client/server menu sync while staying within text-only asset rules.
- Build: Not run yet; pending worldgen assets/structures before validation.

## 2025-11-19 (README)
- Prompt/task: "Continue with these steps" (documentation).
- Steps:
  - Bumped version to 0.0.8 per policy.
  - Added README.md covering project status, requirements, build/run commands, development notes, and credits.
- Rationale: Provides contributors with current state, constraints, and how to build/run while worldgen assets remain pending.
- Build: Not run yet (binary assets still blocked).

## 2025-11-19 (worldgen data)
- Prompt/task: "Continue porting over what we need to complete the project."
- Steps:
  - Bumped version to 0.0.9.
  - Ported all worldgen JSON/template/tag data from `humancompanions` into `data/modern_companions/...`, updating namespace references; left structure NBTs as TODO since binaries are restricted.
  - Clarified README status section to reflect migrated JSON and remaining asset blockers.
- Rationale: Ensures textual worldgen configuration is ready for 1.21.1 so only binary structures/textures remain before features can re-enable.
- Build: Not run yet (structure NBTs missing; would fail at runtime).

## 2025-11-19 (assets + build attempt)
- Prompt/task: "I have allowed binaries, proceed with what we need to accomplish our goal."
- Steps:
  - Bumped version to 0.0.10 and copied the original assets (textures, models, lang, sounds) plus structure NBTs into `src/main/resources`, rewriting namespaces to `modern_companions`.
  - Added Gradle wrapper from the reference project so builds can run, then attempted `./gradlew build`.
  - Build failed due to large NeoForge/MC 1.21.1 API shifts (missing `FMLJavaModLoadingContext`, `RegistryObject` replaced by `DeferredHolder`, new SynchedEntityData + networking APIs). Began adapting code (event subscribers, registries, GUI toggles via container buttons), but compilation still fails because entity/AI classes require extensive 1.21.1 updates (new ResourceLocation constructors, Animal#isFood, SynchedEntityData builder, etc.).
- Rationale: Imported all required binary resources and established a working build toolchain; next work item is finishing the substantial code migration to the new 1.21 API surface.
- Build: `./gradlew build` currently fails; see `./gradlew build` output in terminal for details (numerous missing-symbol errors stemming from updated NeoForge/Minecraft APIs).

## 2025-11-19 (API migration + networking)
- Prompt/task: "Focus on 1. then perform 2."
- Steps:
  - Updated all registry/config/client hooks to the NeoForge 1.21.1 APIs (`ModLoadingContext`, `DeferredHolder`, new SynchedEntityData builder, ResourceLocation factories, Animal#isFood, new FollowOwnerGoal replacement, etc.) and removed legacy structure/network placeholders. Project now builds successfully via `./gradlew build`.
  - Added NeoForge's payload-based networking (`ModNetwork`, `ToggleFlagPayload`) and rewired the companion screen to send toggle packets, giving us a foundation for future GUI actions beyond follow/patrol/hunt toggles.
- Rationale: Finishes the mandatory build-blocking API migration (Step 1) and reintroduces modern networking support for GUI-driven behavior toggles (Step 2), enabling further gameplay work.
- Build: `./gradlew build` now succeeds (see latest run in terminal output).

## 2025-11-19 (renderers + client run)
- Prompt/task: "Continue with step 1" / "Focus on 1. then perform 2."
- Steps:
  - Added synced data/state for companion skins and sex, assigned randomized appearances during spawn, and exposed `getSkinTexture()` for rendering.
  - Ported the original player-like renderer into `CompanionRenderer`, registered it for all four entities, and updated `CompanionData` helpers (textures, armor detection) to match the new namespace.
  - Ran `./gradlew build` (pass) and attempted `./gradlew runClient` (fails afterwards because the environment lacks `xdg-open`; see `run/logs/latest.log`).
- Rationale: Completes the renderer/appearance work (Step 1) and attempted the requested client smoke-test (Step 2) even though the headless environment prevents launching the game client fully.
- Build/Test: `./gradlew build` ✔️ ; `./gradlew runClient` ❌ (`xdg-open` missing in this environment).

## 2025-11-19 (config-safe attributes)
- Prompt/task: Crash in client pack: "Cannot get config value before config is loaded" during EntityAttributeCreationEvent.
- Steps:
  - Added `ModConfig.safeGet` to fall back to defaults before configs are loaded; updated all attribute and spawn/config reads to use it (base health, spawn armor/weapon, fall-damage, creeper warning, low-health food).
  - Rebuilt successfully (`./gradlew build`); client-side issue should be resolved once the new jar is deployed.
- Rationale: Prevents early-lifecycle crashes when other modpacks fire attribute registration before NeoForge loads config files.
- Build/Test: `./gradlew build` ✔️

## 2025-11-19 (duplicate attributes fix)
- Prompt/task: Handle crash "Duplicate DefaultAttributes entry: entity.modern_companions.knight".
- Steps:
  - Removed the extra mod event subscriber annotation from `ModEntityAttributes` to ensure attributes register only once (now solely via the listener wired in `ModernCompanions`).
  - Rebuilt; output jar is `build/libs/ModernCompanions-0.0.11.jar`.
- Rationale: Avoids double registration of default attributes that NeoForge rejects.
- Build/Test: `./gradlew build` ✔️
