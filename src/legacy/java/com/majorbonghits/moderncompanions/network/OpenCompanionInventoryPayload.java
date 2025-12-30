package com.majorbonghits.moderncompanions.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Client -> server request to open the default companion inventory screen.
 */
public record OpenCompanionInventoryPayload(int entityId) {
    public static void encode(OpenCompanionInventoryPayload payload, FriendlyByteBuf buf) {
        buf.writeVarInt(payload.entityId);
    }

    public static OpenCompanionInventoryPayload decode(FriendlyByteBuf buf) {
        return new OpenCompanionInventoryPayload(buf.readVarInt());
    }
}
