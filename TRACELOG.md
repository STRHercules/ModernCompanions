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

## 2025-11-19 (feature parity push)
- Prompt/task: "Textures are now working, let's get started on making sure we have ported ALL functions and features from the original Companions mod to Modern Companions."
- Steps:
  - Ported full gameplay data from the original mod: companion names/skins/food tables, armor selection, health variance, XP/level tracking, taming requirements, patrol/guard/hunt/alert/stationary flags, and friendly-fire protections. Added NeoForge living events to award XP on kills and block owner/companion damage per config.
  - Restored AI roles: custom follow/guard/patrol movement, creeper avoidance, low-health eating, ranged roles (archer/arbalist) using new attack goals, and melee roles (knight/axeguard) with weapon/armor auto-equipping. Updated GUI to include mode cycling, alert/hunt/stationary toggles, clear target, and release actions via new CompanionActionPayload.
  - Introduced TagsInit for weapon tags, refreshed networking handlers, and rewired inventory persistence to the new data-component APIs. Bumped version to 0.1.01 and re-ran full Gradle build under NeoForge 1.21.1.
- Rationale: Brings Modern Companions to feature parity with the original Human Companions while complying with NeoForge 1.21.1 APIs and repo constraints.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-20
- Prompt/task: "Continue polishing and porting what we need."
- Steps:
  - Polished companion GUI to surface health and level while keeping new behavior toggles; added compact formatting helper.
  - Incremented version to 0.1.02 per AGENTS rule and validated with `./gradlew build -x test`.
- Rationale: Improves in-game visibility of companion status and keeps versioning/builds in compliance with project rules.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-20 (Arbalist crossbow parity)
- Prompt/task: "2."
- Steps:
  - Replaced bow-based fallback with a dedicated crossbow attack goal (`ArbalistCrossbowAttackGoal`) adapted from vanilla 1.21 behavior (charge, cooldown, LOS checks, stationary/guard handling).
  - Restored CrossbowAttackMob wiring on Arbalist (charging flag, performCrossbowAttack bridge) and kept auto-equip of carried crossbows.
  - Bumped version to 0.1.03 and reran `./gradlew build -x test` successfully.
- Rationale: Brings Arbalist combat in line with 1.21 crossbow mechanics and restores role parity with the original mod.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-20 (patrol radius + food UI)
- Prompt/task: "2."
- Steps:
  - Added patrol-radius change payload and buttons in the companion UI; owners can now nudge patrol radius up/down (2–32) and see the current value.
  - Surfaced detailed food requirements (requested items/remaining counts) in the screen; added a helper getter on companions.
  - Registered new SetPatrolRadiusPayload in networking and updated GUI to send it; kept optimistic local updates. Bumped version to 0.1.04 and verified `./gradlew build -x test`.
- Rationale: Improves player control over patrol behavior and clarity on taming requirements, matching original mod usability.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-20 (XP progress UI)
- Prompt/task: "2."
- Steps:
  - Exposed companion XP progress/total on the entity and displayed percentage-to-next-level in the GUI alongside health and patrol radius.
  - Bumped version to 0.1.05 and confirmed `./gradlew build -x test` succeeds.
- Rationale: Gives players immediate feedback on companion leveling progress without extra GUI steps, improving parity with the original experience.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-20 (XP bar + numbers)
- Prompt/task: "1."
- Steps:
  - Added XP bar with numeric progress (current/needed) to the companion screen for clearer leveling feedback.
  - Kept the patrol radius and food info; health/level now show alongside the bar.
  - Bumped version to 0.1.06 and validated with `./gradlew build -x test`.
- Rationale: Provides at-a-glance leveling status comparable to the original mod’s experience cues.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-20 (Original GUI textures)
- Prompt/task: "We need to incorporate the assets [...] construct the GUI exactly how they did."
- Steps:
  - Copied original GUI textures (inventory/background and control buttons) into `assets/modern_companions/textures/`.
  - Rebuilt companion screen layout to match the original: textured buttons on the sidebar and stats floated to the right of the inventory. Used custom texture buttons to render the imported PNGs and kept patrol radius/food + XP bar on the right panel.
  - Adjusted texture paths/casing to ensure they load correctly (moved under `textures/gui`). Verified build still passes (`./gradlew build -x test`) with version 0.1.08.
