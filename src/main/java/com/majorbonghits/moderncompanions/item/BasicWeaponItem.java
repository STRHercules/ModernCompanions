package com.majorbonghits.moderncompanions.item;

import com.majorbonghits.moderncompanions.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;

/**
 * Sword-like weapons that keep sweeping; derived from the BasicWeapons base class.
 */
public abstract class BasicWeaponItem extends SwordItem {
    private static final boolean BETTER_COMBAT_LOADED = ModList.get().isLoaded("bettercombat");

    public BasicWeaponItem(Tier material, TagKey<Block> effectiveBlocks, float attackDamage, float attackSpeed, double extraReach, Properties properties) {
        super(material, properties
            .component(DataComponents.TOOL, createToolProperties(material, effectiveBlocks))
            .component(DataComponents.ATTRIBUTE_MODIFIERS, createAttributes(material, attackDamage, attackSpeed, extraReach)));
    }

    private static Tool createToolProperties(Tier material, TagKey<Block> effectiveBlocks) {
        List<Tool.Rule> rules = new ArrayList<>();
        rules.add(Tool.Rule.minesAndDrops(material.getIncorrectBlocksForDrops(), material.getSpeed()));
        rules.add(Tool.Rule.overrideSpeed(effectiveBlocks, material.getSpeed()));

        if (effectiveBlocks.equals(BlockTags.SWORD_EFFICIENT)) {
            rules.add(Tool.Rule.minesAndDrops(List.of(Blocks.COBWEB), 15.0F));
        }

        return new Tool(rules, 1.0F, 1);
    }

    private static ItemAttributeModifiers createAttributes(Tier tier, float attackDamage, float attackSpeed, double reach) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_ID,
                    attackDamage + tier.getAttackDamageBonus(),
                    AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_ID,
                    attackSpeed,
                    AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);

        // Better Combat injects its own reach handling; skip ours to avoid duplicate modifiers.
        if (!BETTER_COMBAT_LOADED) {
            builder.add(Attributes.ENTITY_INTERACTION_RANGE,
                new AttributeModifier(
                    Constants.REACH_MODIFIER_ID,
                    reach,
                    AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
        }

        return builder.build();
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
    }
}
