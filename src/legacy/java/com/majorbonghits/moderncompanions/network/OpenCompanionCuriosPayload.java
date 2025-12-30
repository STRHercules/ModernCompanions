package com.majorbonghits.moderncompanions.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Client -> server request to open the Curios UI for a companion entity.
 */
public record OpenCompanionCuriosPayload(int entityId) {
    public static void encode(OpenCompanionCuriosPayload payload, FriendlyByteBuf buf) {
        buf.writeVarInt(payload.entityId);
    }

    public static OpenCompanionCuriosPayload decode(FriendlyByteBuf buf) {
        return new OpenCompanionCuriosPayload(buf.readVarInt());
    }
}
