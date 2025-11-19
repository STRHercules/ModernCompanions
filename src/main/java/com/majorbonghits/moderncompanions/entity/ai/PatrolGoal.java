package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Patrolling random stroll constrained to a patrol radius around the patrol position.
 */
public class PatrolGoal extends RandomStrollGoal {
    protected final float probability;
    public Vec3 patrolVec;
    public AbstractHumanCompanionEntity companion;
    public int radius;

    public PatrolGoal(AbstractHumanCompanionEntity mob, int interval, int radius) {
        this(mob, 1.0D, 0.001F, interval, radius);
    }

    public PatrolGoal(AbstractHumanCompanionEntity mob, double speed, float probability, int interval, int radius) {
        super(mob, speed);
        this.probability = probability;
        this.companion = mob;
        this.interval = interval;
        this.radius = radius;
    }

    @Override
    public boolean canUse() {
        if (companion.getPatrolPos().isEmpty() || !companion.isPatrolling()) {
            return false;
        }
        this.patrolVec = Vec3.atBottomCenterOf(companion.getPatrolPos().orElse(companion.blockPosition()));
        return super.canUse();
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        Vec3 vec = getRawPosition();
        if (vec != null) {
            double distance = vec.distanceTo(patrolVec);
            if (distance > radius) {
                vec = null;
            }
        }
        return vec;
    }

    public Vec3 getRawPosition() {
        if (this.mob.isInWaterOrBubble()) {
            Vec3 vec3 = LandRandomPos.getPos(this.mob, radius, 7);
            return vec3 == null ? super.getPosition() : vec3;
        } else {
            return this.mob.getRandom().nextFloat() >= this.probability ? LandRandomPos.getPos(this.mob, radius, 7) :
                    super.getPosition();
        }
    }
}
