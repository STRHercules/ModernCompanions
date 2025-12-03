package com.majorbonghits.moderncompanions.entity.job;

import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Optional;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.common.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Miner job: scan a 3D patrol cube for ores and tunnel to them using a
 * walkable staircase (never straight down). Keeps drops and leaves a
 * traversable tunnel (<=1 block deltas, 2-block headroom).
 */
public class MinerJobGoal extends Goal {
    private static final Logger LOGGER = LoggerFactory.getLogger("ModernCompanions-Miner");
    private static final int SEARCH_COOLDOWN = 20;
    private static final int BREAK_COOLDOWN = 3;
    private static final int MAX_PLAN_STEPS = 8192;
    private static final TagKey<Block>[] ORE_TAGS = new TagKey[]{
            BlockTags.COAL_ORES, BlockTags.COPPER_ORES, BlockTags.IRON_ORES, BlockTags.GOLD_ORES,
            BlockTags.REDSTONE_ORES, BlockTags.LAPIS_ORES, BlockTags.DIAMOND_ORES, BlockTags.EMERALD_ORES,
            Tags.Blocks.ORES
    };

    private final AbstractHumanCompanionEntity companion;
    private final int baseRadius;
    private final boolean enabled;
    private final Set<Block> allowBlocks = new HashSet<>();
    private final Set<Block> denyBlocks = new HashSet<>();

    private BlockPos targetOre;
    private final List<BlockPos> oreQueue = new ArrayList<>();
    private int oreIndex = 0;
    private final ArrayDeque<BlockPos> digQueue = new ArrayDeque<>();
    private int searchCooldown;
    private int breakTicksRemaining;
    private int swingCooldown;
    private int progressStallTicks = 0;
    private int forceBreakTicker = 0;
    private BlockPos lastStallTarget = null;
    private Vec3 lastProgressPos = Vec3.ZERO;
    private int globalRescanTicker = 0;
    private boolean sessionPlanned = false;
    private final Set<BlockPos> unreachableOres = new HashSet<>();
    private int lastActionTick = 0;
    private int idleTicks = 0;
    private BlockPos plannedCenter = BlockPos.ZERO;
    private int plannedRadius = 0;
    private int plannedUp = 0;
    private int plannedDown = 0;
    private boolean announcedNoWork = false;

