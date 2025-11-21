package com.majorbonghits.moderncompanions.client.screen;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.entity.Arbalist;
import com.majorbonghits.moderncompanions.entity.Archer;
import com.majorbonghits.moderncompanions.menu.CompanionMenu;
import com.majorbonghits.moderncompanions.network.CompanionActionPayload;
import com.majorbonghits.moderncompanions.network.SetPatrolRadiusPayload;
import com.majorbonghits.moderncompanions.network.ToggleFlagPayload;
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
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.Optional;

/**
 * Companion inventory screen styled like the original mod, including sidebar buttons and right-hand stats.
 */
public class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "textures/inventory_stats.png");
    private static final int BG_WIDTH = 345;
    private static final int BG_HEIGHT = 256;
    private static final ResourceLocation ALERT_BTN = ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "textures/alertbutton.png");
    private static final ResourceLocation HUNT_BTN = ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "textures/huntingbutton.png");
    private static final ResourceLocation PATROL_BTN = ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "textures/patrolbutton.png");
    private static final ResourceLocation CLEAR_BTN = ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "textures/clearbutton.png");
    private static final ResourceLocation STATIONARY_BTN = ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "textures/stationerybutton.png");
    private static final ResourceLocation RELEASE_BTN = ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "textures/releasebutton.png");
    // Right-hand stats panel on inventory_stats.png runs from (229,7) to (327,107)
    private static final int STATS_LEFT = 229;
    private static final int STATS_TOP = 7;
    private static final int STATS_RIGHT = 327;
    // Wanted food strip sits lower on the texture (228,135) to (328,157)
    private static final int FOOD_LEFT = 228;
    private static final int FOOD_TOP = 135;
    private static final int FOOD_RIGHT = 328;
    private static final int FOOD_BOTTOM = 157;

    private CompanionButton alertButton;
    private CompanionButton huntButton;
    private CompanionButton patrolButton;
    private CompanionButton stationaryButton;
    private CompanionButton clearButton;
    private CompanionButton releaseButton;
    private CompanionButton radiusMinus;
    private CompanionButton radiusPlus;

    private int sidebarX;

    public CompanionScreen(CompanionMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = BG_WIDTH;
        this.imageHeight = BG_HEIGHT; // draw full texture 1:1; prevents GL wrapping
        this.inventoryLabelY = this.imageHeight - 94;
        this.sidebarX = 174;
    }

    @Override
    protected void init() {
        super.init();
        // Nudge whole GUI down by 1px to align with texture shadow
        this.topPos += 1;
        int rowHeight = 15;
        int row1 = topPos + 66;
        int row2 = row1 + rowHeight;
        int row3 = row2 + rowHeight;
        int col1 = leftPos + sidebarX + 3;
        int col2 = col1 + 19;

        alertButton = addRenderableWidget(new CompanionButton("alert", col1, row1, 16, 12, 0, 0, 13, ALERT_BTN, () -> sendToggle("alert"), true));
        huntButton = addRenderableWidget(new CompanionButton("hunting", col2, row1, 16, 12, 0, 0, 13, HUNT_BTN, () -> sendToggle("hunt"), true));
        patrolButton = addRenderableWidget(new CompanionButton("patrolling", col1, row2, 16, 12, 0, 0, 13, PATROL_BTN, () -> sendAction("cycle_orders"), true));
        stationaryButton = addRenderableWidget(new CompanionButton("stationery", col2, row2, 16, 12, 0, 0, 13, STATIONARY_BTN, () -> sendToggle("stationery"), true));
        clearButton = addRenderableWidget(new CompanionButton("clear", leftPos + sidebarX + 5, row3, 31, 12, 0, 0, 13, CLEAR_BTN, () -> sendAction("clear_target"), false));
        releaseButton = addRenderableWidget(new CompanionButton("release", leftPos + sidebarX + 3, topPos + 148, 34, 12, 0, 0, 13, RELEASE_BTN, () -> {
            sendAction("release");
            this.onClose();
        }, false));

        int radiusY = topPos + 148 + 16;
        ResourceLocation radiusTex = ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "textures/gui/radiusbutton.png");
        radiusMinus = addRenderableWidget(new CompanionButton("radius-", leftPos + sidebarX + 3, radiusY, 16, 12, 17, 0, 13, radiusTex, () -> adjustRadius(-2), false));
        radiusPlus = addRenderableWidget(new CompanionButton("radius+", leftPos + sidebarX + 21, radiusY, 16, 12, 0, 0, 13, radiusTex, () -> adjustRadius(2), false));
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        gfx.blit(BG, x, y, 0, 0, this.imageWidth, this.imageHeight, BG_WIDTH, BG_HEIGHT);
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        // vanilla labels suppressed; we draw custom stats at right
        safeCompanion().ifPresent(companion -> {
            // renderLabels already translates to (leftPos, topPos); use texture-relative coords
            int statsX = STATS_LEFT + 4;
            int statsWidth = (STATS_RIGHT - STATS_LEFT) - 8;
            int y = STATS_TOP + 2;

            gfx.drawString(this.font, Component.literal("Class").withStyle(ChatFormatting.UNDERLINE), statsX, y, 0x000000, false);
            y += 10;
            String cls = companion instanceof Arbalist ? "Arbalist" : companion instanceof Archer ? "Archer" : companion.getType().toShortString();
            gfx.drawString(this.font, Component.literal(capitalize(cls)), statsX, y, 0x000000, false);
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

            gfx.drawString(this.font, Component.literal("Patrol Radius: " + companion.getPatrolRadius()), statsX, y, 0x000000, false);
            // Wanted food block anchored to dedicated strip on the texture
            int foodX = FOOD_LEFT + 2;
            int foodY = FOOD_TOP + 2;
            int foodWidth = (FOOD_RIGHT - FOOD_LEFT) - 4;
            String food = companion.getWantedFoodsCompact();
            if (food.isEmpty()) {
                food = "All set";
            }
            for (FormattedCharSequence line : this.font.split(Component.literal(food), foodWidth)) {
                gfx.drawString(this.font, line, foodX, foodY, 0x000000, false);
                foodY += 10;
                if (foodY > FOOD_BOTTOM) break; // stay inside strip
            }
        });
    }

    @Override
    public void containerTick() {
        super.containerTick();
    }

    /* ---------- Button actions ---------- */

    private void sendToggle(String flag) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getConnection() == null) return;
        safeCompanion().ifPresent(companion -> {
            boolean newValue = !companion.getFlagValue(flag);
            mc.getConnection().send(new ServerboundCustomPayloadPacket(new ToggleFlagPayload(menu.getCompanionId(), flag, newValue)));
            companion.applyFlag(flag, newValue);
        });
    }

    private void sendAction(String action) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getConnection() == null) return;
        mc.getConnection().send(new ServerboundCustomPayloadPacket(new CompanionActionPayload(menu.getCompanionId(), action)));
    }

    private void adjustRadius(int delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getConnection() == null) return;
        safeCompanion().ifPresent(companion -> {
            int target = Math.max(2, Math.min(32, companion.getPatrolRadius() + delta));
            mc.getConnection().send(new ServerboundCustomPayloadPacket(new SetPatrolRadiusPayload(menu.getCompanionId(), target)));
            companion.setPatrolRadius(target);
        });
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

    private class CompanionButton extends Button {
        private final String name;
        private final int yTexStart;
        private final int yDiffTex;
        private final ResourceLocation texture;
        private final boolean toggleFlag;
        private int xTexStart;

        CompanionButton(String name, int x, int y, int w, int h, int xTexStart, int yTexStart, int yDiffTex, ResourceLocation texture, Runnable onClick, boolean toggleFlag) {
            super(x, y, w, h, Component.empty(), b -> onClick.run(), DEFAULT_NARRATION);
            this.name = name;
            this.xTexStart = xTexStart;
            this.yTexStart = yTexStart;
            this.yDiffTex = yDiffTex;
            this.texture = texture;
            this.toggleFlag = toggleFlag;
        }

        @Override
        public void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
            updateTex();
            int v = this.yTexStart;
            if (toggleFlag) {
                v = this.isHoveredOrFocused() ? this.yTexStart + this.yDiffTex : this.yTexStart;
            } else if (this.isHoveredOrFocused()) {
                v = this.yTexStart + this.yDiffTex;
            }
            RenderSystem.enableBlend();
            gfx.blit(this.texture, this.getX(), this.getY(), this.xTexStart, v, this.width, this.height, 256, 256);
            RenderSystem.disableBlend();
        }


        private void updateTex() {
            AbstractHumanCompanionEntity c = safeCompanion().orElse(null);
            switch (name) {
                case "alert" -> this.xTexStart = flag(c != null && c.isAlert(), 0, 17);
                case "hunting" -> this.xTexStart = flag(c != null && c.isHunting(), 0, 17);
                case "stationery" -> this.xTexStart = flag(c != null && c.isStationery(), 0, 17);
                case "patrolling" -> {
                    if (c == null) { this.xTexStart = 0; break; }
                    if (c.isFollowing()) this.xTexStart = 0;
                    else if (c.isPatrolling()) this.xTexStart = 17;
                    else this.xTexStart = 34;
                }
                case "radius+" -> this.xTexStart = 0;
                case "radius-" -> this.xTexStart = 17;
                default -> this.xTexStart = 0;
            }
        }

        private int flag(boolean value, int on, int off) {
            return value ? on : off;
        }
    }

    private String capitalize(String name) {
        if (name == null || name.isEmpty()) return "";
        if (name.length() == 1) return name.toUpperCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
