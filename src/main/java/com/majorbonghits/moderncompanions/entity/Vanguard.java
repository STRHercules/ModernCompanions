package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.core.ModConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

/**
 * Shield-first tank that soaks projectiles and pulls threats off the player.
 */
public class Vanguard extends Knight {

    private static final ResourceLocation KB_MOD = ResourceLocation.fromNamespaceAndPath(
            com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, "vanguard_kb");
    private static final ResourceLocation HEALTH_MOD = ResourceLocation.fromNamespaceAndPath(
            com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, "vanguard_health");

    private int auraTicker;
    private int tauntTicker;

    public Vanguard(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        // Keep up on targets while holding shield.
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 0.95D, true));
        boostDefenseStats();
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            checkSword();
            checkShield();
            tickDefenseAura();
            tickTaunt();
        }
        super.tick();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        float adjusted = amount;
        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            adjusted *= 0.7F; // heavy shield + armor soaks projectiles
        }
        return super.hurt(source, adjusted);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data) {
        if (ModConfig.safeGet(ModConfig.SPAWN_WEAPON)) {
            // Lean toward iron shield start; avoids power creep by capping sword quality.
            ItemStack sword = getSpawnSword();
            ItemStack shield = Items.SHIELD.getDefaultInstance();
            this.inventory.setItem(4, sword);
            this.inventory.setItem(5, shield);
            checkSword();
            checkShield();
        }
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        checkShield();
    }

    private void boostDefenseStats() {
        AttributeInstance kb = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kb != null && kb.getModifier(KB_MOD) == null) {
            kb.addPermanentModifier(new AttributeModifier(KB_MOD, 0.35D, AttributeModifier.Operation.ADD_VALUE));
        }
        AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
        if (health != null && health.getModifier(HEALTH_MOD) == null) {
            health.addPermanentModifier(new AttributeModifier(HEALTH_MOD, 4.0D, AttributeModifier.Operation.ADD_VALUE));
            this.setHealth(this.getMaxHealth());
        }
    }

    private void checkShield() {
        ItemStack offhand = this.getItemBySlot(EquipmentSlot.OFFHAND);
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.is(Items.SHIELD)) {
                if (offhand.isEmpty()) {
                    this.setItemSlot(EquipmentSlot.OFFHAND, stack);
                    offhand = stack;
                }
            }
        }
    }

    private void tickDefenseAura() {
        if (++auraTicker % 40 != 0) return;
        if (this.level().isClientSide()) return;
        level().getEntities(this, this.getBoundingBox().inflate(6.0D), this::isAlly).forEach(entity -> {
            if (entity instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, true, true));
                if (level() instanceof ServerLevel server && level().random.nextBoolean()) {
                    server.sendParticles(ParticleTypes.HAPPY_VILLAGER, living.getX(), living.getY() + 1.1D,
                            living.getZ(), 1, 0.1, 0.1, 0.1, 0.0F);
                }
            }
        });
    }

    private void tickTaunt() {
        if (++tauntTicker % 30 != 0) return;
        if (!(this.level() instanceof ServerLevelAccessor) || this.getOwner() == null) return;
        LivingEntity owner = this.getOwner();
        this.level().getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(12.0D),
                mob -> mob.getTarget() == owner && mob instanceof Monster).forEach(mob -> mob.setTarget(this));
    }

    private boolean isAlly(Entity entity) {
        if (entity == this) return false;
        if (entity instanceof AbstractHumanCompanionEntity companion) {
            return companion.getOwner() != null && this.getOwner() != null && companion.getOwner() == this.getOwner();
        }
        if (entity instanceof TamableAnimal tamable) {
            return tamable.isTame() && this.getOwner() != null && this.getOwner().equals(tamable.getOwner());
        }
        return entity == this.getOwner();
    }
}