    public MinerJobGoal(AbstractHumanCompanionEntity companion, int searchRadius, boolean enabled) {
        this.companion = companion;
        this.baseRadius = Math.max(4, searchRadius);
        this.enabled = enabled;
        loadConfigBlockLists();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!isActiveJob()) return false;
        if (searchCooldown > 0) {
            searchCooldown--;
            return false;
        }
        searchCooldown = SEARCH_COOLDOWN;
        bootstrapPlan();
        return tryPlanNextOre();
    }

    @Override
    public boolean canContinueToUse() {
        return isActiveJob() && (!digQueue.isEmpty() || !oreQueue.isEmpty());
    }

    @Override
    public void start() {
        moveToCurrentDigPos();
        lastActionTick = companion.tickCount;
    }

    @Override
    public void stop() {
        persistPlanProgress();
        digQueue.clear();
        targetOre = null;
        companion.getNavigation().stop();
        breakTicksRemaining = 0;
        swingCooldown = 0;
        progressStallTicks = 0;
        sessionPlanned = false;
        lastActionTick = 0;
        idleTicks = 0;
        announcedNoWork = false;
        lastStallTarget = null;
        forceBreakTicker = 0;
        info("Goal stopped; state persisted (remaining=%d, mined=%d)", oreQueue.size(), companion.getMinerOresMined());
    }

    @Override
    public void tick() {
        if (digQueue.isEmpty()) {
            // Immediately replan so we never sit at "end of plan".
            if (!tryPlanNextOre()) {
                companion.getNavigation().stop();
                searchCooldown = 0; // force quick rescan
                idleTicks++;
                debug("Tick idle=%d (oreQueue=%d digQueue=%d mined=%d counted=%d)", idleTicks, oreQueue.size(), digQueue.size(), companion.getMinerOresMined(), companion.getMinerOresCounted());
                return;
            }
            idleTicks = 0;
            if (digQueue.isEmpty()) return;
        }
        BlockPos current = digQueue.peekFirst();
        trackStallTarget(current);
        idleTicks = 0;

        // If the current block was removed externally, pop and continue.
        if (companion.level().getBlockState(current).isAir()) {
            digQueue.pollFirst();
            moveToCurrentDigPos();
            return;
        }

        // Hard pause detection: if we haven't swung or mined for a while, force a replan.
        if (companion.tickCount - lastActionTick > 60) {
            lastActionTick = companion.tickCount;
            info("Stall detected near %s (ore=%s digQueue=%d remaining=%d mined=%d)",
                    fmt(companion.blockPosition()), fmt(targetOre), digQueue.size(), oreQueue.size(), companion.getMinerOresMined());
            if (!tryPlanNextOre()) {
                companion.getNavigation().stop();
                searchCooldown = 0;
            }
            ensureDiggingProgress();
            forceBreakImmediate();
            dumpDigQueue();
            return;
        }

        // Navigate toward current dig position.
        if (companion.distanceToSqr(Vec3.atCenterOf(current)) > 6.0D) {
            moveToCurrentDigPos();
            ensureDiggingProgress();
            return;
        }
        // Periodic global rescan to keep progressing if stuck near ores
        globalRescanTicker++;
        if (globalRescanTicker > 100) {
            globalRescanTicker = 0;
            if (!tryPlanNextOre()) {
                companion.getNavigation().stop();
                searchCooldown = 0;
            }
            ensureDiggingProgress();
            debug("Global rescan triggered; queues: ore=%d dig=%d", oreQueue.size(), digQueue.size());
            return;
        }
        // Stall detection: if we haven't moved meaningfully for a while, replan.
        if (companion.position().distanceToSqr(lastProgressPos) < 0.25) {
            progressStallTicks++;
            if (progressStallTicks > 100) {
                progressStallTicks = 0;
                info("Movement stall at %s (ore=%s digQueue=%d)", fmt(companion.blockPosition()), fmt(targetOre), digQueue.size());
                if (!planPathToOre(targetOre)) {
                    tryPlanNextOre();
                }
                dropCurrentIfStillStuck();
                ensureDiggingProgress();
                forceBreakImmediate();
                return;
            }
        } else {
            lastProgressPos = companion.position();
            progressStallTicks = 0;
        }

        // Break timing: swing, then decrement.
        if (breakTicksRemaining <= 0) {
            breakTicksRemaining = computeBreakTicks(current);
            swingCooldown = 0;
        }
        if (swingCooldown-- <= 0) {
            companion.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
            swingCooldown = BREAK_COOLDOWN;
            lastActionTick = companion.tickCount;
        }
        breakTicksRemaining--;

        if (breakTicksRemaining <= 0) {
            mine(current);
            digQueue.pollFirst();
            progressStallTicks = 0;
            forceBreakTicker = 0;
            lastActionTick = companion.tickCount;
            if (digQueue.isEmpty()) {
                tryPlanNextOre();
            }
            moveToCurrentDigPos();
            ensureDiggingProgress();
        }
    }

    /* -------------------- Planning -------------------- */

    /**
     * Find the nearest mineable block. If oresOnly is true, restrict to ores; otherwise
     * also allow filler stone-like blocks that are accessible (air-adjacent) to start tunneling.
     */
    private BlockPos findNearestMineable(boolean oresOnly) {
        BlockPos center = workCenter();
        int hr = horizontalRadius();
        int up = verticalRadiusUp();
        int down = verticalRadiusDown();
        Level level = companion.level();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-hr, -down, -hr),
                center.offset(hr, up, hr))) {
            if (oresOnly) {
                if (!isOre(pos)) continue;
            } else {
                if (!(isOre(pos) || isFiller(pos))) continue;
                if (!isAccessibleStart(level, pos)) continue;
            }
            double dist = pos.distSqr(companion.blockPosition());
            if (dist < bestDist) {
                bestDist = dist;
                best = pos.immutable();
            }
        }
        return best;
    }

    // One-time per patrol session: load persisted ore plan or resurvey the cube so we can resume after reloads.
    private void bootstrapPlan() {
        if (sessionPlanned) return;
        loadPersistedPlan();
        if (oreQueue.isEmpty() || workAreaChanged()) {
            surveyAndPersist(true);
        }
        sessionPlanned = true;
    }

    private void loadPersistedPlan() {
        oreQueue.clear();
        oreQueue.addAll(companion.getMinerOreMemory());
        oreIndex = Math.min(companion.getMinerOreIndex(), Math.max(oreQueue.size() - 1, 0));
        plannedCenter = companion.getMinerPlanCenter();
        plannedRadius = companion.getMinerPlanRadius();
        plannedUp = companion.getMinerPlanUp();
        plannedDown = companion.getMinerPlanDown();
        pruneInvalidOres();
    }

    private void surveyAndPersist(boolean resetMined) {
        oreQueue.clear();
        unreachableOres.clear();
        plannedCenter = workCenter();
        plannedRadius = horizontalRadius();
        plannedUp = verticalRadiusUp();
        plannedDown = verticalRadiusDown();
        Level level = companion.level();
        for (BlockPos pos : BlockPos.betweenClosed(plannedCenter.offset(-plannedRadius, -plannedDown, -plannedRadius),
                plannedCenter.offset(plannedRadius, plannedUp, plannedRadius))) {
            if (!isOreState(level.getBlockState(pos))) continue;
            oreQueue.add(pos.immutable());
        }
        oreQueue.sort(Comparator.comparingDouble(p -> p.distSqr(companion.blockPosition())));
        oreIndex = 0;
        companion.setMinerOreIndex(0);
        companion.overwriteMinerOreMemory(oreQueue);
        companion.setMinerPlanCenter(plannedCenter);
        companion.setMinerPlanRadius(plannedRadius);
        companion.setMinerPlanUp(plannedUp);
        companion.setMinerPlanDown(plannedDown);
        companion.setMinerOresCounted(oreQueue.size());
        if (resetMined) companion.setMinerOresMined(0);
        announcedNoWork = oreQueue.isEmpty();
        info("Surveyed %d ores in cube center=%s r=%d up=%d down=%d (resetMined=%s)",
                oreQueue.size(), fmt(plannedCenter), plannedRadius, plannedUp, plannedDown, resetMined);
    }

    private void mergeNewlyFoundOres() {
        BlockPos center = workCenter();
        int hr = horizontalRadius();
        int up = verticalRadiusUp();
        int down = verticalRadiusDown();
        Level level = companion.level();
        Set<BlockPos> known = new HashSet<>(oreQueue);
        int found = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-hr, -down, -hr),
                center.offset(hr, up, hr))) {
            BlockState state = level.getBlockState(pos);
            if (!isOreState(state)) continue;
            BlockPos copy = pos.immutable();
            if (known.add(copy)) {
                oreQueue.add(copy);
                found++;
            }
        }
        if (found > 0) {
            oreQueue.sort(Comparator.comparingDouble(p -> p.distSqr(companion.blockPosition())));
            companion.setMinerOresCounted(companion.getMinerOresCounted() + found);
            unreachableOres.clear();
            persistPlanProgress();
            announcedNoWork = false;
            info("Merged %d newly seen ores; total now %d", found, oreQueue.size());
        }
    }

    private void pruneInvalidOres() {
        Level level = companion.level();
        boolean changed = false;
        for (int i = oreQueue.size() - 1; i >= 0; i--) {
            BlockPos pos = oreQueue.get(i);
            if (!withinVolume(pos)) {
                oreQueue.remove(i);
                changed = true;
                continue;
            }
            if (!isOreState(level.getBlockState(pos))) {
                companion.incrementMinerOresMined();
                oreQueue.remove(i);
                changed = true;
                debug("Pruned missing ore (counting as mined): %s", fmt(pos));
            }
        }
        if (changed) {
            clampOreIndex();
            persistPlanProgress();
            info("After prune: counted=%d mined=%d remaining=%d", companion.getMinerOresCounted(), companion.getMinerOresMined(), oreQueue.size());
        }
    }

    private void clampOreIndex() {
        if (oreQueue.isEmpty()) {
            oreIndex = 0;
        } else {
            oreIndex = Math.max(0, Math.min(oreIndex, oreQueue.size() - 1));
        }
        companion.setMinerOreIndex(oreIndex);
    }

    private boolean workAreaChanged() {
        return plannedRadius != horizontalRadius()
                || plannedUp != verticalRadiusUp()
                || plannedDown != verticalRadiusDown()
                || !plannedCenter.equals(workCenter());
    }

    private void persistPlanProgress() {
        companion.overwriteMinerOreMemory(oreQueue);
        companion.setMinerOreIndex(Math.max(0, Math.min(oreIndex, oreQueue.size() - 1)));
        plannedCenter = workCenter();
        plannedRadius = horizontalRadius();
        plannedUp = verticalRadiusUp();
        plannedDown = verticalRadiusDown();
        companion.setMinerPlanCenter(plannedCenter);
        companion.setMinerPlanRadius(plannedRadius);
        companion.setMinerPlanUp(plannedUp);
        companion.setMinerPlanDown(plannedDown);
    }

    private void notifyNoWork() {
        if (announcedNoWork) return;
        announcedNoWork = true;
        if (!(companion.getOwner() instanceof net.minecraft.world.entity.player.Player player)) return;
        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("chat.type.text",
                companion.getDisplayName(),
                net.minecraft.network.chat.Component.translatable("chat.modern_companions.miner.no_ore")));
        info("No ores left within patrol cube; notifying owner %s", player.getScoreboardName());
    }

    // Invoked when pathing would otherwise stall: keep breaking toward goal even if navigation cannot find a route yet.
    private void ensureDiggingProgress() {
        if (digQueue.isEmpty()) return;
        BlockPos current = digQueue.peekFirst();
        double dist = companion.distanceToSqr(Vec3.atCenterOf(current));
        if (dist < 9.0D) return; // already in range to swing
        // If navigation failed to produce a path, fall back to direct move to let block breaking open a corridor.
        if (companion.getNavigation().isDone()) {
            companion.getNavigation().moveTo(current.getX() + 0.5D, current.getY(), current.getZ() + 0.5D, 1.05D);
            debug("Force-moving toward dig target to unblock tunnel: %s", fmt(current));
            // If we're too far and navigation is stuck, break the head block immediately to open space.
            forceBreakImmediate();
        }
    }

    /** If the head-of-queue block is unmineable or we've been stuck, discard it so we don't hang forever. */
    private void dropCurrentIfStillStuck() {
        BlockPos current = digQueue.peekFirst();
        if (current == null) return;
        BlockState state = companion.level().getBlockState(current);
        if (!isMineableBlock(state)) {
            digQueue.pollFirst();
            info("Dropping unmineable/stuck block %s (state=%s) from queue", fmt(current), state.getBlock().getName().getString());
            return;
        }
        // If we've already waited multiple stall cycles, mine-and-pop the head to advance.
        companion.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
        lastActionTick = companion.tickCount;
        mine(current);
        digQueue.pollFirst();
        info("Dropping stalled block %s to advance queue (remaining dig=%d)", fmt(current), digQueue.size());
    }

    /** As a last resort, forcibly break the current block to keep progress moving. */
    private void forceBreakImmediate() {
        if (digQueue.isEmpty()) return;
        BlockPos current = digQueue.peekFirst();
        if (current == null) return;
        BlockState state = companion.level().getBlockState(current);
        if (!isMineableBlock(state)) {
            digQueue.pollFirst();
            info("Force-break skipped unmineable %s (removed)", fmt(current));
            forceBreakTicker = 0;
            return;
        }
        info("Force-breaking stalled block %s (%s) to resume", fmt(current), state.getBlock().getName().getString());
        companion.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
        lastActionTick = companion.tickCount;
        mine(current);
        digQueue.pollFirst();
        forceBreakTicker = 0;
    }

    private void trackStallTarget(BlockPos current) {
        if (current == null) {
            lastStallTarget = null;
            forceBreakTicker = 0;
            return;
        }
        if (current.equals(lastStallTarget)) {
            // accumulate stall time per head block
            forceBreakTicker++;
        } else {
            lastStallTarget = current;
            forceBreakTicker = 0;
        }
    }

    private void dumpDigQueue() {
        int i = 0;
        StringBuilder sb = new StringBuilder();
        for (BlockPos p : digQueue) {
            if (i++ >= 8) break; // limit noise
            sb.append("[").append(fmt(p)).append(":").append(companion.level().getBlockState(p).getBlock().getName().getString()).append("] ");
        }
        info("Queue peek (len=%d): %s", digQueue.size(), sb.toString());
    }

    private void debug(String msg, Object... args) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(tagged(String.format(msg, args)));
        }
    }

    private void info(String msg, Object... args) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(tagged(String.format(msg, args)));
        }
    }

    private String tagged(String msg) {
        return "[Miner " + companion.getId() + "] " + msg;
    }

    private String fmt(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    /**
     * Attempt to build a plan to the next reachable ore; if none found, return false.
     * Falls back to the nearest accessible filler start if no ore exists.
     */
    private boolean tryPlanNextOre() {
        bootstrapPlan();
        pruneInvalidOres();
        if (workAreaChanged()) {
            surveyAndPersist(true);
        } else {
            mergeNewlyFoundOres();
        }
        if (oreQueue.isEmpty()) {
            notifyNoWork();
            companion.getNavigation().stop();
            searchCooldown = 0;
            return false;
        }

        clampOreIndex();
        for (int i = oreIndex; i < oreQueue.size(); i++) {
            BlockPos ore = oreQueue.get(i);
            if (unreachableOres.contains(ore)) continue;
            debug("Planning path to ore[%d/%d] at %s", i, oreQueue.size(), fmt(ore));
            if (planPathToOre(ore)) {
                targetOre = ore;
                oreIndex = i;
                companion.setMinerOreIndex(i);
                persistPlanProgress();
                progressStallTicks = 0;
                globalRescanTicker = 0;
                announcedNoWork = false;
                return true;
            }
            debug("Ore unreachable; marking and continuing: %s", fmt(ore));
            unreachableOres.add(ore);
        }

        for (int i = 0; i < oreIndex && i < oreQueue.size(); i++) {
            BlockPos ore = oreQueue.get(i);
            if (unreachableOres.contains(ore)) continue;
            debug("Planning wrap-around path to ore[%d/%d] at %s", i, oreQueue.size(), fmt(ore));
            if (planPathToOre(ore)) {
                targetOre = ore;
                oreIndex = i;
                companion.setMinerOreIndex(i);
                persistPlanProgress();
                progressStallTicks = 0;
                globalRescanTicker = 0;
                announcedNoWork = false;
                return true;
            }
            debug("Ore unreachable; marking and continuing: %s", fmt(ore));
            unreachableOres.add(ore);
        }

        // No reachable ore; try a filler start to keep digging.
        targetOre = findNearestMineable(false);
        if (targetOre != null) {
            digQueue.clear();
            if (planPathToOre(targetOre)) {
                persistPlanProgress();
                announcedNoWork = false;
                debug("Fallback tunneling toward filler at %s", fmt(targetOre));
                return true;
            }
        }
        notifyNoWork();
        companion.getNavigation().stop();
        searchCooldown = 0; // allow quick retry next tick
        return false;
    }

    private boolean planPathToOre(BlockPos ore) {
        digQueue.clear();
        BlockPos cursor = companion.blockPosition();
        Level level = companion.level();
        int steps = 0;
        debug("Planning path to %s from %s", fmt(ore), fmt(cursor));

        // First, descend (or stay level) toward ore with staircase pattern.
        while (cursor.getY() > ore.getY() && steps++ < MAX_PLAN_STEPS) {
            int dx = Integer.compare(ore.getX(), cursor.getX());
            int dz = Integer.compare(ore.getZ(), cursor.getZ());
            // Ensure there is a horizontal component so we never dig straight down.
            if (dx == 0 && dz == 0) {
                // Nudge along X first to create a stair landing.
                dx = (cursor.getX() + 1 <= workCenter().getX() + horizontalRadius()) ? 1 : -1;
            }
            BlockPos next = cursor.offset(dx, -1, dz);
            if (!withinVolume(next)) {
                debug("Path abort: next stair outside volume %s", fmt(next));
                return false;
            }
            if (isHazard(level.getBlockState(next))) {
                info("Path abort: hazard at %s", fmt(next));
                return false;
            }
            if (Math.abs(next.getY() - cursor.getY()) != 1) {
                debug("Path abort: invalid step delta from %s to %s", fmt(cursor), fmt(next));
                return false;
            }

            enqueueStep(cursor, next, level);
            cursor = next;
        }

        // Horizontal / upward approach.
        while (!cursor.equals(ore) && steps++ < MAX_PLAN_STEPS) {
            int dx = Integer.compare(ore.getX(), cursor.getX());
            int dz = Integer.compare(ore.getZ(), cursor.getZ());
            int dy = Integer.compare(ore.getY(), cursor.getY());

            BlockPos next;
            if (dy > 0) { // need to go up
                next = cursor.offset(dx, 1, dz);
            } else {
                // prefer horizontal step first
                if (Math.abs(ore.getX() - cursor.getX()) >= Math.abs(ore.getZ() - cursor.getZ())) {
                    next = cursor.offset(dx, 0, 0);
                } else {
                    next = cursor.offset(0, 0, dz);
                }
            }

            if (!withinVolume(next)) {
                debug("Path abort: next step outside volume %s", fmt(next));
                return false;
            }
            if (isHazard(level.getBlockState(next))) {
                info("Path abort: hazard at %s", fmt(next));
                return false;
            }
            if (Math.abs(next.getY() - cursor.getY()) > 1) {
                debug("Path abort: too-steep step from %s to %s", fmt(cursor), fmt(next));
                return false;
            }

            enqueueStep(cursor, next, level);
            cursor = next;
        }

        // Ensure we mine the ore itself if not already queued.
        if (!digQueue.contains(ore) && withinVolume(ore)) {
            digQueue.addLast(ore);
        }
        debug("Planned %d dig steps toward %s", digQueue.size(), fmt(ore));
        return !digQueue.isEmpty();
    }

    /**
     * Adds the floor/headroom blocks for a step into the dig queue if they are solid and
     * mineable (ore or filler). Leaves air untouched.
     */
    private void enqueueStep(BlockPos from, BlockPos to, Level level) {
        // Floor (to)
        addIfMineable(level, to);
        // Headroom above the destination (2-block tall)
        addIfMineable(level, to.above());
        addIfMineable(level, to.above(2));
    }

    private void addIfMineable(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return;
        if (isMineableBlock(state)) {
            digQueue.addLast(pos.immutable());
        }
    }

    /* -------------------- Block classification -------------------- */

    private boolean isOre(BlockPos pos) {
        return isOreState(companion.level().getBlockState(pos));
    }

    private boolean isOreState(BlockState state) {
        if (state.is(Tags.Blocks.ORES)) return true;
        for (TagKey<Block> tag : ORE_TAGS) {
            if (state.is(tag)) return true;
        }
        return false;
    }

    private boolean isMineableBlock(BlockState state) {
        Block block = state.getBlock();
        if (!allowBlocks.isEmpty() && !allowBlocks.contains(block)) return false;
        if (denyBlocks.contains(block)) return false;

        // Ores are always valid targets.
        if (state.is(Tags.Blocks.ORES)) return true;
        for (TagKey<Block> tag : ORE_TAGS) {
            if (state.is(tag)) return true;
        }

        // Filler materials we can tunnel through.
        if (state.is(BlockTags.STONE_ORE_REPLACEABLES) || state.is(BlockTags.BASE_STONE_OVERWORLD)) return true;
        if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.ROOTED_DIRT) || state.is(Blocks.PODZOL) || state.is(Blocks.GRAVEL)
                || state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.MYCELIUM)) return true;
        return false;
    }

    private boolean isFiller(BlockPos pos) {
        BlockState state = companion.level().getBlockState(pos);
        return state.is(BlockTags.STONE_ORE_REPLACEABLES) || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    private boolean isAccessibleStart(Level level, BlockPos pos) {
        // Require some air neighbor so we aren't picking a buried block we cannot path to.
        for (BlockPos air : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (level.getBlockState(air).isAir()) {
                return true;
            }
        }
        return false;
    }

    private boolean isHazard(BlockState state) {
        // Allow water; only treat lava/fire/magma as hazardous.
        if (state.is(Blocks.LAVA) || state.is(Blocks.FIRE) || state.is(Blocks.MAGMA_BLOCK)) return true;
        if (state.getFluidState().isSource() && state.getFluidState().is(net.minecraft.tags.FluidTags.LAVA)) return true;
        return false;
    }

    /* -------------------- Movement & mining -------------------- */

    private void moveToCurrentDigPos() {
        BlockPos current = digQueue.peekFirst();
        if (current == null) return;
        BlockPos stand = findAdjacentAir(current).orElse(current);
        debug("Navigating toward dig target %s (stand at %s)", fmt(current), fmt(stand));
        companion.getNavigation().moveTo(stand.getX() + 0.5D, stand.getY(), stand.getZ() + 0.5D, 1.05D);
    }

    private Optional<BlockPos> findAdjacentAir(BlockPos target) {
        Level level = companion.level();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(target.offset(-1, -1, -1), target.offset(1, 1, 1))) {
            if (!level.getBlockState(pos).isAir()) continue;
            double d = pos.distSqr(companion.blockPosition());
            if (d < bestDist) {
                bestDist = d;
                best = pos.immutable();
            }
        }
        return Optional.ofNullable(best);
    }

    private void mine(BlockPos pos) {
        if (!(companion.level() instanceof ServerLevel server)) return;
        if (!server.hasChunkAt(pos)) return;
        BlockState state = server.getBlockState(pos);
        if (!isMineableBlock(state)) return;
        boolean wasOre = isOreState(state);
        var blockEntity = server.getBlockEntity(pos);
        var drops = Block.getDrops(state, server, pos, blockEntity, companion, companion.getMainHandItem());
        server.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        for (ItemStack drop : drops) {
            ItemStack leftover = companion.getInventory().addItem(drop.copy());
            if (!leftover.isEmpty()) {
                companion.spawnAtLocation(leftover);
            }
        }
        if (wasOre) {
            companion.incrementMinerOresMined();
            removeOreFromPlan(pos);
            info("Mined ore at %s (mined=%d / counted=%d / remaining=%d)",
                    fmt(pos), companion.getMinerOresMined(), companion.getMinerOresCounted(), oreQueue.size());
        }
    }

    private void removeOreFromPlan(BlockPos pos) {
        boolean changed = oreQueue.remove(pos);
        changed = companion.getMinerOreMemory().remove(pos) || changed;
        if (changed) {
            clampOreIndex();
            persistPlanProgress();
        }
    }

    /* -------------------- Job state -------------------- */

    private boolean isActiveJob() {
        if (!enabled) return false;
        if (companion.getJob() != CompanionJob.MINER) return false;
        if (!companion.isPatrolling()) return false;
        if (companion.isOrderedToSit() || !companion.isTame()) return false;
        if (!hasPickaxe()) return false;
        return true;
    }

    private void loadConfigBlockLists() {
        List<? extends String> allowIds = ModConfig.safeGet(ModConfig.JOB_MINER_ALLOW_BLOCKS);
        List<? extends String> denyIds = ModConfig.safeGet(ModConfig.JOB_MINER_DENY_BLOCKS);
        resolveBlocksInto(allowIds, allowBlocks);
        resolveBlocksInto(denyIds, denyBlocks);
    }

    private void resolveBlocksInto(List<? extends String> ids, Set<Block> targetSet) {
        if (ids == null) return;
        for (String raw : ids) {
            try {
                ResourceLocation id = ResourceLocation.parse(raw);
                Block block = BuiltInRegistries.BLOCK.get(id);
                if (block != null && !block.defaultBlockState().isAir()) {
                    targetSet.add(block);
                }
            } catch (Exception ignored) {
                // Keep running on malformed ids.
            }
        }
    }

    private boolean hasPickaxe() {
        return hasTool(stack -> stack.getItem() instanceof PickaxeItem);
    }

    private boolean hasTool(java.util.function.Predicate<ItemStack> matcher) {
        if (matcher.test(companion.getMainHandItem())) return true;
        for (int i = 0; i < companion.getInventory().getContainerSize(); i++) {
            if (matcher.test(companion.getInventory().getItem(i))) {
                return true;
            }
        }
        return false;
    }

    /* -------------------- Volume helpers -------------------- */

    private BlockPos workCenter() {
        return companion.isPatrolling() && companion.getPatrolPos().isPresent()
                ? companion.getPatrolPos().get()
                : companion.blockPosition();
    }

    private int horizontalRadius() {
        return Math.min(128, Math.max(baseRadius, companion.getPatrolRadius()));
    }

    private int verticalRadiusUp() {
        return Math.min(32, Math.max(4, baseRadius));
    }

    private int verticalRadiusDown() {
        return Math.min(48, Math.max(8, baseRadius));
    }

    private boolean withinVolume(BlockPos pos) {
        BlockPos c = workCenter();
        int hr = horizontalRadius();
        int up = verticalRadiusUp();
        int down = verticalRadiusDown();
        return pos.getX() >= c.getX() - hr && pos.getX() <= c.getX() + hr
                && pos.getZ() >= c.getZ() - hr && pos.getZ() <= c.getZ() + hr
                && pos.getY() >= c.getY() - down && pos.getY() <= c.getY() + up;
    }

    /* -------------------- Break timing -------------------- */

    private int computeBreakTicks(BlockPos pos) {
        Level level = companion.level();
        BlockState state = level.getBlockState(pos);
        ItemStack tool = companion.getMainHandItem();
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0) return 40;
        float speed = tool.getDestroySpeed(state);
        if (!tool.isCorrectToolForDrops(state)) {
            speed = Math.max(1.0F, speed / 3.0F);
        }
        float relative = speed > 0 ? (speed / hardness) : 0.05F;
        int ticks = (int) Math.ceil(20.0F / Math.max(0.05F, relative));
        ticks = Math.max(20, Math.min(120, ticks * 2));
        return ticks;
    }
}
