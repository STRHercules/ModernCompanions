package com.majorbonghits.moderncompanions.item;

import com.majorbonghits.moderncompanions.core.ModItems;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Tool that converts an owned companion into a portable stored item (while keeping all NBT).
 */
public class CompanionMoverItem extends Item {
    public CompanionMoverItem(Properties properties) {
        super(properties.stacksTo(1).durability(128));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity,
            InteractionHand hand) {
        if (!(entity instanceof AbstractHumanCompanionEntity companion)) {
            return InteractionResult.PASS;
        }

        Level level = entity.level();
        Vec3 position = entity.position();

        if (!companion.isTame() || !player.getUUID().equals(companion.getOwnerUUID())) {
            spawnParticles(level, position, true);
            playSound(level, position, SoundEvents.VILLAGER_NO);
            if (!level.isClientSide()) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(
                                "tooltip.modern_companions.companion_mover.not_owner"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (level.isClientSide()) {
            spawnParticles(level, position, false);
            playSound(level, position, SoundEvents.ITEM_PICKUP);
            return InteractionResult.SUCCESS;
        }

        ItemStack stored = StoredCompanionItem.createFromCompanion(companion, ModItems.STORED_COMPANION.get());
        boolean added = player.getInventory().add(stored);
        if (!added) {
            ItemEntity drop = new ItemEntity(level, position.x(), position.y() + 0.2D, position.z(), stored);
            drop.setUnlimitedLifetime();
            level.addFreshEntity(drop);
        }

        companion.discard(); // remove from world after data is preserved
        spawnParticles(level, position, false);
        playSound(level, position, SoundEvents.ITEM_PICKUP);

        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        stack.hurtAndBreak(1, player, slot);
        return InteractionResult.CONSUME;
    }

    private static void spawnParticles(Level level, Vec3 position, boolean failedCapture) {
        for (int i = 0; i < 15; i++) {
            double offsetX = level.random.nextDouble() * 0.6D - 0.3D;
            double offsetY = level.random.nextDouble() * 0.6D;
            double offsetZ = level.random.nextDouble() * 0.6D - 0.3D;
            double velocityX = level.random.nextGaussian() * 0.02D;
            double velocityY = level.random.nextGaussian() * (failedCapture ? 0.06D : 0.12D);
            double velocityZ = level.random.nextGaussian() * 0.02D;
            if (level.isClientSide()) {
                level.addParticle(failedCapture ? ParticleTypes.SMOKE : ParticleTypes.POOF,
                        position.x() + offsetX, position.y() + offsetY, position.z() + offsetZ,
                        velocityX, velocityY, velocityZ);
            } else if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.sendParticles(failedCapture ? ParticleTypes.SMOKE : ParticleTypes.POOF,
                        position.x() + offsetX, position.y() + offsetY, position.z() + offsetZ, 1,
                        velocityX, velocityY, velocityZ, 0.0D);
            }
        }
    }

    private static void playSound(Level level, Vec3 position, net.minecraft.sounds.SoundEvent sound) {
        level.playSound(null, position.x(), position.y(), position.z(), sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }
}
