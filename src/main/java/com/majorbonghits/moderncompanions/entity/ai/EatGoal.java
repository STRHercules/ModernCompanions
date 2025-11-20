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

    public EatGoal(AbstractHumanCompanionEntity entity) {
        this.companion = entity;
    }

    @Override
    public boolean canUse() {
        if (companion.getHealth() < companion.getMaxHealth()) {
            food = companion.checkFood();
            return !food.isEmpty();
        }
        return false;
    }

    @Override
    public void start() {
        companion.setItemSlot(EquipmentSlot.OFFHAND, food);
        companion.startUsingItem(InteractionHand.OFF_HAND);
        companion.setEating(true);
    }

    @Override
    public void stop() {
        companion.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        companion.setEating(false);
    }

    @Override
    public void tick() {
        if (companion.getHealth() < companion.getMaxHealth()) {
            food = companion.checkFood();
            if (!food.isEmpty()) {
                start();
            }
        }
    }
}
