package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * Keeps patrolling companions within their radius.
 */
public class MoveBackToPatrolGoal extends Goal {
    public Vec3 patrolVec;
    public AbstractHumanCompanionEntity companion;
    public int radius;

    public MoveBackToPatrolGoal(AbstractHumanCompanionEntity companion, int radius) {
        this.companion = companion;
        this.radius = radius;
    }

    @Override
    public boolean canUse() {
        if (this.companion.getPatrolPos().isEmpty() || !companion.isPatrolling()) {
            return false;
        }
        this.patrolVec = Vec3.atBottomCenterOf(this.companion.getPatrolPos().orElse(companion.blockPosition()));
        Vec3 currentVec = Vec3.atBottomCenterOf(this.companion.blockPosition());
        return patrolVec.distanceTo(currentVec) > radius;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void tick() {
        if (companion.getTarget() == null) {
            companion.getNavigation().moveTo(patrolVec.x, patrolVec.y, patrolVec.z, 1.0);
        }
    }
}
