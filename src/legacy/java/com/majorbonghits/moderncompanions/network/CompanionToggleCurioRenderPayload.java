package com.majorbonghits.moderncompanions.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Client -> server: toggle render visibility for a companion's curio slot.
 */
public record CompanionToggleCurioRenderPayload(int entityId, String identifier, int index) {
    public static void encode(CompanionToggleCurioRenderPayload payload, FriendlyByteBuf buf) {
        buf.writeVarInt(payload.entityId);
        buf.writeUtf(payload.identifier);
        buf.writeVarInt(payload.index);
    }

    public static CompanionToggleCurioRenderPayload decode(FriendlyByteBuf buf) {
        return new CompanionToggleCurioRenderPayload(buf.readVarInt(), buf.readUtf(), buf.readVarInt());
    }
}
