package com.majorbonghits.moderncompanions.entity;

import net.minecraft.nbt.CompoundTag;

import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import com.majorbonghits.moderncompanions.entity.magic.AbstractMageCompanion;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import javax.annotation.Nullable;

import java.util.List;

/**
 * Ranged caster focused on precision lightning with a multi-target burst.
 */
public class LightningMage extends AbstractMageCompanion {
    private static final int HEAVY_COOLDOWN_TICKS = 150;

    public LightningMage(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.isAlliedTo(target) || (this.getOwner() != null && this.getOwner() == target)) return;
        if (isOwnerInDanger(target, 3.0F)) return;
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(server);
        if (bolt == null) return;
        bolt.moveTo(target.getX(), target.getY(), target.getZ());
        bolt.setVisualOnly(true); // prevent fire spread; damage applied manually below
        server.addFreshEntity(bolt);
        target.hurt(server.damageSources().lightningBolt(), magicDamage(5.0F));
        swingCast();
    }

    @Override
    public boolean tryHeavyAttack(LivingEntity target, float distanceFactor) {
        if (heavyCooldown > 0) return false;
        if (!(this.level() instanceof ServerLevel server)) return false;

        List<LivingEntity> hostiles = server.getEntitiesOfClass(LivingEntity.class,
                target.getBoundingBox().inflate(5.0D),
                other -> other != this && other != target && !this.isAlliedTo(other) && other.isAlive());

        if (hostiles.size() < 1) { // need at least two total including main target
            return false;
        }

        // Include the primary target at the front of the list
        hostiles.add(0, target);
        int boltsCast = 0;
        for (LivingEntity victim : hostiles) {
            if (victim == null || !victim.isAlive()) continue;
            if (this.isAlliedTo(victim) || (this.getOwner() != null && this.getOwner() == victim)) continue;
            if (this.isOwnerInDanger(victim, 3.0F)) continue;
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(server);
            if (bolt == null) continue;
            bolt.moveTo(victim.getX(), victim.getY(), victim.getZ());
            bolt.setVisualOnly(true); // visuals only, damage applied below
            server.addFreshEntity(bolt);
            float bonus = server.isThundering() ? 2.0F : 0.0F;
            victim.hurt(server.damageSources().lightningBolt(), magicDamage(6.0F + bonus));
            boltsCast++;
            if (boltsCast >= 4) break; // cap heavy burst
        }

        if (boltsCast == 0) return false;

        heavyCooldown = HEAVY_COOLDOWN_TICKS;
        swingCast();
        return true;
    }

    @Override
    public int getLightIntervalTicks() {
        return 26;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data, @Nullable CompoundTag dataTag) {
        if (ModConfig.safeGet(ModConfig.SPAWN_WEAPON)) {
            this.inventory.setItem(4, Items.STICK.getDefaultInstance()); // staff visual
            this.inventory.setItem(5, new ItemStack(BuiltInRegistries.ITEM.get(Constants.id("iron_dagger"))));
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        return super.finalizeSpawn(level, difficulty, reason, data, dataTag);
    }

    @Override
    public int getHeavyRecoveryTicks() {
        return HEAVY_COOLDOWN_TICKS;
    }
}
