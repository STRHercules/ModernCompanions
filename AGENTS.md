# AGENTS.md

This repo contains both editable mod code and read-only relevant sources. These rules ensure we don’t break upstream code while building the `Modern Companions` mod.
This file defines the contribution rules and boundaries for development in this repository.  
Follow these guidelines to ensure stable builds, prevent accidental corruption of game/framework code, and keep commits clean and reviewable.


## Golden Rules

* You MUST consult `TASK.md` for detailed outline of overall goal, if blank - move on with given instruction.
* You must confirm the project builds successfully **before committing any changes**.
* Never create or commit binary files (.dll, .exe, .pdb, .zip, .png, etc.). Text-only changes.
* Edit only where allowed (see Directory Policy). Treat game/framework sources as read-only.
* Leave clear and concise comments detailing the process alongside any code written.
* Increase `build.gradle` version number with every change.


## Directory Policy
```
/ (root)
├─ ModernCompanions/                  # Catalogue of verified Builds
├─ ModDevGradle-main/                 # Gradle Source Code (READ-ONLY)
├─ NeoForge-1.21.x                    # NeoForge Source Code for Minecraft 1.21.1 (READ-ONLY)
├─ Minecraft_Client_Source_1.21.1/    # Minecraft 1.21.1 Decompiled Source (READ-ONLY)
└─ src/                               # Modern Companions Source Code (Only code to edit!)
```

### Allowed edits
- `src/**`
- Root docs: `README.md`, `CONTRIBUTING.md`, `.gitignore`, `.editorconfig`, `SUGGESTIONS.md`, `TRACELOG.md`
- Build configs: `gradle.properties`, `build.gradle`, `settings.gradle`

### Forbidden edits
- Anything under the other top-level folders listed as read-only
- Binary artifacts anywhere
- READ-ONLY Directories
- `TASK.md`
- `AGENTS.md`

## What To Do If Build Fails
* Suggest a text-only fix (e.g., add HintPath using a relative path) but don’t break the read-only policy.

## What To Do When Build Succeeds
* Review the code changes in the commit.
* Suggest refactors, optimizations, or improvements for readability and performance.
* Propose feature expansions or enhancements directly related to the commit.
* Output the suggestions as an entry in `SUGGESTIONS.md`

## What To Do Before Committing 
* Every commit must include an entry in `TRACELOG.md` detailing:
  - The prompt/task given
  - The steps taken
  - Rationale for chosen implementation

## Commit Checklist
- [ ] Project builds with no errors
- [ ] No binary files included
- [ ] Changes limited to allowed directories
- [ ] Clear comments added for all new/modified code
- [ ] `TRACELOG.md` updated with new entry including prompt + detailing steps
- [ ] `SUGGESTIONS.md` updated with new entry
- [ ] Documentation/configs updated if relevant (README, configs, etc.)
- [ ] `build.gradle` version incrimented