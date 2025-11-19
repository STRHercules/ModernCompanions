package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.entity.ai.CustomFollowOwnerGoal;
import com.majorbonghits.moderncompanions.entity.ai.AvoidCreeperGoal;
import com.majorbonghits.moderncompanions.entity.ai.LowHealthGoal;
import com.majorbonghits.moderncompanions.entity.ai.PatrolGoal;
import com.majorbonghits.moderncompanions.entity.ai.MoveBackToPatrolGoal;
import com.majorbonghits.moderncompanions.entity.ai.MoveBackToGuardGoal;
import com.majorbonghits.moderncompanions.entity.ai.HuntGoal;
import com.majorbonghits.moderncompanions.entity.ai.AlertGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;
import com.majorbonghits.moderncompanions.core.ModMenuTypes;
import com.majorbonghits.moderncompanions.menu.CompanionMenu;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

/**
 * Heavily simplified port of the original AbstractHumanCompanionEntity.
 * TODO: restore full AI, inventory GUI, networking, eating, patrol/guard logic, and worldgen interactions.
 */
public abstract class AbstractHumanCompanionEntity extends TamableAnimal {
    private static final EntityDataAccessor<Integer> SKIN_VARIANT = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SEX = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FOLLOWING = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PATROLLING = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> GUARDING = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> STATIONERY = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ALERT = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HUNTING = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<BlockPos>> PATROL_POS = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Integer> PATROL_RADIUS = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> FOOD1 = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> FOOD2 = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> FOOD1_AMT = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FOOD2_AMT = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);

    protected final SimpleContainer inventory = new SimpleContainer(27);
    private final Random rand = new Random();
    private Map<net.minecraft.world.item.Item, Integer> foodRequirements = new HashMap<>();

    protected AbstractHumanCompanionEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    /* ---------- Registration ---------- */

    public static AttributeSupplier.Builder createAttributes() {
        double baseHealth = ModConfig.BASE_HEALTH != null ? ModConfig.safeGet(ModConfig.BASE_HEALTH).doubleValue() : 20.0D;
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, baseHealth)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SKIN_VARIANT, 0);
        builder.define(SEX, 0);
        builder.define(FOLLOWING, true);
        builder.define(PATROLLING, false);
        builder.define(GUARDING, false);
        builder.define(STATIONERY, false);
        builder.define(ALERT, true);
        builder.define(HUNTING, false);
        builder.define(PATROL_POS, Optional.empty());
        builder.define(PATROL_RADIUS, 4);
        builder.define(FOOD1, "minecraft:apple");
        builder.define(FOOD2, "minecraft:bread");
        builder.define(FOOD1_AMT, 1);
        builder.define(FOOD2_AMT, 1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(3, new CustomFollowOwnerGoal(this, 1.1D, 4.0F, 2.0F, false));
        this.goalSelector.addGoal(4, new AvoidCreeperGoal(this, 1.0D, 1.2D));
        this.goalSelector.addGoal(5, new LowHealthGoal(this));
        this.goalSelector.addGoal(6, new PatrolGoal(this, 40, getPatrolRadius()));
        this.goalSelector.addGoal(7, new MoveBackToPatrolGoal(this, getPatrolRadius()));
        this.goalSelector.addGoal(8, new MoveBackToGuardGoal(this));
        this.goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 0.9D));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(4, new HuntGoal(this));
        this.targetSelector.addGoal(5, new AlertGoal(this));
    }

    /* ---------- Flags & helpers ---------- */

    public boolean isFollowing() { return this.entityData.get(FOLLOWING); }
    public void setFollowing(boolean value) { this.entityData.set(FOLLOWING, value); }

    public boolean isPatrolling() { return this.entityData.get(PATROLLING); }
    public void setPatrolling(boolean value) { this.entityData.set(PATROLLING, value); }

    public boolean isGuarding() { return this.entityData.get(GUARDING); }
    public void setGuarding(boolean value) { this.entityData.set(GUARDING, value); }

    public boolean isStationery() { return this.entityData.get(STATIONERY); }
    public void setStationery(boolean value) { this.entityData.set(STATIONERY, value); }

    public boolean isAlert() { return this.entityData.get(ALERT); }
    public void setAlert(boolean value) { this.entityData.set(ALERT, value); }

    public boolean isHunting() { return this.entityData.get(HUNTING); }
    public void setHunting(boolean value) { this.entityData.set(HUNTING, value); }

    public Optional<BlockPos> getPatrolPos() { return this.entityData.get(PATROL_POS); }
    public void setPatrolPos(@Nullable BlockPos pos) { this.entityData.set(PATROL_POS, Optional.ofNullable(pos)); }

    public int getPatrolRadius() { return this.entityData.get(PATROL_RADIUS); }
    public void setPatrolRadius(int radius) { this.entityData.set(PATROL_RADIUS, Mth.clamp(radius, 1, 64)); }

    public void clearPatrol() {
        setPatrolPos(null);
        setPatrolling(false);
        setPatrolRadius(4);
    }

    public SimpleContainer getInventory() { return inventory; }
    public Map<net.minecraft.world.item.Item, Integer> getFoodRequirements() { return foodRequirements; }
    public int getSkinIndex() { return this.entityData.get(SKIN_VARIANT); }
    public void setSkinIndex(int index) {
        int sex = getSex();
        int max = CompanionData.skins[sex].length;
        this.entityData.set(SKIN_VARIANT, Mth.clamp(index, 0, Math.max(0, max - 1)));
    }

    public int getSex() { return this.entityData.get(SEX); }
    public void setSex(int value) {
        this.entityData.set(SEX, Mth.clamp(value, 0, CompanionData.skins.length - 1));
    }

    public ResourceLocation getSkinTexture() {
        int sex = Mth.clamp(getSex(), 0, CompanionData.skins.length - 1);
        ResourceLocation[] variants = CompanionData.skins[sex];
        int idx = Mth.clamp(getSkinIndex(), 0, variants.length - 1);
        return variants[idx];
    }

    public boolean hasFoodInInventory() {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (CompanionData.isFood(inventory.getItem(i).getItem())) return true;
        }
        return false;
    }

    public void eatOneFood() {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (CompanionData.isFood(stack.getItem())) {
                stack.shrink(1);
                this.heal(4.0F);
                return;
            }
        }
    }

    public int getCompanionSkin() {
        return getSkinIndex();
    }

    /* ---------- Interaction ---------- */

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (hand == InteractionHand.MAIN_HAND) {
                ItemStack held = player.getItemInHand(hand);
                if (!isTame()) {
                    if (CompanionData.isFood(held.getItem())) {
                        if (foodRequirements.isEmpty()) {
                            assignFoodRequirements();
                        }
                        if (foodRequirements.containsKey(held.getItem()) && foodRequirements.get(held.getItem()) > 0) {
                            held.shrink(1);
                            foodRequirements.put(held.getItem(), foodRequirements.get(held.getItem()) - 1);
                            syncFoodRequirements();
                            if (foodRequirements.values().stream().allMatch(v -> v <= 0)) {
                                this.tame(player);
                                setFollowing(true);
                                clearPatrol();
                                serverPlayer.sendSystemMessage(Component.translatable("chat.type.text", getDisplayName(),
                                        Component.literal("Thanks!")));
                                return InteractionResult.SUCCESS;
                            } else {
                                serverPlayer.sendSystemMessage(Component.translatable("chat.type.text", getDisplayName(),
                                        CompanionData.tameFail[rand.nextInt(CompanionData.tameFail.length)]));
                                return InteractionResult.SUCCESS;
                            }
                        } else {
                            serverPlayer.sendSystemMessage(Component.translatable("chat.type.text", getDisplayName(),
                                    CompanionData.WRONG_FOOD[rand.nextInt(CompanionData.WRONG_FOOD.length)]));
                            return InteractionResult.SUCCESS;
                        }
                    } else {
                        serverPlayer.sendSystemMessage(Component.translatable("chat.type.text", getDisplayName(),
                                CompanionData.notTamed[rand.nextInt(CompanionData.notTamed.length)]));
                        serverPlayer.sendSystemMessage(Component.literal(getFoodStatus()));
                        return InteractionResult.SUCCESS;
                    }
                }

                // Open inventory menu when owned
                if (isOwnedBy(player)) {
                    // Shift-right-click toggles guarding at current position; second toggle clears guard/patrol.
                    if (player.isShiftKeyDown()) {
                        if (isGuarding()) {
                            setGuarding(false);
                            clearPatrol();
                            serverPlayer.sendSystemMessage(Component.translatable("chat.type.text", getDisplayName(),
                                    Component.literal("Standing down.")));
                        } else {
                            setGuarding(true);
                            setPatrolling(false);
                            setPatrolPos(blockPosition());
                            serverPlayer.sendSystemMessage(Component.translatable("chat.type.text", getDisplayName(),
                                    Component.literal("Guarding here.")));
                        }
                        return InteractionResult.SUCCESS;
                    }

                    MenuProvider provider = new SimpleMenuProvider(
                            (id, inv, p) -> new CompanionMenu(id, inv, this),
                            getDisplayName());
                    serverPlayer.openMenu(provider, buf -> buf.writeVarInt(getId()));
                    return InteractionResult.CONSUME;
                }
            }
        }
        return super.mobInteract(player, hand);
    }

    private void assignFoodRequirements() {
        foodRequirements = CompanionData.getRandomFoodRequirement(rand);
        var entries = foodRequirements.entrySet().stream().toList();
        this.entityData.set(FOOD1, BuiltInRegistries.ITEM.getKey(entries.get(0).getKey()).toString());
        this.entityData.set(FOOD1_AMT, entries.get(0).getValue());
        this.entityData.set(FOOD2, BuiltInRegistries.ITEM.getKey(entries.get(1).getKey()).toString());
        this.entityData.set(FOOD2_AMT, entries.get(1).getValue());
    }

    private void syncFoodRequirements() {
        if (foodRequirements.isEmpty()) return;
        foodRequirements.forEach((item, count) -> {
            String id = BuiltInRegistries.ITEM.getKey(item).toString();
            if (id.equals(entityData.get(FOOD1))) {
                entityData.set(FOOD1_AMT, count);
            } else if (id.equals(entityData.get(FOOD2))) {
                entityData.set(FOOD2_AMT, count);
            }
        });
    }

    private String getFoodStatus() {
        String f1 = entityData.get(FOOD1_AMT) > 0 ? entityData.get(FOOD1_AMT) + "x " + entityData.get(FOOD1) : "done";
        String f2 = entityData.get(FOOD2_AMT) > 0 ? entityData.get(FOOD2_AMT) + "x " + entityData.get(FOOD2) : "done";
        return "Wants: " + f1 + " and " + f2;
    }

    /* ---------- Breeding / persistence ---------- */

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Following", isFollowing());
        tag.putBoolean("Patrolling", isPatrolling());
        tag.putBoolean("Guarding", isGuarding());
        tag.putBoolean("Stationery", isStationery());
        tag.putBoolean("Alert", isAlert());
        tag.putBoolean("Hunting", isHunting());
        getPatrolPos().ifPresent(pos -> tag.putLong("PatrolPos", pos.asLong()));
        tag.putInt("PatrolRadius", getPatrolRadius());
        tag.putString("Food1", entityData.get(FOOD1));
        tag.putString("Food2", entityData.get(FOOD2));
        tag.putInt("Food1Amt", entityData.get(FOOD1_AMT));
        tag.putInt("Food2Amt", entityData.get(FOOD2_AMT));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setFollowing(tag.getBoolean("Following"));
        setPatrolling(tag.getBoolean("Patrolling"));
        setGuarding(tag.getBoolean("Guarding"));
        setStationery(tag.getBoolean("Stationery"));
        setAlert(tag.getBoolean("Alert"));
        setHunting(tag.getBoolean("Hunting"));
        if (tag.contains("PatrolPos")) setPatrolPos(BlockPos.of(tag.getLong("PatrolPos")));
        setPatrolRadius(tag.getInt("PatrolRadius"));
        if (tag.contains("Food1")) {
            entityData.set(FOOD1, tag.getString("Food1"));
            entityData.set(FOOD2, tag.getString("Food2"));
            entityData.set(FOOD1_AMT, tag.getInt("Food1Amt"));
            entityData.set(FOOD2_AMT, tag.getInt("Food2Amt"));
            foodRequirements = new HashMap<>();
            var item1 = BuiltInRegistries.ITEM.get(ResourceLocation.parse(entityData.get(FOOD1)));
            var item2 = BuiltInRegistries.ITEM.get(ResourceLocation.parse(entityData.get(FOOD2)));
            foodRequirements.put(item1, entityData.get(FOOD1_AMT));
            foodRequirements.put(item2, entityData.get(FOOD2_AMT));
        }
    }

    /* ---------- Spawning ---------- */

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null; // companions do not breed
    }

    @Override
    public MobCategory getClassification(boolean forSpawnCount) {
        return MobCategory.AMBIENT;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return CompanionData.isFood(stack.getItem());
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            checkArmor();
            checkWeapon();
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data) {
        assignFoodRequirements();
        setSex(this.random.nextBoolean() ? 0 : 1);
        setSkinIndex(this.random.nextInt(CompanionData.skins[getSex()].length));
        // small random health variance like original
        double base = ModConfig.safeGet(ModConfig.BASE_HEALTH);
        double varied = base + (rand.nextInt(9) - 4);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(varied);
        this.setHealth((float) varied);

        if (ModConfig.safeGet(ModConfig.SPAWN_WEAPON)) {
            this.inventory.setItem(0, new ItemStack(Items.IRON_SWORD));
        }
        if (ModConfig.safeGet(ModConfig.SPAWN_ARMOR)) {
            this.inventory.setItem(1, new ItemStack(Items.IRON_HELMET));
            this.inventory.setItem(2, new ItemStack(Items.IRON_CHESTPLATE));
            this.inventory.setItem(3, new ItemStack(Items.IRON_LEGGINGS));
            this.inventory.setItem(4, new ItemStack(Items.IRON_BOOTS));
        }
        checkArmor();
        checkWeapon();
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    /* ---------- Network-driven flag setters ---------- */
    public void applyFlag(String flag, boolean value) {
        switch (flag) {
            case "follow" -> setFollowing(value);
            case "patrol" -> {
                setPatrolling(value);
                setGuarding(false);
                if (value) setPatrolPos(blockPosition());
            }
            case "guard" -> {
                setGuarding(value);
                setPatrolling(false);
                if (value) setPatrolPos(blockPosition());
            }
            case "hunt" -> setHunting(value);
            case "alert" -> setAlert(value);
            case "stationery" -> setStationery(value);
            default -> {}
        }
    }

    public boolean getFlagValue(String flag) {
        return switch (flag) {
            case "follow" -> isFollowing();
            case "patrol" -> isPatrolling();
            case "guard" -> isGuarding();
            case "hunt" -> isHunting();
            case "alert" -> isAlert();
            case "stationery" -> isStationery();
            default -> false;
        };
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (!ModConfig.safeGet(ModConfig.FALL_DAMAGE) && source.is(DamageTypeTags.IS_FALL)) {
            return true;
        }
        return super.isInvulnerableTo(source);
    }

    /* ---------- Equipment helpers (stubs) ---------- */
    public void checkArmor() {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) continue;
            EquipmentSlot slot = this.getEquipmentSlotForItem(stack);
            if (CompanionData.isArmorSlot(slot) && CompanionData.isBetterArmor(stack, getItemBySlot(slot))) {
                setItemSlot(slot, stack.copy());
            }
        }
    }

    public void checkWeapon() {
        // TODO: port original weapon selection; for now ensure a basic sword if none
        ItemStack hand = getMainHandItem();
        if (hand.isEmpty()) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        }
    }
}
