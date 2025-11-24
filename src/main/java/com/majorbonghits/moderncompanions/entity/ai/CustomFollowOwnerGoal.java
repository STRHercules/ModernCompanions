package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Lightweight follow-owner goal that respects the companion's follow flag.
 */
public class CustomFollowOwnerGoal extends Goal {
    private static final double TELEPORT_DISTANCE_SQ = 35.0D * 35.0D; // Companion snaps back once ~35 blocks away.
    private static final int TELEPORT_ATTEMPTS = 10;
    private static final int TELEPORT_RANGE = 3;

    private final AbstractHumanCompanionEntity companion;
    private final double speedModifier;
    private final float startDistance;
    private final float stopDistance;
    private final boolean teleport;
    private LivingEntity owner;
    private int timeToRecalc;

    public CustomFollowOwnerGoal(AbstractHumanCompanionEntity companion, double speed, float startDist, float stopDist, boolean teleport) {
        this.companion = companion;
        this.speedModifier = speed;
        this.startDistance = startDist;
        this.stopDistance = stopDist;
        this.teleport = teleport;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!companion.isFollowing() || companion.isOrderedToSit()) {
            return false;
        }
        LivingEntity livingentity = companion.getOwner();
        if (livingentity == null || livingentity.isSpectator() || livingentity.level() != companion.level()) {
            return false;
        }
        if (companion.distanceToSqr(livingentity) < (double) (this.startDistance * this.startDistance)) {
            return false;
        }
        this.owner = livingentity;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return owner != null
                && !companion.getNavigation().isDone()
                && companion.isFollowing()
                && !companion.isOrderedToSit()
                && owner.level() == companion.level()
                && companion.distanceToSqr(owner) > (double) (this.stopDistance * this.stopDistance);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.companion.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (owner == null) {
            return;
        }

        if (owner.level() != companion.level()) {
            return;
        }

        companion.getLookControl().setLookAt(owner, 10.0F, companion.getMaxHeadXRot());

        if (--timeToRecalc <= 0) {
            timeToRecalc = 10;
            double distanceSq = companion.distanceToSqr(owner);
            if (distanceSq >= TELEPORT_DISTANCE_SQ && teleport) {
                if (!tryTeleportCloseToOwner()) {
                    companion.getNavigation().moveTo(owner, speedModifier); // Fallback if no safe spot found.
                }
            } else {
                companion.getNavigation().moveTo(owner, speedModifier);
            }
        }
    }

    /**
     * Mimics vanilla pet recall: look for a nearby open spot around the owner before teleporting.
     */
    private boolean tryTeleportCloseToOwner() {
        BlockPos ownerPos = owner.blockPosition();
        for (int attempt = 0; attempt < TELEPORT_ATTEMPTS; attempt++) {
            int dx = randomBetween(-TELEPORT_RANGE, TELEPORT_RANGE);
            int dz = randomBetween(-TELEPORT_RANGE, TELEPORT_RANGE);
            BlockPos targetPos = ownerPos.offset(dx, 0, dz);
            if (isTeleportFriendly(targetPos)) {
                companion.teleportTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
                companion.getNavigation().stop();
                return true;
            }
        }
        return false;
    }

    private boolean isTeleportFriendly(BlockPos pos) {
        return companion.level().isEmptyBlock(pos)
                && companion.level().isEmptyBlock(pos.above())
                && companion.level().noCollision(companion, companion.getBoundingBox().move(
                pos.getX() - companion.getX(),
                pos.getY() - companion.getY(),
                pos.getZ() - companion.getZ()));
    }

    private int randomBetween(int min, int max) {
        return companion.getRandom().nextInt(max - min + 1) + min;
    }
}
