package com.majorbonghits.moderncompanions.menu;

import com.majorbonghits.moderncompanions.core.ModMenuTypes;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraft.util.Mth;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Minimal Curios container bound to a companion. Mirrors the player's Curios handler
 * but routes all effects to the companion entity.
 */
public class CompanionCuriosMenu extends AbstractContainerMenu {
    private static final int GRID_COLUMNS = 4;
    private static final int PLAYER_INV_OFFSET = 30; // push player inventory lower on the GUI
    private static final int CURIO_START_X = 8;
    private static final int CURIO_START_Y = 18;

    private final AbstractHumanCompanionEntity companion;
    private final ICuriosItemHandler curiosHandler;
    private final int curioSlotCount;
    private final int curioRows;
    private final int playerInventoryY;

    public CompanionCuriosMenu(int id, Inventory playerInv, int companionId) {
        this(id, playerInv, resolveCompanion(playerInv, companionId));
    }

    public CompanionCuriosMenu(int id, Inventory playerInv, AbstractHumanCompanionEntity companion) {
        super(ModMenuTypes.COMPANION_CURIOS_MENU.get(), id);
        this.companion = companion;
        this.curiosHandler = companion != null ? CuriosApi.getCuriosInventory(companion).orElse(null) : null;

        int builtSlots = addCurioSlots();
        this.curioSlotCount = builtSlots;
        this.curioRows = Math.max(1, Mth.ceil((float) builtSlots / GRID_COLUMNS));
        this.playerInventoryY = CURIO_START_Y + curioRows * 18 + 20 + PLAYER_INV_OFFSET;
        addPlayerSlots(playerInv);
    }

    private int addCurioSlots() {
        int index = 0;
        if (curiosHandler == null) {
            return 0;
        }

        List<Map.Entry<String, ICurioStacksHandler>> ordered = new ArrayList<>(curiosHandler.getCurios().entrySet());
        ordered.sort(Comparator
                .comparingInt((Map.Entry<String, ICurioStacksHandler> e) -> CuriosApi.getSlot(e.getKey())
                        .map(ISlotType::getOrder)
                        .orElse(Integer.MAX_VALUE))
                .thenComparing(Map.Entry::getKey));

        for (Map.Entry<String, ICurioStacksHandler> entry : ordered) {
            IDynamicStackHandler stacks = entry.getValue().getStacks();
            List<Boolean> activeStatuses = Collections.nCopies(stacks.getSlots(), true);
            for (int i = 0; i < stacks.getSlots(); i++) {
                int col = index % GRID_COLUMNS;
                int row = index / GRID_COLUMNS;
                int x = CURIO_START_X + col * 18;
                int y = CURIO_START_Y + row * 18;
                this.addSlot(new CompanionCurioSlot(entry.getKey(), companion, stacks, i, x, y,
                        entry.getValue().getRenders(), activeStatuses, entry.getValue().canToggleRendering()));
                index++;
            }
        }
        return index;
    }

    private void addPlayerSlots(Inventory playerInv) {
        // Main inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, playerInventoryY + row * 18));
            }
        }
        // Hotbar
        int hotbarY = playerInventoryY + 58;
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, hotbarY));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return companion != null && curiosHandler != null && player.distanceToSqr(companion) < 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            moved = stack.copy();
            if (index < curioSlotCount) {
                if (!this.moveItemStackTo(stack, curioSlotCount, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, curioSlotCount, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return moved;
    }

    public int getCompanionId() {
        return companion != null ? companion.getId() : -1;
    }

    public int getCurioRows() {
        return curioRows;
    }

    public int getPlayerInventoryY() {
        return playerInventoryY;
    }

    public AbstractHumanCompanionEntity getCompanion() {
        return companion;
    }

    private static AbstractHumanCompanionEntity resolveCompanion(Inventory inv, int id) {
        if (inv.player.level().getEntity(id) instanceof AbstractHumanCompanionEntity companion) {
            return companion;
        }
        return null;
    }

    /**
     * Slot wrapper that supplies the companion as the SlotContext owner so Curios effects
     * are applied to the companion instead of the player.
     */
    public static class CompanionCurioSlot extends SlotItemHandler {
        private final String identifier;
        private final AbstractHumanCompanionEntity owner;
        private final List<Boolean> activeStatuses;
        private final List<Boolean> renderStatuses;
        private final boolean canToggleRender;

        CompanionCurioSlot(String identifier, AbstractHumanCompanionEntity owner, IDynamicStackHandler handler, int index,
                           int x, int y, List<Boolean> renderStatuses, List<Boolean> activeStatuses, boolean canToggleRender) {
            super(handler, index, x, y);
            this.identifier = identifier;
            this.owner = owner;
            this.renderStatuses = renderStatuses;
            this.activeStatuses = activeStatuses;
            this.canToggleRender = canToggleRender;
            CuriosApi.getSlot(identifier).ifPresent(slotType -> this.setBackground(InventoryMenu.BLOCK_ATLAS, slotType.getIcon()));
        }

        private SlotContext context() {
            return new SlotContext(identifier, owner, this.getSlotIndex(), false, getRenderStatusInternal());
        }

        private boolean getRenderStatusInternal() {
            if (!canToggleRender) {
                return true;
            }
            return renderStatuses.size() > this.getSlotIndex() && renderStatuses.get(this.getSlotIndex());
        }

        private boolean isSlotActive() {
            return activeStatuses.size() > this.getSlotIndex() && activeStatuses.get(this.getSlotIndex());
        }

        @Override
        public void set(ItemStack stack) {
            ItemStack current = this.getItem();
            boolean noChange = current.isEmpty() && stack.isEmpty();
            super.set(stack);
            if (!noChange && !ItemStack.matches(current, stack) && owner != null && isSlotActive()) {
                CuriosApi.getCurio(stack).ifPresent(curio -> curio.onEquipFromUse(context()));
            }
        }

        public String getIdentifier() {
            return this.identifier;
        }

        public boolean canToggleRender() {
            return this.canToggleRender;
        }

        public boolean getRenderStatus() { return getRenderStatusInternal(); }
    }
}
