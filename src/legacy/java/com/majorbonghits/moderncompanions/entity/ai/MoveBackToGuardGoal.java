package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * Holds guarding companions at their assigned position.
 */
public class MoveBackToGuardGoal extends Goal {
    public AbstractHumanCompanionEntity companion;
    public Vec3 guardVec;

    public MoveBackToGuardGoal(AbstractHumanCompanionEntity companion) {
        this.companion = companion;
    }

    @Override
    public boolean canUse() {
        if (this.companion.getPatrolPos().isEmpty() || !companion.isGuarding()) {
            return false;
        }
        this.guardVec = Vec3.atBottomCenterOf(this.companion.getPatrolPos().orElse(companion.blockPosition()));
        Vec3 currentVec = Vec3.atBottomCenterOf(this.companion.blockPosition());
        return guardVec.distanceTo(currentVec) > 1.0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void tick() {
        if (companion.getTarget() == null) {
            companion.getNavigation().moveTo(guardVec.x, guardVec.y, guardVec.z, 1.0);
        }
    }
}
