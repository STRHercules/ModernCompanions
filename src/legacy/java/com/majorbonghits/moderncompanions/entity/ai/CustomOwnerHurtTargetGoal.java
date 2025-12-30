package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.core.ModConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Creeper;

import java.util.EnumSet;

/**
 * Attack whatever the owner last hurt, respecting friendly-fire config.
 */
public class CustomOwnerHurtTargetGoal extends TargetGoal {
    private final TamableAnimal companion;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public CustomOwnerHurtTargetGoal(TamableAnimal animal) {
        super(animal, false);
        this.companion = animal;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (this.companion.isTame() && !this.companion.isOrderedToSit()) {
            LivingEntity owner = this.companion.getOwner();
            if (owner == null) return false;
            this.ownerLastHurt = owner.getLastHurtMob();
            if (this.ownerLastHurt instanceof TamableAnimal tame && tame.isTame()) {
                if (tame.getOwner() == this.companion.getOwner() && !ModConfig.safeGet(ModConfig.FRIENDLY_FIRE_COMPANIONS)) {
                    return false;
                }
            } else if (this.ownerLastHurt instanceof Creeper || this.ownerLastHurt instanceof ArmorStand) {
                return false;
            }
            int i = owner.getLastHurtMobTimestamp();
            return i != this.timestamp && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT) && this.companion.wantsToAttack(this.ownerLastHurt, owner);
        }
        return false;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurt);
        LivingEntity owner = this.companion.getOwner();
        if (owner != null) {
            this.timestamp = owner.getLastHurtMobTimestamp();
        }
        super.start();
    }
}
