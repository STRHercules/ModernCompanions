package com.majorbonghits.moderncompanions.network;

import com.majorbonghits.moderncompanions.ModernCompanions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Generic companion command payload (release, clear target, cycle orders).
 */
public record CompanionActionPayload(int entityId, String action) implements CustomPacketPayload {
    public static final Type<CompanionActionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "companion_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CompanionActionPayload> CODEC =
            StreamCodec.of(CompanionActionPayload::encode, CompanionActionPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, CompanionActionPayload payload) {
        buf.writeVarInt(payload.entityId);
        buf.writeUtf(payload.action);
    }

    private static CompanionActionPayload decode(RegistryFriendlyByteBuf buf) {
        return new CompanionActionPayload(buf.readVarInt(), buf.readUtf());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
