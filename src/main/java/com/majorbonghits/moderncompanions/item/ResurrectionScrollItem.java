package com.majorbonghits.moderncompanions.item;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.item.component.CustomData;
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
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return data.copyTag().getBoolean(ACTIVATED_TAG);
    }

    public static void setActivated(ItemStack stack, boolean activated) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(ACTIVATED_TAG, activated));
        if (activated) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        } else {
            stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        }
    }

    public static boolean hasCompanionData(ItemStack stack) {
        CustomData stored = stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
        return !stored.isEmpty() && stored.copyTag().contains(ENTITY_TYPE_TAG);
    }

    @Nullable
    private static ResourceLocation readEntityId(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
        if (data.isEmpty()) {
            return null;
        }
        return ResourceLocation.tryParse(data.copyTag().getString(ENTITY_TYPE_TAG));
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

        CustomData.set(DataComponents.ENTITY_DATA, stack, entityData);
        CustomData.update(DataComponents.CUSTOM_DATA, stack,
                tag -> tag.putString(COMPANION_NAME_TAG, companion.getName().getString()));

        stack.set(DataComponents.ITEM_NAME, Component.translatable(
                "item.modern_companions.resurrection_scroll.named", companion.getName()));
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
            stack.consume(1, player);
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

        CustomData storedData = stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
        CompoundTag tag = storedData.copyTag();

        companion.load(tag);
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
        if (stack.has(DataComponents.ITEM_NAME)) {
            return stack.get(DataComponents.ITEM_NAME);
        }
        return Component.translatable("item.modern_companions.resurrection_scroll");
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
            TooltipFlag flag) {
        String name = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
                .getString(COMPANION_NAME_TAG);
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
