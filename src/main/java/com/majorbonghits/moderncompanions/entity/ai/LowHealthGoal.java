package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * When low health, companions will attempt to eat food from their inventory.
 */
public class LowHealthGoal extends Goal {
    private final AbstractHumanCompanionEntity companion;

    public LowHealthGoal(AbstractHumanCompanionEntity companion) {
        this.companion = companion;
    }

    @Override
    public boolean canUse() {
        return ModConfig.safeGet(ModConfig.LOW_HEALTH_FOOD)
                && companion.getHealth() < companion.getMaxHealth() * 0.5f
                && companion.hasFoodInInventory();
    }

    @Override
    public void start() {
        companion.eatOneFood();
    }
}
