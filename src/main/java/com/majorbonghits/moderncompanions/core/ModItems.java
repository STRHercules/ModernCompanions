package com.majorbonghits.moderncompanions.core;

import com.majorbonghits.moderncompanions.ModernCompanions;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Deferred registration of mod items (currently companion spawn eggs).
 */
public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, ModernCompanions.MOD_ID);

    public static final DeferredHolder<Item, Item> ARBALIST_SPAWN_EGG = ITEMS.register("arbalist_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.ARBALIST, 0xE8AF5A, 0xFF0000, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ARCHER_SPAWN_EGG = ITEMS.register("archer_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.ARCHER, 0xE8AF5A, 0x0000FF, new Item.Properties()));

    public static final DeferredHolder<Item, Item> AXEGUARD_SPAWN_EGG = ITEMS.register("axeguard_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.AXEGUARD, 0xE8AF5A, 0x00FF00, new Item.Properties()));

    public static final DeferredHolder<Item, Item> KNIGHT_SPAWN_EGG = ITEMS.register("knight_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.KNIGHT, 0xE8AF5A, 0xFFFF00, new Item.Properties()));
}
