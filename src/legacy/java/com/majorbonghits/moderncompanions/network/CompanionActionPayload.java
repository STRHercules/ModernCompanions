package com.majorbonghits.moderncompanions.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Generic companion command payload (release, clear target, cycle orders).
 */
public record CompanionActionPayload(int entityId, String action) {
    public static void encode(CompanionActionPayload payload, FriendlyByteBuf buf) {
        buf.writeVarInt(payload.entityId);
        buf.writeUtf(payload.action);
    }

    public static CompanionActionPayload decode(FriendlyByteBuf buf) {
        return new CompanionActionPayload(buf.readVarInt(), buf.readUtf());
    }
}
