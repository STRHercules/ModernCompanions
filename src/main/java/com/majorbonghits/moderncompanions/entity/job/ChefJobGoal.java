package com.majorbonghits.moderncompanions.entity.job;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import java.util.EnumSet;
import java.util.Map;
import java.util.HashMap;

/**
 * Chef job converts raw food in the companion inventory when standing near a
 * heat source (campfire/furnace/smoker). Simplified: items are cooked
 * instantly with a cooldown to avoid block entity mutation.
 */
public class ChefJobGoal extends Goal {
    private static final int COOK_COOLDOWN = 40;

    private static final Map<Item, Item> RAW_TO_COOKED = new HashMap<>();
    static {
        RAW_TO_COOKED.put(Items.BEEF, Items.COOKED_BEEF);
        RAW_TO_COOKED.put(Items.PORKCHOP, Items.COOKED_PORKCHOP);
        RAW_TO_COOKED.put(Items.CHICKEN, Items.COOKED_CHICKEN);
        RAW_TO_COOKED.put(Items.MUTTON, Items.COOKED_MUTTON);
        RAW_TO_COOKED.put(Items.RABBIT, Items.COOKED_RABBIT);
        RAW_TO_COOKED.put(Items.SALMON, Items.COOKED_SALMON);
        RAW_TO_COOKED.put(Items.COD, Items.COOKED_COD);
    }

    private final AbstractHumanCompanionEntity companion;
    private final int searchRadius;
    private final boolean enabled;
    private BlockPos heatSource;
    private int cooldown;

