package com.majorbonghits.moderncompanions.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Sets a companion's patrol radius.
 */
public record SetPatrolRadiusPayload(int entityId, int radius) {
    public static void encode(SetPatrolRadiusPayload payload, FriendlyByteBuf buf) {
        buf.writeVarInt(payload.entityId);
        buf.writeVarInt(payload.radius);
    }

    public static SetPatrolRadiusPayload decode(FriendlyByteBuf buf) {
        return new SetPatrolRadiusPayload(buf.readVarInt(), buf.readVarInt());
    }
}
