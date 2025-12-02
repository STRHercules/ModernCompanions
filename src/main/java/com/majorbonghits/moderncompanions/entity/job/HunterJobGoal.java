package com.majorbonghits.moderncompanions.entity.job;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.entity.CompanionData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

import java.util.EnumSet;

/**
 * Hunter loop that softly mirrors the legacy hunt toggle: periodically finds a
 * nearby valid target and lets existing target goals handle combat. Kept light
 * to avoid double-pathing with built-in attack goals.
 */
public class HunterJobGoal extends Goal {
    private static final int CHECK_INTERVAL = 20;
    private final double searchRadius;
    private final boolean enabled;

    private final AbstractHumanCompanionEntity companion;
    private int tickDown;

    public HunterJobGoal(AbstractHumanCompanionEntity companion, double searchRadius, boolean enabled) {
        this.companion = companion;
        this.searchRadius = Math.max(6.0D, searchRadius);
        this.enabled = enabled;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return isActiveJob();
    }

    @Override
    public boolean canContinueToUse() {
        return isActiveJob();
    }

    @Override
    public void tick() {
        if (tickDown-- > 0) return;
        tickDown = CHECK_INTERVAL;
        if (companion.getTarget() != null && companion.getTarget().isAlive()) return;
        LivingEntity target = findTarget();
        if (target != null) {
            companion.setTarget(target);
        }
    }

    private LivingEntity findTarget() {
        AABB box = companion.getBoundingBox().inflate(searchRadius);
        for (LivingEntity entity : companion.level().getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            for (Class<?> c : CompanionData.huntMobs) {
                if (c.isInstance(entity) && !entity.isAlliedTo(companion)) {
                    return entity;
                }
            }
        }
        return null;
    }

    private boolean isActiveJob() {
        if (!enabled) return false;
        if (companion.getJob() != CompanionJob.HUNTER) return false;
        if (companion.isOrderedToSit() || !companion.isTame()) return false;
        if (!hasWeapon()) return false;
        var owner = companion.getOwner();
        double max = Math.max(8.0D, searchRadius);
        if (owner != null && companion.distanceToSqr(owner) <= max * max) return true;
        return companion.isPatrolling() && companion.getPatrolPos().isPresent()
                && companion.getPatrolPos().get().distSqr(companion.blockPosition()) <= Math.pow(Math.max(8.0D, companion.getPatrolRadius() + 4), 2);
    }

    private boolean hasWeapon() {
        return hasTool(stack -> stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof AxeItem
                || stack.getItem() instanceof BowItem
                || stack.getItem() instanceof CrossbowItem);
    }

    private boolean hasTool(java.util.function.Predicate<ItemStack> matcher) {
        if (matcher.test(companion.getMainHandItem())) return true;
        for (int i = 0; i < companion.getInventory().getContainerSize(); i++) {
            if (matcher.test(companion.getInventory().getItem(i))) {
                return true;
            }
        }
        return false;
    }
}
