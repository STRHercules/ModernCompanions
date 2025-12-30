package com.majorbonghits.moderncompanions.entity;

import net.minecraft.nbt.CompoundTag;

import com.majorbonghits.moderncompanions.Constants;
import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.entity.magic.AbstractMageCompanion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Random;

/**
 * Dark caster that weakens foes with wither skulls and can raise short-lived minions.
 */
public class Necromancer extends AbstractMageCompanion {
    private static final int HEAVY_COOLDOWN_TICKS = 180;
    private final Random rng = new Random();

    public Necromancer(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.isAlliedTo(target)) return;
        if (isOwnerInDanger(target, 3.0F)) return;

        Vec3 dir = target.position().add(0, Math.min(0.1F, target.getBbHeight() * 0.08F), 0)
                .subtract(this.position()).normalize().scale(0.55D);
        var skull = com.majorbonghits.moderncompanions.core.ModEntityTypes.SOFT_WITHER_SKULL.get().create(server);
        if (skull == null) return;
        skull.setNoGravity(true);
        skull.shoot(dir.x, dir.y, dir.z, 1.25F, 0.0F);
        skull.setPos(this.getX(), this.getY() + 1.1F, this.getZ()); // lower muzzle to avoid face
        skull.setOwner(this);
        server.addFreshEntity(skull);
        swingCast();
    }

    @Override
    public boolean tryHeavyAttack(LivingEntity target, float distanceFactor) {
        if (heavyCooldown > 0) return false;
        if (!(this.level() instanceof ServerLevel server)) return false;

        List<SummonedWitherSkeleton> owned = server.getEntitiesOfClass(SummonedWitherSkeleton.class,
                this.getBoundingBox().inflate(128.0D, 64.0D, 128.0D),
                skel -> skel.getSummoner() == this && skel.isAlive());

        // Only summon if no existing summons are alive
        if (!owned.isEmpty()) return false;

        int toSummon = 1 + rng.nextInt(3);
        int spawned = 0;
        int lifetimeSeconds = 60 + rng.nextInt(121); // 60–180 seconds

        for (int i = 0; i < toSummon; i++) {
            SummonedWitherSkeleton skeleton = com.majorbonghits.moderncompanions.core.ModEntityTypes.SUMMONED_WITHER_SKELETON.get().create(server);
            if (skeleton == null) continue;
            double offsetX = (rng.nextDouble() - 0.5D) * 2.5D;
            double offsetZ = (rng.nextDouble() - 0.5D) * 2.5D;
            skeleton.moveTo(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ, this.getYRot(), 0.0F);
            skeleton.configureSummon(this, lifetimeSeconds);
            if (!skeleton.isFriendlyTo(target)) {
                skeleton.setTarget(target);
            }
            server.addFreshEntity(skeleton);
            spawned++;
        }

        if (spawned == 0) return false;

        heavyCooldown = HEAVY_COOLDOWN_TICKS;
        swingCast();
        return true;
    }

    @Override
    public int getLightIntervalTicks() {
        return 20;
    }

    @Override
    public float getPreferredRange() {
        return 20.0F;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data, @Nullable CompoundTag dataTag) {
        if (ModConfig.safeGet(ModConfig.SPAWN_WEAPON)) {
            this.inventory.setItem(4, new ItemStack(BuiltInRegistries.ITEM.get(Constants.id("stone_dagger"))));
            this.inventory.setItem(5, Items.BONE.getDefaultInstance());
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        return super.finalizeSpawn(level, difficulty, reason, data, dataTag);
    }

    @Override
    public int getHeavyRecoveryTicks() {
        return HEAVY_COOLDOWN_TICKS;
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        if (other instanceof SummonedWitherSkeleton summoned && summoned.getSummoner() == this) {
            return true;
        }
        return super.isAlliedTo(other);
    }
}
