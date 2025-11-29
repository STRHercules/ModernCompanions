package com.majorbonghits.moderncompanions.network;

import com.majorbonghits.moderncompanions.ModernCompanions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client -> server request to open the default companion inventory screen.
 */
public record OpenCompanionInventoryPayload(int entityId) implements CustomPacketPayload {
    public static final Type<OpenCompanionInventoryPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "open_companion_inventory"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenCompanionInventoryPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            OpenCompanionInventoryPayload::entityId,
            OpenCompanionInventoryPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