- Rationale: Restores the look-and-feel of the original Companions GUI while preserving modern behavior and controls.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-21 (radius buttons + stats visibility)
- Prompt/task: "Buttons are not displaying properly ... radius buttons reuse assets."
- Steps:
  - Wired radius +/- to the new `radiusbutton.png` sprite sheet with correct UVs; treated them as click-only (no toggle state).
  - Fixed CompanionButton rendering to use hover+mouse press for non-toggle buttons; added missing toggle flag plumbing.
  - Kept stats panel on the right with darker text and ensured companion lookup each frame. Version bumped to 0.1.09 and build confirmed.
- Rationale: Aligns radius controls with provided art and restores consistent button behavior; keeps stats visible when companion entity is available client-side.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-20 (inventory stats panel)
- Prompt/task: "Let's swap the companion inventory gui to: `assets/.../inventory_stats.png` and use the new right-hand stats area."
- Steps:
  - Swapped the CompanionScreen background to `inventory_stats.png`, widening the canvas to 345px to expose the added right-hand panel.
  - Anchored stat rendering within the new panel bounds (229,7)-(326,106) with margins plus dynamic bar sizing to avoid overflow.
  - Kept the existing sidebar buttons in place and trimmed food/status text to fit the new panel width.
  - Bumped version to 0.1.10 per policy and reran `./gradlew build -x test` successfully.
- Rationale: Aligns the companion GUI with the newly provided texture while keeping stats confined to the dedicated panel and maintaining version/build hygiene.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-21 (inventory texture wrap fix)
- Prompt/task: "The inventory screen is being squished/wrapped; display inventory_stats 1:1."
- Steps:
  - Locked CompanionScreen to the exact texture dimensions (345x256) and used the explicit-sized blit call to prevent GL wrapping of widths >256.
  - Kept slot/button layout unchanged so the new texture draws 1:1 without stretching or tiling.
  - Bumped version to 0.1.11 and reran `./gradlew build -x test`, which passed.
- Rationale: Ensures the new inventory_stats background renders at native resolution without squashing or repeat artifacts.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-21 (stats panel alignment)
- Prompt/task: "Stat info is too far away; keep it inside (229,7)-(327,107) on inventory_stats."
- Steps:
  - Corrected stat text anchoring to use GUI-relative coordinates (renderLabels already offsets by left/top), eliminating the double-offset that pushed text off the panel.
  - Updated panel bounds to 327px max X per the texture and kept the stats width clamped inside that region.
  - Bumped version to 0.1.12 and reran `./gradlew build -x test`, which passed.
- Rationale: Places the stat block precisely in the intended texture panel without overflow.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-21 (live XP + food strip)
- Prompt/task: "XP bar/count not updating live; move wanted food to the lower strip and drop the 'Wants:' label."
- Steps:
  - Synced companion XP progress via a new data parameter so clients see real-time bar/needed XP updates while the GUI is open.
  - Added a compact wanted-food formatter and rendered it in the dedicated strip at (228,135)-(328,157) without the 'Wants:' prefix; overflow is wrapped and clamped to the strip.
  - Kept the rest of the stats panel intact and bumped version to 0.1.13; `./gradlew build -x test` passes.
- Rationale: Restores live feedback for leveling and aligns the requested food display with the provided texture layout.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-21 (companion inventory size)
- Prompt/task: "Companion inventory too small; player inventory is sitting too high."
- Steps:
  - Doubled companion storage from 27 to 54 slots (6x9) by enlarging the entity SimpleContainer and menu row count, which naturally lowers the player inventory to the correct Y offset.
  - Bumped version to 0.1.14 and verified build with `./gradlew build -x test`.
- Rationale: Matches GUI layout expectations and keeps player slots aligned with the background texture while giving companions more carry capacity. Updated fallback menu container to the new size for safety.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-21 (1px nudge + class title)
- Prompt/task: "Shift the GUI down 1px and capitalize the displayed class name."
- Steps:
  - Offset the screen anchor by 1px and render the background at `leftPos/topPos` so all slots/buttons move together.
  - Capitalized the class label readout (axeguard -> Axeguard, etc.).
  - Bumped version to 0.1.15; build verified with `./gradlew build -x test`.
