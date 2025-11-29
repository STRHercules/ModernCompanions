package com.majorbonghits.moderncompanions.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.menu.CompanionCuriosMenu;
import com.majorbonghits.moderncompanions.network.OpenCompanionInventoryPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import com.majorbonghits.moderncompanions.network.CompanionToggleCurioRenderPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import java.util.Optional;

/**
 * Companion Curios screen with the same stats sidebar as the default inventory
 * plus a Back button to return to the main companion inventory.
 */
public class CompanionCuriosScreen extends AbstractContainerScreen<CompanionCuriosMenu> {
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "textures/inventory_curio.png");
    private static final int BG_WIDTH = 345;
    private static final int BG_HEIGHT = 256;
    // Stats panel coordinates match inventory_stats.png
    private static final int TOP_STATS_LEFT = 229;
    private static final int TOP_STATS_TOP = 7;
    private static final int TOP_STATS_RIGHT = 327;
    private static final int ATTR_LEFT = 228;
    private static final int ATTR_TOP = 137;
    private static final int ATTR_RIGHT = 326;
    private static final int ATTR_BOTTOM = 194;
    private static final int FOOD_LEFT = 228;
    private static final int FOOD_TOP = 215;
    private static final int FOOD_RIGHT = 327;
    private static final int FOOD_BOTTOM = 236;
    private static final ResourceLocation CURIO_TEX = ResourceLocation.fromNamespaceAndPath("curios", "textures/gui/curios/inventory.png");

    private Button backButton;
    private final List<RenderToggleButton> toggleButtons = new ArrayList<>();

    public CompanionCuriosScreen(CompanionCuriosMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = BG_WIDTH;
        this.imageHeight = BG_HEIGHT;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.topPos += 1; // align shadow like main screen
        int btnX = this.leftPos + 174 + 2;
        int btnY = this.topPos + 200;
        backButton = addRenderableWidget(Button.builder(Component.literal("Back"), b -> openMainInventory())
                .pos(btnX, btnY)
                .size(38, 16)
                .build());

        // Render toggle buttons for curio slots
        toggleButtons.clear();
        for (var slot : this.menu.slots) {
            if (slot instanceof CompanionCuriosMenu.CompanionCurioSlot curioSlot && curioSlot.canToggleRender()) {
                int x = this.leftPos + slot.x + 12;
                int y = this.topPos + slot.y - 1;
                RenderToggleButton btn = new RenderToggleButton(x, y, curioSlot::getRenderStatus, () -> toggleRender(curioSlot.getIdentifier(), slot.getSlotIndex()));
                this.addRenderableWidget(btn);
                toggleButtons.add(btn);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        RenderSystem.enableBlend();
        gfx.blit(BG, x, y, 0, 0, this.imageWidth, this.imageHeight, BG_WIDTH, BG_HEIGHT);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        safeCompanion().ifPresent(companion -> {
            int statsX = TOP_STATS_LEFT + 4;
            int statsWidth = (TOP_STATS_RIGHT - TOP_STATS_LEFT) - 8;
            int y = TOP_STATS_TOP + 2;

            gfx.drawString(this.font, Component.literal("Class").withStyle(ChatFormatting.UNDERLINE), statsX, y, 0x000000, false);
            y += 10;
            gfx.drawString(this.font, Component.literal(companion.getClassDisplayName()), statsX, y, 0x000000, false);
            y += 12;

            gfx.drawString(this.font, Component.literal("Health").withStyle(ChatFormatting.UNDERLINE), statsX, y, 0x000000, false);
            y += 10;
            gfx.drawString(this.font, Component.literal(String.format("%.1f / %d", companion.getHealth(), (int) companion.getMaxHealth())), statsX, y, 0x000000, false);
            y += 12;

            float xpFrac = companion.getExperienceProgress();
            int xpNeeded = companion.getXpNeededForNextLevel();
            int xpHave = Math.round(xpFrac * xpNeeded);
            gfx.drawString(this.font, Component.literal("Level " + companion.getExpLvl()), statsX, y, 0x000000, false);
            y += 10;
            int barW = Math.max(60, Math.min(90, statsWidth));
            int barH = 6;
            int filledW = (int) (barW * xpFrac);
            gfx.fill(statsX, y, statsX + barW, y + barH, 0xFF777777);
            gfx.fill(statsX + 1, y + 1, statsX + 1 + filledW, y + barH - 1, 0xFF55AA55);
            y += 10;
            gfx.drawString(this.font, Component.literal(xpHave + "/" + xpNeeded), statsX, y, 0x000000, false);
            y += 12;

            gfx.drawString(this.font, Component.literal("Kills: " + companion.getKillCount()), statsX, y, 0x000000, false);
            y += 12;

            gfx.drawString(this.font, Component.literal("Patrol Radius: " + companion.getPatrolRadius()), statsX, y, 0x000000, false);
            renderAttributes(gfx, companion);
            renderWantedFood(gfx, companion);
        });
    }

    private void openMainInventory() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getConnection() == null) return;
        mc.getConnection().send(new ServerboundCustomPayloadPacket(new OpenCompanionInventoryPayload(menu.getCompanionId())));
    }

    private Optional<AbstractHumanCompanionEntity> safeCompanion() {
        AbstractHumanCompanionEntity c = menu.getCompanion();
        if (c == null && this.minecraft != null && this.minecraft.level != null) {
            var e = this.minecraft.level.getEntity(menu.getCompanionId());
            if (e instanceof AbstractHumanCompanionEntity comp) {
                c = comp;
            }
        }
        return Optional.ofNullable(c);
    }

    private void renderAttributes(GuiGraphics gfx, AbstractHumanCompanionEntity companion) {
        int x = ATTR_LEFT + 3;
        int y = ATTR_TOP + 3;
        int width = (ATTR_RIGHT - ATTR_LEFT) - 6;
        drawStatLine(gfx, x, y, width, "Strength", companion.getStrength(), companion.getSpecialistAttributeIndex() == 0);
        y += 10;
        drawStatLine(gfx, x, y, width, "Dexterity", companion.getDexterity(), companion.getSpecialistAttributeIndex() == 1);
        y += 10;
        drawStatLine(gfx, x, y, width, "Intelligence", companion.getIntelligence(), companion.getSpecialistAttributeIndex() == 2);
        y += 10;
        drawStatLine(gfx, x, y, width, "Endurance", companion.getEndurance(), companion.getSpecialistAttributeIndex() == 3);
    }

    private void drawStatLine(GuiGraphics gfx, int x, int y, int width, String name, int value, boolean highlight) {
        String line = name + ": " + value + (highlight ? " ★" : "");
        int color = highlight ? 0xFFD54F : 0x000000;
        for (FormattedCharSequence seq : this.font.split(Component.literal(line), width)) {
            gfx.drawString(this.font, seq, x, y, color, false);
            y += 10;
            if (y > ATTR_BOTTOM) break;
        }
    }

    private void renderWantedFood(GuiGraphics gfx, AbstractHumanCompanionEntity companion) {
        int foodX = FOOD_LEFT + 2;
        int foodY = FOOD_TOP + 2;
        int foodWidth = (FOOD_RIGHT - FOOD_LEFT) - 4;
        String food = companion.getFoodStatusForGui();
        if (food.isEmpty()) {
            food = "Not Hungry";
        }
        for (FormattedCharSequence line : this.font.split(Component.literal(food), foodWidth)) {
            gfx.drawString(this.font, line, foodX, foodY, 0x000000, false);
            foodY += 10;
            if (foodY > FOOD_BOTTOM) break;
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        super.render(gfx, mouseX, mouseY, partialTick);
        this.renderTooltip(gfx, mouseX, mouseY);
    }

    private void toggleRender(String identifier, int slotIndex) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundCustomPayloadPacket(new CompanionToggleCurioRenderPayload(menu.getCompanionId(), identifier, slotIndex)));
        }
    }

    private class RenderToggleButton extends Button {
        private final BooleanSupplier isOn;

        RenderToggleButton(int x, int y, BooleanSupplier isOn, Runnable onPress) {
            super(x, y, 8, 8, Component.empty(), b -> onPress.run(), DEFAULT_NARRATION);
            this.isOn = isOn;
        }

        @Override
        public void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
            int texX = isOn.getAsBoolean() ? 75 : 83;
            gfx.blit(CURIO_TEX, getX(), getY(), texX, 0, 8, 8, 256, 256);
        }
    }
}
