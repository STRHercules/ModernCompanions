package com.majorbonghits.moderncompanions.entity.job;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Lightweight lumberjack loop: scan for a natural log with adjacent leaves,
 * walk over, break logs with a tool-speed delay, and stash drops in the
 * companion inventory. The companion will clear connected logs, break leaves if
 * stuck, and replant a sapling if available.
 */
public class LumberjackJobGoal extends Goal {
    private static final Logger LOGGER = LoggerFactory.getLogger("ModernCompanions-Lumberjack");
    private static final int SEARCH_COOLDOWN_TICKS = 40;
    private static final int MAX_LOGS_PER_TREE = 96;
    private static final int MAX_LEAF_CLEAR_TICKS = 20;

    private final AbstractHumanCompanionEntity companion;
    private final int searchRadius;
    private final boolean enabled;
    private BlockPos targetLog;
    private BlockPos stumpPos;
    private final Queue<BlockPos> pendingLogs = new PriorityQueue<>(this::compareLogPriority);
    private int searchCooldown;
    private int breakTicksRemaining;
    private int stuckTicks;
    private int swingCooldown;
    private boolean replantedThisTree;
    private int stuckAfterDepositCooldown;
    private int idleNavTicks;
    private int debugCooldown;
    private int progressIdleTicks;
    private static final int STALL_KICK_TICKS = 120;
    private static final int MAX_STAND_SEARCH_RADIUS = 1;