- Rationale: Aligns the inventory art with its shadow and improves label readability.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-21 (pickup toggle + auto-loot)
- Prompt/task: "I want companions to pick up items with a toggleable button and small magnet effect."
- Steps:
  - Added a synced `pickup` flag with save/load support on companions, defaulting to enabled and reset on release.
  - Implemented a gentle 3-block magnet sweep in server ticks that pulls nearby item entities and funnels them into the companion inventory when pickup is on.
  - Wired a new pickup toggle button beneath CLEAR using `pickupbutton.png`, updating button logic to handle vertical toggle textures and sending the existing ToggleFlag payload.
  - Bumped version to 0.1.16 and ran `./gradlew build -x test` successfully.
- Rationale: Gives companions player-like item collection with a clear on/off control so loot from their kills reliably lands in the companion inventory.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-21 (safe foods)
- Prompt/task: "Make sure companions do NOT eat raw foods, or spider eyes, rotten flesh."
- Steps:
  - Added an explicit disallow list (raw meats/fish, spider eyes, rotten flesh) and removed raw fish from the companion food pool; food checks now reject blacklisted items for taming and self-healing.
  - Ensured inventory eating routines skip non-approved foods; random food requirements only pick from allowed foods.
  - Bumped version to 0.1.17 and rebuilt with `./gradlew build -x test`.
- Rationale: Prevents companions from consuming unsafe/raw items while keeping taming and auto-heal behavior intact.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-21 (RPG attribute spread)
- Prompt/task: "Add STR/DEX/INT/END attributes to companions with varied effectiveness."
- Steps:
  - Added synced STR/DEX/INT/END data with NBT save/load and random generation: base 4 in each, 23 free points spread, plus a 2–6% specialist roll granting +5 to one stat.
  - Applied stat effects: STR boosts attack damage/knockback; DEX raises move/attack speed and small knockback resistance; END grants extra health, toughness-based physical damage reduction, and higher knockback resistance; INT increases XP gain rate.
  - Wired spawn/load flows to generate stats, adjust base health from END, and reapply attribute modifiers safely; ensured stats influence XP gain and damage handling.
  - Bumped version to 0.1.18 and ran `./gradlew build -x test`.
- Rationale: Gives companions a traditional RPG-style stat spread so each spawn feels distinct in combat, mobility, survivability, and progression.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-21 (GUI attributes + wanted food move)
- Prompt/task: "Display companion stats on the inventory GUI; move wanted food to 227,225-328,248; place attributes at 228,137-326,194."
- Steps:
  - Added an Attributes block on the right panel showing STR/DEX/INT/END with underline header, confined to the new bounds.
  - Shifted the wanted food readout to the lower strip (227,225)-(328,248) with wrapping and fallback text when fulfilled.
  - Kept class/health/xp/patrol info in the top stats area and reran `./gradlew build -x test`.
- Rationale: Surfaces RPG stats directly in the companion inventory while relocating the food section to the requested area without overlapping other UI elements.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-21 (wanted food strip adjust)
- Prompt/task: "Relocate the wanted food section to 228,215-327,236."
- Steps:
  - Updated CompanionScreen texture bounds for the food strip to match the new coordinates and preserved wrapping within the tighter height.
  - Bumped version to 0.1.19 and reran `./gradlew build -x test`.
- Rationale: Aligns the wanted-food display with the newly requested location on the inventory texture while keeping text constrained.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-21 (low-health food requests)
- Prompt/task: "Companions at missing hearts say they're full—make them ask for food and show it in the GUI."
- Steps:
  - Added a low-health request check that pings the owner every 10s when the companion is hurt, tamed, and has no food, with a clear chat line.
  - Exposed a new GUI status string: if hurt+tamed with no food it shows “Needs food to heal”, otherwise “Healing...” or empties when healthy; renderWantedFood now uses this status.
  - Bumped version to 0.1.20 and reran `./gradlew build -x test`.
- Rationale: Ensures injured companions proactively ask for food and that the inventory screen reflects their healing needs instead of staying silent/“full.”
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-21 (food actually heals)
- Prompt/task: "Eating animation plays and food is consumed, but health doesn’t restore."
- Steps:
  - Simplified EatGoal to heal immediately when food is available: consume one food, apply healing, mark eating state, and reset when healthy or out of food.
  - Removed the unused hold/use animation that never completed the vanilla eating cycle for companions.
  - Bumped version to 0.1.21 and reran `./gradlew build -x test`.
