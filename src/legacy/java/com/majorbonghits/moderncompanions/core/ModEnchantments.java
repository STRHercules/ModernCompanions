package com.majorbonghits.moderncompanions.core;

import com.majorbonghits.moderncompanions.Constants;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Enchantment keys + helpers. Enchantments themselves are data-driven (JSON under data/modern_companions/enchantment).
 */
public final class ModEnchantments {
    private ModEnchantments() {}

    public static final ResourceKey<Enchantment> EMPOWER = key("empower");
    public static final ResourceKey<Enchantment> NIMBILITY = key("nimbility");
    public static final ResourceKey<Enchantment> ENLIGHTENMENT = key("enlightenment");
    public static final ResourceKey<Enchantment> VITALITY = key("vitality");

    private static ResourceKey<Enchantment> key(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, Constants.id(path));
    }

    public static java.util.Optional<Holder<Enchantment>> holder(RegistryAccess access, ResourceKey<Enchantment> key) {
        return access.registryOrThrow(Registries.ENCHANTMENT).getHolder(key).map(h -> (Holder<Enchantment>) h);
    }
}