    public ChefJobGoal(AbstractHumanCompanionEntity companion, int searchRadius, boolean enabled) {
        this.companion = companion;
        this.searchRadius = Math.max(3, searchRadius);
        this.enabled = enabled;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!isActiveJob()) return false;
        heatSource = findHeatSource();
        return heatSource != null;
    }

    @Override
    public boolean canContinueToUse() {
        return isActiveJob() && heatSource != null && isHeatSource(heatSource);
    }

    @Override
    public void start() {
        moveToHeat();
    }

    @Override
    public void stop() {
        heatSource = null;
        companion.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (heatSource == null) return;
        if (!isHeatSource(heatSource)) {
            heatSource = findHeatSource();
            if (heatSource == null) return;
            moveToHeat();
            return;
        }
        double dist = companion.distanceToSqr(heatSource.getX() + 0.5D, heatSource.getY() + 0.5D, heatSource.getZ() + 0.5D);
        if (dist > 4.0D) {
            moveToHeat();
            return;
        }
        if (cooldown-- > 0) return;
        cooldown = COOK_COOLDOWN;
        cookOneItem();
    }

    private void cookOneItem() {
        if (!(companion.level() instanceof ServerLevel server)) return;
        var be = server.getBlockEntity(heatSource);
        boolean cooked = false;
        if (be instanceof AbstractFurnaceBlockEntity furnace) {
            cooked = pullCooked(furnace) || cookInFurnace(furnace);
        }
        if (!cooked) {
            cookDirect();
        }
    }

    private boolean cookInFurnace(AbstractFurnaceBlockEntity furnace) {
        if (!hasFuel(furnace)) return false;
        int inputSlot = 0;
        int fuelSlot = 1;
        int outputSlot = 2;

        // Clean finished cooked items first to avoid jammed output.
        ItemStack output = furnace.getItem(outputSlot);
        if (!output.isEmpty() && RAW_TO_COOKED.containsValue(output.getItem())) {
            ItemStack moved = output.copy();
            furnace.setItem(outputSlot, ItemStack.EMPTY);
            ItemStack leftover = companion.getInventory().addItem(moved);
            if (!leftover.isEmpty()) companion.spawnAtLocation(leftover);
            furnace.setChanged();
        }

        ItemStack rawStack = findFirstRawIngredient();
        if (rawStack.isEmpty()) return false;
        Item cooked = RAW_TO_COOKED.get(rawStack.getItem());
        if (cooked == null) return false;

        ItemStack input = furnace.getItem(inputSlot);
        if (!input.isEmpty() && (!ItemStack.isSameItemSameComponents(input, rawStack) || input.getCount() >= input.getMaxStackSize())) {
            return false;
        }

        ItemStack cookedStack = new ItemStack(cooked);
        ItemStack existingOutput = furnace.getItem(outputSlot);
        if (!existingOutput.isEmpty() && (!ItemStack.isSameItemSameComponents(existingOutput, cookedStack) || existingOutput.getCount() >= existingOutput.getMaxStackSize())) {
            return false;
        }

        ItemStack inserted = rawStack.split(1);
        if (input.isEmpty()) {
            furnace.setItem(inputSlot, inserted);
        } else {
            input.grow(1);
            furnace.setItem(inputSlot, input);
        }
        furnace.setChanged();
        return true;
    }

    private boolean pullCooked(AbstractFurnaceBlockEntity furnace) {
        ItemStack output = furnace.getItem(2);
        if (output.isEmpty() || !RAW_TO_COOKED.containsValue(output.getItem())) {
            return false;
        }
        ItemStack moved = output.copy();
        furnace.setItem(2, ItemStack.EMPTY);
        ItemStack leftover = companion.getInventory().addItem(moved);
        if (!leftover.isEmpty()) {
            companion.spawnAtLocation(leftover);
        }
        furnace.setChanged();
        return true;
    }

    private boolean hasFuel(AbstractFurnaceBlockEntity furnace) {
        boolean lit = furnace.getBlockState().hasProperty(AbstractFurnaceBlock.LIT) && furnace.getBlockState().getValue(AbstractFurnaceBlock.LIT);
        boolean stocked = !furnace.getItem(1).isEmpty();
        return lit || stocked;
    }

    private ItemStack findFirstRawIngredient() {
        for (int i = 0; i < companion.getInventory().getContainerSize(); i++) {
            ItemStack stack = companion.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            if (RAW_TO_COOKED.containsKey(stack.getItem())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private void cookDirect() {
        for (int i = 0; i < companion.getInventory().getContainerSize(); i++) {
            ItemStack stack = companion.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            Item cooked = RAW_TO_COOKED.get(stack.getItem());
            if (cooked == null) continue;
            stack.shrink(1);
            ItemStack cookedStack = new ItemStack(cooked);
            ItemStack leftover = companion.getInventory().addItem(cookedStack);
            if (!leftover.isEmpty()) {
                companion.spawnAtLocation(leftover);
            }
            return;
        }
    }

    private BlockPos findHeatSource() {
        BlockPos origin = companion.blockPosition();
        Level level = companion.level();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-searchRadius, -1, -searchRadius),
                origin.offset(searchRadius, 2, searchRadius))) {
            if (isHeatSource(pos)) {
                return pos.immutable();
            }
        }
        return null;
    }

    private boolean isHeatSource(BlockPos pos) {
        var state = companion.level().getBlockState(pos);
        return state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE) || state.is(Blocks.FURNACE) || state.is(Blocks.SMOKER);
    }

    private void moveToHeat() {
        if (heatSource == null) return;
        companion.getNavigation().moveTo(heatSource.getX() + 0.5D, heatSource.getY() + 0.5D, heatSource.getZ() + 0.5D, 1.0D);
    }

    private boolean isActiveJob() {
        if (!enabled) return false;
        if (companion.getJob() != CompanionJob.CHEF) return false;
        if (!companion.isPatrolling()) return false;
        if (companion.isOrderedToSit() || !companion.isTame()) return false;
        return isWithinPatrolArea();
    }

    private boolean isWithinPatrolArea() {
        return companion.isPatrolling() && companion.getPatrolPos().isPresent()
                && companion.getPatrolPos().get().distSqr(companion.blockPosition()) <= Math.pow(Math.max(8.0D, companion.getPatrolRadius() + 4), 2);
    }
}
