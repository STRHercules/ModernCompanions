package com.majorbonghits.moderncompanions.item;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.entity.job.CompanionJob;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.Container;

import java.util.UUID;

/**
 * Wand that links a working companion to a delivery chest.
 * Right-click a companion to select it, then shift-right-click a chest to bind.
 */
public class AssignmentWandItem extends Item {
    private static final String TAG_SELECTED = "SelectedCompanion";
    private static final String TAG_SELECTED_NAME = "SelectedCompanionName";
    private static final String PLAYER_TAG = "modern_companions_assignment";

    public AssignmentWandItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof AbstractHumanCompanionEntity companion)) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!companion.isOwnedBy(player)) {
            player.sendSystemMessage(Component.translatable("message.modern_companions.assignment_wand.not_owner"));
            return InteractionResult.CONSUME;
        }
        writeSelectionToStack(stack, companion.getUUID(), companion.getDisplayName().getString());
        writeSelectionToPlayer(player, companion.getUUID(), companion.getDisplayName().getString());
        player.sendSystemMessage(Component.translatable("message.modern_companions.assignment_wand.selected", companion.getDisplayName()));
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();
        if (!(context.getLevel() instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        Selection selection = readSelection(stack, player);
        if (selection == null) {
            player.sendSystemMessage(Component.translatable("message.modern_companions.assignment_wand.no_selection"));
            return InteractionResult.CONSUME;
        }

        UUID companionId = selection.id();
        var entity = serverLevel.getEntity(companionId);
        if (!(entity instanceof AbstractHumanCompanionEntity companion)) {
            player.sendSystemMessage(Component.translatable("message.modern_companions.assignment_wand.missing_companion"));
            clearSelection(stack, player);
            return InteractionResult.CONSUME;
        }
        if (!companion.isOwnedBy(player)) {
            player.sendSystemMessage(Component.translatable("message.modern_companions.assignment_wand.not_owner"));
            clearSelection(stack, player);
            return InteractionResult.CONSUME;
        }
        if (companion.getJob() == CompanionJob.NONE) {
            player.sendSystemMessage(Component.translatable("message.modern_companions.assignment_wand.needs_job"));
            clearSelection(stack, player);
            return InteractionResult.CONSUME;
        }

        BlockPos pos = context.getClickedPos();
        BlockEntity be = serverLevel.getBlockEntity(pos);
        if (!(be instanceof Container)) {
            player.sendSystemMessage(Component.translatable("message.modern_companions.assignment_wand.invalid_target"));
            return InteractionResult.CONSUME;
        }

        companion.assignDeliveryChest(serverLevel, pos);
        player.sendSystemMessage(Component.translatable("message.modern_companions.assignment_wand.bound",
                companion.getDisplayName(), Component.literal(pos.getX() + ", " + pos.getY() + ", " + pos.getZ())));
        clearSelection(stack, player);
        return InteractionResult.CONSUME;
    }

    private void clearSelection(ItemStack stack, Player player) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove(TAG_SELECTED);
            tag.remove(TAG_SELECTED_NAME);
        });
        if (player != null) {
            CompoundTag persistent = player.getPersistentData();
            persistent.remove(PLAYER_TAG);
        }
    }

    private void writeSelectionToStack(ItemStack stack, UUID id, String name) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putUUID(TAG_SELECTED, id);
            tag.putString(TAG_SELECTED_NAME, name);
        });
    }

    private void writeSelectionToPlayer(Player player, UUID id, String name) {
        CompoundTag payload = new CompoundTag();
        payload.putUUID(TAG_SELECTED, id);
        payload.putString(TAG_SELECTED_NAME, name);
        player.getPersistentData().put(PLAYER_TAG, payload);
    }

    private Selection readSelection(ItemStack stack, Player player) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.hasUUID(TAG_SELECTED)) {
            return new Selection(tag.getUUID(TAG_SELECTED), tag.getString(TAG_SELECTED_NAME));
        }
        if (player != null) {
            CompoundTag payload = player.getPersistentData().getCompound(PLAYER_TAG);
            if (payload.hasUUID(TAG_SELECTED)) {
                return new Selection(payload.getUUID(TAG_SELECTED), payload.getString(TAG_SELECTED_NAME));
            }
        }
        return null;
    }

    private record Selection(UUID id, String name) {}
}