    public LumberjackJobGoal(AbstractHumanCompanionEntity companion, int searchRadius, boolean enabled) {
        this.companion = companion;
        this.searchRadius = Math.max(4, searchRadius);
        this.enabled = enabled;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!isActiveJob()) {
            return false;
        }
        if (searchCooldown > 0) {
            searchCooldown--;
            return false;
        }
        if (!prepareTreeTargets()) {
            targetLog = null;
            stumpPos = null;
            replantedThisTree = false;
            logDebug("no_target", "state", "scan_empty");
            return false;
        }
        replantedThisTree = false;
        searchCooldown = SEARCH_COOLDOWN_TICKS;
        logDebug("start", "target", targetLog, "pending", pendingLogs.size());
        return targetLog != null;
    }

    @Override
    public boolean canContinueToUse() {
        return isActiveJob() && (targetLog != null || !pendingLogs.isEmpty());
    }

    @Override
    public void start() {
        moveToTarget();
    }

    @Override
    public void stop() {
        if (!replantedThisTree) {
            tryReplantSapling();
        }
        targetLog = null;
        stumpPos = null;
        pendingLogs.clear();
        companion.getNavigation().stop();
        breakTicksRemaining = 0;
        stuckTicks = 0;
        swingCooldown = 0;
        replantedThisTree = false;
        stuckAfterDepositCooldown = 0;
    }

    @Override
    public void tick() {
        if (!isActiveJob()) {
            return;
        }
        if (stuckAfterDepositCooldown > 0) {
            stuckAfterDepositCooldown--;
        }
        // If we're actively pathing or chopping, we're making progress.
        if (!companion.getNavigation().isDone() || breakTicksRemaining > 0) {
            progressIdleTicks = 0;
        } else {
            progressIdleTicks++;
        }

        // If navigation finished unexpectedly while we still have a target, re-issue the path after a short pause.
        if (targetLog != null && companion.getNavigation().isDone()) {
            idleNavTicks++;
            if (idleNavTicks >= 20) { // about 1 second
                logDebug("idle_repath", "target", targetLog, "pos", companion.blockPosition());
                moveToTarget();
                idleNavTicks = 0;
            }
        } else {
            idleNavTicks = 0;
        }
        if (targetLog == null || !isTreeLog(targetLog)) {
            targetLog = nextLogTarget();
        }
        if (targetLog == null) {
            if (!replantedThisTree) {
                tryReplantSapling();
                replantedThisTree = true;
            }
            return;
        }
        double horizDist = horizontalDistanceTo(targetLog);
        // horizontalDistanceTo returns squared distance; allow up to ~4 blocks away (distance <= 4 => distSq <= 16)
        if (horizDist > 16.0D) {
            moveToTarget();
            // Only consider clearing leaves when pathing failed for a while and leaves are likely blocking.
            var nav = companion.getNavigation();
            var path = nav.getPath();
            boolean noPath = path == null || nav.isDone();
            boolean nearStump = stumpPos != null && targetLog != null && targetLog.getY() <= stumpPos.getY() + 1;
            boolean closeEnough = horizDist <= 36.0D; // within ~6 blocks squared
            if (noPath && nearStump && closeEnough && hasNearbyLeaves(targetLog)) {
                stuckTicks++;
                if (stuckTicks >= MAX_LEAF_CLEAR_TICKS * 2) { // give ~2s before breaking leaves
                    logDebug("clear_leaves", "around", targetLog);
                    clearLeavesNear(targetLog);
                    stuckTicks = 0;
                }
            } else {
                stuckTicks = 0;
            }
            return;
        }
        if (breakTicksRemaining <= 0) {
            breakTicksRemaining = computeBreakTicks(targetLog);
            swingCooldown = 0;
            logDebug("begin_break", "pos", targetLog, "ticks", breakTicksRemaining);
        }
        if (swingCooldown-- <= 0) {
            companion.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
            swingCooldown = 6;
        }
        breakTicksRemaining--;
        if (breakTicksRemaining <= 0) {
            chopLog(targetLog);
            enqueueAdjacentLogs(targetLog);
            logDebug("chopped", "pos", targetLog, "queue", pendingLogs.size());
            targetLog = nextLogTarget();
            breakTicksRemaining = 0;
        }
        if (targetLog != null && progressIdleTicks >= STALL_KICK_TICKS) {
            recoverFromStall();
            progressIdleTicks = 0;
        }
    }

    private boolean isActiveJob() {
        if (!enabled) return false;
        if (companion.getJob() != CompanionJob.LUMBERJACK) return false;
        if (!companion.isPatrolling()) return false;
        if (companion.isOrderedToSit() || !companion.isTame()) return false;
        if (!hasAxe()) return false;
        return isWithinWorkArea(20.0D) || isWithinPatrolArea();
    }

    public void forceRescanAfterDeposit() {
        // Clear current plan and allow immediate rescan next tick.
        pendingLogs.clear();
        targetLog = null;
        stumpPos = null;
        searchCooldown = 0;
        replantedThisTree = false;
        stuckAfterDepositCooldown = 20;
        idleNavTicks = 0;
        logDebug("post_deposit_rescan", "pos", companion.blockPosition());
    }

    private boolean prepareTreeTargets() {
        pendingLogs.clear();
        stumpPos = null;
        targetLog = null;

        Level level = companion.level();
        BlockPos origin = companion.isPatrolling() && companion.getPatrolPos().isPresent()
                ? companion.getPatrolPos().get()
                : companion.blockPosition();
        int effectiveRadius = Math.min(128, Math.max(searchRadius, companion.getPatrolRadius()));
        BlockPos start = null;
        double best = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-effectiveRadius, -2, -effectiveRadius),
                origin.offset(effectiveRadius, 6, effectiveRadius))) {
            if (!isNaturalTreeLog(pos)) continue;
            double dist = pos.distSqr(origin);
            if (dist < best) {
                best = dist;
                start = pos.immutable();
            }
        }
        if (start == null) return false;

        Deque<BlockPos> frontier = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        frontier.add(start);
        visited.add(start);
        stumpPos = start;
        int count = 0;

        while (!frontier.isEmpty() && count < MAX_LOGS_PER_TREE) {
            BlockPos current = frontier.poll();
            pendingLogs.add(current);
            if (current.getY() < stumpPos.getY()) {
                stumpPos = current;
            }
            count++;
            for (BlockPos adj : BlockPos.betweenClosed(current.offset(-1, -1, -1), current.offset(1, 1, 1))) {
                if (visited.contains(adj)) continue;
                if (!isTreeLog(adj)) continue;
                visited.add(adj.immutable());
                frontier.add(adj.immutable());
            }
        }

        targetLog = nextLogTarget();
        return targetLog != null;
    }

    private boolean isTreeLog(BlockPos pos) {
        return companion.level().getBlockState(pos).is(BlockTags.LOGS);
    }

    private boolean isNaturalTreeLog(BlockPos pos) {
        if (!isTreeLog(pos)) return false;
        return hasNearbyLeaves(pos);
    }

    private void chopLog(BlockPos pos) {
        if (!(companion.level() instanceof ServerLevel server)) {
            return;
        }
        BlockState state = server.getBlockState(pos);
        if (!state.is(BlockTags.LOGS)) {
            return;
        }
        if (!server.hasChunkAt(pos)) {
            return;
        }
        var blockEntity = server.getBlockEntity(pos);
        var drops = Block.getDrops(state, server, pos, blockEntity, companion, companion.getMainHandItem());
        server.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        for (ItemStack drop : drops) {
            ItemStack leftover = companion.getInventory().addItem(drop.copy());
            if (!leftover.isEmpty()) {
                companion.spawnAtLocation(leftover);
            }
        }
        companion.incrementLumberLogsSession();
    }

    private double horizontalDistanceTo(BlockPos pos) {
        double dx = (pos.getX() + 0.5D) - companion.getX();
        double dz = (pos.getZ() + 0.5D) - companion.getZ();
        return dx * dx + dz * dz;
    }

    private void moveToTarget() {
        if (targetLog == null) {
            return;
        }
        BlockPos stand = findGroundStandPos();
        double tx = stand != null ? stand.getX() + 0.5D : targetLog.getX() + 0.5D;
        double tz = stand != null ? stand.getZ() + 0.5D : targetLog.getZ() + 0.5D;
        double baseY = stumpPos != null ? stumpPos.getY() + 0.05D : targetLog.getY();
        double ty = stand != null ? stand.getY() + 0.05D : Math.min(baseY, companion.getY());
        logDebug("nav", "target", targetLog, "stand", stand, "pos", companion.blockPosition(), "pending", pendingLogs.size());
        companion.getNavigation().moveTo(tx, ty, tz, 1.1D);
    }

    private BlockPos nextLogTarget() {
        BlockPos next;
        while ((next = pendingLogs.poll()) != null) {
            if (isTreeLog(next)) {
                return next;
            }
        }
        return null;
    }

    private void enqueueAdjacentLogs(BlockPos source) {
        for (BlockPos pos : BlockPos.betweenClosed(source.offset(-1, -1, -1), source.offset(1, 2, 1))) {
            if (pendingLogs.size() >= MAX_LOGS_PER_TREE) break;
            if (isTreeLog(pos) && !pendingLogs.contains(pos)) {
                pendingLogs.add(pos.immutable());
            }
        }
    }

    private boolean hasNearbyLeaves(BlockPos pos) {
        Level level = companion.level();
        for (BlockPos leafPos : BlockPos.betweenClosed(pos.offset(-2, 0, -2), pos.offset(2, 3, 2))) {
            if (level.getBlockState(leafPos).is(BlockTags.LEAVES)) {
        return true;
    }
        }
        return false;
    }

    private void clearLeavesNear(BlockPos target) {
        Level level = companion.level();
        for (BlockPos leafPos : BlockPos.betweenClosed(target.offset(-1, 0, -1), target.offset(1, 2, 1))) {
            BlockState state = level.getBlockState(leafPos);
            if (state.is(BlockTags.LEAVES)) {
                level.destroyBlock(leafPos, true, companion);
                return;
            }
        }
    }

    private void recoverFromStall() {
        logDebug("stall_recover", "target", targetLog, "queue", pendingLogs.size(), "pos", companion.blockPosition());
        moveToTarget();
        var nav = companion.getNavigation();
        if (nav.isDone() || nav.getPath() == null) {
            BlockPos skipped = targetLog;
            targetLog = nextLogTarget();
            breakTicksRemaining = 0;
            swingCooldown = 0;
            stuckTicks = 0;
            idleNavTicks = 0;
            if (targetLog == null) {
                searchCooldown = 0; // allow immediate rescan
            } else {
                moveToTarget();
            }
            logDebug("stall_skip", "skipped", skipped, "next", targetLog);
        }
    }

    private BlockPos findGroundStandPos() {
        BlockPos base = stumpPos != null ? stumpPos : targetLog;
        if (base == null) return null;
        Level level = companion.level();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-MAX_STAND_SEARCH_RADIUS, 0, -MAX_STAND_SEARCH_RADIUS),
                base.offset(MAX_STAND_SEARCH_RADIUS, 0, MAX_STAND_SEARCH_RADIUS))) {
            if (!level.getBlockState(pos).isAir()) continue;
            BlockState below = level.getBlockState(pos.below());
            if (!below.isFaceSturdy(level, pos.below(), Direction.UP)) continue;
            double d = pos.distSqr(companion.blockPosition());
            if (d < bestDist) {
                bestDist = d;
                best = pos.immutable();
            }
        }
        if (best == null && level.getBlockState(base).isAir()) {
            best = base.immutable();
        }
        return best;
    }

    private int computeBreakTicks(BlockPos pos) {
        Level level = companion.level();
        BlockState state = level.getBlockState(pos);
        ItemStack tool = companion.getMainHandItem();
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0) return 20;
        float speed = tool.getDestroySpeed(state);
        if (!tool.isCorrectToolForDrops(state)) {
            speed = Math.max(1.0F, speed / 3.0F);
        }
        float relative = speed > 0 ? (speed / hardness) : 0.05F;
        int ticks = (int) Math.ceil(20.0F / Math.max(0.05F, relative));
        // Slow down to feel like multiple swings per log.
        ticks *= 2;
        return Math.max(20, Math.min(120, ticks));
    }

    private int compareLogPriority(BlockPos a, BlockPos b) {
        if (a.getY() != b.getY()) {
            return Integer.compare(a.getY(), b.getY()); // lower logs first
        }
        if (stumpPos != null) {
            double da = a.distSqr(stumpPos);
            double db = b.distSqr(stumpPos);
            return Double.compare(da, db);
        }
        return 0;
    }

    private void tryReplantSapling() {
        if (stumpPos == null || !(companion.level() instanceof ServerLevel server)) return;
        BlockPos placePos = stumpPos;
        BlockPos ground = stumpPos.below();
        BlockState groundState = server.getBlockState(ground);
        BlockState airCheck = server.getBlockState(placePos);
        if (!airCheck.isAir()) return;

        if (!groundState.is(BlockTags.DIRT)
                && !groundState.is(Blocks.GRASS_BLOCK)
                && !groundState.is(Blocks.PODZOL)
                && !groundState.is(Blocks.MYCELIUM)
                && !groundState.is(Blocks.MOSS_BLOCK)) {
            return;
        }

        Predicate<ItemStack> saplingMatcher = stack -> stack.getItem() instanceof BlockItem bi
                && bi.getBlock().defaultBlockState().is(BlockTags.SAPLINGS);
        ItemStack sapling = ItemStack.EMPTY;
        if (saplingMatcher.test(companion.getMainHandItem())) {
            sapling = companion.getMainHandItem();
        } else {
            int slot = findInventorySlot(saplingMatcher);
            if (slot >= 0) {
                sapling = companion.getInventory().getItem(slot);
            }
        }
        if (sapling.isEmpty()) return;
        BlockItem bi = (BlockItem) sapling.getItem();
        BlockState saplingState = bi.getBlock().defaultBlockState();
        if (!saplingState.canSurvive(server, placePos)) return;
        if (server.setBlock(placePos, saplingState, 3)) {
            sapling.shrink(1);
            stumpPos = null;
        }
    }

    private int findInventorySlot(Predicate<ItemStack> matcher) {
        for (int i = 0; i < companion.getInventory().getContainerSize(); i++) {
            if (matcher.test(companion.getInventory().getItem(i))) {
                return i;
            }
        }
        return -1;
    }

    private boolean hasAxe() {
        return hasTool(stack -> stack.getItem() instanceof AxeItem);
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

    private boolean isWithinWorkArea(double ownerMax) {
        LivingEntity owner = companion.getOwner();
        if (owner != null && companion.distanceToSqr(owner) <= ownerMax * ownerMax) {
            return true;
        }
        return false;
    }

    private boolean isWithinPatrolArea() {
        return companion.isPatrolling() && companion.getPatrolPos().isPresent()
                && companion.getPatrolPos().get().distSqr(companion.blockPosition()) <= Math.pow(Math.max(8.0D, companion.getPatrolRadius() + 4), 2);
    }

    private void logDebug(String msg, Object... kv) {
        if (!LOGGER.isInfoEnabled()) return;
        StringBuilder sb = new StringBuilder("[Lumberjack ").append(companion.getId()).append("] ").append(msg);
        for (int i = 0; i + 1 < kv.length; i += 2) {
            sb.append(" | ").append(kv[i]).append('=').append(kv[i + 1]);
        }
        sb.append(" | pos=").append(companion.blockPosition());
        LOGGER.info(sb.toString());
    }
}
