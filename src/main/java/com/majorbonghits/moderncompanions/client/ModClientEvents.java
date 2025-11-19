package com.majorbonghits.moderncompanions.client;

import com.majorbonghits.moderncompanions.client.renderer.CompanionRenderer;
import com.majorbonghits.moderncompanions.client.screen.CompanionScreen;
import com.majorbonghits.moderncompanions.core.ModEntityTypes;
import com.majorbonghits.moderncompanions.core.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import static com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = MOD_ID, value = Dist.CLIENT)
public final class ModClientEvents {
    private ModClientEvents() {}

    @SubscribeEvent
    public static void onRegisterMenus(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.COMPANION_MENU.get(), CompanionScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.KNIGHT.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ARCHER.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ARBALIST.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.AXEGUARD.get(), CompanionRenderer::new);
    }
}
