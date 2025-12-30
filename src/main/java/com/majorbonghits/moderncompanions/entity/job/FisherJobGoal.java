package com.majorbonghits.moderncompanions.entity.job;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.entity.projectile.CompanionFishingHook;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.EnumSet;
import java.util.Random;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Fisher job: stand near a water block and periodically generate simple fishing
 * loot (cod/salmon) with a short delay. Keeps the cadence low to avoid item
 * spam.
 */
public class FisherJobGoal extends Goal {
    private static final int FISH_INTERVAL = 120;
    private static final int SEARCH_COOLDOWN = 10; // quicker reacquire when nearby water exists
    private static final int RESCAN_STUCK_TICKS = 80;
    private static final int MAX_RINGS_PER_SCAN = 8;
    private static final int MIN_WATER_ADJACENT = 2;
    private static final int RECAST_DELAY = 20;
    private static final int CAST_ATTEMPTS = 12;
    private static final int CAST_MIN_DIST = 5;
    private static final int CAST_MAX_DIST = 7;
    private static final int CAST_SIDE_SPREAD = 2;

    private final AbstractHumanCompanionEntity companion;
    private final int searchRadius;
    private final boolean enabled;
    private final Random random = new Random();
    private BlockPos waterSpot;
    private BlockPos standPos;
    private int fishCooldown;
    private int searchCooldown;
    private int idleNavTicks;
    private BlockPos scanOrigin;
    private int scanRing;
    private int recastCooldown;
    private CompanionFishingHook activeHook;

