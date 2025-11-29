package com.majorbonghits.moderncompanions.compat.curios;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.menu.CompanionCuriosMenu;
import com.majorbonghits.moderncompanions.network.CompanionToggleCurioRenderPayload;
import com.majorbonghits.moderncompanions.network.OpenCompanionCuriosPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncRender;

/**
 * Curios-specific payload registration and handlers. Only registered when Curios is loaded.
 */
public final class CuriosNetwork {
    private CuriosNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(ModernCompanions.MOD_ID);
        registrar.playToServer(OpenCompanionCuriosPayload.TYPE, OpenCompanionCuriosPayload.CODEC, CuriosNetwork::handleOpenCurios)
                .playToServer(CompanionToggleCurioRenderPayload.TYPE, CompanionToggleCurioRenderPayload.CODEC, CuriosNetwork::handleToggleCurioRender);
    }

    private static void handleOpenCurios(OpenCompanionCuriosPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            Entity entity = serverPlayer.level().getEntity(payload.entityId());
            if (entity instanceof AbstractHumanCompanionEntity companion && companion.isOwnedBy(serverPlayer)) {
                CuriosApi.getCuriosInventory(companion).ifPresentOrElse(handler -> {
                    serverPlayer.openMenu(new SimpleMenuProvider(
                                    (id, inv, player) -> new CompanionCuriosMenu(id, inv, companion),
                                    Component.literal("Curios - " + companion.getName().getString())),
                            buf -> buf.writeVarInt(companion.getId()));
                }, () -> serverPlayer.displayClientMessage(Component.literal("This companion has no curio slots."), true));
            }
        });
    }

    private static void handleToggleCurioRender(CompanionToggleCurioRenderPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
            Entity entity = serverPlayer.level().getEntity(payload.entityId());
            if (entity instanceof AbstractHumanCompanionEntity companion && companion.isOwnedBy(serverPlayer)) {
                CuriosApi.getCuriosInventory(companion)
                        .flatMap(handler -> handler.getStacksHandler(payload.identifier()))
                        .ifPresent(stacks -> {
                            var renders = stacks.getRenders();
                            int idx = payload.index();
                            if (idx >= 0 && idx < renders.size()) {
                                boolean value = !renders.get(idx);
                                renders.set(idx, value);
                                net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(companion,
                                        new SPacketSyncRender(companion.getId(), payload.identifier(), idx, value));
                            }
                        });
            }
        });
    }
}
