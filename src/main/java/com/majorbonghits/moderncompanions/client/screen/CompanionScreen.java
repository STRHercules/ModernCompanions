package com.majorbonghits.moderncompanions.client.screen;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.menu.CompanionMenu;
import com.majorbonghits.moderncompanions.network.CompanionActionPayload;
import com.majorbonghits.moderncompanions.network.SetPatrolRadiusPayload;
import com.majorbonghits.moderncompanions.network.ToggleFlagPayload;
import com.majorbonghits.moderncompanions.network.OpenCompanionCuriosPayload;
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
import net.neoforged.fml.ModList;

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
    private static final ResourceLocation PICKUP_BTN = ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "textures/pickupbutton.png");
    private static final ResourceLocation SPRINT_BTN = ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "textures/stationerybutton.png");
    private static final ResourceLocation RELEASE_BTN = ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "textures/releasebutton.png");
    // Right-hand info panel on inventory_stats.png
    private static final int TOP_STATS_LEFT = 229;
    private static final int TOP_STATS_TOP = 7;
    private static final int TOP_STATS_RIGHT = 327;
    // Attribute block lives in 228,137 to 326,194
    private static final int ATTR_LEFT = 228;
    private static final int ATTR_TOP = 137;
    private static final int ATTR_RIGHT = 326;
    private static final int ATTR_BOTTOM = 194;
    // Wanted food strip shifted down to 228,215 to 327,236
    private static final int FOOD_LEFT = 228;
    private static final int FOOD_TOP = 215;
    private static final int FOOD_RIGHT = 327;
    private static final int FOOD_BOTTOM = 236;

    private CompanionButton alertButton;
    private CompanionButton huntButton;
    private CompanionButton patrolButton;
    private CompanionButton sprintButton;
    private CompanionButton clearButton;
    private CompanionButton pickupButton;
    private CompanionButton releaseButton;
    private CompanionButton radiusMinus;
    private CompanionButton radiusPlus;
    private Button curiosButton;
    private Button journalButton;

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
        sprintButton = addRenderableWidget(new CompanionButton("sprint", col2, row2, 16, 12, 0, 0, 13, SPRINT_BTN, () -> sendToggle("sprint"), true));
        clearButton = addRenderableWidget(new CompanionButton("clear", leftPos + sidebarX + 5, row3, 31, 12, 0, 0, 13, CLEAR_BTN, () -> sendAction("clear_target"), false));
        int row4 = row3 + rowHeight;
        pickupButton = addRenderableWidget(new CompanionButton("pickup", leftPos + sidebarX + 3, row4, 34, 12, 0, 0, 0, PICKUP_BTN, () -> sendToggle("pickup"), true));
        releaseButton = addRenderableWidget(new CompanionButton("release", leftPos + sidebarX + 3, topPos + 148, 34, 12, 0, 0, 13, RELEASE_BTN, () -> {
            sendAction("release");
            this.onClose();
        }, false));

        int radiusY = topPos + 148 + 16;
        ResourceLocation radiusTex = ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "textures/gui/radiusbutton.png");
        radiusMinus = addRenderableWidget(new CompanionButton("radius-", leftPos + sidebarX + 3, radiusY, 16, 12, 17, 0, 13, radiusTex, () -> adjustRadius(-2), false));
        radiusPlus = addRenderableWidget(new CompanionButton("radius+", leftPos + sidebarX + 21, radiusY, 16, 12, 0, 0, 13, radiusTex, () -> adjustRadius(2), false));

        int curiosY = topPos + 180; // move curios up
        if (ModList.get().isLoaded("curios")) {
            curiosButton = addRenderableWidget(Button.builder(Component.literal("Curios"), b -> openCurios())
                    .pos(leftPos + sidebarX + 2, curiosY)
                    .size(38, 16)
                    .build());
        }

        int journalY = topPos + 200;
        journalButton = addRenderableWidget(Button.builder(Component.translatable("button.modern_companions.journal"), b -> openJournal())
                .pos(leftPos + sidebarX + 2, journalY)
                .size(38, 16)
                .build());
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
            int statsX = TOP_STATS_LEFT + 4;
            int statsWidth = (TOP_STATS_RIGHT - TOP_STATS_LEFT) - 8;
            int y = TOP_STATS_TOP + 2;

            gfx.drawString(this.font, Component.literal("Class").withStyle(ChatFormatting.UNDERLINE), statsX, y, 0x000000, false);
            y += 10;
            String cls = companion.getClassDisplayName();
            gfx.drawString(this.font, Component.literal(cls), statsX, y, 0x000000, false);
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

    @Override
    public void containerTick() {
        super.containerTick();
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        super.render(gfx, mouseX, mouseY, partialTick);
        this.renderTooltip(gfx, mouseX, mouseY);
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

    private void openCurios() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getConnection() == null) return;
        mc.getConnection().send(new ServerboundCustomPayloadPacket(new OpenCompanionCuriosPayload(menu.getCompanionId())));
    }

    private void openJournal() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        mc.setScreen(new CompanionJournalScreen(this, menu.getCompanionId()));
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
        private int yTexStart;
        private final int yDiffTex;
        private final ResourceLocation texture;
        private final boolean toggleFlag;
        private final int baseY;
        private int xTexStart;

        CompanionButton(String name, int x, int y, int w, int h, int xTexStart, int yTexStart, int yDiffTex, ResourceLocation texture, Runnable onClick, boolean toggleFlag) {
            super(x, y, w, h, Component.empty(), b -> onClick.run(), DEFAULT_NARRATION);
            this.name = name;
            this.xTexStart = xTexStart;
            this.yTexStart = yTexStart;
            this.baseY = yTexStart;
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
            this.yTexStart = this.baseY;
            switch (name) {
                case "alert" -> this.xTexStart = flag(c != null && c.isAlert(), 0, 17);
                case "hunting" -> this.xTexStart = flag(c != null && c.isHunting(), 0, 17);
                case "sprint" -> this.xTexStart = flag(c != null && c.isSprintEnabled(), 0, 17);
                case "pickup" -> {
                    boolean on = c != null && c.isPickupEnabled();
                    this.xTexStart = 0;
                    this.yTexStart = on ? this.baseY + 13 : this.baseY;
                }
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

    private void renderAttributes(GuiGraphics gfx, AbstractHumanCompanionEntity companion) {
        int x = ATTR_LEFT + 3;
        int y = ATTR_TOP + 3;
        int width = (ATTR_RIGHT - ATTR_LEFT) - 6;
        //gfx.drawString(this.font, Component.literal("Attributes").withStyle(ChatFormatting.UNDERLINE), x, y, 0x000000, false);
        //y += 10;
        drawStatLine(gfx, x, y, width, "Strength", companion.getStrength(), isSpecialist(companion, 0));
        y += 10;
        drawStatLine(gfx, x, y, width, "Dexterity", companion.getDexterity(), isSpecialist(companion, 1));
        y += 10;
        drawStatLine(gfx, x, y, width, "Intelligence", companion.getIntelligence(), isSpecialist(companion, 2));
        y += 10;
        drawStatLine(gfx, x, y, width, "Endurance", companion.getEndurance(), isSpecialist(companion, 3));
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

    private boolean isSpecialist(AbstractHumanCompanionEntity companion, int idx) {
        return companion.getSpecialistAttributeIndex() == idx;
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
            if (foodY > FOOD_BOTTOM) break; // stay inside strip
        }
    }

    private int drawWrappedLine(GuiGraphics gfx, Component text, int x, int y, int width) {
        int currentY = y;
        for (FormattedCharSequence seq : this.font.split(text, width)) {
            gfx.drawString(this.font, seq, x, currentY, 0x000000, false);
            currentY += 10;
        }
        return currentY;
    }

    private Component traitName(String id) { return Component.empty(); }
    private Component backstoryName(String id) { return Component.empty(); }
    private String formatFirstTamed(AbstractHumanCompanionEntity companion) { return ""; }
    private String formatDistance(long meters) { return ""; }
}
