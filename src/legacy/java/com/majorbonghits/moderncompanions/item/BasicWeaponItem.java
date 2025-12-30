package com.majorbonghits.moderncompanions.item;

import com.majorbonghits.moderncompanions.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Sword-like weapons that keep sweeping; derived from the BasicWeapons base class.
 */
public abstract class BasicWeaponItem extends SwordItem {
    private final TagKey<Block> effectiveBlocks;
    private final Tier material;

    public BasicWeaponItem(Tier material, TagKey<Block> effectiveBlocks, float attackDamage, float attackSpeed, double extraReach, Properties properties) {
        super(material, (int) attackDamage, attackSpeed, properties);
        this.effectiveBlocks = effectiveBlocks;
        this.material = material;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.is(effectiveBlocks)) {
            return material.getSpeed();
        }
        if (effectiveBlocks.equals(BlockTags.SWORD_EFFICIENT) && state.is(Blocks.COBWEB)) {
            return 15.0F;
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }
}
