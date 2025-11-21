package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.entity.ai.ArcherRangedBowAttackGoal;
import com.majorbonghits.moderncompanions.entity.ai.FollowBeastmasterGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.majorbonghits.moderncompanions.item.ClubItem;
import com.majorbonghits.moderncompanions.item.SpearItem;
import com.majorbonghits.moderncompanions.item.HammerItem;
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
    private static final String PET_LOOKUP_TAG = "BeastLookup";
    private static final int PET_LOAD_GRACE_TICKS = 80;

    private UUID petId;
    private int petRespawnTimer;
    private int missingPetGrace;

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
        if (bow.isEmpty() || !(bow.getItem() instanceof BowItem)) {
            return; // no valid bow, skip shot
        }
        ItemStack projectile = this.getProjectile(bow);
        if (projectile.isEmpty() || !(projectile.getItem() instanceof ArrowItem)) {
            return; // no arrows available
        }
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
        tag.putInt(PET_LOOKUP_TAG, missingPetGrace);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID(PET_TAG)) petId = tag.getUUID(PET_TAG);
        petRespawnTimer = tag.getInt(PET_RESPAWN_TAG);
        missingPetGrace = tag.getInt(PET_LOOKUP_TAG);
        checkBow();
    }

    private void checkBow() {
        ItemStack hand = this.getItemBySlot(EquipmentSlot.MAINHAND);

        // If the current weapon is not preferred or no longer present, clear it to allow reassignment.
        if (!hand.isEmpty() && (!isPreferredWeapon(hand) || !inventoryContains(hand.getItem()))) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            hand = ItemStack.EMPTY;
        }

        // Prefer bows first; fall back to clubs/hammers/spears.
        ItemStack candidate = ItemStack.EMPTY;
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.getItem() instanceof BowItem) {
                candidate = stack;
                break;
            }
            if (candidate.isEmpty() && (stack.getItem() instanceof ClubItem
                    || stack.getItem() instanceof HammerItem
                    || stack.getItem() instanceof SpearItem)) {
                candidate = stack;
            }
        }

        if (hand.isEmpty() && !candidate.isEmpty()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, candidate);
        }
    }

    private boolean isPreferredWeapon(ItemStack stack) {
        return stack.getItem() instanceof BowItem
                || stack.getItem() instanceof ClubItem
                || stack.getItem() instanceof HammerItem
                || stack.getItem() instanceof SpearItem;
    }

    private boolean inventoryContains(net.minecraft.world.item.Item item) {
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            if (this.inventory.getItem(i).getItem() == item) return true;
        }
        return false;
    }

    private boolean isBeast(LivingEntity target) {
        return target instanceof Wolf || target instanceof Hoglin || target.getType().getCategory() == MobCategory.CREATURE;
    }

    private void managePet() {
        if (!this.isTame() || !(this.level() instanceof ServerLevel server)) return;
        LivingEntity pet = petId != null ? (LivingEntity) server.getEntity(petId) : null;

        // If we know about a pet but the chunk has not finished loading, give it time to appear
        // and attempt to reattach to an already-existing tamed wolf before spawning a new one.
        if (petId != null && pet == null) {
            pet = findExistingPet(server);
            if (pet != null) {
                petId = pet.getUUID();
                missingPetGrace = 0;
                setupPetGoalsIfNeeded(pet);
            } else {
                if (missingPetGrace == 0) {
                    missingPetGrace = PET_LOAD_GRACE_TICKS;
                } else {
                    missingPetGrace--;
                    if (missingPetGrace <= 0) {
                        petId = null; // treat as lost only after grace expires
                    }
                }
                return; // wait for grace period before considering a respawn
            }
        } else {
            missingPetGrace = 0;
        }

        if (pet != null) {
            if (pet.isAlive()) {
                setupPetGoalsIfNeeded(pet);
                drivePetCombat(pet);
                return; // living pet already handled
            }
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

    /**
     * Pushes simple combat orders to passive pets so they help the Beastmaster by closing distance
     * and attempting vanilla melee hits, even if they do not naturally attack.
     */
    private void drivePetCombat(LivingEntity pet) {
        LivingEntity target = pickThreat();
        if (target == null || target == this.getOwner()) return;
        if (target instanceof Player && target.equals(this.getOwner())) return; // never attack owner/player
        if (!(pet instanceof Mob mob)) return;

        // Add a basic melee goal if the mob normally has no attack behavior.
        maybeAddMeleeGoal(mob);

        if (!target.isAlive()) {
            mob.setTarget(null);
            return;
        }
        if (mob.getTarget() != target) {
            mob.setTarget(target);
        }
        if (mob.getNavigation().isDone() || mob.distanceToSqr(target) > 4.0D) {
            mob.getNavigation().moveTo(target, 1.25D);
        }
        if (mob.distanceToSqr(target) < 2.25D && mob.tickCount % 12 == 0) {
            swingAndDamage(mob, target); // nudge passive mobs to apply damage when in range
        }
    }

    private void swingAndDamage(Mob mob, LivingEntity target) {
        float dmg = 3.0F;
        AttributeInstance attack = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            dmg = (float) attack.getValue();
        }
        mob.swing(InteractionHand.MAIN_HAND, true);
        target.hurt(mob.damageSources().mobAttack(mob), dmg);
    }

    @Nullable
    private LivingEntity pickThreat() {
        // Highest priority: whoever is attacking the Beastmaster.
        LivingEntity attacker = this.getLastHurtByMob();
        if (attacker != null && attacker.isAlive()) return attacker;

        // Next: whoever is attacking the owner player.
        if (this.getOwner() instanceof Player player) {
            LivingEntity playerAttacker = player.getLastHurtByMob();
            if (playerAttacker != null && playerAttacker.isAlive()) return playerAttacker;
        }

        // Otherwise, help with Beastmaster's active target.
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) return target;
        return null;
    }

    private void maybeAddMeleeGoal(Mob mob) {
        boolean hasAttackAttribute = mob.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE);

        // If the mob lacks attack damage, remove any melee goals we may have injected to avoid attribute lookups.
        if (!hasAttackAttribute) {
            mob.goalSelector.getAvailableGoals().stream()
                    .filter(w -> w.getGoal() instanceof MeleeAttackGoal)
                    .map(WrappedGoal::getGoal)
                    .toList()
                    .forEach(mob.goalSelector::removeGoal);
            return;
        }

        boolean hasAttackGoal = mob.goalSelector.getAvailableGoals().stream()
                .anyMatch(w -> w.getGoal() instanceof MeleeAttackGoal);
        if (!hasAttackGoal && mob instanceof PathfinderMob pathMob) {
            pathMob.goalSelector.addGoal(2, new MeleeAttackGoal(pathMob, 1.25D, true));
        }
    }

    @Nullable
    private LivingEntity findExistingPet(ServerLevel server) {
        UUID ownerId = this.getOwnerUUID();
        if (ownerId == null) return null;
        // Try any nearby living entity matching the stored UUID or owned by our player.
        if (petId != null) {
            LivingEntity byId = (LivingEntity) server.getEntity(petId);
            if (byId != null) return byId;
        }
        return server.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(32.0D),
                        e -> e.getUUID().equals(petId)
                                || (e instanceof TamableAnimal tam && tam.isTame() && ownerId.equals(tam.getOwnerUUID())))
                .stream().findFirst().orElse(null);
    }

    private void spawnPet(ServerLevel server) {
        LivingEntity pet = createRandomPet(server);
        if (pet == null) return;
        pet.moveTo(this.getX() + (this.random.nextDouble() - 0.5D) * 2.0D, this.getY(),
                this.getZ() + (this.random.nextDouble() - 0.5D) * 2.0D, this.getYRot(), this.getXRot());

        if (pet instanceof TamableAnimal tamable && this.getOwner() instanceof Player player) {
            tamable.tame(player);
            tamable.setOwnerUUID(player.getUUID());
        }

        if (pet instanceof Mob mob) {
            setupPetGoalsIfNeeded(mob);
            mob.setPersistenceRequired();
        }

        server.addFreshEntity(pet);
        this.petId = pet.getUUID();
    }

    @Nullable
    private LivingEntity createRandomPet(ServerLevel server) {
        // Rare picks first
        int roll = this.random.nextInt(1000);
        if (roll < 5) { // 0.5% polar bear
            return EntityType.POLAR_BEAR.create(server);
        }
        if (roll < 15) { // next 1% hoglin
            return EntityType.HOGLIN.create(server);
        }

        // Common pool
        int common = this.random.nextInt(9);
        return switch (common) {
            case 0 -> EntityType.CAMEL.create(server);
            case 1 -> EntityType.CAT.create(server);
            case 2 -> EntityType.FOX.create(server);
            case 3 -> EntityType.GOAT.create(server);
            case 4 -> EntityType.OCELOT.create(server);
            case 5 -> EntityType.PANDA.create(server);
            case 6 -> EntityType.PIG.create(server);
            case 7 -> EntityType.SPIDER.create(server);
            case 8 -> EntityType.WOLF.create(server);
            default -> EntityType.WOLF.create(server);
        };
    }

    private void sanitizePetGoals(Mob mob) {
        // Clear hostile/untargeted AI so the pet only follows Beastmaster orders.
        mob.targetSelector.getAvailableGoals().stream()
                .map(WrappedGoal::getGoal)
                .toList()
                .forEach(mob.targetSelector::removeGoal);
    }

    private void addFollowGoal(Mob mob) {
        boolean hasFollow = mob.goalSelector.getAvailableGoals().stream()
                .anyMatch(w -> w.getGoal() instanceof FollowBeastmasterGoal);
        if (!hasFollow) {
            mob.goalSelector.addGoal(1, new FollowBeastmasterGoal(mob, this, 1.2D, 4.0F, 2.0F));
        }
    }

    private void setupPetGoalsIfNeeded(LivingEntity pet) {
        if (!(pet instanceof Mob mob)) return;
        sanitizePetGoals(mob);
        addFollowGoal(mob);
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
