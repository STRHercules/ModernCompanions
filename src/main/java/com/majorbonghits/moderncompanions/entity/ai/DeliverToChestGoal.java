package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.entity.job.CompanionJob;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Optional;

/**
 * Courier loop: when a working companion has cargo and an assigned chest,
 * walk over and dump everything except equipped gear. Pauses politely if the
 * chunk is missing or the chest vanished.
 */
public class DeliverToChestGoal extends Goal {
    private static final double STANDOFF_RANGE_SQR = 4.0D; // stop within 2 blocks of chest
    private static final int STUCK_ALERT_TICKS = 200;

    private final AbstractHumanCompanionEntity companion;
    private final double speed;
    private BlockPos targetChest;
    private int stuckTicks;

    public DeliverToChestGoal(AbstractHumanCompanionEntity companion, double speed) {
        this.companion = companion;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!(companion.level() instanceof ServerLevel server)) return false;
        if (!companion.isTame() || companion.isOrderedToSit()) return false;
        if (companion.getJob() == CompanionJob.NONE) return false;
        boolean forced = companion.isForceDeliverRequested();
        if (!companion.isPatrolling() && !forced) return false; // job behaviors engage while patrolling unless forced
        if (companion.getTarget() != null) return false; // do not courier mid-fight
        boolean hasCargo = companion.hasDeliverableCargo();
        if (!hasCargo && !forced) return false;

        long gameTime = server.getGameTime();
        boolean inventoryFull = companion.isInventoryFull();
        boolean dayElapsed = gameTime - companion.getLastDeliveryGameTime() >= 24000L;

        if (!forced && !(inventoryFull || dayElapsed)) return false;

        Optional<BlockPos> chestPos = companion.getAssignedChest();
        Optional<net.minecraft.resources.ResourceKey<Level>> dim = companion.getAssignedChestDimension();
        if (chestPos.isEmpty() || dim.isEmpty()) return false;
        if (!server.dimension().equals(dim.get())) return false;

        targetChest = chestPos.get();
        companion.refreshDeliveryChunkTicket(server);
        if (!server.isLoaded(targetChest)) {
            companion.alertChestUnloaded();
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return targetChest != null && companion.hasDeliverableCargo();
    }

    @Override
    public void start() {
        moveTowardChest();
    }

    @Override
    public void stop() {
        targetChest = null;
        companion.getNavigation().stop();
        stuckTicks = 0;
    }

    @Override
    public void tick() {
        if (!(companion.level() instanceof ServerLevel server)) {
            stop();
            return;
        }
        if (targetChest == null) {
            stop();
            return;
        }
        if (!server.isLoaded(targetChest)) {
            companion.alertChestUnloaded();
            stop();
            return;
        }

        double dist = companion.distanceToSqr(Vec3.atCenterOf(targetChest));
        if (dist > STANDOFF_RANGE_SQR) {
            if (companion.getNavigation().isDone()) {
                moveTowardChest();
            }
            stuckTicks++;
            if (stuckTicks % 40 == 0) {
                tryNudgePath(server);
            }
            if (stuckTicks >= STUCK_ALERT_TICKS) {
                companion.notifyCourierOwnerText(net.minecraft.network.chat.Component.translatable(
                        "message.modern_companions.courier.stuck",
                        targetChest.getX(), targetChest.getY(), targetChest.getZ()));
                stuckTicks = 0;
            }
            return;
        }

        AbstractHumanCompanionEntity.DeliveryResult result = companion.deliverInventoryToChest(server, targetChest);
        switch (result) {
            case SUCCESS -> {}
            case FULL -> companion.notifyCourierOwnerText(net.minecraft.network.chat.Component.translatable("message.modern_companions.courier.full"));
            case MISSING -> companion.notifyCourierOwnerText(net.minecraft.network.chat.Component.translatable("message.modern_companions.courier.missing"));
        }
        stop();
    }

    private void tryNudgePath(ServerLevel server) {
        if (targetChest == null) return;
        // Break soft blockers along line of sight
        Vec3 start = companion.position();
        Vec3 end = Vec3.atCenterOf(targetChest);
        int steps = (int) Math.max(4, start.distanceTo(end));
        for (int i = 1; i <= steps; i++) {
            double t = i / (double) steps;
            BlockPos pos = BlockPos.containing(start.x + (end.x - start.x) * t,
                    start.y + (end.y - start.y) * t,
                    start.z + (end.z - start.z) * t);
            if (!server.isLoaded(pos)) continue;
            var state = server.getBlockState(pos);
            if (state.isAir()) continue;
            if (state.is(net.minecraft.tags.BlockTags.LEAVES) || state.is(net.minecraft.tags.BlockTags.DIRT)
                    || state.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD) || state.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)) {
                server.destroyBlock(pos, true, companion);
                break;
            }
        }
        companion.getNavigation().moveTo(targetChest.getX() + 0.5D, targetChest.getY() + 1.0D, targetChest.getZ() + 0.5D, speed);
    }

    private void moveTowardChest() {
        if (targetChest == null) return;
        companion.getNavigation().moveTo(targetChest.getX() + 0.5D, targetChest.getY() + 1.0D, targetChest.getZ() + 0.5D, speed);
    }
}
