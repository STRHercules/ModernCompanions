package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.magic.AbstractMageCompanion;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Ranged casting goal tailored for mage-style companions.
 * Keeps distance, strafes when in range, and lets the caster decide
 * whether to perform a light or heavy spell.
 */
public class MageRangedAttackGoal<T extends AbstractMageCompanion> extends Goal {
    private final T caster;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private final float preferredMinRange = 12.0F;
    private final int baseLightInterval;
    private int attackTime = -1;
    private int seeTime;
    private int strafeTime = -1;
    private boolean strafeClockwise;

    public MageRangedAttackGoal(T caster, double speed, int lightInterval, float attackRadius) {
        this.caster = caster;
        this.speedModifier = speed;
        this.baseLightInterval = lightInterval;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.caster.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() || !this.caster.getNavigation().isDone();
    }

    @Override
    public void stop() {
        super.stop();
        this.caster.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = -1;
        this.caster.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.caster.getTarget();
        if (target == null) return;

        double distSqr = this.caster.distanceToSqr(target);
        boolean hasLineOfSight = this.caster.getSensing().hasLineOfSight(target);
        boolean seenLastTick = this.seeTime > 0;
        this.seeTime = hasLineOfSight ? this.seeTime + 1 : this.seeTime - 1;

        if (hasLineOfSight != seenLastTick) {
            this.seeTime = 0;
        }

        if (distSqr > (double) this.attackRadiusSqr || this.seeTime < 5) {
            this.caster.getNavigation().moveTo(target, this.speedModifier);
        } else {
            this.caster.getNavigation().stop();
            ++this.strafeTime;
        }

        // Back off when too close to keep casting from range
        if (distSqr < (double) (preferredMinRange * preferredMinRange)) {
            double dx = this.caster.getX() - target.getX();
            double dz = this.caster.getZ() - target.getZ();
            double len = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
            double awayX = this.caster.getX() + dx / len * 1.8D;
            double awayZ = this.caster.getZ() + dz / len * 1.8D;
            this.caster.getNavigation().moveTo(awayX, this.caster.getY(), awayZ, this.speedModifier);
        }

        if (this.strafeTime >= 20) {
            if ((double) this.caster.getRandom().nextFloat() < 0.3D) {
                this.strafeClockwise = !this.strafeClockwise;
            }
            this.strafeTime = 0;
        }

        float distance = Mth.sqrt((float) distSqr);
        float normalizedDistance = distance / Mth.sqrt(this.attackRadiusSqr);
        float clamped = Mth.clamp(normalizedDistance, 0.1F, 1.0F);

        // Force facing the target to avoid spinning while casting
        this.caster.getLookControl().setLookAt(target, 45.0F, 45.0F);
        this.caster.setYHeadRot(this.caster.getYRot());

        if (--this.attackTime <= 0) {
            if (!hasLineOfSight) {
                this.attackTime = this.baseLightInterval;
                return;
            }
            boolean heavy = this.caster.tryHeavyAttack(target, clamped);
            if (!heavy) {
                this.caster.performRangedAttack(target, clamped);
            }
            // Always pace by light interval; heavy is gated by internal cooldown
            int interval = this.caster.getLightIntervalTicks();
            this.attackTime = Math.max(10, interval);
        } else if (this.attackTime < 0) {
            this.attackTime = this.baseLightInterval;
        }
    }
}
