package com.majorbonghits.moderncompanions.entity;

import net.minecraft.nbt.CompoundTag;

import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.entity.magic.AbstractMageCompanion;
import com.majorbonghits.moderncompanions.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;

/**
 * Caster that favors repeated blaze fireballs with a periodic ghast blast.
 */
public class FireMage extends AbstractMageCompanion {
    private static final int HEAVY_COOLDOWN_TICKS = 560;

    public FireMage(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!(this.level() instanceof ServerLevel server)) return;
        if (isOwnerInDanger(target, 2.5F)) return;

        // Calculated strike toward target (no random spread), blaze-like but fire-safe.
        Vec3 dir = new Vec3(
                target.getX() - this.getX(),
                target.getY(0.5D) - this.getEyeY(),
                target.getZ() - this.getZ()).normalize();
        if (dir.lengthSqr() < 1.0E-6D) return;
        var fireball = new com.majorbonghits.moderncompanions.entity.projectile.NonIgnitingSmallFireball(server, this, dir);
        fireball.setPos(this.getX(), this.getEyeY() - 0.2F, this.getZ());
        fireball.shoot(dir.x, dir.y, dir.z, 1.15F, 0.0F); // zero inaccuracy for precision
        fireball.setOwner(this);
        server.addFreshEntity(fireball);
        swingCast();
    }

    @Override
    public boolean tryHeavyAttack(LivingEntity target, float distanceFactor) {
        if (heavyCooldown > 0) return false;
        if (!(this.level() instanceof ServerLevel server)) return false;
        if (isOwnerInDanger(target, 4.0F)) return false;
        // 50% chance to use heavy when off cooldown to keep focus on light attacks
        if (this.random.nextBoolean()) return false;

        // Heavy shot at mid-torso height, slightly slower to keep arc flat
        Vec3 dir = new Vec3(
                target.getX() - this.getX(),
                target.getY(0.35F) - this.getEyeY(),
                target.getZ() - this.getZ()).normalize();
        var fireball = new com.majorbonghits.moderncompanions.entity.projectile.NonExplodingLargeFireball(server, this, dir.scale(0.9D), 1);
        fireball.setPos(this.getX(), this.getEyeY() - 0.1F, this.getZ());
        fireball.shoot(dir.x, dir.y, dir.z, 1.1F, 0.0F);
        fireball.setOwner(this);
        server.addFreshEntity(fireball);

        heavyCooldown = HEAVY_COOLDOWN_TICKS;
        swingCast();
        return true;
    }

    @Override
    public int getLightIntervalTicks() {
        return 32;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data, @Nullable CompoundTag dataTag) {
        if (ModConfig.safeGet(ModConfig.SPAWN_WEAPON)) {
            // Lean into caster visuals while still obeying weapon preference.
            this.inventory.setItem(4, Items.BLAZE_ROD.getDefaultInstance());
            this.inventory.setItem(5, new ItemStack(BuiltInRegistries.ITEM.get(Constants.id("wooden_quarterstaff"))));
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        return super.finalizeSpawn(level, difficulty, reason, data, dataTag);
    }

    @Override
    public int getHeavyRecoveryTicks() {
        return HEAVY_COOLDOWN_TICKS;
    }
}
