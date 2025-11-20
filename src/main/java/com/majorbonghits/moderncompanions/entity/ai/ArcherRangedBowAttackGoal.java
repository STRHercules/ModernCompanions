package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;

import java.util.EnumSet;

/**
 * Bow attack goal mirroring the original Companion archer behavior.
 */
public class ArcherRangedBowAttackGoal<T extends AbstractHumanCompanionEntity & RangedAttackMob> extends Goal {
    private final T mob;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private int attackIntervalMin;
    private int seeTime;
    private int attackTime = -1;
    private boolean strafingClockwise;
    private int strafingTime = -1;

    public ArcherRangedBowAttackGoal(T mob, double speed, int interval, float radius) {
        this.mob = mob;
        this.speedModifier = speed;
        this.attackIntervalMin = interval;
        this.attackRadiusSqr = radius * radius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return (this.canUse() || !this.mob.getNavigation().isDone());
    }

    @Override
    public void start() {
        super.start();
        this.mob.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = -1;
        this.mob.stopUsingItem();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return;
        }

        double d0 = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean hasLineOfSight = this.mob.getSensing().hasLineOfSight(target);
        boolean hadLineOfSight = this.seeTime > 0;
        if (hasLineOfSight != hadLineOfSight) {
            this.seeTime = 0;
        }

        if (hasLineOfSight) {
            ++this.seeTime;
        } else {
            --this.seeTime;
        }

        if (d0 > (double) this.attackRadiusSqr || this.seeTime < 5) {
            this.mob.getNavigation().moveTo(target, this.speedModifier);
        } else {
            this.mob.getNavigation().stop();
            ++this.strafingTime;
        }

        if (this.strafingTime >= 20) {
            if ((double) this.mob.getRandom().nextFloat() < 0.3D) {
                this.strafingClockwise = !this.strafingClockwise;
            }
            this.strafingTime = 0;
        }

        float distance = Mth.sqrt((float) d0);
        float f = distance / Mth.sqrt(this.attackRadiusSqr);
        float clamped = Mth.clamp(f, 0.1F, 1.0F);

        if (this.seeTime >= 5) {
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (--this.attackTime == 0) {
            if (!hasLineOfSight) {
                return;
            }
            this.mob.performRangedAttack(target, clamped);
            this.attackTime = Mth.floor(clamped * (float) this.attackIntervalMin + 20.0F * clamped);
        } else if (this.attackTime < 0) {
            this.attackTime = this.attackIntervalMin;
        }
    }
}
