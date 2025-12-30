package com.majorbonghits.moderncompanions.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;

public class GlaiveItem extends BasicWeaponItem {
    public GlaiveItem(Tier tier, float attackDamage, float attackSpeed, double reach, Properties properties) {
        super(tier, BlockTags.SWORD_EFFICIENT, attackDamage, attackSpeed, reach, properties);
    }
}
