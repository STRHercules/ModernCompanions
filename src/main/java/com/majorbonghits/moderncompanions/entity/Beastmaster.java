package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.entity.ai.ArcherRangedBowAttackGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Ranged companion that always fights with a loyal pet and buffs nearby tamed animals.
 */
public class Beastmaster extends AbstractHumanCompanionEntity implements RangedAttackMob {
    private static final String PET_TAG = "BeastmasterPet";
    private static final String PET_RESPAWN_TAG = "BeastRespawn";

    private UUID petId;
    private int petRespawnTimer;

    public Beastmaster(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.goalSelector.addGoal(2, new ArcherRangedBowAttackGoal<>(this, 1.05D, 22, 20.0F));
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            checkBow();
            managePet();
            applyAnimalBuffs();
        }
        super.tick();
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean hit = super.doHurtTarget(entity);
        if (entity instanceof LivingEntity living && !this.level().isClientSide()) {
            if (isBeast(living)) {
                living.hurt(this.damageSources().mobAttack(this), 2.5F);
            }
        }
        return hit;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        ItemStack bow = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof BowItem));
        ItemStack projectile = this.getProjectile(bow);
        var arrow = ProjectileUtil.getMobArrow(this, projectile, distanceFactor, bow);
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333333333333333D) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + distance * 0.18F, dz, 1.55F, (float) (this.level().getDifficulty().getId() * 3));
        this.level().addFreshEntity(arrow);
        if (!this.level().isClientSide) {
            this.getMainHandItem().hurtAndBreak(1, this, EquipmentSlot.MAINHAND);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data) {
        if (ModConfig.safeGet(ModConfig.SPAWN_WEAPON)) {
            this.inventory.setItem(4, Items.BOW.getDefaultInstance());
            checkBow();
        }
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (petId != null) tag.putUUID(PET_TAG, petId);
        tag.putInt(PET_RESPAWN_TAG, petRespawnTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID(PET_TAG)) petId = tag.getUUID(PET_TAG);
        petRespawnTimer = tag.getInt(PET_RESPAWN_TAG);
        checkBow();
    }

    private void checkBow() {
        ItemStack hand = this.getItemBySlot(EquipmentSlot.MAINHAND);
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.getItem() instanceof BowItem) {
                if (hand.isEmpty()) {
                    this.setItemSlot(EquipmentSlot.MAINHAND, stack);
                    hand = stack;
                }
            }
        }
    }

    private boolean isBeast(LivingEntity target) {
        return target instanceof Wolf || target instanceof Hoglin || target.getType().getCategory() == MobCategory.CREATURE;
    }

    private void managePet() {
        if (!this.isTame() || !(this.level() instanceof ServerLevel server)) return;
        LivingEntity pet = petId != null ? (LivingEntity) server.getEntity(petId) : null;
        if (pet != null) {
            if (pet.isAlive()) return; // living pet already handled
            petId = null;
            petRespawnTimer = 120;
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 120, 1, true, true));
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120, 0, true, true));
        }
        if (petRespawnTimer > 0) {
            petRespawnTimer--;
            return;
        }
        spawnPet(server);
    }

    private void spawnPet(ServerLevel server) {
        Wolf wolf = EntityType.WOLF.create(server);
        if (wolf == null) return;
        wolf.moveTo(this.getX() + (this.random.nextDouble() - 0.5D) * 2.0D, this.getY(),
                this.getZ() + (this.random.nextDouble() - 0.5D) * 2.0D, this.getYRot(), this.getXRot());
        if (this.getOwner() instanceof Player player) {
            wolf.tame(player);
            wolf.setOwnerUUID(player.getUUID());
        }
        wolf.setPersistenceRequired();
        server.addFreshEntity(wolf);
        this.petId = wolf.getUUID();
    }

    private void applyAnimalBuffs() {
        if (this.level().random.nextInt(80) != 0 || !(this.level() instanceof ServerLevel)) return;
        this.level().getEntitiesOfClass(TamableAnimal.class, this.getBoundingBox().inflate(10.0D),
                tamable -> tamable.isTame() && this.getOwner() != null && this.getOwner().equals(tamable.getOwner()))
                .forEach(tame -> {
                    boolean boostDamage = this.random.nextBoolean();
                    int amplifier = this.random.nextFloat() < 0.35F ? 1 : 0; // occasional upgraded brew
                    MobEffectInstance effect = boostDamage
                            ? new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, amplifier, true, true)
                            : new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, amplifier, true, true);
                    tame.addEffect(effect);
                });
    }

    public ItemStack getProjectile(ItemStack stack) {
        if (stack.getItem() instanceof net.minecraft.world.item.ProjectileWeaponItem weapon) {
            var predicate = weapon.getSupportedHeldProjectiles();
            ItemStack projectiles = net.minecraft.world.item.ProjectileWeaponItem.getHeldProjectile(this, predicate);
            return projectiles.isEmpty() ? new ItemStack(Items.ARROW) : projectiles;
        }
        return new ItemStack(Items.ARROW);
    }
}
