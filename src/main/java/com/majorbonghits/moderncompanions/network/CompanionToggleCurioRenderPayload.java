package com.majorbonghits.moderncompanions.network;

import com.majorbonghits.moderncompanions.ModernCompanions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client -> server: toggle render visibility for a companion's curio slot.
 */
public record CompanionToggleCurioRenderPayload(int entityId, String identifier, int index) implements CustomPacketPayload {
    public static final Type<CompanionToggleCurioRenderPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "toggle_companion_curio_render"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CompanionToggleCurioRenderPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CompanionToggleCurioRenderPayload::entityId,
            ByteBufCodecs.STRING_UTF8, CompanionToggleCurioRenderPayload::identifier,
            ByteBufCodecs.VAR_INT, CompanionToggleCurioRenderPayload::index,
            CompanionToggleCurioRenderPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
