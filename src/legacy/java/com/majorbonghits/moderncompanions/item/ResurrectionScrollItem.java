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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Item that stores a fallen companion's full entity data and can respawn them once activated.
 */
public class ResurrectionScrollItem extends Item {
    private static final String ACTIVATED_TAG = "Activated";
    private static final String COMPANION_NAME_TAG = "CompanionName";
    private static final String ENTITY_TYPE_TAG = "id";
    private static final String ENTITY_DATA_TAG = "CompanionEntityData";

    public ResurrectionScrollItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack createFromCompanion(AbstractHumanCompanionEntity companion, Item scrollItem) {
        ItemStack stack = new ItemStack(scrollItem);
        storeCompanionData(stack, companion);
        setActivated(stack, false);
        return stack;
    }

    public static boolean isActivated(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(ACTIVATED_TAG);
    }

    public static void setActivated(ItemStack stack, boolean activated) {
        stack.getOrCreateTag().putBoolean(ACTIVATED_TAG, activated);
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

        // Reset lethal state so the revived companion spawns healthy at the new location.
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

        stack.setHoverName(Component.translatable(
                "item.modern_companions.resurrection_scroll.named", companion.getName()));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isActivated(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (tryActivate(stack, context.getPlayer())) {
            return InteractionResult.CONSUME;
        }

        if (!validateReady(stack, (ServerLevel) level, context.getPlayer())) {
            return InteractionResult.FAIL;
        }

        Vec3 spawnPos = resolveSpawnPosition(context.getClickLocation(), context.getClickedFace(), (ServerLevel) level);

        Entity revived = revive((ServerLevel) level, stack, spawnPos, context.getPlayer());
        if (revived != null) {
            stack.shrink(1);
            level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, BlockPos.containing(spawnPos));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.success(stack);
        }

        if (tryActivate(stack, player)) {
            return InteractionResultHolder.consume(stack);
        }

        if (!validateReady(stack, serverLevel, player)) {
            return InteractionResultHolder.fail(stack);
        }

        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }

        BlockPos pos = hit.getBlockPos();
        if (!(level.getBlockState(pos).getBlock() instanceof LiquidBlock)) {
            return InteractionResultHolder.pass(stack);
        }

        Vec3 spawnPos = resolveSpawnPosition(hit.getLocation(), hit.getDirection(), serverLevel);
        Entity revived = revive(serverLevel, stack, spawnPos, player);
        if (revived != null) {
            stack.shrink(1);
            player.level().gameEvent(player, GameEvent.ENTITY_PLACE, BlockPos.containing(revived.position()));
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    private boolean validateReady(ItemStack stack, ServerLevel level, @Nullable Player player) {
        if (!isActivated(stack)) {
            if (player != null) {
                player.displayClientMessage(
                        Component.translatable("tooltip.modern_companions.resurrection_scroll.inactive"), true);
            }
            return false;
        }
        if (!hasCompanionData(stack)) {
            if (player != null) {
                player.displayClientMessage(
                        Component.translatable("tooltip.modern_companions.resurrection_scroll.empty"), true);
            }
            return false;
        }
        return true;
    }

    private Vec3 resolveSpawnPosition(Vec3 hitLocation, Direction face, ServerLevel level) {
        // Start at the exact hit location, nudge slightly outward along the clicked face to avoid clipping.
        Vec3 nudge = Vec3.atLowerCornerOf(face.getNormal()).scale(0.02);
        Vec3 pos = hitLocation.add(nudge);

        double x = pos.x();
        double y = pos.y();
        double z = pos.z();

        double minY = level.getMinBuildHeight() + 0.05D;
        double maxY = level.getMaxBuildHeight() - 0.1D;
        y = Math.min(Math.max(y, minY), maxY);

        return new Vec3(x, y, z);
    }

    private boolean tryActivate(ItemStack stack, @Nullable Player player) {
        if (player == null || isActivated(stack)) return false;

        ItemStack offhand = player.getOffhandItem();
        if (!offhand.is(Items.NETHER_STAR)) {
            player.displayClientMessage(
                    Component.translatable("tooltip.modern_companions.resurrection_scroll.needs_nether_star"), true);
            return false;
        }

        offhand.shrink(1);
        setActivated(stack, true);
        player.displayClientMessage(
                Component.translatable("tooltip.modern_companions.resurrection_scroll.activated"), true);
        return true;
    }

    @Nullable
    private Entity revive(ServerLevel level, ItemStack stack, Vec3 pos, @Nullable Player player) {
        ResourceLocation typeId = readEntityId(stack);
        if (typeId == null) {
            return null;
        }

        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(typeId);
        if (!(type.create(level) instanceof AbstractHumanCompanionEntity companion)) {
            return null; // only companions can be revived
        }

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(ENTITY_DATA_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }
        CompoundTag entityTag = tag.getCompound(ENTITY_DATA_TAG);

        companion.load(entityTag);
        companion.onResurrectedEvent();
        double safeY = Math.max(pos.y(), level.getMinBuildHeight() + 0.01D);
        companion.moveTo(pos.x(), safeY, pos.z(), level.random.nextFloat() * 360.0F, 0.0F);
        companion.setHealth(companion.getMaxHealth());
        companion.setDeltaMovement(Vec3.ZERO);
        companion.setOnGround(true);
        level.addFreshEntity(companion);

        if (player != null) {
            player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
            BlockPos spawnedAt = BlockPos.containing(companion.position());
            player.displayClientMessage(
                    Component.translatable("message.modern_companions.resurrection_scroll.spawned",
                            spawnedAt.getX(), spawnedAt.getY(), spawnedAt.getZ()),
                    true);
        }

        return companion;
    }

    @Override
    public Component getName(ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            return stack.getHoverName();
        }
        return Component.translatable("item.modern_companions.resurrection_scroll");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        String name = tag != null ? tag.getString(COMPANION_NAME_TAG) : "";
        if (!name.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.modern_companions.resurrection_scroll.bound", name));
        }

        if (isActivated(stack)) {
            tooltip.add(Component.translatable("tooltip.modern_companions.resurrection_scroll.active"));
        } else {
            tooltip.add(Component.translatable("tooltip.modern_companions.resurrection_scroll.inactive"));
        }

        if (!hasCompanionData(stack)) {
            tooltip.add(Component.translatable("tooltip.modern_companions.resurrection_scroll.empty"));
        }
    }
}
