package com.majorbonghits.moderncompanions.client.screen;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.menu.CompanionMenu;
import com.majorbonghits.moderncompanions.network.ToggleFlagPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Minimal placeholder screen with toggle buttons for companion behavior flags.
 */
public class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("minecraft:textures/gui/container/generic_54.png");
    private final Map<String, Button> flagButtons = new LinkedHashMap<>();

    public CompanionScreen(CompanionMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        gfx.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void init() {
        super.init();
        flagButtons.clear();
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        int y = top + 10;
        for (String flag : CompanionMenu.FLAG_KEYS) {
            Button button = Button.builder(Component.empty(), b -> sendToggle(flag))
                    .pos(left - 90, y)
                    .size(80, 18)
                    .build();
            flagButtons.put(flag, addRenderableWidget(button));
            y += 22;
        }
        refreshLabels();
    }

    private void sendToggle(String flag) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getConnection() == null) {
            return;
        }
        safeCompanion().ifPresent(companion -> {
            boolean newValue = !companion.getFlagValue(flag);
            ToggleFlagPayload payload = new ToggleFlagPayload(menu.getCompanionId(), flag, newValue);
            mc.getConnection().send(new ServerboundCustomPayloadPacket(payload));
            companion.applyFlag(flag, newValue);
            refreshLabels();
        });
    }

    private void refreshLabels() {
        safeCompanion().ifPresent(companion -> flagButtons.forEach((flag, button) -> {
            boolean value = companion.getFlagValue(flag);
            button.setMessage(Component.literal(labelFor(flag) + ": " + (value ? "ON" : "OFF")));
        }));
    }

    private static String labelFor(String flag) {
        return switch (flag) {
            case "follow" -> "Follow";
            case "patrol" -> "Patrol";
            case "guard" -> "Guard";
            case "hunt" -> "Hunt";
            case "alert" -> "Alert";
            case "stationery" -> "Stationary";
            default -> flag;
        };
    }

    @Override
    public void containerTick() {
        super.containerTick();
        refreshLabels();
    }

    private Optional<AbstractHumanCompanionEntity> safeCompanion() {
        return Optional.ofNullable(menu.getCompanion());
    }
}
