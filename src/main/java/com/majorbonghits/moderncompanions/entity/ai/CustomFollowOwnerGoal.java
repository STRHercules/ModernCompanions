package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Lightweight follow-owner goal that respects the companion's follow flag.
 */
public class CustomFollowOwnerGoal extends Goal {
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
        if (livingentity == null || livingentity.isSpectator()) {
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

        companion.getLookControl().setLookAt(owner, 10.0F, companion.getMaxHeadXRot());

        if (--timeToRecalc <= 0) {
            timeToRecalc = 10;
            double distanceSq = companion.distanceToSqr(owner);
            if (distanceSq >= 144.0D && teleport) {
                companion.teleportTo(owner.getX(), owner.getY(), owner.getZ());
                companion.getNavigation().stop();
            } else {
                companion.getNavigation().moveTo(owner, speedModifier);
            }
        }
    }
}
