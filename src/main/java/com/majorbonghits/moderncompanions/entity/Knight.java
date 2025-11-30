package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.core.ModEntityTypes;
import com.majorbonghits.moderncompanions.core.TagsInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.majorbonghits.moderncompanions.item.*;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public class Knight extends AbstractHumanCompanionEntity {

    public Knight(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
    }

    private boolean isPreferredWeapon(ItemStack stack) {
        return stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof DaggerItem
                || stack.getItem() instanceof ClubItem
                || stack.getItem() instanceof SpearItem
                || stack.getItem() instanceof GlaiveItem
                || stack.getItem() instanceof QuarterstaffItem
                || stack.getItem() instanceof HammerItem
                || stack.is(TagsInit.Items.SWORDS); // allow legacy tag fallback
    }

    public void checkSword() {
        ItemStack hand = this.getItemBySlot(EquipmentSlot.MAINHAND);
        ItemStack preferred = ItemStack.EMPTY;
        ItemStack fallback = !hand.isEmpty() && inventoryContains(hand.getItem()) && !isShieldItem(hand) ? hand : ItemStack.EMPTY;
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemstack = this.inventory.getItem(i);
            if (itemstack.isEmpty()) continue;
            if (preferred.isEmpty() && isPreferredWeapon(itemstack)) {
                preferred = itemstack;
            }
            if (fallback.isEmpty() && !isShieldItem(itemstack)) {
                fallback = itemstack;
            }
        }
        ItemStack desired = !preferred.isEmpty() ? preferred : fallback;
        if (!ItemStack.isSameItemSameComponents(hand, desired)) {
            this.setItemSlot(EquipmentSlot.MAINHAND, desired);
        }
        setPreferredWeaponBonus(!preferred.isEmpty() && ItemStack.isSameItemSameComponents(desired, preferred));
    }

    private boolean inventoryContains(net.minecraft.world.item.Item item) {
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            if (this.inventory.getItem(i).getItem() == item) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        checkSword();
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            checkSword();
        }
        super.tick();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data) {
        if (ModConfig.safeGet(ModConfig.SPAWN_WEAPON)) {
            ItemStack itemstack = getSpawnSword();
            if (!itemstack.isEmpty()) {
                this.inventory.setItem(4, itemstack);
                checkSword();
            }
        }
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    public ItemStack getSpawnSword() {
        float materialFloat = this.random.nextFloat();
        if (materialFloat < 0.5F) {
            return Items.WOODEN_SWORD.getDefaultInstance();
        } else if (materialFloat < 0.90F) {
            return Items.STONE_SWORD.getDefaultInstance();
        } else {
            return Items.IRON_SWORD.getDefaultInstance();
        }
    }
}
