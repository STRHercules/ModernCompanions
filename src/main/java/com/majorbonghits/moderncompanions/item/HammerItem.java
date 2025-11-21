package com.majorbonghits.moderncompanions.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.phys.Vec3;

public class HammerItem extends BasicWeaponSweeplessItem {
    public HammerItem(Tier tier, float attackDamage, float attackSpeed, double reach, Properties properties) {
        super(tier, BlockTags.AIR, attackDamage, attackSpeed, reach, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            float cooldown = player.getAttackStrengthScale(0.5f);
            if (cooldown >= 0.9F) {
                // Give a chunky pop-up similar to BasicWeapons.
                Vec3 currentMovement = target.getDeltaMovement();
                target.setDeltaMovement(currentMovement.x, currentMovement.y + 0.8, currentMovement.z);
                target.hurtMarked = true;
            }
        }
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }
}
