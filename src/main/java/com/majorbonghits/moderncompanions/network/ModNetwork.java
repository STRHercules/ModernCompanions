package com.majorbonghits.moderncompanions.network;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.entity.job.CompanionJob;
import com.majorbonghits.moderncompanions.menu.CompanionMenu;
import com.majorbonghits.moderncompanions.network.OpenCompanionInventoryPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.world.SimpleMenuProvider;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = ModernCompanions.MOD_ID)
public final class ModNetwork {
    private ModNetwork() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(ModernCompanions.MOD_ID)
                .playToServer(ToggleFlagPayload.TYPE, ToggleFlagPayload.CODEC, ModNetwork::handleToggleFlag)
                .playToServer(CompanionActionPayload.TYPE, CompanionActionPayload.CODEC, ModNetwork::handleAction)
                .playToServer(SetPatrolRadiusPayload.TYPE, SetPatrolRadiusPayload.CODEC, ModNetwork::handlePatrolRadius)
                .playToServer(SetCompanionJobPayload.TYPE, SetCompanionJobPayload.CODEC, ModNetwork::handleSetJob)
                .playToServer(OpenCompanionInventoryPayload.TYPE, OpenCompanionInventoryPayload.CODEC, ModNetwork::handleOpenInventory);
    }

    private static void handleToggleFlag(ToggleFlagPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            Entity entity = serverPlayer.level().getEntity(payload.entityId());
            if (entity instanceof AbstractHumanCompanionEntity companion && companion.isOwnedBy(serverPlayer)) {
                companion.applyFlag(payload.flag(), payload.value());
                switch (payload.flag()) {
                    case "alert" -> companion.setAlert(payload.value());
                    case "hunt" -> companion.setHunting(payload.value());
                    case "sprint" -> companion.setSprintEnabled(payload.value());
                    case "patrol" -> {
                        companion.setPatrolPos(companion.blockPosition());
                        companion.setPatrolling(payload.value());
                    }
                    case "guard" -> {
                        companion.setGuarding(payload.value());
                        companion.setPatrolPos(companion.blockPosition());
                    }
                    case "follow" -> companion.setFollowing(payload.value());
                    case "pickup" -> companion.setPickupEnabled(payload.value());
                    default -> {}
                }
            }
        });
    }

    private static void handleAction(CompanionActionPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            Entity entity = serverPlayer.level().getEntity(payload.entityId());
            if (entity instanceof AbstractHumanCompanionEntity companion && companion.isOwnedBy(serverPlayer)) {
                switch (payload.action()) {
                    case "cycle_orders" -> companion.cycleOrders();
                    case "clear_target" -> companion.setTarget(null);
                    case "release" -> {
                        companion.release();
                        serverPlayer.sendSystemMessage(Component.literal(companion.getDisplayName().getString().split(" ")[0] +
                                " is no longer your companion."));
                    }
                    default -> {}
                }
            }
        });
    }

    private static void handlePatrolRadius(SetPatrolRadiusPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            Entity entity = serverPlayer.level().getEntity(payload.entityId());
            if (entity instanceof AbstractHumanCompanionEntity companion && companion.isOwnedBy(serverPlayer)) {
                companion.setPatrolRadius(payload.radius());
            }
        });
    }

    private static void handleSetJob(SetCompanionJobPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            Entity entity = serverPlayer.level().getEntity(payload.entityId());
            if (entity instanceof AbstractHumanCompanionEntity companion && companion.isOwnedBy(serverPlayer)) {
                companion.setJob(CompanionJob.fromId(payload.jobId()));
                companion.onJobChanged();
            }
        });
    }

    private static void handleOpenInventory(OpenCompanionInventoryPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            Entity entity = serverPlayer.level().getEntity(payload.entityId());
            if (entity instanceof AbstractHumanCompanionEntity companion && companion.isOwnedBy(serverPlayer)) {
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (id, inv, player) -> new CompanionMenu(id, inv, companion), companion.getDisplayName()),
                        buf -> buf.writeVarInt(companion.getId()));
            }
        });
    }
}
