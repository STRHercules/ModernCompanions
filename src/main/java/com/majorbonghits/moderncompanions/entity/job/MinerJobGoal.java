package com.majorbonghits.moderncompanions.entity.job;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.core.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * Safe miner that only targets exposed ores/stone within a tight radius and
 * avoids digging deep tunnels. Intended as a controlled placeholder until a
 * fuller mining routine is built.
 */
public class MinerJobGoal extends Goal {
    private static final int SEARCH_COOLDOWN = 40;
    private static final int BREAK_COOLDOWN = 12;
    private static final TagKey<Block>[] ORE_TAGS = new TagKey[]{
            BlockTags.COAL_ORES,
            BlockTags.COPPER_ORES,
            BlockTags.IRON_ORES,
            BlockTags.GOLD_ORES,
            BlockTags.REDSTONE_ORES,
            BlockTags.LAPIS_ORES,
            BlockTags.DIAMOND_ORES,
            BlockTags.EMERALD_ORES
    };

    private final AbstractHumanCompanionEntity companion;
    private final int searchRadius;
    private final boolean enabled;
    private final Set<Block> allowBlocks = new HashSet<>();
    private final Set<Block> denyBlocks = new HashSet<>();
    private BlockPos target;
    private int searchCooldown;
    private int breakCooldown;

    public MinerJobGoal(AbstractHumanCompanionEntity companion, int searchRadius, boolean enabled) {
        this.companion = companion;
        this.searchRadius = Math.max(4, searchRadius);
        this.enabled = enabled;
        loadConfigBlockLists();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!isActiveJob()) return false;
        if (searchCooldown > 0) {
            searchCooldown--;
            return false;
        }
        target = findTargetBlock();
        searchCooldown = SEARCH_COOLDOWN;
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return isActiveJob() && target != null && isMineable(target);
    }

    @Override
    public void start() {
        moveToTarget();
    }

    @Override
    public void stop() {
        target = null;
        companion.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (target == null) return;
        if (!isMineable(target)) {
            target = findAdjacentMineable(target);
            if (target == null) return;
        }
        if (companion.distanceToSqr(Vec3.atCenterOf(target)) > 6.0D) {
            moveToTarget();
            return;
        }
        if (breakCooldown-- > 0) return;
        breakCooldown = BREAK_COOLDOWN;
        mine(target);
        target = findAdjacentMineable(target);
    }

    private BlockPos findTargetBlock() {
        BlockPos origin = companion.blockPosition();
        Level level = companion.level();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-searchRadius, -3, -searchRadius),
                origin.offset(searchRadius, 3, searchRadius))) {
            if (!isMineable(pos)) continue;
            double dist = pos.distSqr(origin);
            if (dist < bestDist) {
                best = pos.immutable();
                bestDist = dist;
            }
        }
        return best;
    }

    private BlockPos findAdjacentMineable(BlockPos anchor) {
        for (BlockPos pos : BlockPos.betweenClosed(anchor.offset(-1, -1, -1), anchor.offset(1, 1, 1))) {
            if (isMineable(pos)) return pos.immutable();
        }
        return null;
    }

    private boolean isMineable(BlockPos pos) {
        Level level = companion.level();
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return false;
        boolean isOre = false;
        for (TagKey<Block> tag : ORE_TAGS) {
            if (state.is(tag)) {
                isOre = true;
                break;
            }
        }
        if (!isOre && !state.is(BlockTags.STONE_ORE_REPLACEABLES) && !state.is(BlockTags.BASE_STONE_OVERWORLD)) {
            return false;
        }
        Block block = state.getBlock();
        if (!allowBlocks.isEmpty() && !allowBlocks.contains(block)) {
            return false;
        }
        if (denyBlocks.contains(block)) {
            return false;
        }
        // Only mine exposed blocks with air nearby to avoid deep shafts.
        boolean hasAir = false;
        for (BlockPos air : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (level.getBlockState(air).isAir()) {
                hasAir = true;
                break;
            }
        }
        if (!hasAir) return false;
        LivingEntity owner = companion.getOwner();
        if (owner != null && pos.getY() < owner.getBlockY() - 6) {
            return false; // avoid digging deep below the owner
        }
        return true;
    }

    private void mine(BlockPos pos) {
        if (!(companion.level() instanceof ServerLevel server)) return;
        if (!server.hasChunkAt(pos)) return;
        BlockState state = server.getBlockState(pos);
        if (!isMineable(pos)) return;
        var blockEntity = server.getBlockEntity(pos);
        var drops = Block.getDrops(state, server, pos, blockEntity, companion, companion.getMainHandItem());
        server.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        for (ItemStack drop : drops) {
            ItemStack leftover = companion.getInventory().addItem(drop.copy());
            if (!leftover.isEmpty()) {
                companion.spawnAtLocation(leftover);
            }
        }
    }

    private void moveToTarget() {
        if (target == null) return;
        companion.getNavigation().moveTo(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, 1.05D);
    }

    private boolean isActiveJob() {
        if (!enabled) return false;
        if (companion.getJob() != CompanionJob.MINER) return false;
        if (companion.isOrderedToSit() || !companion.isTame()) return false;
        if (!hasPickaxe()) return false;
        return isWithinWorkArea(16.0D);
    }

    private void loadConfigBlockLists() {
        List<? extends String> allowIds = ModConfig.safeGet(ModConfig.JOB_MINER_ALLOW_BLOCKS);
        List<? extends String> denyIds = ModConfig.safeGet(ModConfig.JOB_MINER_DENY_BLOCKS);
        resolveBlocksInto(allowIds, allowBlocks);
        resolveBlocksInto(denyIds, denyBlocks);
    }

    private void resolveBlocksInto(List<? extends String> ids, Set<Block> targetSet) {
        if (ids == null) return;
        for (String raw : ids) {
            try {
                ResourceLocation id = ResourceLocation.parse(raw);
                Block block = BuiltInRegistries.BLOCK.get(id);
                if (block != null && !block.defaultBlockState().isAir()) {
                    targetSet.add(block);
                }
            } catch (Exception ignored) {
                // Ignore malformed ids to keep the miner running.
            }
        }
    }

    private boolean hasPickaxe() {
        return hasTool(stack -> stack.getItem() instanceof PickaxeItem);
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
        return companion.isPatrolling() && companion.getPatrolPos().isPresent()
                && companion.getPatrolPos().get().distSqr(companion.blockPosition()) <= Math.pow(Math.max(8.0D, companion.getPatrolRadius() + 4), 2);
    }
}
