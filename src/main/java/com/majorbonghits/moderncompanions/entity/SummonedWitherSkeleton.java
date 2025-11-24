package com.majorbonghits.moderncompanions.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Temporary minions raised by the Necromancer.
 * They inherit wither skeleton visuals but avoid attacking their summoner or allies.
 */
public class SummonedWitherSkeleton extends WitherSkeleton {
    private static final String KEY_OWNER = "Summoner";
    private static final String KEY_LIFETIME = "Lifetime";

    private UUID ownerId;
    private int lifetimeTicks = 20 * 60;

    public SummonedWitherSkeleton(EntityType<? extends WitherSkeleton> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setPersistenceRequired();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;

        if (--lifetimeTicks <= 0) {
            this.discard();
            return;
        }

        // Drop any friendly target to avoid infighting
        if (this.getTarget() != null && isFriendlyTo(this.getTarget())) {
            this.setTarget(null);
        }

        if (this.tickCount % 20 == 0) {
            LivingEntity owner = getSummoner();
            if (owner != null) {
                LivingEntity ownersTarget = owner.getLastHurtMob();
                if (ownersTarget != null && this.canAttack(ownersTarget)) {
                    this.setTarget(ownersTarget);
                } else if (this.getTarget() == null || !this.getTarget().isAlive()) {
                    LivingEntity nearby = this.level().getNearestEntity(
                            LivingEntity.class,
                            TargetingConditions.forCombat().ignoreLineOfSight().selector(this::canAttack),
                            this,
                            this.getX(), this.getY(), this.getZ(),
                            this.getBoundingBox().inflate(12.0D));
                    if (nearby != null) {
                        this.setTarget(nearby);
                    }
                }
            }
        }

        // Stay near summoner
        LivingEntity owner = getSummoner();
        if (owner != null) {
            double dist = this.distanceToSqr(owner);
            double followRange = 28.0D; // ~5.3 blocks radius before pulling back
            double hardRange = 52.0D;   // ~7.2 blocks radius before teleporting
            if (dist > followRange * followRange) {
                this.getNavigation().moveTo(owner, 1.25D);
            }
            if (dist > hardRange * hardRange) {
                this.teleportTo(owner.getX(), owner.getY(), owner.getZ());
            }
        }
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        if (isFriendlyTo(other)) return true;
        return super.isAlliedTo(other);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (isFriendlyTo(target)) return false;
        return super.canAttack(target);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Minions are intentionally weaker; no special damage scaling needed beyond base stats.
        return super.hurt(source, amount);
    }

    @Override
    protected boolean isSunBurnTick() {
        // Prevent sunlight burning; summons should persist outdoors.
        return false;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        // Keep summons alive even on Peaceful so they don't vanish instantly.
        return false;
    }

    public void configureSummon(@Nullable LivingEntity owner, int lifetimeSeconds) {
        this.ownerId = owner != null ? owner.getUUID() : null;
        this.lifetimeTicks = Math.max(20, lifetimeSeconds * 20);
        // Tone down stats so the summon is helpful but not oppressive.
        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(4.0D); // 2 hearts
            this.setHealth(4.0F);
        }
        if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3.0D);
        }
        this.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, lifetimeTicks, 0, true, false));
        this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, lifetimeTicks, 0, true, false));
        // Avoid free loot farming.
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            this.setDropChance(slot, 0.0F);
        }
    }

    @Nullable
    public LivingEntity getSummoner() {
        if (ownerId == null || !(this.level() instanceof ServerLevel server)) return null;
        Entity entity = server.getEntity(ownerId);
        return entity instanceof LivingEntity living ? living : null;
    }

    public boolean isFriendlyTo(Entity other) {
        if (other == null) return false;
        if (ownerId != null && ownerId.equals(other.getUUID())) return true;
        if (other instanceof SummonedWitherSkeleton summoned) {
            return summoned.ownerId != null && summoned.ownerId.equals(this.ownerId);
        }
        if (other instanceof AbstractHumanCompanionEntity companion && companion.getOwnerUUID() != null) {
            return ownerId != null && ownerId.equals(companion.getOwnerUUID());
        }
        if (other instanceof net.minecraft.world.entity.TamableAnimal tam) {
            return ownerId != null && ownerId.equals(tam.getOwnerUUID());
        }
        LivingEntity owner = getSummoner();
        return owner != null && other.isAlliedTo(owner);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerId != null) tag.putUUID(KEY_OWNER, ownerId);
        tag.putInt(KEY_LIFETIME, lifetimeTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID(KEY_OWNER)) ownerId = tag.getUUID(KEY_OWNER);
        lifetimeTicks = tag.getInt(KEY_LIFETIME);
    }

    @Override
    public ItemStack getPickedResult(net.minecraft.world.phys.HitResult target) {
        return ItemStack.EMPTY;
    }
}
