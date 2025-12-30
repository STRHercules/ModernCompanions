package com.majorbonghits.moderncompanions.entity;

import net.minecraft.nbt.CompoundTag;

import com.majorbonghits.moderncompanions.core.ModConfig;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.majorbonghits.moderncompanions.item.GlaiveItem;
import com.majorbonghits.moderncompanions.item.SpearItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import javax.annotation.Nullable;

/**
 * Burst specialist that calls lightning through their trident on a cooldown.
 */
public class Stormcaller extends AbstractHumanCompanionEntity {
    private int lightningCooldown;

    public Stormcaller(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.05D, true));
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            checkTrident();
            if (lightningCooldown > 0) lightningCooldown--;
        }
        super.tick();
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean hit = super.doHurtTarget(entity);
        if (!this.level().isClientSide() && entity instanceof LivingEntity living) {
            tryCallLightning(living);
        }
        return hit;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data, @Nullable CompoundTag dataTag) {
        if (ModConfig.safeGet(ModConfig.SPAWN_WEAPON)) {
            this.inventory.setItem(4, Items.TRIDENT.getDefaultInstance());
            checkTrident();
        }
        return super.finalizeSpawn(level, difficulty, reason, data, dataTag);
    }

    private void checkTrident() {
        ItemStack hand = this.getItemBySlot(EquipmentSlot.MAINHAND);
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.is(Items.TRIDENT) || stack.getItem() instanceof SpearItem || stack.getItem() instanceof GlaiveItem) {
                if (hand.isEmpty()) {
                    this.setItemSlot(EquipmentSlot.MAINHAND, stack);
                    hand = stack;
                }
            }
        }
    }

    private void tryCallLightning(LivingEntity target) {
        if (lightningCooldown > 0) return;
        if (!(this.level() instanceof ServerLevel server)) return;
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(server);
        if (bolt == null) return;
        bolt.moveTo(target.getX(), target.getY(), target.getZ());
        if (this.getOwner() instanceof ServerPlayer sp) bolt.setCause(sp);
        server.addFreshEntity(bolt);
        float bonus = server.isThundering() || server.isRaining() ? 4.0F : 0.0F;
        target.hurt(server.damageSources().lightningBolt(), 6.0F + bonus);
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0, true, true));
        lightningCooldown = server.isThundering() ? 100 : 160;
    }
}
