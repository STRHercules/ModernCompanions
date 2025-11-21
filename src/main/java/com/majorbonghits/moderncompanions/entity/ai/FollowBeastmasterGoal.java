package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.Beastmaster;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Simple follow goal that makes any Beastmaster pet stick to its master,
 * even if the mob type does not support taming/following by default.
 */
public class FollowBeastmasterGoal extends Goal {
    private final Mob pet;
    private final Beastmaster master;
    private final PathNavigation navigation;
    private final double speedModifier;
    private final float stopDistance;
    private final float startDistance;
    private int timeToRecalcPath;
    private int postCombatHoldTicks;

    public FollowBeastmasterGoal(Mob pet, Beastmaster master, double speedModifier, float startDistance, float stopDistance) {
        this.pet = pet;
        this.master = master;
        this.navigation = pet.getNavigation();
        this.speedModifier = speedModifier;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!master.isAlive()) return false;
        double dist = this.pet.distanceToSqr(master);
        return dist > (double) (startDistance * startDistance);
    }

    @Override
    public boolean canContinueToUse() {
        if (!master.isAlive()) return false;
        double dist = this.pet.distanceToSqr(master);
        return dist > (double) (stopDistance * stopDistance) && !this.navigation.isDone();
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void stop() {
        this.navigation.stop();
    }

    @Override
    public void tick() {
        // If the pet is in combat or just left combat, pause follow/teleport to avoid rubber-banding.
        if (isActivelyAttacking()) {
            postCombatHoldTicks = 30; // ~1.5s grace to allow multiple attacks
            return;
        }
        if (postCombatHoldTicks > 0) {
            postCombatHoldTicks--;
            return;
        }

        this.pet.getLookControl().setLookAt(master, 10.0F, this.pet.getMaxHeadXRot());

        if (--this.timeToRecalcPath > 0) return;
        this.timeToRecalcPath = 10;

        double dist = this.pet.distanceToSqr(master);
        if (dist >= 144.0D) {
            tryTeleportCloseToOwner();
            return;
        }

        this.navigation.moveTo(master, this.speedModifier);
    }

    private boolean isActivelyAttacking() {
        return this.pet.getTarget() != null && this.pet.getTarget().isAlive();
    }

    private void tryTeleportCloseToOwner() {
        BlockPos masterPos = master.blockPosition();
        for (int i = 0; i < 10; ++i) {
            int dx = this.randomBetween(-3, 3);
            int dz = this.randomBetween(-3, 3);
            BlockPos pos = masterPos.offset(dx, 0, dz);
            if (this.isTeleportFriendly(pos)) {
                this.pet.moveTo(Vec3.atBottomCenterOf(pos));
                this.navigation.stop();
                return;
            }
        }
    }

    private boolean isTeleportFriendly(BlockPos pos) {
        return this.pet.level().isEmptyBlock(pos)
                && this.pet.level().isEmptyBlock(pos.above())
                && this.pet.level().noCollision(this.pet, this.pet.getBoundingBox().move(pos.getX() - this.pet.getX(), pos.getY() - this.pet.getY(), pos.getZ() - this.pet.getZ()));
    }

    private int randomBetween(int min, int max) {
        return this.pet.getRandom().nextInt(max - min + 1) + min;
    }
}
