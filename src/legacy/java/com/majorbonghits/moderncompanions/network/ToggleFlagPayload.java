package com.majorbonghits.moderncompanions.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Payload for toggling a companion behavior flag.
 */
public record ToggleFlagPayload(int entityId, String flag, boolean value) {
    public static void encode(ToggleFlagPayload payload, FriendlyByteBuf buf) {
        buf.writeVarInt(payload.entityId);
        buf.writeUtf(payload.flag);
        buf.writeBoolean(payload.value);
    }

    public static ToggleFlagPayload decode(FriendlyByteBuf buf) {
        return new ToggleFlagPayload(buf.readVarInt(), buf.readUtf(), buf.readBoolean());
    }
}
