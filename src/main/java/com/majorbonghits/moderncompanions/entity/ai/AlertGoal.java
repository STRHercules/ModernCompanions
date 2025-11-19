package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.entity.CompanionData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

/**
 * Targets hostile mobs listed in CompanionData when alert flag is active.
 */
public class AlertGoal extends NearestAttackableTargetGoal<LivingEntity> {
    private final AbstractHumanCompanionEntity companion;

    public AlertGoal(AbstractHumanCompanionEntity companion) {
        super(companion, LivingEntity.class, true, AlertGoal::isAlertTarget);
        this.companion = companion;
    }

    private static boolean isAlertTarget(LivingEntity entity) {
        for (Class<?> c : CompanionData.alertMobs) {
            if (c.isInstance(entity)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canUse() {
        return companion.isAlert() && super.canUse();
    }
}
