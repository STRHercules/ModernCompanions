package com.majorbonghits.moderncompanions.menu;

import com.majorbonghits.moderncompanions.core.ModMenuTypes;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Simple 3x9 inventory for companions (placeholder until full GUI is ported).
 */
public class CompanionMenu extends AbstractContainerMenu {
    private static final int COMPANION_ROWS = 3;
    private final Container container;
    private final int companionId;
    private final AbstractHumanCompanionEntity companion;

    public CompanionMenu(int id, Inventory playerInv, int companionId) {
        this(id, playerInv, resolveContainer(playerInv, companionId), companionId, resolveEntity(playerInv, companionId));
    }

    public CompanionMenu(int id, Inventory playerInv, AbstractHumanCompanionEntity companion) {
        this(id, playerInv, companion.getInventory(), companion.getId(), companion);
    }

    private CompanionMenu(int id, Inventory playerInv, Container container, int companionId, AbstractHumanCompanionEntity companion) {
        super(ModMenuTypes.COMPANION_MENU.get(), id);
        this.container = container;
        this.companionId = companionId;
        this.companion = companion;
        checkContainerSize(container, COMPANION_ROWS * 9);
        container.startOpen(playerInv.player);

        // Companion inventory slots
        for (int row = 0; row < COMPANION_ROWS; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(container, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        int offset = (COMPANION_ROWS - 4) * 18;
        // Player inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 103 + row * 18 + offset));
            }
        }
        // Hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 161 + offset));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index < COMPANION_ROWS * 9) {
                if (!this.moveItemStackTo(stack, COMPANION_ROWS * 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, COMPANION_ROWS * 9, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    public int getCompanionId() {
        return companionId;
    }

    public AbstractHumanCompanionEntity getCompanion() {
        return companion;
    }

    private static Container resolveContainer(Inventory inv, int id) {
        AbstractHumanCompanionEntity c = resolveEntity(inv, id);
        return c != null ? c.getInventory() : new SimpleContainer(27);
    }

    private static AbstractHumanCompanionEntity resolveEntity(Inventory inv, int id) {
        if (inv.player.level().getEntity(id) instanceof AbstractHumanCompanionEntity c) {
            return c;
        }
        return null;
    }
}
