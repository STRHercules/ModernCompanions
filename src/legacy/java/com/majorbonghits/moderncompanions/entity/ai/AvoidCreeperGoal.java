package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Creeper;

/**
 * Mirrors the original behavior: companions warn and avoid creepers when enabled.
 */
public class AvoidCreeperGoal extends AvoidEntityGoal<Creeper> {
    private final AbstractHumanCompanionEntity companion;

    public AvoidCreeperGoal(AbstractHumanCompanionEntity mob, double walkSpeedModifier, double sprintSpeedModifier) {
        super(mob, Creeper.class, 6.0F, walkSpeedModifier, sprintSpeedModifier);
        this.companion = mob;
    }

    @Override
    public boolean canUse() {
        if (!ModConfig.safeGet(ModConfig.CREEPER_WARNING)) {
            return false;
        }
        return super.canUse();
    }

    @Override
    public void start() {
        super.start();
        companion.setAlert(true);
    }

    @Override
    public void stop() {
        super.stop();
        companion.setAlert(false);
    }
}
