package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;

/**
 * Lets companions consume edible items from their inventory when injured.
 */
public class EatGoal extends Goal {
    protected final AbstractHumanCompanionEntity companion;
    private ItemStack food = ItemStack.EMPTY;
    private int useTicks;

    public EatGoal(AbstractHumanCompanionEntity entity) {
        this.companion = entity;
    }

    @Override
    public boolean canUse() {
        if (companion.getHealth() >= companion.getMaxHealth()) return false;
        food = companion.checkFood();
        return !food.isEmpty();
    }

    @Override
    public void start() {
        companion.setItemSlot(EquipmentSlot.OFFHAND, food);
        companion.startUsingItem(InteractionHand.OFF_HAND);
        companion.setEating(true);
        useTicks = food.getUseDuration();
        if (useTicks <= 0) useTicks = 32;
        companion.swing(InteractionHand.OFF_HAND, true);
    }

    @Override
    public void stop() {
        companion.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        companion.setEating(false);
        useTicks = 0;
    }

    @Override
    public void tick() {
        if (companion.getHealth() >= companion.getMaxHealth()) {
            stop();
            return;
        }
        food = companion.checkFood();
        if (food.isEmpty()) {
            stop();
            return;
        }
        if (useTicks > 0) {
            useTicks--;
            if (useTicks % 4 == 0) {
                companion.swing(InteractionHand.OFF_HAND, true);
            }
        }
        if (useTicks <= 0) {
            if (companion.healFromFoodStack(food)) {
                useTicks = food.getUseDuration();
                if (useTicks <= 0) useTicks = 32;
                companion.startUsingItem(InteractionHand.OFF_HAND);
            } else {
                stop();
            }
        }
    }
}
