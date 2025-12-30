package com.majorbonghits.moderncompanions.network;

import com.majorbonghits.moderncompanions.ModernCompanions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client -> server payload for updating a companion's job assignment.
 */
public record SetCompanionJobPayload(int entityId, String jobId) implements CustomPacketPayload {
    public static final Type<SetCompanionJobPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "set_job"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetCompanionJobPayload> CODEC =
            StreamCodec.of(SetCompanionJobPayload::encode, SetCompanionJobPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, SetCompanionJobPayload payload) {
        buf.writeVarInt(payload.entityId);
        buf.writeUtf(payload.jobId);
    }

    private static SetCompanionJobPayload decode(RegistryFriendlyByteBuf buf) {
        return new SetCompanionJobPayload(buf.readVarInt(), buf.readUtf());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
