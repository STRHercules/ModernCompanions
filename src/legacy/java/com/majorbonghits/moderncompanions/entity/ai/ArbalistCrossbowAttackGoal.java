package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;

/**
 * Crossbow combat mirroring vanilla RangedCrossbowAttackGoal, with guard/stationary tweaks.
 */
public class ArbalistCrossbowAttackGoal<T extends AbstractHumanCompanionEntity & CrossbowAttackMob> extends Goal {
    private static final UniformInt PATHFINDING_DELAY_RANGE = TimeUtil.rangeOfSeconds(1, 2);
    private final T mob;
    private CrossbowState state = CrossbowState.UNCHARGED;
    private final double speed;
    private final float attackRadiusSqr;
    private int seeTime;
    private int attackDelay;
    private int updatePathDelay;

    public ArbalistCrossbowAttackGoal(T mob, double speed, float radius) {
        this.mob = mob;
        this.speed = speed;
        this.attackRadiusSqr = radius * radius;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return hasValidTarget() && isHoldingCrossbow();
    }

    @Override
    public boolean canContinueToUse() {
        return hasValidTarget() && (canUse() || !this.mob.getNavigation().isDone()) && isHoldingCrossbow();
    }

    private boolean hasValidTarget() {
        return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
    }

    private boolean isHoldingCrossbow() {
        return this.mob.isHolding(is -> is.getItem() instanceof CrossbowItem);
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.seeTime = 0;
        this.state = CrossbowState.UNCHARGED;
        if (this.mob.isUsingItem()) {
            this.mob.stopUsingItem();
            this.mob.setChargingCrossbow(false);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) return;

        boolean canSee = this.mob.getSensing().hasLineOfSight(target);
        if (canSee) ++this.seeTime; else --this.seeTime;

        double dist = this.mob.distanceToSqr(target);
        boolean pathNeeded = (dist > (double) this.attackRadiusSqr || this.seeTime < 5) && this.attackDelay == 0;

        if (pathNeeded) {
            --this.updatePathDelay;
            if (this.updatePathDelay <= 0) {
                this.mob.getNavigation().moveTo(target, this.canRun() ? this.speed : this.speed * 0.5D);
                this.updatePathDelay = PATHFINDING_DELAY_RANGE.sample(this.mob.getRandom());
            }
        } else {
            this.updatePathDelay = 0;
            this.mob.getNavigation().stop();
        }

        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (this.state == CrossbowState.UNCHARGED) {
            if (!pathNeeded) {
                this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof CrossbowItem));
                this.state = CrossbowState.CHARGING;
                this.mob.setChargingCrossbow(true);
            }
        } else if (this.state == CrossbowState.CHARGING) {
            if (!this.mob.isUsingItem()) {
                this.state = CrossbowState.UNCHARGED;
            }
            int using = this.mob.getTicksUsingItem();
            ItemStack stack = this.mob.getUseItem();
            if (using >= CrossbowItem.getChargeDuration(stack)) {
                this.mob.releaseUsingItem();
                this.state = CrossbowState.CHARGED;
                this.attackDelay = 20 + this.mob.getRandom().nextInt(20);
                this.mob.setChargingCrossbow(false);
            }
        } else if (this.state == CrossbowState.CHARGED) {
            --this.attackDelay;
            if (this.attackDelay == 0) {
                this.state = CrossbowState.READY_TO_ATTACK;
            }
        } else if (this.state == CrossbowState.READY_TO_ATTACK && canSee) {
            // Use the shooter (mob) for performCrossbowAttack; the default implementation looks up the target itself.
            this.mob.performCrossbowAttack(this.mob, 1.6F);
            this.state = CrossbowState.UNCHARGED;
        }
    }

    private boolean canRun() {
        return this.state == CrossbowState.UNCHARGED;
    }

    private enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK
    }
}
