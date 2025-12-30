package com.majorbonghits.moderncompanions.item;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.entity.Beastmaster;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Teleports all living companions (and Beastmaster pets) owned by the player to the player's location.
 */
public class SummoningWandItem extends Item {
    private static final int SEARCH_RADIUS = 256; // chunks-wide sweep to catch far companions in the same dimension

    public SummoningWandItem(Properties properties) {
        super(properties.stacksTo(1)
                .durability(256)
                .rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server)) {
            return InteractionResultHolder.success(stack);
        }

        Vec3 targetPos = player.position();
        List<AbstractHumanCompanionEntity> recalled = recallCompanions(server, player, targetPos);

        if (!recalled.isEmpty()) {
            // Play a teleport cue at the player for feedback.
            server.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(slot));
            player.getCooldowns().addCooldown(this, 10); // small spam guard
        }

        return InteractionResultHolder.sidedSuccess(stack, server.isClientSide());
    }

    private List<AbstractHumanCompanionEntity> recallCompanions(ServerLevel server, Player player, Vec3 target) {
        List<AbstractHumanCompanionEntity> moved = new ArrayList<>();
        AABB searchBox = new AABB(BlockPos.containing(target)).inflate(SEARCH_RADIUS);

        for (AbstractHumanCompanionEntity companion : server.getEntitiesOfClass(AbstractHumanCompanionEntity.class, searchBox,
                c -> c.isAlive() && c.isTame() && player.getUUID().equals(c.getOwnerUUID()))) {
            Optional<Vec3> safe = findSafeSpot(server, target, companion);
            safe.ifPresent(pos -> {
                companion.teleportTo(pos.x(), pos.y(), pos.z());
                companion.getNavigation().stop();
                moved.add(companion);
                if (companion instanceof Beastmaster beastmaster) {
                    teleportPetIfPresent(server, beastmaster, pos);
                }
            });
        }

        return moved;
    }

    private void teleportPetIfPresent(ServerLevel server, Beastmaster beastmaster, Vec3 ownerPos) {
        LivingEntity pet = beastmaster.getPetEntity(server);
        if (pet != null && pet.isAlive()) {
            findSafeSpot(server, ownerPos, pet).ifPresent(pos -> {
                pet.teleportTo(pos.x(), pos.y(), pos.z());
                if (pet instanceof net.minecraft.world.entity.PathfinderMob mob) {
                    mob.getNavigation().stop();
                }
            });
        }
    }

    private Optional<Vec3> findSafeSpot(ServerLevel level, Vec3 center, LivingEntity entity) {
        BlockPos base = BlockPos.containing(center);
        for (int i = 0; i < 12; i++) {
            int dx = level.random.nextInt(5) - 2;
            int dz = level.random.nextInt(5) - 2;
            BlockPos candidate = base.offset(dx, 0, dz);
            if (isTeleportFriendly(level, candidate, entity)) {
                return Optional.of(Vec3.atCenterOf(candidate));
            }
        }
        // Fallback to exact center if nothing else fit.
        BlockPos centerPos = BlockPos.containing(center);
        if (isTeleportFriendly(level, centerPos, entity)) {
            return Optional.of(Vec3.atCenterOf(centerPos));
        }
        return Optional.empty();
    }

    private boolean isTeleportFriendly(ServerLevel level, BlockPos pos, LivingEntity entity) {
        return level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above())
                && level.noCollision(entity, entity.getBoundingBox().move(
                pos.getX() - entity.getX(),
                pos.getY() - entity.getY(),
                pos.getZ() - entity.getZ()));
    }
}
