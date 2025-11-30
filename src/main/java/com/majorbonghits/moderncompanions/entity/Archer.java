package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.entity.ai.ArcherRangedBowAttackGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public class Archer extends AbstractHumanCompanionEntity implements RangedAttackMob {

    public Archer(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.goalSelector.addGoal(2, new ArcherRangedBowAttackGoal<>(this, 1.0D, 20, 20.0F));
    }

    public void checkBow() {
        ItemStack hand = this.getItemBySlot(EquipmentSlot.MAINHAND);
        ItemStack preferred = ItemStack.EMPTY;
        ItemStack fallback = !hand.isEmpty() && !isShieldItem(hand) ? hand : ItemStack.EMPTY;
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.isEmpty()) continue;
            if (preferred.isEmpty() && stack.getItem() instanceof BowItem) {
                preferred = stack;
            }
            if (fallback.isEmpty() && !isShieldItem(stack)) {
                fallback = stack;
            }
        }
        ItemStack desired = !preferred.isEmpty() ? preferred : fallback;
        if (!ItemStack.isSameItemSameComponents(hand, desired)) {
            this.setItemSlot(EquipmentSlot.MAINHAND, desired);
        }
        setPreferredWeaponBonus(!preferred.isEmpty() && ItemStack.isSameItemSameComponents(desired, preferred));
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            checkBow();
        }
        super.tick();
    }

    @Override
    public void performRangedAttack(net.minecraft.world.entity.LivingEntity target, float distanceFactor) {
        ItemStack bow = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof BowItem));
        ItemStack projectile = this.getProjectile(bow);
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, projectile, distanceFactor, bow);
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333333333333333D) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + distance * 0.20F, dz, 1.6F, (float) (this.level().getDifficulty().getId() * 3));
        this.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(arrow);
        if (!this.level().isClientSide) {
            this.getMainHandItem().hurtAndBreak(1, this, EquipmentSlot.MAINHAND);
            if (this.getMainHandItem().isEmpty() && this.isTame() && this.getOwner() != null) {
                MutableComponent broken = Component.literal("My bow broke!");
                this.getOwner().sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(), broken));
            }
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        checkBow();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data) {
        if (ModConfig.safeGet(ModConfig.SPAWN_WEAPON)) {
            this.inventory.setItem(4, Items.BOW.getDefaultInstance());
            checkBow();
        }
        return super.finalizeSpawn(level, difficulty, reason, data);
    }
}
