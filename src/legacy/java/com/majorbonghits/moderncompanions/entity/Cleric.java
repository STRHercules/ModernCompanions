package com.majorbonghits.moderncompanions.entity;

import net.minecraft.nbt.CompoundTag;

import com.majorbonghits.moderncompanions.core.ModConfig;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.majorbonghits.moderncompanions.item.QuarterstaffItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import javax.annotation.Nullable;

/**
 * Supportive healer built to counter undead and keep allies standing.
 */
public class Cleric extends AbstractHumanCompanionEntity {
    private int healTicker;

    public Cleric(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            checkStaff();
            tickHealing();
            tickBlessings();
        }
        super.tick();
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean hit = super.doHurtTarget(entity);
        if (!this.level().isClientSide() && entity instanceof Mob mob) {
            if (mob.getMobType() == MobType.UNDEAD || mob instanceof Zombie) {
                mob.hurt(this.damageSources().mobAttack(this), 3.0F);
            }
        }
        return hit;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data, @Nullable CompoundTag dataTag) {
        if (ModConfig.safeGet(ModConfig.SPAWN_WEAPON)) {
            this.inventory.setItem(4, Items.GOLDEN_SWORD.getDefaultInstance());
            this.inventory.setItem(5, Items.TOTEM_OF_UNDYING.getDefaultInstance());
            checkStaff();
        }
        return super.finalizeSpawn(level, difficulty, reason, data, dataTag);
    }

    private void checkStaff() {
        ItemStack hand = this.getItemBySlot(EquipmentSlot.MAINHAND);
        ItemStack preferred = ItemStack.EMPTY;
        ItemStack fallback = !hand.isEmpty() && !isShieldItem(hand) ? hand : ItemStack.EMPTY;
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.isEmpty()) continue;
            if (preferred.isEmpty() && (stack.is(Items.GOLDEN_SWORD) || stack.getItem() instanceof BowItem || stack.getItem() instanceof QuarterstaffItem)) { // keep totem visuals golden-themed
                preferred = stack;
            }
            if (fallback.isEmpty() && !isShieldItem(stack)) {
                fallback = stack;
            }
        }
        ItemStack desired = !preferred.isEmpty() ? preferred : fallback;
        if (!ItemStack.isSameItemSameTags(hand, desired)) {
            this.setItemSlot(EquipmentSlot.MAINHAND, desired);
        }
        setPreferredWeaponBonus(!preferred.isEmpty() && ItemStack.isSameItemSameTags(desired, preferred));
        ItemStack offhand = this.getItemBySlot(EquipmentSlot.OFFHAND);
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.is(Items.TOTEM_OF_UNDYING) && offhand.isEmpty()) {
                this.setItemSlot(EquipmentSlot.OFFHAND, stack);
                offhand = stack;
            }
        }
    }

    private void tickHealing() {
        if (++healTicker % 30 != 0) return;
        this.level().getEntities(this, this.getBoundingBox().inflate(6.0D), this::isAlly)
                .forEach(e -> {
                    if (e instanceof LivingEntity living && living.getHealth() / living.getMaxHealth() < 0.65F) {
                        living.heal(2.5F);
                        living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, true));
                    }
                });
    }

    private void tickBlessings() {
        if (this.random.nextInt(120) != 0) return;
        this.level().getEntities(this, this.getBoundingBox().inflate(6.0D), this::isAlly)
                .forEach(e -> {
                    if (e instanceof LivingEntity living) {
                        living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0, true, true));
                        living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0, true, true));
                    }
                });
    }

    private boolean isAlly(Entity entity) {
        if (entity == this) return false;
        if (entity instanceof AbstractHumanCompanionEntity comp) {
            return comp.getOwner() != null && this.getOwner() != null && comp.getOwner() == this.getOwner();
        }
        if (entity instanceof TamableAnimal tamable) {
            return tamable.isTame() && this.getOwner() != null && this.getOwner().equals(tamable.getOwner());
        }
        return entity == this.getOwner();
    }
}
