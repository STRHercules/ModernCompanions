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
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerLevelAccessor;
import javax.annotation.Nullable;
import net.minecraft.world.entity.monster.RangedAttackMob;
import com.majorbonghits.moderncompanions.core.TagsInit;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Shield-first tank that soaks projectiles and pulls threats off the player.
 */
public class Vanguard extends Knight {

    private static final ResourceLocation KB_MOD = new ResourceLocation(
            com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, "vanguard_kb");
    private static final ResourceLocation HEALTH_MOD = new ResourceLocation(
            com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, "vanguard_health");
    private static final int SHIELD_DISABLE_TICKS = 60;
    private static final int PROJECTILE_MEMORY_TICKS = 80;

    private static UUID modifierId(ResourceLocation id) {
        // Forge 1.20.1 uses UUIDs for attribute modifiers.
        return UUID.nameUUIDFromBytes(id.toString().getBytes(StandardCharsets.UTF_8));
    }
    private static final int MIN_BLOCK_WINDOW = 12;

    private int auraTicker;
    private int tauntTicker;
    private int shieldCooldownTicks;
    private int projectileThreatTicks;
    private int minBlockTicks;

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
            tickShieldUse();
        }
        super.tick();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        float adjusted = amount;
        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            adjusted *= 0.7F; // heavy shield + armor soaks projectiles
            projectileThreatTicks = PROJECTILE_MEMORY_TICKS;
        }
        if (isShieldRaised() && source.getDirectEntity() instanceof LivingEntity attacker) {
            ItemStack weapon = attacker.getMainHandItem();
            if (weapon.getItem() instanceof AxeItem) {
                shieldCooldownTicks = SHIELD_DISABLE_TICKS;
                stopUsingShield();
            }
        }
        return super.hurt(source, adjusted);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data, @Nullable CompoundTag dataTag) {
        if (ModConfig.safeGet(ModConfig.SPAWN_WEAPON)) {
            // Lean toward iron shield start; avoids power creep by capping sword quality.
            ItemStack sword = getSpawnSword();
            ItemStack shield = Items.SHIELD.getDefaultInstance();
            this.inventory.setItem(4, sword);
            this.inventory.setItem(5, shield);
            checkSword();
            checkShield();
        }
        return super.finalizeSpawn(level, difficulty, reason, data, dataTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        checkShield();
    }

    private void boostDefenseStats() {
        AttributeInstance kb = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kb != null && kb.getModifier(modifierId(KB_MOD)) == null) {
            kb.addPermanentModifier(new AttributeModifier(modifierId(KB_MOD), KB_MOD.toString(), 0.35D,
                    AttributeModifier.Operation.ADDITION));
        }
        AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
        if (health != null && health.getModifier(modifierId(HEALTH_MOD)) == null) {
            health.addPermanentModifier(new AttributeModifier(modifierId(HEALTH_MOD), HEALTH_MOD.toString(), 4.0D,
                    AttributeModifier.Operation.ADDITION));
            this.setHealth(this.getMaxHealth());
        }
    }

    private void checkShield() {
        ItemStack offhand = this.getItemBySlot(EquipmentSlot.OFFHAND);
        if (!isShield(offhand)) {
            offhand = ItemStack.EMPTY;
            this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (isShield(stack)) {
                if (offhand.isEmpty()) {
                    this.setItemSlot(EquipmentSlot.OFFHAND, stack);
                    offhand = stack;
                }
            }
        }
        // Prevent mirror-wielding shields: clear main-hand shield if we just assigned offhand.
        ItemStack main = this.getItemBySlot(EquipmentSlot.MAINHAND);
        if (isShield(main)) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    private void tickShieldUse() {
        if (shieldCooldownTicks > 0) shieldCooldownTicks--;
        if (projectileThreatTicks > 0) projectileThreatTicks--;
        if (minBlockTicks > 0) minBlockTicks--;

        if (this.isEating()) {
            stopUsingShield();
            return;
        }

        if (!isShield(this.getItemBySlot(EquipmentSlot.OFFHAND))) {
            stopUsingShield();
            return;
        }

        boolean shouldBlock = shieldCooldownTicks <= 0 && shouldRaiseShield();
        boolean blocking = isShieldRaised();

        if (shouldBlock && !blocking) {
            // Raise the shield and keep it up briefly to avoid flicker.
            this.startUsingItem(InteractionHand.OFF_HAND);
            minBlockTicks = MIN_BLOCK_WINDOW;
        } else if (!shouldBlock && blocking && minBlockTicks <= 0) {
            stopUsingShield();
        }
    }

    private boolean shouldRaiseShield() {
        LivingEntity target = this.getTarget();
        boolean recentProjectile = projectileThreatTicks > 0;
        if (target == null || !target.isAlive()) {
            return recentProjectile;
        }

        double distSq = this.distanceToSqr(target);
        boolean hasLine = this.getSensing().hasLineOfSight(target);
        boolean targetRanged = target instanceof RangedAttackMob
                || target.getMainHandItem().getItem() instanceof ProjectileWeaponItem
                || target.getOffhandItem().getItem() instanceof ProjectileWeaponItem;

        boolean closingGap = distSq > 9.0D && hasLine;
        boolean lowHealth = this.getHealth() < this.getMaxHealth() * 0.6F && hasLine;
        boolean inMelee = distSq < 6.25D && !targetRanged && !recentProjectile;

        if (inMelee) {
            return false; // Drop shield to stay offensive when face-to-face.
        }
        return targetRanged || closingGap || lowHealth || recentProjectile;
    }

    private boolean isShieldRaised() {
        return this.isUsingItem() && isShield(this.getUseItem());
    }

    private void stopUsingShield() {
        if (isShieldRaised()) {
            this.stopUsingItem();
        }
    }

    private boolean isShield(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(Items.SHIELD) || stack.is(TagsInit.Items.SHIELDS)) return true;
        // Fallback: treat any item whose registry path contains "shield" as a shield (e.g., modded uniques without tags).
        return stack.getItem().builtInRegistryHolder().unwrapKey()
                .map(key -> key.location().getPath().contains("shield"))
                .orElse(false);
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
