package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.entity.CompanionData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

/**
 * Hunts passive mobs when hunting flag is enabled.
 */
public class HuntGoal extends NearestAttackableTargetGoal<LivingEntity> {
    private final AbstractHumanCompanionEntity companion;

    public HuntGoal(AbstractHumanCompanionEntity companion) {
        super(companion, LivingEntity.class, true, HuntGoal::isHuntTarget);
        this.companion = companion;
    }

    private static boolean isHuntTarget(LivingEntity entity) {
        for (Class<?> c : CompanionData.huntMobs) {
            if (c.isInstance(entity)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canUse() {
        return companion.isHunting() && super.canUse();
    }
}
