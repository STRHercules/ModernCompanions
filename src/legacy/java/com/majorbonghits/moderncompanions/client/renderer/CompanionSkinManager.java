package com.majorbonghits.moderncompanions.client.renderer;

import com.google.common.hash.Hashing;
import com.majorbonghits.moderncompanions.Constants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.platform.NativeImage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight cache that downloads remote companion skins and registers them as dynamic textures.
 */
public final class CompanionSkinManager {
    private static final Map<String, ResourceLocation> URL_CACHE = new ConcurrentHashMap<>();
    private static final Set<String> IN_FLIGHT = ConcurrentHashMap.newKeySet();

    private CompanionSkinManager() {}

    public static ResourceLocation getOrCreate(String url, ResourceLocation fallback) {
        ResourceLocation cached = URL_CACHE.get(url);
        if (cached != null) {
            return cached;
        }

        // Kick off a background download the first time we see this URL; until it finishes, keep using fallback.
        if (IN_FLIGHT.add(url)) {
            CompletableFuture.runAsync(() -> downloadAndRegister(url, fallback), Util.backgroundExecutor());
        }
        return fallback;
    }

    private static void downloadAndRegister(String url, ResourceLocation fallback) {
        String digest = Hashing.sha1().hashString(url, StandardCharsets.UTF_8).toString();
        ResourceLocation location = new ResourceLocation(Constants.MOD_ID, "custom_skins/" + digest);
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection(Minecraft.getInstance().getProxy());
            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(10000);

            int code = conn.getResponseCode();
            if (code / 100 != 2) {
                Constants.LOG.warn("Custom companion skin download failed (HTTP {}): {}", code, url);
                return;
            }

            InputStream in = conn.getInputStream();
            NativeImage image = NativeImage.read(in);
            if (image == null) {
                Constants.LOG.warn("Custom companion skin download returned null image: {}", url);
                URL_CACHE.put(url, fallback);
                return;
            }
            int w = image.getWidth();
            int h = image.getHeight();
            if (w != 64 || (h != 64 && h != 32)) {
                Constants.LOG.warn("Custom companion skin has invalid size {}x{} (expected 64x64 or 64x32): {}", w, h, url);
                image.close();
                URL_CACHE.put(url, fallback);
                return;
            }

            NativeImage prepared = image;
            if (h == 32) {
                // Expand legacy 64x32 into 64x64 like vanilla HttpTexture would.
                prepared = new NativeImage(64, 64, true);
                prepared.copyFrom(image);
                image.close();
                prepared.fillRect(0, 32, 64, 32, 0);
            }

            final NativeImage readyImage = prepared;
            Minecraft.getInstance().execute(() -> {
                try {
                    TextureManager manager = Minecraft.getInstance().getTextureManager();
                    DynamicTexture dyn = new DynamicTexture(readyImage);
                    manager.register(location, dyn);
                    URL_CACHE.put(url, location);
                } catch (IllegalStateException ex) {
                    // If the image somehow got freed, log and fall back.
                    Constants.LOG.warn("Failed to bind custom companion skin (image freed): {}", url, ex);
                    readyImage.close();
                    URL_CACHE.put(url, fallback);
                }
            });
        } catch (IOException io) {
            Constants.LOG.warn("Failed to download custom companion skin from {}", url, io);
            URL_CACHE.put(url, fallback);
        } finally {
            IN_FLIGHT.remove(url);
        }
    }
}
