package com.majorbonghits.moderncompanions.compat.curios;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.menu.CompanionCuriosMenu;
import com.majorbonghits.moderncompanions.network.CompanionToggleCurioRenderPayload;
import com.majorbonghits.moderncompanions.network.OpenCompanionCuriosPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncRender;

import java.util.function.Supplier;

/**
 * Curios-specific payload registration and handlers. Only registered when Curios is loaded.
 */
public final class CuriosNetwork {
    private CuriosNetwork() {}

    public static int register(SimpleChannel channel, int index) {
        channel.registerMessage(index++, OpenCompanionCuriosPayload.class, OpenCompanionCuriosPayload::encode, OpenCompanionCuriosPayload::decode, CuriosNetwork::handleOpenCurios);
        channel.registerMessage(index++, CompanionToggleCurioRenderPayload.class, CompanionToggleCurioRenderPayload::encode, CompanionToggleCurioRenderPayload::decode, CuriosNetwork::handleToggleCurioRender);
        return index;
    }

    private static void handleOpenCurios(OpenCompanionCuriosPayload payload, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.getSender();
            if (serverPlayer == null) {
                return;
            }
            Entity entity = serverPlayer.level().getEntity(payload.entityId());
            if (entity instanceof AbstractHumanCompanionEntity companion && companion.isOwnedBy(serverPlayer)) {
                var handlerOpt = CuriosApi.getCuriosInventory(companion);
                if (handlerOpt.isPresent()) {
                    // Forge 1.20.1 requires NetworkHooks for extra menu data.
                    NetworkHooks.openScreen(serverPlayer,
                            new SimpleMenuProvider((id, inv, player) -> new CompanionCuriosMenu(id, inv, companion),
                                    Component.literal("Curios - " + companion.getName().getString())),
                            buf -> buf.writeVarInt(companion.getId()));
                } else {
                    serverPlayer.displayClientMessage(Component.literal("This companion has no curio slots."), true);
                }
            }
        });
        ctx.setPacketHandled(true);
    }

    private static void handleToggleCurioRender(CompanionToggleCurioRenderPayload payload, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.getSender();
            if (serverPlayer == null) return;
            Entity entity = serverPlayer.level().getEntity(payload.entityId());
            if (entity instanceof AbstractHumanCompanionEntity companion && companion.isOwnedBy(serverPlayer)) {
                CuriosApi.getCuriosInventory(companion).ifPresent(handler ->
                        handler.getStacksHandler(payload.identifier()).ifPresent(stacks -> {
                            var renders = stacks.getRenders();
                            int idx = payload.index();
                            if (idx >= 0 && idx < renders.size()) {
                                boolean value = !renders.get(idx);
                                renders.set(idx, value);
                                NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> companion),
                                        new SPacketSyncRender(companion.getId(), payload.identifier(), idx, value));
                            }
                        }));
            }
        });
        ctx.setPacketHandled(true);
    }
}
