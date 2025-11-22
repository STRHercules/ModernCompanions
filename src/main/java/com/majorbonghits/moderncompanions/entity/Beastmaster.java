package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.ModernCompanions;
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
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import com.majorbonghits.moderncompanions.item.ClubItem;
import com.majorbonghits.moderncompanions.item.SpearItem;
import com.majorbonghits.moderncompanions.item.HammerItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * Ranged companion that always fights with a loyal pet and buffs nearby tamed
 * animals.
 */
public class Beastmaster extends AbstractHumanCompanionEntity implements RangedAttackMob {
    public static final String BEASTMASTER_OWNER_TAG = "BeastmasterOwner";
    private static final String PET_TAG = "BeastmasterPet";
    private static final String PET_RESPAWN_TAG = "BeastRespawn";
    private static final String PET_LOOKUP_TAG = "BeastLookup";
    private static final String PET_TYPE_TAG = "BeastPetType";
    private static final int PET_LOAD_GRACE_TICKS = 80;
    private static final String[] PET_NAMES = new String[] {
            "Fang", "Ember", "Shadow", "Ash", "Pebble", "Luna", "Mistral", "Bolt", "Blaze", "Copper",
            "Rogue", "Echo", "Nova", "Willow", "Koda", "Storm", "Maple", "Onyx", "Bramble", "Tango",
            "Sable", "Cinder", "Aspen", "Mango", "Hazel", "Riven", "Basil", "Skye", "Thistle", "Hollow",
            "Marble", "Sparrow", "Juniper", "Clover", "Garnet", "Jasper", "Quartz", "Slate", "Nimbus", "Pip",
            "Pixel", "Atlas", "Soot", "Dusk", "Rumble", "Velvet", "Glint", "Cobb", "Indigo", "Taffy",
            "Poppy", "Rust", "Zephyr", "Comet", "Drift", "Puddle", "Mochi", "Ivy", "Thyme", "Cricket",
            "Freckle", "Pepper", "Kumo", "Moss", "Berry", "Twix", "Gizmo", "Fable", "Rook", "Draco",
            "Smudge", "Jinx", "Sunny", "Aurora", "Cosmo", "Sprout", "Nugget", "Biscuit", "Noodle", "Chai",
            "Pumpkin", "Glacier", "Frost", "Whisper", "Galaxy", "Mocha", "Sprinkle", "Truffle", "Phoenix", "Rune",
            "Tinsel", "Fidget", "Button", "Havoc", "Flint", "Blossom", "Dottie", "Ziggy", "Lyric", "Miso",
            "Rascal", "Quill", "Misty", "Clove", "Curry", "River", "Stormy", "Indy", "Loaf", "Pickle",
            "Sushi", "Bean", "Mellow",
            "Raven", "Cobalt", "Violet", "Nyx", "Cypress", "Lotus", "Opal", "Topaz", "Brick", "Sprite",
            "Parker", "Indie", "Bubbles", "Rafa", "Rhea", "Finn", "Rory", "Sora", "Loki", "Opie",
            "Pepita", "Rolo", "Ritz", "Cocoa", "Churro", "Frito", "Tempo", "Rhythm", "Melody", "Aria",
            "Jelly", "Peanut", "Sesame", "Waffle", "Pancake", "Taco", "Nacho", "Salsa", "Kiwi", "Fig",
            "Plum", "Cherry", "Petal", "Mulberry", "Drizzle", "Marshmallow", "Pebbles", "Cinnamon", "Saffron", "Nebula"
    };

