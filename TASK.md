# Miner AI Behavior – Stair-Stepping Ore Hunter

This document describes how **Miners** should behave when searching for and mining ores. It is written as implementation guidance for Codex so it can safely update the Miner logic.

The goal:  
Miners should **search for ores in a 3D patrol cube** and **dig toward them in a safe, stair-like pattern** (no vertical drops), so both the miner and players can walk the tunnel they create.

---

## 1. High-Level Goals

1. Miners prioritize **finding and mining ores** inside their patrol area.
2. To reach ores, they **dig through stone-like blocks** (stone, andesite, diorite, granite, etc.).
3. Their patrol logic should mirror **Lumberjacks’ patrol radius**, but extended into a **3D cube** (N/S/E/W and up/down).
4. When ores are **below** the miner, they **dig downward using a staircase pattern**, not a straight vertical shaft.
5. The resulting tunnels should be **naturally traversable**:
   - No falls greater than 1 block.
   - Floor steps go down 1 block at a time.
   - At least a 2-block tall tunnel (3 if that’s the mod’s standard).

---

## 2. Terminology & Constants

Use names similar to the existing worker configuration where possible.

- `workCenter`: Block position that defines the center of the miner’s work area  
  (usually the miner’s hut / workstation).
- `horizontalRadius` (same as lumberjack patrol radius):  
  Max distance in X/Z from `workCenter`.
- `verticalRadiusUp`: How far **up** from `workCenter.y` the miner is allowed to scan/mine.
- `verticalRadiusDown`: How far **down** from `workCenter.y` the miner is allowed to scan/mine.
- `scanVolume`: The full **3D cube**:  
  - X: `[centerX - horizontalRadius, centerX + horizontalRadius]`  
  - Y: `[centerY - verticalRadiusDown, centerY + verticalRadiusUp]`  
  - Z: `[centerZ - horizontalRadius, centerZ + horizontalRadius]`
- **Ore blocks**: Any block tagged / registered as ore (e.g. in an `#ores` tag).
- **Filler blocks**: Stone, andesite, diorite, granite, deepslate, etc. (blocks the miner is allowed to dig through to reach ores).

---

## 3. Patrol & Scan Logic (3D Cube)

The miner’s work radius should behave like the lumberjack’s patrol radius, but extended vertically:

1. **Scan the cube** around `workCenter` for ore:
   - Only consider positions inside `scanVolume`.
   - Ignore blocks outside this cube (hard boundary).
2. Prefer **closer** ores first:
   - Use distance from **current miner position** (not just from `workCenter`).
   - Ties can be broken by Y level (e.g. prefer higher ores first, then lower ones).
3. If no ore is visible in the volume:
   - Optionally run a **“prospecting” pattern**: digging small tunnels in different directions within the cube, still respecting stair rules when going down.
   - Or idle / return to hut, depending on existing behavior.

Scanning can be done using:
- Breadth-first search (BFS) from the miner’s current position, constrained to `scanVolume`, or
- A periodic raster-scan / sampling of block positions inside the cube, then picking the best ore candidate.

---

## 4. Safe Stair-Style Downward Mining

When the miner detects ores **below** their current Y level and needs to dig down to reach them, they **must not** dig a vertical 1x1 shaft. Instead they should **carve a staircase**.

**Key rules for “natural stairs”:**

1. **No drop over 1 block:**
   - Between any two consecutive walkable positions along the path, `deltaY` ∈ {0, -1, +1}.  
   - Going downward, only step down 1 block at a time.
2. **Clear headroom:**
   - For each step, clear at least 2 blocks of vertical space above the floor (or whatever the mod standard is).
3. **Consistent corridor width:**
   - At minimum, a 1-block wide corridor (2-block high).
   - Optionally 2-block wide if that’s already standard for tunnels in the mod.
4. **No isolated ledges or holes:**
   - Do not leave floating platforms or 2+ block drops in the floor.
   - If digging out a block would create a 2-block fall directly in the main path, the miner should fill/avoid that or reroute.

The miner is **not placing stair blocks**. Instead, they mine in a pattern that leaves the floor as a series of descending steps.

---

## 5. Example Stair Digging Pattern

This is one concrete way the miner can create a stair when going down toward an ore vein.

Assume we choose a direction vector `dir` (e.g. facing toward the target ore in X/Z).  
For each “step”:

