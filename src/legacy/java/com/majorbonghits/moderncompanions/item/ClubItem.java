package com.majorbonghits.moderncompanions.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;

public class ClubItem extends BasicWeaponSweeplessItem {
    public ClubItem(Tier tier, float attackDamage, float attackSpeed, double reach, Properties properties) {
        super(tier, BlockTags.SWORD_EFFICIENT, attackDamage, attackSpeed, reach, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            float cooldown = player.getAttackStrengthScale(0.5f);
            if (cooldown > 0.9F) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 15));
            }
        }
        stack.hurtAndBreak(1, attacker, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }
}
