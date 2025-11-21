package com.majorbonghits.moderncompanions;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Constants {
    public static final String MOD_ID = "modern_companions";
    public static final String MOD_NAME = "Modern Companions";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    /** ResourceLocation used for the reach attribute modifier on custom weapons. */
    public static final ResourceLocation REACH_MODIFIER_ID = id("reach_modifier");

    private Constants() {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
