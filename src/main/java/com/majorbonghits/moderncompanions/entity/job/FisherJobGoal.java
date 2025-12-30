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
    private static final int SEARCH_COOLDOWN = 60; // throttle rescans to ease server load
    private static final int RESCAN_STUCK_TICKS = 80;
    private static final int MAX_CANDIDATES_PER_SCAN = 256;

    private final AbstractHumanCompanionEntity companion;
    private final int searchRadius;
    private final boolean enabled;
    private final Random random = new Random();
    private BlockPos waterSpot;
    private BlockPos standPos;
    private int fishCooldown;
    private int searchCooldown;
    private int idleNavTicks;

    public FisherJobGoal(AbstractHumanCompanionEntity companion, int searchRadius, boolean enabled) {
        this.companion = companion;
        this.searchRadius = Math.max(4, searchRadius);
        this.enabled = enabled;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!isActiveJob()) return false;
        if (waterSpot != null && standPos != null && isWater(waterSpot) && isStandValid(standPos)) {
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
        if (companion.getNavigation().isDone() && dist > 1.5D) {
            idleNavTicks++;
            if (idleNavTicks > RESCAN_STUCK_TICKS) {
                if (findWaterAndStand()) {
                    moveToStand();
                    idleNavTicks = 0;
                    return;
                }
                idleNavTicks = 0;
            } else {
                moveToStand();
            }
        } else {
            idleNavTicks = 0;
        }
        if (fishCooldown-- > 0) return;
        fishCooldown = FISH_INTERVAL + random.nextInt(40);
        faceWater();
        reelIn();
    }

    private void faceWater() {
        if (waterSpot == null) return;
        Vec3 look = Vec3.atCenterOf(waterSpot);
        companion.getLookControl().setLookAt(look.x, look.y, look.z);
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
        BlockPos origin = companion.isPatrolling() && companion.getPatrolPos().isPresent()
                ? companion.getPatrolPos().get()
                : companion.blockPosition();
        Level level = companion.level();
        int radius = Math.min(48, Math.max(searchRadius, companion.getPatrolRadius()));
        int evaluated = 0;

        for (int r = 0; r <= radius && evaluated < MAX_CANDIDATES_PER_SCAN; r++) {
            for (int dy = -2; dy <= 2 && evaluated < MAX_CANDIDATES_PER_SCAN; dy++) {
                for (int dx = -r; dx <= r && evaluated < MAX_CANDIDATES_PER_SCAN; dx++) {
                    for (int dz = -r; dz <= r && evaluated < MAX_CANDIDATES_PER_SCAN; dz++) {
                        if (Math.abs(dx) != r && Math.abs(dz) != r) continue; // perimeter only
                        BlockPos candidate = origin.offset(dx, dy, dz);
                        evaluated++;
                        if (!isStandValid(candidate)) continue;
                        BlockPos water = adjacentWater(level, candidate);
                        if (water == null) continue;
                        // Path to the solid floor; navigator handles stepping onto the block.
                        var path = companion.getNavigation().createPath(candidate, 0);
                        if (path == null) continue;
                        standPos = candidate.immutable();
                        waterSpot = water.immutable();
                        return true;
                    }
                }
            }
        }
        waterSpot = null;
        standPos = null;
        return false;
    }

    private boolean isWater(BlockPos pos) {
        return isWater(companion.level(), pos);
    }

    private boolean isWater(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.is(Blocks.WATER) || state.getFluidState().isSource() && state.getFluidState().is(net.minecraft.tags.FluidTags.WATER);
    }

    private BlockPos adjacentWater(Level level, BlockPos stand) {
        // Favor horizontal adjacency first, then vertical within reach.
        for (BlockPos side : BlockPos.betweenClosed(stand.offset(-1, 0, -1), stand.offset(1, 0, 1))) {
            if (isWater(level, side)) return side.immutable();
        }
        for (BlockPos side : BlockPos.betweenClosed(stand.offset(-1, -1, -1), stand.offset(1, 1, 1))) {
            if (isWater(level, side)) return side.immutable();
        }
        return null;
    }

    private boolean isStandValid(BlockPos pos) {
        Level level = companion.level();
        var state = level.getBlockState(pos);
        // Need a solid block to stand on, with headroom.
        var feet = level.getBlockState(pos.above());
        return state.isSolid()
                && !state.liquid()
                && feet.getFluidState().isEmpty()
                && feet.getCollisionShape(level, pos.above()).isEmpty();
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
