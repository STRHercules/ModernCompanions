package com.majorbonghits.moderncompanions.entity.job;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

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
    private static final int SEARCH_COOLDOWN = 20;

    private final AbstractHumanCompanionEntity companion;
    private final int searchRadius;
    private final boolean enabled;
    private final Random random = new Random();
    private BlockPos waterSpot;
    private BlockPos standPos;
    private int fishCooldown;
    private int searchCooldown;

    public FisherJobGoal(AbstractHumanCompanionEntity companion, int searchRadius, boolean enabled) {
        this.companion = companion;
        this.searchRadius = Math.max(4, searchRadius);
        this.enabled = enabled;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!isActiveJob()) return false;
        if (searchCooldown > 0) {
            searchCooldown--;
            return false;
        }
        boolean found = findWaterAndStand();
        if (found) {
            moveToStand();
        }
        searchCooldown = SEARCH_COOLDOWN;
        return found;
    }

    @Override
    public boolean canContinueToUse() {
        return isActiveJob() && waterSpot != null && standPos != null && isWater(waterSpot) && isStandValid(standPos);
    }

    @Override
    public void start() {
        moveToStand();
    }

    @Override
    public void stop() {
        waterSpot = null;
        standPos = null;
        companion.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (waterSpot == null || standPos == null) return;
        if (!isWater(waterSpot) || !isStandValid(standPos)) {
            if (!findWaterAndStand()) {
                return;
            }
            moveToStand();
            return;
        }
        double dist = companion.distanceToSqr(Vec3.atCenterOf(standPos));
        if (dist > 9.0D) {
            moveToStand();
            return;
        }
        // If pathing failed for a while, rescan.
        if (companion.getNavigation().isDone() && dist > 1.5D) {
            if (findWaterAndStand()) {
                moveToStand();
                return;
            }
        }
        if (fishCooldown-- > 0) return;
        fishCooldown = FISH_INTERVAL + random.nextInt(40);
        reelIn();
    }

    private void reelIn() {
        if (!(companion.level() instanceof ServerLevel server)) return;
        companion.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true); // animate the cast/haul
        ItemStack catchStack = rollFishingLoot();
        if (!catchStack.isEmpty()) {
            ItemStack leftover = companion.getInventory().addItem(catchStack);
            if (!leftover.isEmpty()) {
                companion.spawnAtLocation(leftover);
            }
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
        BlockPos origin = companion.isPatrolling() && companion.getPatrolPos().isPresent()
                ? companion.getPatrolPos().get()
                : companion.blockPosition();
        Level level = companion.level();
        BlockPos bestStand = null;
        BlockPos bestWater = null;
        double bestDist = Double.MAX_VALUE;

        int radius = Math.min(128, Math.max(searchRadius, companion.getPatrolRadius()));
        for (BlockPos candidate : BlockPos.betweenClosed(origin.offset(-radius, -6, -radius),
                origin.offset(radius, 6, radius))) {
            if (!isStandValid(candidate)) continue;
            BlockPos water = adjacentWater(candidate);
            if (water == null) continue;
            if (companion.getNavigation().createPath(candidate, 0) == null) continue;
            double dist = candidate.distSqr(origin);
            if (dist < bestDist) {
                bestDist = dist;
                bestStand = candidate.immutable();
                bestWater = water.immutable();
            }
        }

        if (bestStand != null && bestWater != null) {
            standPos = bestStand;
            waterSpot = bestWater;
            return true;
        }

        waterSpot = null;
        standPos = null;
        return false;
    }

    private boolean isWater(BlockPos pos) {
        var state = companion.level().getBlockState(pos);
        return state.is(Blocks.WATER) || state.getFluidState().isSource() && state.getFluidState().is(net.minecraft.tags.FluidTags.WATER);
    }

    private BlockPos adjacentWater(BlockPos stand) {
        // Favor horizontal adjacency first, then vertical within reach.
        for (BlockPos side : BlockPos.betweenClosed(stand.offset(-1, 0, -1), stand.offset(1, 0, 1))) {
            if (isWater(side)) return side.immutable();
        }
        for (BlockPos side : BlockPos.betweenClosed(stand.offset(-1, -1, -1), stand.offset(1, 1, 1))) {
            if (isWater(side)) return side.immutable();
        }
        return null;
    }

    private boolean isStandValid(BlockPos pos) {
        Level level = companion.level();
        var state = level.getBlockState(pos);
        // Need a solid block to stand on, with headroom.
        return state.isSolid()
                && !state.liquid()
                && level.getBlockState(pos.above()).isAir();
    }

    private void moveToStand() {
        if (standPos == null) return;
        companion.getNavigation().moveTo(standPos.getX() + 0.5D, standPos.getY() + 1.0D, standPos.getZ() + 0.5D, 1.0D);
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
