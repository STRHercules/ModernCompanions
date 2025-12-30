package com.majorbonghits.moderncompanions.client;

import com.majorbonghits.moderncompanions.client.renderer.CompanionRenderer;
import com.majorbonghits.moderncompanions.client.screen.CompanionScreen;
import com.majorbonghits.moderncompanions.core.ModEntityTypes;
import com.majorbonghits.moderncompanions.core.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = MOD_ID, value = Dist.CLIENT)
public final class ModClientEvents {
    private ModClientEvents() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Register menu screens on the client thread for Forge 1.20.1.
        event.enqueueWork(() -> MenuScreens.register(ModMenuTypes.COMPANION_MENU.get(), CompanionScreen::new));
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.KNIGHT.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ARCHER.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ARBALIST.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.AXEGUARD.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.VANGUARD.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BERSERKER.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BEASTMASTER.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CLERIC.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ALCHEMIST.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SCOUT.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.STORMCALLER.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.FIRE_MAGE.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.LIGHTNING_MAGE.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.NECROMANCER.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SUMMONED_WITHER_SKELETON.get(), net.minecraft.client.renderer.entity.WitherSkeletonRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.FIREBOLT.get(), ctx -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(ctx, 0.75F, true));
        event.registerEntityRenderer(ModEntityTypes.FIREBURST.get(), ctx -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(ctx, 1.5F, true));
        event.registerEntityRenderer(ModEntityTypes.SOFT_WITHER_SKULL.get(), net.minecraft.client.renderer.entity.WitherSkullRenderer::new);
    }
}
