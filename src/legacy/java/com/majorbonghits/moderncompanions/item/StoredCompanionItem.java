package com.majorbonghits.moderncompanions.item;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Item that holds a living companion's full entity data so it can be redeployed later.
 */
public class StoredCompanionItem extends Item {
    private static final String COMPANION_NAME_TAG = "CompanionName";
    private static final String ENTITY_TYPE_TAG = "id";
    private static final String ENTITY_DATA_TAG = "CompanionEntityData";
    private static final String GLOW_TAG = "CompanionGlow";

    public StoredCompanionItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack createFromCompanion(AbstractHumanCompanionEntity companion, Item storedItem) {
        ItemStack stack = new ItemStack(storedItem);
        storeCompanionData(stack, companion);
        stack.getOrCreateTag().putBoolean(GLOW_TAG, true); // visually distinct from normal gems
        return stack;
    }

    public static boolean hasCompanionData(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null
                && tag.contains(ENTITY_DATA_TAG, Tag.TAG_COMPOUND)
                && tag.getCompound(ENTITY_DATA_TAG).contains(ENTITY_TYPE_TAG, Tag.TAG_STRING);
    }

    @Nullable
    private static ResourceLocation readEntityId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(ENTITY_DATA_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }
        return ResourceLocation.tryParse(tag.getCompound(ENTITY_DATA_TAG).getString(ENTITY_TYPE_TAG));
    }

    private static void storeCompanionData(ItemStack stack, AbstractHumanCompanionEntity companion) {
        CompoundTag entityData = new CompoundTag();
        companion.saveWithoutId(entityData);
        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(companion.getType());
        entityData.putString(ENTITY_TYPE_TAG, typeId.toString());

        // Reset transient state so the redeployed companion spawns safely.
        entityData.putFloat("Health", companion.getMaxHealth());
        entityData.remove("DeathTime");
        entityData.remove("HurtTime");
        entityData.remove("HurtByTimestamp");
        entityData.remove("Pos");
        entityData.remove("Motion");
        entityData.remove("Rotation");

        CompoundTag tag = stack.getOrCreateTag();
        tag.put(ENTITY_DATA_TAG, entityData);
        tag.putString(COMPANION_NAME_TAG, companion.getName().getString());

        stack.setHoverName(Component.translatable("item.modern_companions.stored_companion.named", companion.getName()));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(GLOW_TAG);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        if (!hasCompanionData(stack)) {
            notifyMissingData(context.getPlayer());
            return InteractionResult.FAIL;
        }

        Vec3 spawnPos = context.getClickLocation().add(Vec3.atLowerCornerOf(context.getClickedFace().getNormal()).scale(0.02));
        Entity placed = placeCompanion(serverLevel, stack, spawnPos, context.getPlayer());
        if (placed != null) {
            stack.shrink(1);
            level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, BlockPos.containing(placed.position()));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.pass(stack);
        }

        if (!hasCompanionData(stack)) {
            notifyMissingData(player);
            return InteractionResultHolder.fail(stack);
        }

        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }

        Vec3 spawnPos = hit.getLocation().add(Vec3.atLowerCornerOf(hit.getDirection().getNormal()).scale(0.02));
        Entity placed = placeCompanion(serverLevel, stack, spawnPos, player);
        if (placed != null) {
            stack.shrink(1);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, BlockPos.containing(placed.position()));
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Nullable
    private Entity placeCompanion(ServerLevel level, ItemStack stack, Vec3 pos, @Nullable Player player) {
        ResourceLocation typeId = readEntityId(stack);
        if (typeId == null) {
            return null;
        }

        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(typeId);
        if (!(type.create(level) instanceof AbstractHumanCompanionEntity companion)) {
            return null; // safeguard against wrong item data
        }

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(ENTITY_DATA_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }
        CompoundTag entityTag = tag.getCompound(ENTITY_DATA_TAG);

        companion.load(entityTag);
        double safeY = Math.max(pos.y(), level.getMinBuildHeight() + 0.01D);
        companion.moveTo(pos.x(), safeY, pos.z(), level.random.nextFloat() * 360.0F, 0.0F);
        companion.setHealth(companion.getMaxHealth());
        companion.setDeltaMovement(Vec3.ZERO);
        companion.setOnGround(true);
        level.addFreshEntity(companion);

        if (player != null) {
            player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
        }

        return companion;
    }

    @Override
    public Component getName(ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            return stack.getHoverName();
        }
        return Component.translatable("item.modern_companions.stored_companion");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        String name = tag != null ? tag.getString(COMPANION_NAME_TAG) : "";
        if (!name.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.modern_companions.stored_companion.bound", name));
        } else {
            tooltip.add(Component.translatable("tooltip.modern_companions.stored_companion.empty"));
        }
    }

    private void notifyMissingData(@Nullable Player player) {
        if (player != null) {
            player.displayClientMessage(
                    Component.translatable("tooltip.modern_companions.stored_companion.empty"), true);
        }
    }
}
