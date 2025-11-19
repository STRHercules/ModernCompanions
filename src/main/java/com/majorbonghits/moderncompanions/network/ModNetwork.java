package com.majorbonghits.moderncompanions.network;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = ModernCompanions.MOD_ID)
public final class ModNetwork {
    private ModNetwork() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(ModernCompanions.MOD_ID)
                .playToServer(ToggleFlagPayload.TYPE, ToggleFlagPayload.CODEC, ModNetwork::handleToggleFlag);
    }

    private static void handleToggleFlag(ToggleFlagPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            Entity entity = serverPlayer.level().getEntity(payload.entityId());
            if (entity instanceof AbstractHumanCompanionEntity companion && companion.isOwnedBy(serverPlayer)) {
                companion.applyFlag(payload.flag(), payload.value());
            }
        });
    }
}
