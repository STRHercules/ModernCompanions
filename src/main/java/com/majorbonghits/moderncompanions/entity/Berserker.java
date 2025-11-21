package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.core.TagsInit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

/**
 * High-risk melee DPS that ramps damage as health drops and cleaves nearby foes.
 */
public class Berserker extends AbstractHumanCompanionEntity {
    private static final ResourceLocation RAGE_MOD = ResourceLocation.fromNamespaceAndPath(
            com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, "berserker_rage");
    private static final ResourceLocation KB_MOD = ResourceLocation.fromNamespaceAndPath(
            com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, "berserker_kb");

    public Berserker(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, true));
        bumpKnockbackResist();
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            checkWeapons();
            updateRage();
        }
        super.tick();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        float adjusted = amount;
        if (!source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            adjusted *= 1.1F; // lighter armor betting on aggression
        }
        return super.hurt(source, adjusted);
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean hit = super.doHurtTarget(entity);
        if (!this.level().isClientSide() && entity instanceof LivingEntity living && hit) {
            cleaveAround(living);
        }
        return hit;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data) {
        if (ModConfig.safeGet(ModConfig.SPAWN_WEAPON)) {
            this.inventory.setItem(4, pickSpawnWeapon());
            checkWeapons();
        }
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    private ItemStack pickSpawnWeapon() {
        float roll = this.random.nextFloat();
        if (roll < 0.4F) return Items.WOODEN_AXE.getDefaultInstance();
        if (roll < 0.75F) return Items.STONE_AXE.getDefaultInstance();
        if (roll < 0.9F) return Items.IRON_AXE.getDefaultInstance();
        return Items.IRON_SWORD.getDefaultInstance();
    }

    private void bumpKnockbackResist() {
        AttributeInstance kb = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kb != null && kb.getModifier(KB_MOD) == null) {
            kb.addPermanentModifier(new AttributeModifier(KB_MOD, 0.45D, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private void checkWeapons() {
        ItemStack hand = this.getItemBySlot(EquipmentSlot.MAINHAND);
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (isPreferredWeapon(stack)) {
                if (hand.isEmpty()) {
                    this.setItemSlot(EquipmentSlot.MAINHAND, stack);
                    hand = stack;
                }
            }
        }
    }

    private boolean isPreferredWeapon(ItemStack stack) {
        return stack.is(TagsInit.Items.AXES)
                || stack.is(TagsInit.Items.SWORDS)
                || stack.getItem() instanceof AxeItem
                || stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof com.majorbonghits.moderncompanions.item.ClubItem
                || stack.getItem() instanceof com.majorbonghits.moderncompanions.item.HammerItem;
    }

    private void updateRage() {
        AttributeInstance damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage == null) return;
        damage.removeModifier(RAGE_MOD);
        float percentMissing = 1.0F - (this.getHealth() / this.getMaxHealth());
        double bonus = Math.min(5.0D, 6.0D * percentMissing);
        if (bonus > 0.0D) {
            damage.addTransientModifier(new AttributeModifier(RAGE_MOD, bonus, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private void cleaveAround(LivingEntity primary) {
        double reach = 2.8D;
        double splash = Math.max(1.0D, this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.35D);
        this.level().getEntitiesOfClass(LivingEntity.class, primary.getBoundingBox().inflate(reach),
                other -> other != this && other != primary && !this.isAlliedTo(other) && !(other instanceof TamableAnimal tam && tam.isOwnedBy(this.getOwner()))
                        && !(other instanceof AbstractHumanCompanionEntity comp && comp.getOwner() == this.getOwner()))
                .forEach(other -> other.hurt(this.damageSources().mobAttack(this), (float) splash));
    }
}