1. Let `currentPos` be the miner’s current **feet** position.
2. Determine the next step:
   - `nextPos = currentPos + dir + (0, -1, 0)` (one block forward, one block down), as long as that stays within `scanVolume`.
3. To carve the next step:
   - Mine the block at `nextPos` (the new floor).
   - Mine the blocks at `nextPos.up()` and optionally `nextPos.up(2)` to ensure headroom.
4. Move the miner to `nextPos`.
5. Repeat until:
   - Reached the ore block or
   - Hit a safety limit (e.g. `verticalRadiusDown`), or
   - Encountered a hazard (lava, open cave, void, etc.).

If the ore is not directly in front of the miner, the algorithm can recompute `dir` after each step to gently “curve” the staircase toward the ore while still obeying the `deltaY` ≤ 1 rule.

You don’t need this **exact** pattern; the key behavior is:  
> “Never dig straight down. Always leave a path where you walk forward and down in 1-block steps, keeping a walkable tunnel behind you.”

---

## 6. Target Selection Logic

When choosing **which ore** to mine next inside the patrol cube:

1. Build a list of all ore positions inside `scanVolume`.  
2. Filter out ores that are **not realistically reachable** (e.g. above the ceiling height limit or behind restricted blocks, depending on the mod’s rules).
3. Sort candidates by:
   1. **Distance** from current miner position (shortest first, use Euclidean distance or pathfinding cost).
   2. **Y level** (optional tiebreak: prefer closer-to-current-Y ores first).
4. Pick the first ore in the sorted list as the **current target**.

When moving to the chosen ore:

- If the ore is on the **same Y level or above**, the miner may be able to walk / dig horizontally or upwards using standard block-breaking and pathfinding (no special stair logic needed beyond usual rules).
- If the ore is **below** the miner’s current Y level:
  - Use the **stair-mining** behavior described above to descend safely.
- If the ore is **above** the miner's current Y level:
   - Attempt to path naturally, if no path is available, use any stone material (stone, andesite, diorite, granite, etc.) in their inventory to place/create stair-like method to ascend to their target.

---

## 7. Interaction with Stone/Andesite/etc.

Miners should treat stone-like blocks as **filler** that can be removed to reach ores:

- When moving toward a target ore, if a filler block (stone, andesite, diorite, granite, deepslate, etc.) is in the path:
  - Mark it as a valid block to mine.
  - Break it as part of the tunneling / staircase excavation.
- If a non-mineable block is in the way (e.g. protected structure, blocked region):
  - Abort or reroute to another target ore.

This ensures miners don’t just stop when they see stone between themselves and an ore vein. They should **actively tunnel** to reach it while obeying the stair rules.

---

## 8. Safety & Edge Cases

Codex should also account for safety and weird map situations:

1. **Vertical limits:**
   - Never dig below `workCenter.y - verticalRadiusDown`.
   - Never dig above `workCenter.y + verticalRadiusUp`.
2. **Hazards:**
   - If digging reveals lava, open air cavity with a large drop, or other danger directly on the path:
     - Stop progressing in that direction.
     - Mark that target as unsafe.
     - Choose a different ore target.
3. **No valid path:**
   - If no ore in the cube is reachable without breaking safety rules, the miner should:
     - Return to idle behavior (e.g. go back to hut, patrol, etc.).
4. **Path maintenance:**
   - The path behind the miner should always remain walkable:
     - No 2+ block drops.
     - No 1-block crawl spaces.
     - No 1x1 vertical shafts.

---

## 9. Implementation Notes for Codex

When updating the miner logic, Codex should:

1. **Reuse existing patterns** from lumberjacks where possible:
   - Patrol radius handling, respecting `workCenter` and horizontal range.
   - Any shared worker configuration fields (so the UX stays consistent).
2. Add **vertical scan limits** via `verticalRadiusUp` and `verticalRadiusDown`.
3. Introduce a clear separation between:
   - **Ore-finding / scanning**, and
   - **Path planning + stair-mining** toward the ore.
4. Keep the miner’s behavior **deterministic and safe**:
   - Avoid random straight-down digging.
   - Prioritize predictable, staircase-style tunnels.
5. Preserve existing APIs/events where possible so other systems (e.g. UI, town logs, etc.) don’t break.

The end result should be a miner that:
- Treats its work area as a **3D cube** of potential ore.
- Digs **toward** ores through stone-like blocks.
- Always creates **walkable stair-style tunnels** when going down.
