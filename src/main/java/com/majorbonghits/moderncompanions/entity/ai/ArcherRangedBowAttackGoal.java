package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;

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
    private boolean strafingBackwards;
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
        return this.mob.getTarget() != null && this.isHoldingBow();
    }

    @Override
    public boolean canContinueToUse() {
        return (this.canUse() || !this.mob.getNavigation().isDone()) && this.isHoldingBow();
    }

    private boolean isHoldingBow() {
        return this.mob.isHolding(stack -> stack.getItem() instanceof BowItem);
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

        double distanceSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean hasLineOfSight = this.mob.getSensing().hasLineOfSight(target);
        boolean seenLastTick = this.seeTime > 0;
        if (hasLineOfSight != seenLastTick) {
            this.seeTime = 0;
        }

        if (hasLineOfSight) {
            ++this.seeTime;
        } else {
            --this.seeTime;
        }

        // Move until close and in sight, then switch to strafing + draw animation
        if (!(distanceSqr > (double) this.attackRadiusSqr) && this.seeTime >= 20) {
            this.mob.getNavigation().stop();
            ++this.strafingTime;
        } else {
            this.mob.getNavigation().moveTo((Entity) target, this.speedModifier);
            this.strafingTime = -1;
        }

        if (this.strafingTime >= 20) {
            if ((double) this.mob.getRandom().nextFloat() < 0.3D) {
                this.strafingClockwise = !this.strafingClockwise;
            }
            if ((double) this.mob.getRandom().nextFloat() < 0.3D) {
                this.strafingBackwards = !this.strafingBackwards;
            }
            this.strafingTime = 0;
        }

        if (this.strafingTime > -1) {
            if (distanceSqr > (double) (this.attackRadiusSqr * 0.75F)) {
                this.strafingBackwards = false;
            } else if (distanceSqr < (double) (this.attackRadiusSqr * 0.25F)) {
                this.strafingBackwards = true;
            }

            this.mob.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F,
                    this.strafingClockwise ? 0.5F : -0.5F);
            this.mob.lookAt(target, 30.0F, 30.0F);
        } else {
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (this.mob.isUsingItem()) {
            if (!hasLineOfSight && this.seeTime < -60) {
                this.mob.stopUsingItem();
            } else if (hasLineOfSight) {
                int ticksUsing = this.mob.getTicksUsingItem();
                if (ticksUsing >= 20) {
                    ItemStack bow = this.mob.getUseItem();
                    float power = bow.getItem() instanceof BowItem
                            ? BowItem.getPowerForTime(ticksUsing)
                            : 1.0F;
                    // Stop using before firing so the client shows the full draw animation and release.
                    this.mob.stopUsingItem();
                    this.mob.performRangedAttack(target, power);
                    this.attackTime = this.attackIntervalMin;
                }
            }
        } else if (--this.attackTime <= 0 && this.seeTime >= -60) {
            this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof BowItem));
        }
    }
}
