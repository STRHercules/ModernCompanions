package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;

/**
 * Only wander when the companion is set to follow its owner.
 */
public class CustomWaterAvoidingRandomStrollGoal extends WaterAvoidingRandomStrollGoal {
    private final AbstractHumanCompanionEntity companion;

    public CustomWaterAvoidingRandomStrollGoal(AbstractHumanCompanionEntity mob, double speed) {
        super(mob, speed);
        this.companion = mob;
    }

    @Override
    public boolean canUse() {
        if (!companion.isFollowing()) {
            return false;
        }
        return super.canUse();
    }
}
