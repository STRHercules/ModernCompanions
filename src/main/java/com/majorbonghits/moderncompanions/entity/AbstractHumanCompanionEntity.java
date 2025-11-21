package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.core.ModMenuTypes;
import com.majorbonghits.moderncompanions.entity.ai.*;
import com.majorbonghits.moderncompanions.menu.CompanionMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Port of the original AbstractHumanCompanionEntity with taming, leveling, patrol/guard logic, and inventory handling.
 */
public abstract class AbstractHumanCompanionEntity extends TamableAnimal {
    private static final EntityDataAccessor<Integer> SKIN_VARIANT = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SEX = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BASE_HEALTH = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EXP_LVL = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> EATING = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ALERT = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HUNTING = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PATROLLING = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FOLLOWING = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> GUARDING = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> STATIONERY = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<BlockPos>> PATROL_POS = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Integer> PATROL_RADIUS = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> FOOD1 = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> FOOD2 = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> FOOD1_AMT = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FOOD2_AMT = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> EXP_PROGRESS = SynchedEntityData.defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.FLOAT);

    protected final SimpleContainer inventory = new SimpleContainer(54);
    protected final Map<Item, Integer> foodRequirements = new HashMap<>();
    protected final Random rand = new Random();

    public PatrolGoal patrolGoal;
    public MoveBackToPatrolGoal moveBackGoal;

    private int totalExperience;
    private float experienceProgress;
    private int lastLevelUpTime;

    protected AbstractHumanCompanionEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.setTame(false, false);
        if (this.getNavigation() instanceof GroundPathNavigation nav) {
            nav.setCanOpenDoors(true);
            nav.setCanFloat(true);
        }
    }

    /* ---------- Registration ---------- */

    public static AttributeSupplier.Builder createAttributes() {
        double baseHealth = ModConfig.BASE_HEALTH != null ? ModConfig.safeGet(ModConfig.BASE_HEALTH).doubleValue() : 20.0D;
        return TamableAnimal.createMobAttributes()
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.MAX_HEALTH, baseHealth)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SKIN_VARIANT, 0);
        builder.define(SEX, 0);
        builder.define(BASE_HEALTH, ModConfig.safeGet(ModConfig.BASE_HEALTH));
        builder.define(EXP_LVL, 0);
        builder.define(EATING, false);
        builder.define(ALERT, false);
        builder.define(HUNTING, false);
        builder.define(PATROLLING, false);
        builder.define(FOLLOWING, false);
        builder.define(GUARDING, false);
        builder.define(STATIONERY, false);
        builder.define(PATROL_POS, Optional.empty());
        builder.define(PATROL_RADIUS, 10);
        builder.define(FOOD1, "");
        builder.define(FOOD2, "");
        builder.define(FOOD1_AMT, 0);
        builder.define(FOOD2_AMT, 0);
        builder.define(EXP_PROGRESS, 0.0F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new EatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new AvoidCreeperGoal(this, 1.5D, 1.5D));
        this.goalSelector.addGoal(3, new MoveBackToGuardGoal(this));
        this.goalSelector.addGoal(3, new CustomFollowOwnerGoal(this, 1.3D, 8.0F, 2.5F, false));
        this.goalSelector.addGoal(4, new CustomWaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(8, new LowHealthGoal(this));
        patrolGoal = new PatrolGoal(this, 60, getPatrolRadius());
        moveBackGoal = new MoveBackToPatrolGoal(this, getPatrolRadius());
        this.goalSelector.addGoal(3, moveBackGoal);
        this.goalSelector.addGoal(3, patrolGoal);

        this.targetSelector.addGoal(1, new CustomOwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new CustomOwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new CustomHurtByTargetGoal(this));
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
    public void setPatrolRadius(int radius) {
        this.entityData.set(PATROL_RADIUS, Mth.clamp(radius, 1, 64));
        if (patrolGoal != null) patrolGoal.radius = radius;
        if (moveBackGoal != null) moveBackGoal.radius = radius;
    }

    public void clearPatrol() {
        setPatrolPos(null);
        setPatrolling(false);
        setPatrolRadius(4);
    }

    public String getFoodStatus() {
        String f1 = entityData.get(FOOD1_AMT) > 0 ? entityData.get(FOOD1_AMT) + "x " + entityData.get(FOOD1) : "done";
        String f2 = entityData.get(FOOD2_AMT) > 0 ? entityData.get(FOOD2_AMT) + "x " + entityData.get(FOOD2) : "done";
        return "Wants: " + f1 + " and " + f2;
    }
    public String getWantedFoodsCompact() {
        int amt1 = entityData.get(FOOD1_AMT);
        int amt2 = entityData.get(FOOD2_AMT);
        String id1 = entityData.get(FOOD1);
        String id2 = entityData.get(FOOD2);
        String first = amt1 > 0 ? amt1 + "x " + prettyItemName(id1) : "";
        String second = amt2 > 0 ? amt2 + "x " + prettyItemName(id2) : "";
        if (first.isEmpty() && second.isEmpty()) return "";
        if (!first.isEmpty() && !second.isEmpty()) return first + ", " + second;
        return first + second;
    }

    public SimpleContainer getInventory() { return inventory; }
    public Map<Item, Integer> getFoodRequirements() { return foodRequirements; }
    public int getSkinIndex() { return this.entityData.get(SKIN_VARIANT); }
    public void setSkinIndex(int index) {
        int sex = getSex();
        int max = CompanionData.skins[sex].length;
        this.entityData.set(SKIN_VARIANT, Mth.clamp(index, 0, Math.max(0, max - 1)));
    }

    public int getSex() { return this.entityData.get(SEX); }
    public void setSex(int value) { this.entityData.set(SEX, Mth.clamp(value, 0, CompanionData.skins.length - 1)); }

    public int getBaseHealth() { return this.entityData.get(BASE_HEALTH); }
    public void setBaseHealth(int health) { this.entityData.set(BASE_HEALTH, health); }

    public boolean isEating() { return this.entityData.get(EATING); }
    public void setEating(boolean eating) { this.entityData.set(EATING, eating); }

    public int getExpLvl() { return this.entityData.get(EXP_LVL); }
    public void setExpLvl(int lvl) { this.entityData.set(EXP_LVL, Math.max(lvl, 0)); }
    public float getExperienceProgress() { return this.level().isClientSide ? this.entityData.get(EXP_PROGRESS) : this.experienceProgress; }
    public int getTotalExperience() { return this.totalExperience; }

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

    public ItemStack checkFood() {
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemstack = this.inventory.getItem(i);
            FoodProperties food = itemstack.get(DataComponents.FOOD);
            if (food != null && food.nutrition() + this.getHealth() <= this.getMaxHealth()) {
                return itemstack;
            }
        }
        return ItemStack.EMPTY;
    }

    public void eatOneFood() {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            FoodProperties food = stack.get(DataComponents.FOOD);
            if (food != null) {
                stack.shrink(1);
                this.heal(food.nutrition());
                return;
            }
        }
    }

    /* ---------- Interaction ---------- */

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (hand == InteractionHand.MAIN_HAND) {
            if (!this.isTame() && !this.level().isClientSide()) {
                if (held.has(DataComponents.FOOD)) {
                    if (foodRequirements.isEmpty()) {
                        assignFoodRequirements();
                    }
                    if (foodRequirements.containsKey(held.getItem())) {
                        int remaining = foodRequirements.get(held.getItem());
                        if (remaining > 0) {
                            held.shrink(1);
                            foodRequirements.put(held.getItem(), remaining - 1);
                            syncFoodRequirements();
                            if (foodRequirements.values().stream().allMatch(v -> v <= 0)) {
                                this.tame(player);
                                player.sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(),
                                        Component.literal("Thanks!")));
                                player.sendSystemMessage(Component.literal("Companion added"));
                                setPatrolPos(null);
                                setPatrolling(false);
                                setFollowing(true);
                                setPatrolRadius(4);
                                if (patrolGoal != null) patrolGoal.radius = 4;
                                if (moveBackGoal != null) moveBackGoal.radius = 4;
                            } else if (foodRequirements.get(held.getItem()) == 0) {
                                player.sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(),
                                        CompanionData.ENOUGH_FOOD[this.random.nextInt(CompanionData.ENOUGH_FOOD.length)]));
                            } else {
                                player.sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(),
                                        CompanionData.tameFail[this.random.nextInt(CompanionData.tameFail.length)]));
                            }
                        } else {
                            player.sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(),
                                    CompanionData.ENOUGH_FOOD[this.random.nextInt(CompanionData.ENOUGH_FOOD.length)]));
                        }
                    } else {
                        player.sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(),
                                CompanionData.WRONG_FOOD[this.random.nextInt(CompanionData.WRONG_FOOD.length)]));
                    }
                } else {
                    player.sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(),
                            CompanionData.notTamed[this.random.nextInt(CompanionData.notTamed.length)]));
                    player.sendSystemMessage(Component.literal(getFoodStatus()));
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            } else {
                if (this.isAlliedTo(player)) {
                    if (player.isShiftKeyDown()) {
                        if (!this.level().isClientSide()) {
                            toggleSit((ServerPlayer) player);
                        }
                    } else {
                        if (!this.level().isClientSide()) {
                            openGui((ServerPlayer) player);
                        }
                    }
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
        return super.mobInteract(player, hand);
    }

    private void toggleSit(ServerPlayer player) {
        if (!this.isOrderedToSit()) {
            this.setOrderedToSit(true);
            Component text = Component.literal("I'll stand here.");
            player.sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(), text));
        } else {
            this.setOrderedToSit(false);
            Component text = Component.literal("I'll move around.");
            player.sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(), text));
        }
    }

    public void openGui(ServerPlayer player) {
        MenuProvider provider = new SimpleMenuProvider(
                (id, inv, p) -> new CompanionMenu(id, inv, this),
                getDisplayName());
        player.openMenu(provider, buf -> buf.writeVarInt(getId()));
    }

    private void assignFoodRequirements() {
        Map<Item, Integer> newReq = CompanionData.getRandomFoodRequirement(rand);
        foodRequirements.clear();
        foodRequirements.putAll(newReq);
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

    private String prettyItemName(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return id;
        Item item = BuiltInRegistries.ITEM.get(rl);
        return item.getDescription().getString();
    }

    /* ---------- Breeding / persistence ---------- */

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Inventory", this.inventory.createTag(this.registryAccess()));
        tag.putInt("skin", this.getSkinIndex());
        tag.putBoolean("Eating", this.isEating());
        tag.putBoolean("Alert", this.isAlert());
        tag.putBoolean("Hunting", this.isHunting());
        tag.putBoolean("Patrolling", this.isPatrolling());
        tag.putBoolean("Following", this.isFollowing());
        tag.putBoolean("Guarding", this.isGuarding());
        tag.putBoolean("Stationery", this.isStationery());
        tag.putInt("radius", this.getPatrolRadius());
        tag.putInt("sex", this.getSex());
        tag.putInt("baseHealth", this.getBaseHealth());
        tag.putFloat("XpP", this.experienceProgress);
        tag.putInt("XpLevel", this.getExpLvl());
        tag.putInt("XpTotal", this.totalExperience);
        tag.putString("food1", entityData.get(FOOD1));
        tag.putString("food2", entityData.get(FOOD2));
        tag.putInt("food1_amt", entityData.get(FOOD1_AMT));
        tag.putInt("food2_amt", entityData.get(FOOD2_AMT));
        if (this.getPatrolPos().isPresent()) {
            int[] patrolPos = {this.getPatrolPos().get().getX(), this.getPatrolPos().get().getY(), this.getPatrolPos().get().getZ()};
            tag.putIntArray("patrol_pos", patrolPos);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setSkinIndex(tag.getInt("skin"));
        this.setEating(tag.getBoolean("Eating"));
        this.setAlert(tag.getBoolean("Alert"));
        this.setHunting(tag.getBoolean("Hunting"));
        this.setPatrolling(tag.getBoolean("Patrolling"));
        this.setFollowing(tag.getBoolean("Following"));
        this.setGuarding(tag.getBoolean("Guarding"));
        this.setStationery(tag.getBoolean("Stationery"));
        this.setPatrolRadius(tag.getInt("radius"));
        this.setSex(tag.getInt("sex"));
        this.experienceProgress = tag.getFloat("XpP");
        this.totalExperience = tag.getInt("XpTotal");
        this.setExpLvl(tag.getInt("XpLevel"));
        syncExpProgress();
        entityData.set(FOOD1, tag.getString("food1"));
        entityData.set(FOOD2, tag.getString("food2"));
        entityData.set(FOOD1_AMT, tag.getInt("food1_amt"));
        entityData.set(FOOD2_AMT, tag.getInt("food2_amt"));
        foodRequirements.clear();
        ResourceLocation id1 = ResourceLocation.parse(entityData.get(FOOD1));
        ResourceLocation id2 = ResourceLocation.parse(entityData.get(FOOD2));
        foodRequirements.put(BuiltInRegistries.ITEM.get(id1), entityData.get(FOOD1_AMT));
        foodRequirements.put(BuiltInRegistries.ITEM.get(id2), entityData.get(FOOD2_AMT));
        if (tag.getInt("baseHealth") == 0) {
            this.setBaseHealth(ModConfig.safeGet(ModConfig.BASE_HEALTH));
        } else {
            this.setBaseHealth(tag.getInt("baseHealth"));
        }
        if (tag.contains("Inventory", 9)) {
            this.inventory.fromTag(tag.getList("Inventory", 10), this.registryAccess());
        }
        if (tag.contains("patrol_pos")) {
            int[] positions = tag.getIntArray("patrol_pos");
            setPatrolPos(new BlockPos(positions[0], positions[1], positions[2]));
        }
        if (tag.contains("radius")) {
            patrolGoal = new PatrolGoal(this, 60, tag.getInt("radius"));
            moveBackGoal = new MoveBackToPatrolGoal(this, tag.getInt("radius"));
            this.goalSelector.addGoal(3, moveBackGoal);
            this.goalSelector.addGoal(3, patrolGoal);
        }
        this.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        checkArmor();
    }

    /* ---------- Spawning ---------- */

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob parent) {
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
        if (!this.level().isClientSide()) {
            checkArmor();
            if (this.tickCount % 10 == 0) {
                    checkStats();
                    LivingEntity target = this.getTarget();
                    if (target != null && !target.isAlive()) {
                    this.setTarget(null);
                    }
                }
        }
        super.tick();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn) {
        int baseHealth = ModConfig.safeGet(ModConfig.BASE_HEALTH) + CompanionData.getHealthModifier();
        modifyMaxHealth(baseHealth - 20, "companion base health", true);
        this.setHealth(this.getMaxHealth());
        setBaseHealth(baseHealth);
        setSex(this.random.nextInt(2));
        setSkinIndex(this.random.nextInt(CompanionData.skins[getSex()].length));
        setCustomName(Component.literal(CompanionData.getRandomName(getSex())));
        setPatrolPos(this.blockPosition());
        setPatrolling(true);
        setPatrolRadius(15);
        patrolGoal = new PatrolGoal(this, 60, getPatrolRadius());
        moveBackGoal = new MoveBackToPatrolGoal(this, getPatrolRadius());
        this.goalSelector.addGoal(3, moveBackGoal);
        this.goalSelector.addGoal(3, patrolGoal);
        assignFoodRequirements();

        if (ModConfig.safeGet(ModConfig.SPAWN_ARMOR)) {
            for (int i = 0; i < 4; i++) {
                EquipmentSlot armorType = EquipmentSlot.values()[i + 2]; // FEET..HEAD
                ItemStack itemstack = CompanionData.getSpawnArmor(armorType);
                if (!itemstack.isEmpty()) {
                    this.inventory.setItem(i, itemstack);
                }
            }
            checkArmor();
        }
        return super.finalizeSpawn(level, difficulty, reason, spawnDataIn);
    }

    /* ---------- Orders & actions ---------- */

    public void cycleOrders() {
        if (isFollowing()) {
            setPatrolling(true);
            setFollowing(false);
            setGuarding(false);
            setPatrolPos(blockPosition());
        } else if (isPatrolling()) {
            setPatrolling(false);
            setFollowing(false);
            setGuarding(true);
            setPatrolPos(blockPosition());
        } else {
            setPatrolling(false);
            setFollowing(true);
            setGuarding(false);
        }
    }

    public void toggleAlert() {
        setAlert(!isAlert());
    }

    public void toggleHunting() {
        setHunting(!isHunting());
    }

    public void toggleStationery() {
        setStationery(!isStationery());
        if (!isStationery()) {
            this.getNavigation().stop();
        }
    }

    public void release() {
        this.setTame(false, true);
        this.setOwnerUUID(null);
        setFollowing(false);
        setAlert(false);
        setHunting(false);
        setPatrolPos(this.blockPosition());
        setPatrolling(true);
        setStationery(false);
        setPatrolRadius(15);
        assignFoodRequirements();
        if (this.isOrderedToSit()) {
            this.setOrderedToSit(false);
        }
    }

    /* ---------- Experience ---------- */

    public void giveExperiencePoints(int points) {
        this.experienceProgress += (float) points / (float) this.getXpNeededForNextLevel();
        this.totalExperience = Mth.clamp(this.totalExperience + points, 0, Integer.MAX_VALUE);
        syncExpProgress();

        while (this.experienceProgress < 0.0F) {
            float f = this.experienceProgress * (float) this.getXpNeededForNextLevel();
            if (this.getExpLvl() > 0) {
                this.giveExperienceLevels(-1);
                this.experienceProgress = 1.0F + f / (float) this.getXpNeededForNextLevel();
            } else {
                this.giveExperienceLevels(-1);
                this.experienceProgress = 0.0F;
            }
        }

        while (this.experienceProgress >= 1.0F) {
            this.experienceProgress = (this.experienceProgress - 1.0F) * (float) this.getXpNeededForNextLevel();
            this.giveExperienceLevels(1);
            this.experienceProgress /= (float) this.getXpNeededForNextLevel();
        }
        syncExpProgress();
    }

    public void giveExperienceLevels(int levels) {
        setExpLvl(getExpLvl() + levels);
        if (getExpLvl() < 0) {
            setExpLvl(0);
            this.experienceProgress = 0.0F;
            this.totalExperience = 0;
        }
        syncExpProgress();
        if (levels > 0 && this.getExpLvl() % 5 == 0 && (float) this.lastLevelUpTime < (float) this.tickCount - 100.0F) {
            this.lastLevelUpTime = this.tickCount;
        }
    }

    public int getXpNeededForNextLevel() {
        if (this.getExpLvl() >= 30) {
            return 112 + (this.getExpLvl() - 30) * 9;
        } else {
            return this.getExpLvl() >= 15 ? 37 + (this.getExpLvl() - 15) * 5 : 7 + this.getExpLvl() * 2;
        }
    }

    public void modifyMaxHealth(int change, String name, boolean permanent) {
        AttributeInstance attribute = this.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null) return;
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, name.replace(" ", "_"));
        attribute.removeModifier(id);
        AttributeModifier modifier = new AttributeModifier(id, change, AttributeModifier.Operation.ADD_VALUE);
        if (permanent) {
            attribute.addPermanentModifier(modifier);
        } else {
            attribute.addTransientModifier(modifier);
        }
    }

    public void checkStats() {
        if ((int) this.getMaxHealth() != getBaseHealth() + (getExpLvl() / 3)) {
            if (getExpLvl() / 3 != 0) {
                modifyMaxHealth(getExpLvl() / 3, "companion level health", false);
            }
        }
    }

    private void syncExpProgress() {
        if (!this.level().isClientSide) {
            this.entityData.set(EXP_PROGRESS, this.experienceProgress);
        }
    }

    /* ---------- Combat & equipment ---------- */

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() == this.getOwner() && !ModConfig.safeGet(ModConfig.FRIENDLY_FIRE_PLAYER)) {
            return false;
        }
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FALL) && !ModConfig.safeGet(ModConfig.FALL_DAMAGE)) {
            return false;
        }
        hurtArmor(source, amount);
        return super.hurt(source, amount);
    }

    public void hurtArmor(DamageSource source, float amount) {
        if (!(amount <= 0.0F)) {
            amount /= 4.0F;
            if (amount < 1.0F) amount = 1.0F;

            for (ItemStack itemstack : this.getArmorSlots()) {
                if (itemstack.getItem() instanceof ArmorItem armorItem) {
                    itemstack.hurtAndBreak((int) amount, this, armorItem.getEquipmentSlot());
                }
            }
        }
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemstack = this.inventory.getItem(i);
            if (!itemstack.isEmpty()) {
                this.spawnAtLocation(itemstack);
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        ItemStack itemstack = this.getMainHandItem();
        if (!this.level().isClientSide && !itemstack.isEmpty() && entity instanceof LivingEntity) {
            itemstack.hurtAndBreak(1, this, EquipmentSlot.MAINHAND);
            if (this.getMainHandItem().isEmpty() && this.isTame() && this.getOwner() != null) {
                Component broken = Component.literal("My weapon broke!");
                this.getOwner().sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(), broken));
            }
        }
        return super.doHurtTarget(entity);
    }

    public void checkArmor() {
        ItemStack head = this.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = this.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = this.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = this.getItemBySlot(EquipmentSlot.FEET);
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemstack = this.inventory.getItem(i);
            if (itemstack.getItem() instanceof ArmorItem armorItem) {
                switch (armorItem.getEquipmentSlot()) {
                    case HEAD -> {
                        if (head.isEmpty() || CompanionData.isBetterArmor(itemstack, head)) setItemSlot(EquipmentSlot.HEAD, itemstack);
                    }
                    case CHEST -> {
                        if (chest.isEmpty() || CompanionData.isBetterArmor(itemstack, chest)) setItemSlot(EquipmentSlot.CHEST, itemstack);
                    }
                    case LEGS -> {
                        if (legs.isEmpty() || CompanionData.isBetterArmor(itemstack, legs)) setItemSlot(EquipmentSlot.LEGS, itemstack);
                    }
                    case FEET -> {
                        if (feet.isEmpty() || CompanionData.isBetterArmor(itemstack, feet)) setItemSlot(EquipmentSlot.FEET, itemstack);
                    }
                }
            }
        }
    }

    public void checkWeapon() {
        // base class intentionally does nothing; subclasses choose weapons
    }

    /* ---------- Network-driven flag setters ---------- */
    public void applyFlag(String flag, boolean value) {
        switch (flag) {
            case "follow" -> setFollowing(value);
            case "patrol" -> {
                setPatrolling(value);
                setFollowing(!value);
                setGuarding(false);
                if (value) setPatrolPos(blockPosition());
            }
            case "guard" -> {
                setGuarding(value);
                setPatrolling(false);
                setFollowing(!value);
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
}