    public FisherJobGoal(AbstractHumanCompanionEntity companion, int searchRadius, boolean enabled) {
        this.companion = companion;
        this.searchRadius = Math.max(4, searchRadius);
        this.enabled = enabled;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!isActiveJob()) return false;
        if (waterSpot != null && standPos != null && isFishableWater(waterSpot) && isStandValid(standPos)) {
            return true;
        }
        if (searchCooldown-- > 0) return false;
        boolean found = findWaterAndStand();
        searchCooldown = SEARCH_COOLDOWN;
        if (found) moveToStand();
        return found;
    }

    @Override
    public boolean canContinueToUse() {
        return isActiveJob() && waterSpot != null && standPos != null && isFishableWater(waterSpot) && isStandValid(standPos);
    }

    @Override
    public void start() {
        moveToStand();
    }

    @Override
    public void stop() {
        waterSpot = null;
        standPos = null;
        clearLine();
        companion.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (waterSpot == null || standPos == null) return;
        if (!isFishableWater(waterSpot) || !isStandValid(standPos)) {
            if (!findWaterAndStand()) {
                clearLine();
                return;
            }
            clearLine();
            moveToStand();
            return;
        }
        double dist = companion.distanceToSqr(Vec3.atCenterOf(standPos));
        if (dist > 9.0D) {
            clearLine();
            moveToStand();
            return;
        }
        if (companion.getNavigation().isDone() && dist > 1.5D) {
            idleNavTicks++;
            if (idleNavTicks > RESCAN_STUCK_TICKS) {
                if (findWaterAndStand()) {
                    clearLine();
                    moveToStand();
                    idleNavTicks = 0;
                    return;
                }
                idleNavTicks = 0;
            } else {
                clearLine();
                moveToStand();
            }
        } else {
            idleNavTicks = 0;
        }
        if (dist <= 2.25D && !hasLineCast()) {
            // Only cast once we are close enough to the shoreline stand position.
            if (recastCooldown-- <= 0) {
                castLine(selectCastTarget());
                recastCooldown = 0;
            }
        } else if (recastCooldown > 0) {
            recastCooldown--;
        }
        if (!hasLineCast()) return;
        if (fishCooldown-- > 0) return;
        if (!activeHook.isLineInWater()) {
            // Do not reel in unless the line is actually in water.
            clearLine();
            recastCooldown = RECAST_DELAY;
            return;
        }
        fishCooldown = FISH_INTERVAL + random.nextInt(40);
        faceWater();
        companion.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
        reelIn();
        clearLine();
        recastCooldown = RECAST_DELAY;
    }

    private void faceWater() {
        if (waterSpot == null) return;
        Vec3 look = Vec3.atCenterOf(waterSpot);
        companion.getLookControl().setLookAt(look.x, look.y, look.z);
    }

    private void reelIn() {
        if (!(companion.level() instanceof ServerLevel server)) return;
        ItemStack catchStack = rollFishingLoot();
        if (!catchStack.isEmpty()) {
            ItemStack leftover = companion.getInventory().addItem(catchStack);
            if (!leftover.isEmpty()) {
                companion.spawnAtLocation(leftover);
            }
            companion.incrementFishCaughtSession();
        }
    }

    private ItemStack rollFishingLoot() {
        if (!(companion.level() instanceof ServerLevel server)) {
            return ItemStack.EMPTY;
        }
        LootTable lootTable = server.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING);
        if (lootTable == null) {
            return new ItemStack(Items.COD);
        }
        double luck = companion.getAttributes().hasAttribute(Attributes.LUCK)
                ? companion.getAttributeValue(Attributes.LUCK)
                : 0.0D;
        LootParams params = new LootParams.Builder(server)
                .withParameter(LootContextParams.ORIGIN, companion.position())
                .withParameter(LootContextParams.TOOL, companion.getMainHandItem())
                .withLuck((float) luck)
                .create(LootContextParamSets.FISHING);
        var list = lootTable.getRandomItems(params);
        if (!list.isEmpty()) {
            return list.get(random.nextInt(list.size())).copy();
        }
        return new ItemStack(Items.COD);
    }

    private boolean findWaterAndStand() {
        BlockPos origin = companion.blockPosition();
        BlockPos patrolCenter = companion.isPatrolling() && companion.getPatrolPos().isPresent()
                ? companion.getPatrolPos().get()
                : origin;
        Level level = companion.level();
        int radius = Math.min(48, Math.max(searchRadius, companion.getPatrolRadius()));
        int radiusSq = radius * radius;
        if (scanOrigin == null || !scanOrigin.equals(origin)) {
            // Reset the progressive scan when the companion moves.
            scanOrigin = origin.immutable();
            scanRing = 0;
        }
        int ringsScanned = 0;

        for (int r = scanRing; r <= radius; r++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.abs(dx) != r && Math.abs(dz) != r) continue; // perimeter only
                        BlockPos candidate = origin.offset(dx, dy, dz);
                        if (patrolCenter.distSqr(candidate) > radiusSq) continue;
                        BlockPos stand = candidate.above();
                        if (!isStandValid(stand)) continue;
                        BlockPos water = adjacentFishableWater(level, candidate);
                        if (water == null) continue;
                        // Path to the stand air block so navigation targets the actual feet position.
                        var path = companion.getNavigation().createPath(stand, 0);
                        if (path == null) continue;
                        standPos = stand.immutable();
                        waterSpot = water.immutable();
                        scanRing = 0;
                        return true;
                    }
                }
            }
            ringsScanned++;
            if (ringsScanned >= MAX_RINGS_PER_SCAN) {
                // Continue the outward search next time so we eventually reach farther water.
                scanRing = r + 1;
                return false;
            }
        }
        scanRing = 0;
        waterSpot = null;
        standPos = null;
        return false;
    }

    private boolean hasLineCast() {
        return activeHook != null && !activeHook.isRemoved();
    }

    private void castLine(BlockPos target) {
        if (!(companion.level() instanceof ServerLevel server)) return;
        if (target == null) return;
        clearLine();
        companion.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
        // Spawn a visible bobber tied to the companion so clients render the line.
        CompanionFishingHook hook = new CompanionFishingHook(server, companion, target);
        Vec3 bobberPos = Vec3.atCenterOf(target).add(0.0D, 0.1D, 0.0D);
        hook.setPos(bobberPos.x, bobberPos.y, bobberPos.z);
        hook.setDeltaMovement(Vec3.ZERO);
        hook.setNoGravity(true);
        server.addFreshEntity(hook);
        activeHook = hook;
        fishCooldown = FISH_INTERVAL + random.nextInt(40);
        server.playSound(null, companion.blockPosition(), SoundEvents.FISHING_BOBBER_THROW,
                SoundSource.PLAYERS, 0.6F, 1.0F);
    }

    private BlockPos selectCastTarget() {
        if (waterSpot == null) return null;
        Vec3 look = companion.getLookAngle();
        Vec3 flatLook = new Vec3(look.x, 0.0D, look.z);
        if (flatLook.lengthSqr() < 1.0E-4D) {
            flatLook = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            flatLook = flatLook.normalize();
        }
        Vec3 right = new Vec3(-flatLook.z, 0.0D, flatLook.x);
        Vec3 origin = companion.position();
        Level level = companion.level();

        for (int i = 0; i < CAST_ATTEMPTS; i++) {
            int dist = CAST_MIN_DIST + random.nextInt(CAST_MAX_DIST - CAST_MIN_DIST + 1);
            int side = random.nextInt(CAST_SIDE_SPREAD * 2 + 1) - CAST_SIDE_SPREAD;
            Vec3 target = origin.add(flatLook.scale(dist)).add(right.scale(side));
            BlockPos base = BlockPos.containing(target.x, standPos != null ? standPos.getY() - 1 : target.y, target.z);
            for (int dy = -2; dy <= 2; dy++) {
                BlockPos candidate = base.offset(0, dy, 0);
                if (isFishableWater(level, candidate)) {
                    return candidate.immutable();
                }
            }
        }
        return waterSpot;
    }

    private void clearLine() {
        if (activeHook != null && !activeHook.isRemoved()) {
            activeHook.discard();
        }
        activeHook = null;
    }

    private boolean isWater(BlockPos pos) {
        return isWater(companion.level(), pos);
    }

    private boolean isWater(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.is(Blocks.WATER) || state.getFluidState().isSource() && state.getFluidState().is(net.minecraft.tags.FluidTags.WATER);
    }

    private boolean isFishableWater(BlockPos pos) {
        return isFishableWater(companion.level(), pos);
    }

    private boolean isFishableWater(Level level, BlockPos pos) {
        if (!isWater(level, pos)) return false;
        // Require surface water (air above) so the bobber sits at the water surface.
        var above = level.getBlockState(pos.above());
        if (!above.getFluidState().isEmpty() || !above.getCollisionShape(level, pos.above()).isEmpty()) {
            return false;
        }
        // Require nearby water neighbors so companions avoid 1-block puddles.
        int adjacent = 0;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (isWater(level, pos.relative(dir))) {
                adjacent++;
                if (adjacent >= MIN_WATER_ADJACENT) {
                    return true;
                }
            }
        }
        return false;
    }

    private BlockPos adjacentFishableWater(Level level, BlockPos standFloor) {
        // Favor horizontal adjacency first, then vertical within reach.
        for (BlockPos side : BlockPos.betweenClosed(standFloor.offset(-1, 0, -1), standFloor.offset(1, 0, 1))) {
            if (isFishableWater(level, side)) return side.immutable();
        }
        for (BlockPos side : BlockPos.betweenClosed(standFloor.offset(-1, -1, -1), standFloor.offset(1, 1, 1))) {
            if (isFishableWater(level, side)) return side.immutable();
        }
        return null;
    }

    private boolean isStandValid(BlockPos pos) {
        Level level = companion.level();
        BlockPos floor = pos.below();
        var floorState = level.getBlockState(floor);
        var feet = level.getBlockState(pos);
        // Need a solid floor with an open stand space for navigation.
        return floorState.isSolid()
                && !floorState.liquid()
                && feet.getFluidState().isEmpty()
                && feet.getCollisionShape(level, pos).isEmpty();
    }

    private void moveToStand() {
        if (standPos == null) return;
        companion.getNavigation().moveTo(standPos.getX() + 0.5D, standPos.getY(), standPos.getZ() + 0.5D, 1.0D);
    }

    private boolean isActiveJob() {
        if (!enabled) return false;
        if (companion.getJob() != CompanionJob.FISHER) return false;
        if (!companion.isPatrolling()) return false;
        if (companion.isOrderedToSit() || !companion.isTame()) return false;
        if (!hasRod()) return false;
        return isWithinWorkArea(Math.max(8.0D, searchRadius + 2)) || isWithinPatrolArea();
    }

    private boolean hasRod() {
        return hasTool(stack -> stack.is(Items.FISHING_ROD));
    }

    private boolean hasTool(java.util.function.Predicate<ItemStack> matcher) {
        if (matcher.test(companion.getMainHandItem())) return true;
        for (int i = 0; i < companion.getInventory().getContainerSize(); i++) {
            if (matcher.test(companion.getInventory().getItem(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean isWithinWorkArea(double ownerMax) {
        LivingEntity owner = companion.getOwner();
        if (owner != null && companion.distanceToSqr(owner) <= ownerMax * ownerMax) {
            return true;
        }
        return false;
    }

    private boolean isWithinPatrolArea() {
        return companion.isPatrolling() && companion.getPatrolPos().isPresent()
                && companion.getPatrolPos().get().distSqr(companion.blockPosition()) <= Math.pow(Math.max(8.0D, companion.getPatrolRadius() + 4), 2);
    }
}
