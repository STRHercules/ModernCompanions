package com.majorbonghits.moderncompanions.network;

import com.majorbonghits.moderncompanions.ModernCompanions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Payload for toggling a companion behavior flag.
 */
public record ToggleFlagPayload(int entityId, String flag, boolean value) implements CustomPacketPayload {
    public static final Type<ToggleFlagPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "toggle_flag"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleFlagPayload> CODEC =
            StreamCodec.of(ToggleFlagPayload::encode, ToggleFlagPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, ToggleFlagPayload payload) {
        buf.writeVarInt(payload.entityId);
        buf.writeUtf(payload.flag);
        buf.writeBoolean(payload.value);
    }

    private static ToggleFlagPayload decode(RegistryFriendlyByteBuf buf) {
        return new ToggleFlagPayload(buf.readVarInt(), buf.readUtf(), buf.readBoolean());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
