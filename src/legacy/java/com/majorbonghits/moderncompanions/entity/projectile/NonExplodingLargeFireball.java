package com.majorbonghits.moderncompanions.entity.projectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Ghast-style fireball that skips explosions and block fire spread.
 */
public class NonExplodingLargeFireball extends LargeFireball {
    public NonExplodingLargeFireball(EntityType<? extends NonExplodingLargeFireball> type, Level level) {
        super(type, level);
    }

    public NonExplodingLargeFireball(Level level, LivingEntity owner, Vec3 power, int explosionPower) {
        super(level, owner, power.x, power.y, power.z, explosionPower);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity hit = result.getEntity();
        if (this.level() instanceof ServerLevel) {
            Entity owner = this.getOwner();
            DamageSource src = this.damageSources().fireball(this, owner);
            hit.hurt(src, 20.0F);
            hit.setRemainingFireTicks(100); // ignite target only
        }
        if (!this.level().isClientSide) explodeEffect();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide) explodeEffect();
    }

    @Override
    protected void onHit(HitResult result) {
        if (!this.level().isClientSide) explodeEffect();
    }

    private void explodeEffect() {
        this.level().explode(null, this.getX(), this.getY(), this.getZ(), 1.4F, false, ExplosionInteraction.NONE);
        this.discard();
    }
}
