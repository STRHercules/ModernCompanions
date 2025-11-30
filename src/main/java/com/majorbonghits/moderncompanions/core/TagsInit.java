package com.majorbonghits.moderncompanions.core;

import com.majorbonghits.moderncompanions.ModernCompanions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Commonly used item tags for weapon detection.
 */
public final class TagsInit {
    private TagsInit() {}

    public static final class Items {
        public static final TagKey<Item> AXES = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "axes"));
        public static final TagKey<Item> SWORDS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "swords"));
        public static final TagKey<Item> SHIELDS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", "shields"));

        private Items() {}
    }
}
