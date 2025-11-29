package com.majorbonghits.moderncompanions.compat.curios;

import com.majorbonghits.moderncompanions.client.screen.CompanionCuriosScreen;
import com.majorbonghits.moderncompanions.core.ModEntityTypes;
import com.majorbonghits.moderncompanions.core.ModMenuTypes;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import top.theillusivec4.curios.client.render.CuriosLayer;

/**
 * Client-only Curios hooks, registered only when Curios is present.
 */
public final class CuriosClientEvents {
    private CuriosClientEvents() {}

    public static void onRegisterMenus(RegisterMenuScreensEvent event) {
        if (ModMenuTypes.COMPANION_CURIOS_MENU != null) {
            event.register(ModMenuTypes.COMPANION_CURIOS_MENU.get(), CompanionCuriosScreen::new);
        }
    }

    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        if (ModMenuTypes.COMPANION_CURIOS_MENU == null) {
            return;
        }
        var ctx = event.getContext();
        addCuriosLayer(event, ctx, ModEntityTypes.KNIGHT.get());
        addCuriosLayer(event, ctx, ModEntityTypes.ARCHER.get());
        addCuriosLayer(event, ctx, ModEntityTypes.ARBALIST.get());
        addCuriosLayer(event, ctx, ModEntityTypes.AXEGUARD.get());
        addCuriosLayer(event, ctx, ModEntityTypes.VANGUARD.get());
        addCuriosLayer(event, ctx, ModEntityTypes.BERSERKER.get());
        addCuriosLayer(event, ctx, ModEntityTypes.BEASTMASTER.get());
        addCuriosLayer(event, ctx, ModEntityTypes.CLERIC.get());
        addCuriosLayer(event, ctx, ModEntityTypes.ALCHEMIST.get());
        addCuriosLayer(event, ctx, ModEntityTypes.SCOUT.get());
        addCuriosLayer(event, ctx, ModEntityTypes.STORMCALLER.get());
        addCuriosLayer(event, ctx, ModEntityTypes.FIRE_MAGE.get());
        addCuriosLayer(event, ctx, ModEntityTypes.LIGHTNING_MAGE.get());
        addCuriosLayer(event, ctx, ModEntityTypes.NECROMANCER.get());
    }

    private static void addCuriosLayer(EntityRenderersEvent.AddLayers event, EntityRendererProvider.Context ctx, EntityType<?> type) {
        EntityRenderer<?> renderer = event.getRenderer(type);
        if (renderer instanceof LivingEntityRenderer<?, ?> living) {
            @SuppressWarnings({"rawtypes", "unchecked"})
            var layer = new CuriosLayer((net.minecraft.client.renderer.entity.RenderLayerParent) living);
            living.addLayer(layer);
        }
    }
}