    private static final ResourceLocation PET_ATTACK_MOD = ResourceLocation
            .fromNamespaceAndPath(ModernCompanions.MOD_ID, "pet_attack_bonus");
    private static final ResourceLocation PET_HEALTH_MOD = ResourceLocation
            .fromNamespaceAndPath(ModernCompanions.MOD_ID, "pet_health_bonus");
    private static final ResourceLocation PET_SPEED_MOD = ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID,
            "pet_speed_bonus");

    private UUID petId;
    private int petRespawnTimer;
    private int missingPetGrace;
    private ResourceLocation petTypeId;

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
    public void die(DamageSource source) {
        if (!this.level().isClientSide()) {
            despawnPet();
        }
        super.die(source);
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (isOwnPet(entity))
            return false;
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
        if (isOwnPet(target))
            return; // never shoot our own pet
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
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            @Nullable SpawnGroupData data) {
        if (ModConfig.safeGet(ModConfig.SPAWN_WEAPON)) {
            this.inventory.setItem(4, Items.BOW.getDefaultInstance());
            checkBow();
        }
        SpawnGroupData spawnData = super.finalizeSpawn(level, difficulty, reason, data);

        // Guarantee a starting pet immediately on spawn so Beastmasters never appear
        // alone.
        if (level instanceof ServerLevel serverLevel && this.petId == null) {
            spawnPet(serverLevel);
        }

        return spawnData;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (petId != null)
            tag.putUUID(PET_TAG, petId);
        tag.putInt(PET_RESPAWN_TAG, petRespawnTimer);
        tag.putInt(PET_LOOKUP_TAG, missingPetGrace);
        if (petTypeId != null)
            tag.putString(PET_TYPE_TAG, petTypeId.toString());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID(PET_TAG))
            petId = tag.getUUID(PET_TAG);
        petRespawnTimer = tag.getInt(PET_RESPAWN_TAG);
        missingPetGrace = tag.getInt(PET_LOOKUP_TAG);
        if (tag.contains(PET_TYPE_TAG))
            petTypeId = ResourceLocation.tryParse(tag.getString(PET_TYPE_TAG));
        checkBow();
    }

    private void checkBow() {
        ItemStack hand = this.getItemBySlot(EquipmentSlot.MAINHAND);

        // If the current weapon is not preferred or no longer present, clear it to
        // allow reassignment.
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
            if (this.inventory.getItem(i).getItem() == item)
                return true;
        }
        return false;
    }

    private boolean isBeast(LivingEntity target) {
        return target instanceof Wolf || target instanceof Hoglin
                || target.getType().getCategory() == MobCategory.CREATURE;
    }

    private void managePet() {
        if (!(this.level() instanceof ServerLevel server))
            return;
        LivingEntity pet = petId != null ? (LivingEntity) server.getEntity(petId) : null;

        // If we know about a pet but the chunk has not finished loading, give it time
        // to appear
        // and attempt to reattach to an already-existing tamed wolf before spawning a
        // new one.
        if (petId != null && pet == null) {
            pet = findExistingPet(server);
            if (pet != null) {
                petId = pet.getUUID();
                missingPetGrace = 0;
                ensurePetOwnership(pet);
                setupPetGoalsIfNeeded(pet);
            } else {
                if (missingPetGrace == 0) {
                    missingPetGrace = PET_LOAD_GRACE_TICKS;
                } else {
                    missingPetGrace--;
                    if (missingPetGrace <= 0) {
                        petId = null; // treat as lost only after grace expires
                        petRespawnTimer = Math.max(petRespawnTimer, 120); // start respawn timer when pet is truly lost
                    }
                }
                return; // wait for grace period before considering a respawn
            }
        } else {
            missingPetGrace = 0;
        }

        if (pet != null) {
            if (pet.isAlive()) {
                ensurePetOwnership(pet);
                ensurePetTypeFromEntity(pet);
                setupPetGoalsIfNeeded(pet);
                drivePetCombat(pet);
                return; // living pet already handled
            }
            ensurePetTypeFromEntity(pet); // remember type before clearing id
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
     * Pushes simple combat orders to passive pets so they help the Beastmaster by
     * closing distance
     * and attempting vanilla melee hits, even if they do not naturally attack.
     */
    private void drivePetCombat(LivingEntity pet) {
        LivingEntity target = pickThreat();
        if (target == null || target == this || target == this.getOwner())
            return;
        if (!(pet instanceof Mob mob))
            return;

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

    @Override
    public boolean canAttack(LivingEntity target) {
        if (isOwnPet(target))
            return false;
        return super.canAttack(target);
    }

    private boolean isOwnPet(Entity entity) {
        return petId != null && entity != null && petId.equals(entity.getUUID());
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
        if (attacker != null && attacker.isAlive())
            return attacker;

        // Next: whoever is attacking the owner player.
        if (this.getOwner() instanceof Player player) {
            LivingEntity playerAttacker = player.getLastHurtByMob();
            if (playerAttacker != null && playerAttacker.isAlive())
                return playerAttacker;
        }

        // Otherwise, help with Beastmaster's active target.
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive())
            return target;
        return null;
    }

    private void maybeAddMeleeGoal(Mob mob) {
        boolean hasAttackAttribute = mob.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE);

        // If the mob lacks attack damage, remove any melee goals we may have injected
        // to avoid attribute lookups.
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
        // Try any nearby living entity matching the stored UUID or already owned by
        // this Beastmaster.
        if (petId != null) {
            LivingEntity byId = (LivingEntity) server.getEntity(petId);
            if (byId != null) {
                ensurePetTypeFromEntity(byId);
                return byId;
            }
        }
        return server.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(32.0D),
                e -> (petId != null && e.getUUID().equals(petId))
                        || (e instanceof TamableAnimal tam && tam.isTame()
                                && this.getUUID().equals(tam.getOwnerUUID())))
                .stream().findFirst().map(e -> {
                    ensurePetTypeFromEntity(e);
                    return e;
                }).orElse(null);
    }

    private void spawnPet(ServerLevel server) {
        LivingEntity pet = createPet(server);
        if (pet == null)
            return;
        pet.moveTo(this.getX() + (this.random.nextDouble() - 0.5D) * 2.0D, this.getY(),
                this.getZ() + (this.random.nextDouble() - 0.5D) * 2.0D, this.getYRot(), this.getXRot());

        if (pet instanceof TamableAnimal tamable) {
            tamable.setTame(true, true);
            tamable.setOwnerUUID(this.getUUID());
            tamable.setOrderedToSit(false);
        }

        if (pet instanceof Mob mob) {
            // Initialize mob stats/attributes properly for summoned pets (important for
            // pandas/others).
            mob.finalizeSpawn(server, server.getCurrentDifficultyAt(this.blockPosition()),
                    MobSpawnType.MOB_SUMMONED, null);
            setupPetGoalsIfNeeded(mob);
            mob.setPersistenceRequired();
        }

        pet.getPersistentData().putUUID(BEASTMASTER_OWNER_TAG, this.getUUID());
        assignRandomPetName(pet);

        server.addFreshEntity(pet);
        this.petId = pet.getUUID();
    }

    private void ensurePetTypeFromEntity(LivingEntity pet) {
        if (petTypeId == null) {
            petTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(pet.getType());
        }
    }

    private void despawnPet() {
        if (this.level().isClientSide() || !(this.level() instanceof ServerLevel server) || petId == null)
            return;
        Entity pet = server.getEntity(petId);
        if (pet != null) {
            pet.discard();
        }
        petId = null;
    }

    @Nullable
    private LivingEntity createPet(ServerLevel server) {
        EntityType<? extends LivingEntity> type = resolveOrPickPetType();
        LivingEntity pet = type != null ? type.create(server) : null;

        if (pet == null && type == EntityType.PANDA) {
            pet = new Panda(EntityType.PANDA, server); // direct constructor fallback for pandas
        }

        if (pet == null) {
            // Last-resort fallback to wolf so Beastmaster is never left without a pet
            petTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.WOLF);
            pet = EntityType.WOLF.create(server);
        }

        return pet;
    }

    private EntityType<? extends LivingEntity> resolveOrPickPetType() {
        if (petTypeId != null) {
            EntityType<? extends LivingEntity> resolved = (EntityType<? extends LivingEntity>) BuiltInRegistries.ENTITY_TYPE
                    .getOptional(petTypeId).orElse(EntityType.WOLF);
            // If resolved differs, sync petTypeId to the resolved key to keep NBT stable.
            petTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(resolved);
            return resolved;
        }

        // Rare picks first
        int roll = this.random.nextInt(1000);
        EntityType<? extends LivingEntity> chosen;
        if (roll < 5) { // 0.5% polar bear
            chosen = EntityType.POLAR_BEAR;
        } else if (roll < 15) { // next 1% hoglin
            chosen = EntityType.HOGLIN;
        } else {
            // Common pool
            int common = this.random.nextInt(9);
            chosen = switch (common) {
                case 0 -> EntityType.CAMEL;
                case 1 -> EntityType.CAT;
                case 2 -> EntityType.FOX;
                case 3 -> EntityType.GOAT;
                case 4 -> EntityType.OCELOT;
                case 5 -> EntityType.PANDA;
                case 6 -> EntityType.PIG;
                case 7 -> EntityType.SPIDER;
                case 8 -> EntityType.WOLF;
                default -> EntityType.WOLF;
            };
        }

        petTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(chosen);
        return chosen;
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
        if (!(pet instanceof Mob mob))
            return;
        sanitizePetGoals(mob);
        pruneWanderGoals(mob);
        boostPandaSpeedIfNeeded(mob);
        addFollowGoal(mob);
        applyPetScaling(mob);
    }

    /**
     * Strip idle wander goals so pets don't drift far and cause follow/teleport
     * rubber-banding.
     */
    private void pruneWanderGoals(Mob mob) {
        mob.goalSelector.getAvailableGoals().stream()
                .filter(w -> w.getGoal() instanceof net.minecraft.world.entity.ai.goal.RandomStrollGoal
                        || w.getGoal() instanceof net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal)
                .map(WrappedGoal::getGoal)
                .toList()
                .forEach(mob.goalSelector::removeGoal);
    }

    /**
     * Scale pet core stats based on the Beastmaster's attributes so stronger
     * masters yield stronger pets.
     */
    private void applyPetScaling(Mob mob) {
        double str = Math.max(0, this.getStrength());
        double dex = Math.max(0, this.getDexterity());
        double end = Math.max(0, this.getEndurance());

        AttributeInstance attack = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            removeModifier(attack, PET_ATTACK_MOD);
            double bonus = 0.15D * str; // ~1.5 extra damage at STR 10
            if (bonus > 0) {
                attack.addPermanentModifier(
                        new AttributeModifier(PET_ATTACK_MOD, bonus, AttributeModifier.Operation.ADD_VALUE));
            }
        }

        AttributeInstance health = mob.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            removeModifier(health, PET_HEALTH_MOD);
            double bonus = 0.4D * end; // +4 HP at END 10
            if (bonus > 0) {
                health.addPermanentModifier(
                        new AttributeModifier(PET_HEALTH_MOD, bonus, AttributeModifier.Operation.ADD_VALUE));
                mob.setHealth((float) health.getValue());
            }
        }

        AttributeInstance speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            removeModifier(speed, PET_SPEED_MOD);
            double bonus = 0.003D * dex; // modest speed bump tied to DEX
            if (bonus > 0) {
                speed.addPermanentModifier(
                        new AttributeModifier(PET_SPEED_MOD, bonus, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    private void removeModifier(AttributeInstance attribute, ResourceLocation id) {
        attribute.removeModifier(id);
    }

    /**
     * Pandas are extremely slow by default; boost their movement speed so they can
     * keep up with the Beastmaster.
     */
    private void boostPandaSpeedIfNeeded(Mob mob) {
        if (mob instanceof Panda) {
            AttributeInstance speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null && speed.getBaseValue() < 0.30D) {
                speed.setBaseValue(0.30D);
            }
        } else if (mob instanceof Camel) {
            AttributeInstance speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null && speed.getBaseValue() < 0.20D) {
                speed.setBaseValue(0.20D); // half the previous 0.30 boost to keep camels from outpacing masters
            }
        }
    }

    /**
     * Retarget tamed pets so their owner is the Beastmaster instead of the player.
     */
    private void ensurePetOwnership(LivingEntity pet) {
        if (pet instanceof TamableAnimal tamable) {
            if (!this.getUUID().equals(tamable.getOwnerUUID())) {
                tamable.setTame(true, false);
                tamable.setOwnerUUID(this.getUUID());
                tamable.setOrderedToSit(false);
            }
        }
        pet.getPersistentData().putUUID(BEASTMASTER_OWNER_TAG, this.getUUID());
        enforcePetNameVisibility(pet);
    }

    private void assignRandomPetName(LivingEntity pet) {
        if (pet.hasCustomName())
            return;
        String name = PET_NAMES[this.random.nextInt(PET_NAMES.length)];
        pet.setCustomName(Component.literal(name));
        pet.setCustomNameVisible(false);
    }

    private void enforcePetNameVisibility(LivingEntity pet) {
        if (pet.hasCustomName() && pet.isCustomNameVisible()) {
            pet.setCustomNameVisible(false); // show only on look, like companions
        }
    }

    private void applyAnimalBuffs() {
        if (this.level().random.nextInt(80) != 0 || !(this.level() instanceof ServerLevel))
            return;
        this.level().getEntitiesOfClass(TamableAnimal.class, this.getBoundingBox().inflate(10.0D),
                tamable -> tamable.isTame() && tamable.getOwner() != null
                        && (tamable.getOwner().equals(this) || tamable.getOwner().equals(this.getOwner())))
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
