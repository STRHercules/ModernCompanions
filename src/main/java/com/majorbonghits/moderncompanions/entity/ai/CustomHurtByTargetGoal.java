package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

/**
 * Custom revenge goal that avoids intra-owner friendly fire.
 */
public class CustomHurtByTargetGoal extends TargetGoal {
    private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
    private static final int ALERT_RANGE_Y = 10;
    private int timestamp;
    private final Class<?>[] toIgnoreDamage;
    private Class<?>[] toIgnoreAlert;

    public CustomHurtByTargetGoal(PathfinderMob mob, Class<?>... toIgnoreDamage) {
        super(mob, true);
        this.toIgnoreDamage = toIgnoreDamage;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        int i = this.mob.getLastHurtByMobTimestamp();
        LivingEntity attacker = this.mob.getLastHurtByMob();
        if (i != this.timestamp && attacker != null) {
            if (attacker.getType() == EntityType.PLAYER && this.mob.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                return false;
            }
            for (Class<?> clazz : this.toIgnoreDamage) {
                if (clazz.isAssignableFrom(attacker.getClass())) {
                    return false;
                }
            }
            if (attacker instanceof TamableAnimal tamed && this.mob instanceof TamableAnimal selfTame) {
                if (selfTame.getOwner() == tamed.getOwner()) {
                    return false;
                }
            }
            return this.canAttack(attacker, HURT_BY_TARGETING);
        }
        return false;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.mob.getLastHurtByMob());
        this.targetMob = this.mob.getTarget();
        this.timestamp = this.mob.getLastHurtByMobTimestamp();
        this.unseenMemoryTicks = 300;
        this.alertOthers();
        super.start();
    }

    protected void alertOthers() {
        double range = this.getFollowDistance();
        AABB box = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(range, ALERT_RANGE_Y, range);
        List<? extends Mob> list = this.mob.level().getEntitiesOfClass(AbstractHumanCompanionEntity.class, box, EntitySelector.NO_SPECTATORS);
        Iterator<? extends Mob> iterator = list.iterator();

        while (iterator.hasNext()) {
            Mob mob = iterator.next();
            if (this.mob != mob && mob.getTarget() == null) {
                if (this.mob instanceof TamableAnimal tame && mob instanceof TamableAnimal other) {
                    if (tame.getOwner() != other.getOwner()) {
                        continue;
                    }
                }
                if (this.toIgnoreAlert != null) {
                    boolean ignore = false;
                    for (Class<?> clazz : this.toIgnoreAlert) {
                        if (mob.getClass() == clazz) {
                            ignore = true;
                            break;
                        }
                    }
                    if (ignore) continue;
                }
                this.alertOther(mob, this.mob.getLastHurtByMob());
            }
        }
    }

    protected void alertOther(Mob mob, LivingEntity target) {
        mob.setTarget(target);
    }
}
