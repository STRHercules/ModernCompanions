package com.majorbonghits.moderncompanions.client.renderer;

import com.google.common.hash.Hashing;
import com.majorbonghits.moderncompanions.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight cache that downloads remote companion skins and registers them as dynamic textures.
 */
public final class CompanionSkinManager {
    private static final Map<String, ResourceLocation> URL_CACHE = new ConcurrentHashMap<>();

    private CompanionSkinManager() {}

    public static ResourceLocation getOrCreate(String url, ResourceLocation fallback) {
        return URL_CACHE.compute(url, (key, existing) -> {
            if (existing != null) return existing;
            ResourceLocation created = registerTexture(url, fallback);
            return created == null ? fallback : created;
        });
    }

    private static ResourceLocation registerTexture(String url, ResourceLocation fallback) {
        String digest = Hashing.sha1().hashString(url, StandardCharsets.UTF_8).toString();
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(
                Constants.MOD_ID, "custom_skins/" + digest);

        TextureManager manager = Minecraft.getInstance().getTextureManager();
        if (!(manager.getTexture(location) instanceof HttpTexture)) {
            HttpTexture texture = new HttpTexture(null, url, location, true, null);
            manager.register(location, texture);
            try {
                // Force immediate load so we can fall back cleanly if the download fails.
                texture.load(Minecraft.getInstance().getResourceManager());
            } catch (IOException io) {
                Constants.LOG.warn("Failed to download custom companion skin from {}", url, io);
                manager.release(location);
                return fallback;
            }
        }
        return location;
    }
}
