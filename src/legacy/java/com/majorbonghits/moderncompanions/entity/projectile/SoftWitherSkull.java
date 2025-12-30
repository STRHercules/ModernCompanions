package com.majorbonghits.moderncompanions.entity.projectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Wither skull variant that skips block explosions and keeps damage modest.
 */
public class SoftWitherSkull extends WitherSkull {
    public SoftWitherSkull(EntityType<? extends SoftWitherSkull> type, Level level) {
        super(type, level);
    }

    public SoftWitherSkull(Level level, LivingEntity owner, Vec3 power) {
        super(level, owner, power.x, power.y, power.z);
        this.setDangerous(false);
    }

    @Override
    protected void onHit(HitResult result) {
        // Replicate vanilla impact visuals but without block damage.
        if (!this.level().isClientSide) {
            // Spawn sound/particles with no block breaking or knock-on explosion damage.
            this.level().explode(null, this.getX(), this.getY(), this.getZ(), 0.0F, false, Level.ExplosionInteraction.NONE);

            // Apply controlled AoE damage to non-allied entities in a small radius.
            if (this.level() instanceof ServerLevel server) {
                List<LivingEntity> victims = server.getEntitiesOfClass(LivingEntity.class,
                        this.getBoundingBox().inflate(1.5D),
                        LivingEntity::isAlive);
                for (LivingEntity victim : victims) {
                    Entity owner = this.getOwner();
                    if (owner instanceof LivingEntity ownerLiving && victim.isAlliedTo(ownerLiving)) {
                        continue;
                    }
                    DamageSource src = this.damageSources().witherSkull(this, owner);
                    victim.hurt(src, 5.0F);
                }
            }
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity hit = result.getEntity();
        if (this.level() instanceof ServerLevel) {
            Entity owner = this.getOwner();
            // Skip damaging allies/summons of the same owner
            if (owner != null && hit.isAlliedTo(owner)) {
                return;
            }
            DamageSource src = this.damageSources().witherSkull(this, owner);
            hit.hurt(src, 5.0F);
        }
        if (!this.level().isClientSide) this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        // no terrain damage; just vanish without explosion
        if (!this.level().isClientSide) this.discard();
    }
}
