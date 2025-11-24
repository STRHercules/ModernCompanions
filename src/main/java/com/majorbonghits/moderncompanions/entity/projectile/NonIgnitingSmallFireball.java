package com.majorbonghits.moderncompanions.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Small fireball variant that deals fireball damage but does not ignite blocks or entities.
 */
public class NonIgnitingSmallFireball extends SmallFireball {
    public NonIgnitingSmallFireball(EntityType<? extends NonIgnitingSmallFireball> type, Level level) {
        super(type, level);
    }

    public NonIgnitingSmallFireball(Level level, LivingEntity owner, Vec3 power) {
        super(level, owner, power);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide) {
            explodeEffect();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity hit = result.getEntity();
        Level lvl = this.level();
        if (lvl instanceof ServerLevel server) {
            Entity owner = this.getOwner();
            DamageSource source = this.damageSources().fireball(this, owner);
            hit.hurt(source, 10.0F);
        }
        if (!lvl.isClientSide) {
            explodeEffect();
        }
    }

    private void explodeEffect() {
        // Visual/sound explosion, no block damage or fire spread.
        this.level().explode(null, this.getX(), this.getY(), this.getZ(), 1.0F, false, ExplosionInteraction.NONE);
        this.discard();
    }
}
