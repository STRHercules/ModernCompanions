package com.majorbonghits.moderncompanions.client.renderer;

import com.google.common.hash.Hashing;
import com.majorbonghits.moderncompanions.ModernCompanions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight cache that downloads remote companion skins and registers them as dynamic textures.
 */
public final class CompanionSkinManager {
    private static final Map<String, ResourceLocation> URL_CACHE = new ConcurrentHashMap<>();

    private CompanionSkinManager() {}

    public static ResourceLocation getOrCreate(String url) {
        return URL_CACHE.computeIfAbsent(url, CompanionSkinManager::registerTexture);
    }

    private static ResourceLocation registerTexture(String url) {
        String digest = Hashing.sha1().hashString(url, StandardCharsets.UTF_8).toString();
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(
                ModernCompanions.MOD_ID, "custom_skins/" + digest);

        TextureManager manager = Minecraft.getInstance().getTextureManager();
        if (!(manager.getTexture(location) instanceof HttpTexture)) {
            // HttpTexture handles async download + upload on the render thread.
            HttpTexture texture = new HttpTexture(null, url, location, false, null);
            manager.register(location, texture);
        }
        return location;
    }
}
