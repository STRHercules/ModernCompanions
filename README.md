# Modern Companions (NeoForge 1.21.1)

Modern Companions is a NeoForge 1.21.1 port and rebrand of the Human Companions mod by justinwon777, maintained by MajorBonghits. The goal is full feature parity under the new mod id `modern_companions`, with updated APIs and attribution intact.

## Status
- Core code now compiles/builds on NeoForge 1.21.1 after swapping over to the new registry, SynchedEntityData, and networking APIs (gameplay is still incomplete but the project builds).
- Worldgen JSON/template/tag data and all companion structure NBTs/textures/models/lang/sounds have been migrated into the new namespace.
- Rendering + advanced gameplay behavior remain placeholder-only until the remaining AI/entity/renderer work is finished.

## Requirements
- Java 21 (JDK 21)
- Minecraft 1.21.1
- NeoForge 21.1.1 (see `gradle.properties` for exact versions)

## Build & Run
```bash
./gradlew build        # builds the mod jar
./gradlew runClient    # launches the NeoForge dev client
```

## Development Notes
- Source lives in `src/`; other top-level directories are read-only references.
- Version is managed in `gradle.properties` and must be bumped with each change (per AGENTS.md).
- Companion behavior toggles now use NeoForge's payload system (`ToggleFlagPayload` registered in `ModNetwork`, emitted by `CompanionScreen`)—follow this pattern for future GUI actions.
- Worldgen/structure data now resides under `data/modern_companions`; ensure future resources use the same namespace.

## Credits
- Original mod: Human Companions by justinwon777.
- Port & maintenance: MajorBonghits.
