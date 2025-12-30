package com.majorbonghits.moderncompanions.network;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.compat.curios.CuriosNetwork;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.menu.CompanionMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

/**
 * Legacy (1.20.1) networking using SimpleChannel.
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = ModernCompanions.MOD_ID)
public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ModernCompanions.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private ModNetwork() {}

    public static void register() {
        int index = 0;
        CHANNEL.registerMessage(index++, ToggleFlagPayload.class, ToggleFlagPayload::encode, ToggleFlagPayload::decode, ModNetwork::handleToggleFlag);
        CHANNEL.registerMessage(index++, CompanionActionPayload.class, CompanionActionPayload::encode, CompanionActionPayload::decode, ModNetwork::handleAction);
        CHANNEL.registerMessage(index++, SetPatrolRadiusPayload.class, SetPatrolRadiusPayload::encode, SetPatrolRadiusPayload::decode, ModNetwork::handlePatrolRadius);
        CHANNEL.registerMessage(index++, OpenCompanionInventoryPayload.class, OpenCompanionInventoryPayload::encode, OpenCompanionInventoryPayload::decode, ModNetwork::handleOpenInventory);

        if (ModList.get().isLoaded("curios")) {
            CuriosNetwork.register(CHANNEL, index);
        }
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }

    private static void handleToggleFlag(ToggleFlagPayload payload, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.getSender();
            if (serverPlayer == null) {
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
        ctx.setPacketHandled(true);
    }

    private static void handleAction(CompanionActionPayload payload, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.getSender();
            if (serverPlayer == null) {
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
        ctx.setPacketHandled(true);
    }

    private static void handlePatrolRadius(SetPatrolRadiusPayload payload, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.getSender();
            if (serverPlayer == null) {
                return;
            }
            Entity entity = serverPlayer.level().getEntity(payload.entityId());
            if (entity instanceof AbstractHumanCompanionEntity companion && companion.isOwnedBy(serverPlayer)) {
                companion.setPatrolRadius(payload.radius());
            }
        });
        ctx.setPacketHandled(true);
    }

    private static void handleOpenInventory(OpenCompanionInventoryPayload payload, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.getSender();
            if (serverPlayer == null) {
                return;
            }
            Entity entity = serverPlayer.level().getEntity(payload.entityId());
            if (entity instanceof AbstractHumanCompanionEntity companion && companion.isOwnedBy(serverPlayer)) {
                // Forge 1.20.1 requires NetworkHooks to send extra menu data.
                NetworkHooks.openScreen(serverPlayer,
                        new SimpleMenuProvider((id, inv, player) -> new CompanionMenu(id, inv, companion),
                                companion.getDisplayName()),
                        buf -> buf.writeVarInt(companion.getId()));
            }
        });
        ctx.setPacketHandled(true);
    }
}
