package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.core.ModConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

/**
 * Target entities that recently hurt the owner, respecting friendly-fire toggles.
 */
public class CustomOwnerHurtByTargetGoal extends TargetGoal {
    private final TamableAnimal companion;
    private LivingEntity ownerLastHurtBy;
    private int timestamp;

    public CustomOwnerHurtByTargetGoal(TamableAnimal animal) {
        super(animal, false);
        this.companion = animal;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (this.companion.isTame() && !this.companion.isOrderedToSit()) {
            LivingEntity owner = this.companion.getOwner();
            if (owner == null) return false;
            this.ownerLastHurtBy = owner.getLastHurtByMob();
            if (this.ownerLastHurtBy instanceof TamableAnimal tame && tame.isTame()) {
                if (tame.getOwner() == this.companion.getOwner() && !ModConfig.safeGet(ModConfig.FRIENDLY_FIRE_COMPANIONS)) {
                    return false;
                }
            }
            int i = owner.getLastHurtByMobTimestamp();
            return i != this.timestamp && this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT) && this.companion.wantsToAttack(this.ownerLastHurtBy, owner);
        }
        return false;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurtBy);
        LivingEntity owner = this.companion.getOwner();
        if (owner != null) {
            this.timestamp = owner.getLastHurtByMobTimestamp();
        }
        super.start();
    }
}
