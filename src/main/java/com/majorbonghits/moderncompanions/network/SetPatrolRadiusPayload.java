package com.majorbonghits.moderncompanions.network;

import com.majorbonghits.moderncompanions.ModernCompanions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sets a companion's patrol radius.
 */
public record SetPatrolRadiusPayload(int entityId, int radius) implements CustomPacketPayload {
    public static final Type<SetPatrolRadiusPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "set_patrol_radius"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetPatrolRadiusPayload> CODEC =
            StreamCodec.of(SetPatrolRadiusPayload::encode, SetPatrolRadiusPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, SetPatrolRadiusPayload payload) {
        buf.writeVarInt(payload.entityId);
        buf.writeVarInt(payload.radius);
    }

    private static SetPatrolRadiusPayload decode(RegistryFriendlyByteBuf buf) {
        return new SetPatrolRadiusPayload(buf.readVarInt(), buf.readVarInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
