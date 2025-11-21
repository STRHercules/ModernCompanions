package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.core.ModConfig;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

/**
 * Potion-flinging support that splashes heals on allies and soft CC on enemies.
 */
public class Alchemist extends AbstractHumanCompanionEntity {
    private int potionTicker;

    public Alchemist(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            tossSupportiveMix();
            tossHostileMix();
            checkPotionSlot();
        }
        super.tick();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data) {
        if (ModConfig.safeGet(ModConfig.SPAWN_WEAPON)) {
            this.inventory.setItem(4, Items.SPLASH_POTION.getDefaultInstance());
            checkPotionSlot();
        }
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    private void checkPotionSlot() {
        ItemStack hand = this.getMainHandItem();
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.is(Items.SPLASH_POTION)) {
                if (hand.isEmpty()) {
                    this.setItemSlot(EquipmentSlot.MAINHAND, stack);
                    hand = stack;
                }
            }
        }
    }

    private void tossSupportiveMix() {
        if (++potionTicker % 35 != 0) return;
        this.level().getEntities(this, this.getBoundingBox().inflate(8.0D), this::isAlly).forEach(entity -> {
            if (entity instanceof LivingEntity living && living.getHealth() / living.getMaxHealth() < 0.7F) {
                int amp = this.random.nextFloat() < 0.25F ? 1 : 0; // occasional upgraded brew
                living.heal(2.0F + amp);
                living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, amp, true, true));
            }
        });
    }

    private void tossHostileMix() {
        if (this.random.nextInt(45) != 0) return;
        this.level().getEntities(this, this.getBoundingBox().inflate(8.0D),
                e -> e instanceof LivingEntity && !this.isAlliedTo(e) && !(e instanceof TamableAnimal tam && tam.isOwnedBy(this.getOwner()))
                        && !(e instanceof AbstractHumanCompanionEntity comp && comp.getOwner() == this.getOwner()))
                .stream().findFirst().ifPresent(entity -> {
                    if (entity instanceof LivingEntity living) {
                        boolean upgrade = this.random.nextFloat() < 0.2F;
                        living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, upgrade ? 1 : 0, true, true));
                        living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, upgrade ? 1 : 0, true, true));
                    }
                });
    }

    private boolean isAlly(Entity entity) {
        if (entity == this) return true;
        if (entity instanceof AbstractHumanCompanionEntity comp) {
            return comp.getOwner() != null && this.getOwner() != null && comp.getOwner() == this.getOwner();
        }
        if (entity instanceof TamableAnimal tamable) {
            return tamable.isTame() && this.getOwner() != null && this.getOwner().equals(tamable.getOwner());
        }
        return entity == this.getOwner();
    }
}
