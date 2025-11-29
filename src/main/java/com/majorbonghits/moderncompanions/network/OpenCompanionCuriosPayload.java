package com.majorbonghits.moderncompanions.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.majorbonghits.moderncompanions.ModernCompanions;

/**
 * Client -> server request to open the Curios UI for a companion entity.
 */
public record OpenCompanionCuriosPayload(int entityId) implements CustomPacketPayload {
    public static final Type<OpenCompanionCuriosPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "open_companion_curios"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenCompanionCuriosPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            OpenCompanionCuriosPayload::entityId,
            OpenCompanionCuriosPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
