# Modern Companions - NeoForge 1.20.1 Porting Process

## Goal
Port the entire Modern Companions mod (code + data + integrations) to NeoForge 1.20.1 for Minecraft 1.20.1, while keeping upstream sources read-only and the repo stable.

## Parallel Build Requirement (1.20.1 + 1.21.1)
Maintain parallel builds for both target versions:
- Minecraft 1.20.1 + NeoForge 1.20.1.x
- Minecraft 1.21.1 + NeoForge 1.21.1.x

Required developer UX:
```bash
# Build the 1.20.1 target
./gradlew build 1.20.1

# Build the 1.21.1 target
./gradlew build 1.21.1
```

Implementation note:
- Provide distinct Gradle tasks or variant-aware build logic so each command selects its matching
  Minecraft + NeoForge + dependency versions without manual edits to `gradle.properties`.

## Constraints and Repository Policy
- Only edit files under `src/**` and the approved root configs/docs.
- Treat all upstream sources as read-only (Minecraft/NeoForge sources and other mod sources).
- Keep decompiled Minecraft sources outside of version control or in an ignored output path.

## Pre-Flight Checklist
- Create a working branch for the 1.20.1 port.
- Verify the current 1.21.1 build is green before starting.
- Collect dependency versions that explicitly support Minecraft 1.20.1 (NeoForge, Parchment, Curios, Jade, WTHIT, etc.).
- Identify all mod features and integrations to verify after the port (companions, UI, enchantments, loot modifiers, recipes, Curios slots, WTHIT/Jade overlays, configs).

## Decompile Minecraft 1.20.1 Client Sources (Tool: `minecraft-java-decomp`)
This repository includes a local copy of `minecraft-java-decomp` under `minecraft-java-decomp/`.

1) List versions and confirm 1.20.1 is available:
```bash
# Lists available Minecraft versions
node ./minecraft-java-decomp/src/cli.js --versions
```

2) Choose an output folder **outside** the repo or under a clearly ignored location:
```bash
# Example output folder outside the repo
mkdir -p ../minecraft-client-1.20.1
```

3) Decompile the 1.20.1 client:
```bash
# Decompile Minecraft 1.20.1 client sources using local CLI
node ./minecraft-java-decomp/src/cli.js --version 1.20.1 --side client --path ../minecraft-client-1.20.1 --force
```

4) Verify output structure:
```bash
# Quick sanity checks on output
ls ../minecraft-client-1.20.1/net/minecraft/client
ls ../minecraft-client-1.20.1/net/minecraft/world
```

Notes:
- This tool downloads and decompiles from official Mojang sources, so network access is required.
- Treat the decompiled output as read-only reference material.

## Version Alignment (Config Updates)
Update the build configuration to target 1.20.1. This is usually done in `gradle.properties` and `build.gradle`.

Checklist:
- `minecraft_version` -> `1.20.1`
- `neo_version` -> a NeoForge 1.20.1 build that matches your mappings
- `parchment_version` -> the 1.20.1 Parchment release aligned with the NeoForge build
- Dependencies -> Curios/Jade/WTHIT versions that explicitly support 1.20.1
- Mod metadata -> update `src/main/resources/META-INF/neoforge.mods.toml` loader and version ranges
- Data pack/Resource pack formats -> set to the correct 1.20.1 values

## Code Porting Workflow
The safest approach is to downgrade incrementally and fix compile errors in small groups.

1) Update build targets (versions and dependencies), then sync.
2) Run a compile to surface API changes:
   - `./gradlew clean build`
3) Fix errors in batches, re-run the build after each batch.

### Common API Downgrades from 1.21.x -> 1.20.1
Use these as a checklist while resolving compile errors:
- **Data components vs NBT**: 1.20.1 does not include the newer data component system. Replace data-component usage with NBT or capabilities where applicable.
- **Networking**: If the current code uses 1.21+ payload/codec APIs, migrate to 1.20.1-compatible networking (typically `SimpleChannel` + `FriendlyByteBuf`).
- **Registries**: Verify registry bootstrap and registration events align with 1.20.1 NeoForge patterns.
- **Events**: Some event classes/method signatures may differ; resolve with 1.20.1 NeoForge docs or decompiled code.
- **Attributes and AI**: Check entity attribute registration and AI goal references against 1.20.1 mappings.
- **Tags and Loot**: Validate loot modifier JSON and tag formats for 1.20.1.
- **Resources**: Validate `pack.mcmeta` and resource/data pack formats for 1.20.1.

## Using Decompiled Sources to Resolve Differences
When encountering an API mismatch:
1) Locate the 1.20.1 class/method in the decompiled source.
2) Compare it to the current 1.21.1 usage in `src/`.
3) Adjust imports, method signatures, or behavior to match 1.20.1.

Example workflow:
```bash
# Find a class or method in the 1.20.1 source
rg -n "class Entity" ../minecraft-client-1.20.1/net/minecraft

# Find the equivalent usage in this mod's source
rg -n "Entity" src/main/java
```

## Feature Verification Checklist
Test each feature after the build succeeds:
- Companion spawning, persistence, and AI behavior.
- Companion inventory UI and custom screens.
- Enchantments, loot modifiers, and recipes.
- Curios integration (slots, items, rendering).
- WTHIT/Jade overlays and tooltips.
- Configs (client/server), keybinds, and networking behavior.

## Troubleshooting Notes
- If a dependency does not have a 1.20.1-compatible build, you may need to:
  - Remove the integration temporarily, or
  - Use conditional compile flags or loaders, or
  - Implement a compatibility shim in `src/`.
- If a feature depends on a 1.21.x-only API, document the fallback behavior and add a TODO.

## Deliverables
- A clean build targeting NeoForge 1.20.1.
- Updated `src/` code and resources compatible with 1.20.1.
- Updated mod metadata and versions for 1.20.1.
- Verified gameplay feature parity with the 1.21.1 build.
