package com.majorbonghits.moderncompanions.client.screen;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.Optional;

/**
 * Simple read-only journal screen for a companion: shows traits, backstory, morale, bond, and memory stats.
 */
public class CompanionJournalScreen extends Screen {
    private static final ResourceLocation BG = new ResourceLocation(ModernCompanions.MOD_ID, "textures/journal.png");
    private static final int BG_W = 256;
    private static final int BG_H = 240;

    private final Screen parent;
    private final int companionId;
    private int leftPos;
    private int topPos;

    public CompanionJournalScreen(Screen parent, int companionId) {
        super(Component.translatable("gui.modern_companions.journal.title"));
        this.parent = parent;
        this.companionId = companionId;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - BG_W) / 2;
        this.topPos = (this.height - BG_H) / 2;
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), b -> closeToParent())
                .pos(leftPos + BG_W - 60, topPos + BG_H - 24)
                .size(50, 16)
                .build());
    }

    private void closeToParent() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        mc.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        // Custom backdrop; skip the default Screen blur entirely.
        gfx.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0000000);
        gfx.blit(BG, leftPos, topPos, 0, 0, BG_W, BG_H, BG_W, BG_H);

        safeCompanion().ifPresent(companion -> {
            int x = leftPos + 12;
            int y = topPos + 12;
            int width = BG_W - 24;

            y = drawLine(gfx, Component.literal(companion.getName().getString()).withStyle(style -> style.withUnderlined(true)), x, y, width);
            y = drawLine(gfx, Component.translatable("gui.modern_companions.age", companion.getAgeYears()), x, y, width);
            y = drawLine(gfx, Component.translatable("gui.modern_companions.backstory", backstoryName(companion.getBackstoryId())), x, y, width);
            y += 4;

            y = drawLine(gfx, Component.translatable("gui.modern_companions.traits").withStyle(style -> style.withUnderlined(true)), x, y, width);
            y = drawLine(gfx, Component.translatable("gui.modern_companions.traits.primary", traitName(companion.getPrimaryTraitId())), x, y, width);
            y = drawLine(gfx, Component.translatable("gui.modern_companions.traits.secondary", traitName(companion.getSecondaryTraitId())), x, y, width);
            y += 4;

            y = drawLine(gfx, Component.translatable("gui.modern_companions.morale.label", Component.translatable(companion.getMoraleDescriptorKey())), x, y, width);
            y = drawLine(gfx, Component.translatable("gui.modern_companions.bond_level", companion.getBondLevel()), x, y, width);
            int bondNext = companion.getPersonality().xpForNextLevel();
            y = drawLine(gfx, Component.translatable("gui.modern_companions.bond_progress", companion.getBondXp(), bondNext), x, y, width);
            y += 4;

            y = drawLine(gfx, Component.translatable("gui.modern_companions.memory").withStyle(style -> style.withUnderlined(true)), x, y, width);
            y = drawLine(gfx, Component.translatable("gui.modern_companions.memory.first", formatFirstTamed(companion)), x, y, width);
            y = drawLine(gfx, Component.translatable("gui.modern_companions.memory.kills", companion.getKillCount()), x, y, width);
            y = drawLine(gfx, Component.translatable("gui.modern_companions.memory.major_kills", companion.getMajorKills()), x, y, width);
            y = drawLine(gfx, Component.translatable("gui.modern_companions.memory.resurrections", companion.getTimesResurrected()), x, y, width);
            y = drawLine(gfx, Component.translatable("gui.modern_companions.memory.distance", formatDistance(companion.getDistanceTraveledWithOwner())), x, y, width);
        });

        super.render(gfx, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics gfx) {
        // Intentionally empty: prevents the vanilla blurred world background for this screen.
    }

    private Optional<AbstractHumanCompanionEntity> safeCompanion() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) return Optional.empty();
        var e = mc.level.getEntity(companionId);
        if (e instanceof AbstractHumanCompanionEntity comp) {
            return Optional.of(comp);
        }
        return Optional.empty();
    }

    private int drawLine(GuiGraphics gfx, Component text, int x, int y, int width) {
        for (FormattedCharSequence seq : this.font.split(text, width)) {
            gfx.drawString(this.font, seq, x, y, 0x2B2B2B, false);
            y += 10;
        }
        return y;
    }

    private Component traitName(String id) {
        if (id == null || id.isBlank()) {
            return Component.translatable("trait.modern_companions.none");
        }
        return Component.translatable("trait.modern_companions." + id);
    }

    private Component backstoryName(String id) {
        if (id == null || id.isBlank()) {
            return Component.translatable("backstory.modern_companions.none");
        }
        return Component.translatable("backstory.modern_companions." + id);
    }

    private String formatFirstTamed(AbstractHumanCompanionEntity companion) {
        long first = companion.getFirstTamedGameTime();
        if (first < 0 || companion.level() == null) {
            return Component.translatable("gui.modern_companions.memory.unknown").getString();
        }
        long day = (first / 24000L) + 1;
        return Component.translatable("gui.modern_companions.memory.day", day).getString();
    }

    private String formatDistance(long meters) {
        if (meters >= 1000) {
            double km = meters / 1000.0;
            return String.format("%.2f km", km);
        }
        return meters + " m";
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
