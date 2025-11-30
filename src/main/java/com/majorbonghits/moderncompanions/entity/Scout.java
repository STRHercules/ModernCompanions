package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.core.ModConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import com.majorbonghits.moderncompanions.item.DaggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

/**
 * Mobile skirmisher built for hit-and-run strikes and reduced fall risk.
 */
public class Scout extends AbstractHumanCompanionEntity {
    private static final ResourceLocation SPEED_MOD = ResourceLocation.fromNamespaceAndPath(
            com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, "scout_speed");

    public Scout(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.35D, true));
        boostBaseSpeed();
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            maintainAgilityEffects();
            checkDagger();
        }
        super.tick();
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean hit = super.doHurtTarget(entity);
        if (!this.level().isClientSide() && entity instanceof LivingEntity living) {
            if (isBackstab(living) || focusedElsewhere(living)) {
                living.hurt(this.damageSources().mobAttack(this), 3.0F);
            }
        }
        return hit;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return super.causeFallDamage(distance, multiplier * 0.5F, source);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data) {
        if (ModConfig.safeGet(ModConfig.SPAWN_WEAPON)) {
            this.inventory.setItem(4, Items.STONE_SWORD.getDefaultInstance());
            checkDagger();
        }
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    private void boostBaseSpeed() {
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && speed.getModifier(SPEED_MOD) == null) {
            speed.addPermanentModifier(new AttributeModifier(SPEED_MOD, 0.05D, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private void maintainAgilityEffects() {
        this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, true, false));
        this.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 0, true, false));
    }

    private void checkDagger() {
        ItemStack hand = this.getItemBySlot(EquipmentSlot.MAINHAND);
        ItemStack preferred = ItemStack.EMPTY;
        ItemStack fallback = !hand.isEmpty() && !isShieldItem(hand) ? hand : ItemStack.EMPTY;
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.isEmpty()) continue;
            if (preferred.isEmpty() && (stack.getItem() instanceof DaggerItem || stack.is(Items.STONE_SWORD) || stack.is(Items.IRON_SWORD))) {
                preferred = stack;
            }
            if (fallback.isEmpty() && !isShieldItem(stack)) {
                fallback = stack;
            }
        }
        ItemStack desired = !preferred.isEmpty() ? preferred : fallback;
        if (!ItemStack.isSameItemSameComponents(hand, desired)) {
            this.setItemSlot(EquipmentSlot.MAINHAND, desired);
        }
        setPreferredWeaponBonus(!preferred.isEmpty() && ItemStack.isSameItemSameComponents(desired, preferred));
    }

    private boolean isBackstab(LivingEntity target) {
        var toAttacker = this.position().subtract(target.position()).normalize();
        var look = target.getLookAngle().normalize();
        return toAttacker.dot(look) < -0.25D;
    }

    private boolean focusedElsewhere(LivingEntity target) {
        if (target instanceof Mob mob) {
            return mob.getTarget() != this;
        }
        return false;
    }
}