- Rationale: Ensures companions regain health whenever they eat, instead of just burning inventory with no healing.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-21 (healing to full + animation)
- Prompt/task: "Companions eat but stop with 1 heart missing; need full heal and animation."
- Steps:
  - Reworked food selection to choose the smallest-overflow food so healing can occur even if missing health is less than the food’s nutrition.
  - Clamped heal to the missing amount and kept consuming until fully healed; offhand swing restored for a visible eat animation.
  - Version bumped to 0.1.22 and `./gradlew build -x test` passes.
- Rationale: Prevents companions from getting stuck a heart short and shows a clear eat action while consuming appropriate food.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-21 (eating VFX/SFX)
- Prompt/task: "Food vanishes with no visible eating; show the animation."
- Steps:
  - Added explicit eat effects: plays the item’s eating sound and spawns item particles near the face each time a bite is taken.
  - Restored off-hand use animation during eating while keeping instant healing behavior to avoid stalling.
  - Bumped version to 0.1.23 and reran `./gradlew build -x test`.
- Rationale: Makes companion eating noticeable (sound + particles) while preserving the reliable healing flow.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-21 (vanilla-paced eating)
- Prompt/task: "Food/healing are instant; make eating behave like vanilla timing."
- Steps:
  - Reworked EatGoal to respect item use duration: companions hold food in offhand, animate swings, and only heal when the use timer completes; they continue through multiple bites until full or out of food.
  - Swapped instant heal helper for a targeted heal-from-stack method and updated LowHealthGoal to reuse it.
  - Kept eating sounds/particles and bumped version to 0.1.24; build verified with `./gradlew build -x test`.
- Rationale: Eating now follows vanilla pacing and visuals instead of instant consumption while still guaranteeing healing completion.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-21 (food requests cooldown + variety)
- Prompt/task: "Add more request lines and reduce frequency of food requests when hurt."
- Steps:
  - Added 11 varied food-request phrases for injured companions asking their owner.
  - Increased the cooldown between requests to ~30s (600 ticks) to cut spam.
  - Bumped version to 0.1.25 and reran `./gradlew build -x test`.
- Rationale: Makes pleas for food feel more natural and less chat-spammy while still alerting the owner when healing is needed.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-21 (specialist highlight)
- Prompt/task: "Highlight specialist attributes in the GUI."
- Steps:
  - Added a synced specialist attribute index (-1 when none), saved/loaded via NBT and set during stat roll when the +5 specialist bonus applies.
  - Companion GUI now renders the specialist stat in yellow with a star marker.
  - Bumped version to 0.1.26 and reran `./gradlew build -x test`.
- Rationale: Visually calls out specialist companions and which attribute received the bonus.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-21 (Jade/WTHIT attributes)
- Prompt/task: "Expose Companion Attributes in WTHIT/Jade"
- Steps:
  - Added optional Modrinth deps for Jade 15.10.3+neoforge and WTHIT neo-12.8.2; bumped version to 0.1.27.
  - Implemented shared tooltip formatter plus Jade and WTHIT plugins/providers to send STR/DEX/INT/END and render a compact "S:x | D:x | I:x | E:x" line on companion HUD entries; registered WTHIT entrypoint via `waila_plugins.json` and added optional mod deps in `neoforge.mods.toml`.
  - Ran `./gradlew build -x test` to confirm the integration compiles and builds cleanly.
- Rationale: Surfaces RPG attributes at a glance in both popular HUD overlays without requiring either mod as a dependency.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-21 (HUD deps optional)
- Prompt/task: "Jade/WTHIT are OPTIONAL, we do not want them to be hard dependancies"
- Steps:
  - Removed runtimeOnly pulls for Jade/WTHIT so they remain purely compileOnly (no bundled/required jars) while keeping optional dependency flags in mod metadata; bumped version to 0.1.28.
  - Rebuilt to verify the project still compiles without the overlays present.
- Rationale: Ensures both overlays stay optional add-ons and are not brought in transitively by Modern Companions.
- Build/Test: `./gradlew build -x test` ✔️

## 2025-11-21 (No Jade requirement)
- Prompt/task: "Still being told I need Jade installed. We should oinly be loading Jad support IF Jade is installed"
- Steps:
  - Removed Jade/WTHIT dependency entries from `neoforge.mods.toml` so the mod no longer advertises/requests those mods at load time while keeping compileOnly hooks available.
  - Bumped version to 0.1.29 and rebuilt successfully.
- Rationale: Prevents NeoForge from surfacing Jade as a suggested/required dependency; integrations now stay dormant unless the overlays are actually present.
- Build/Test: `./gradlew build -x test` ✔️
