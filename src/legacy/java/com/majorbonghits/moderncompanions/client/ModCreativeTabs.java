package com.majorbonghits.moderncompanions.client;

import com.majorbonghits.moderncompanions.core.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

import static com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID;

/**
 * Adds companion spawn eggs to the vanilla spawn eggs tab.
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = MOD_ID)
public final class ModCreativeTabs {
    private ModCreativeTabs() {}

    @SubscribeEvent
    public static void addItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.ARBALIST_SPAWN_EGG.get());
            event.accept(ModItems.KNIGHT_SPAWN_EGG.get());
            event.accept(ModItems.ARCHER_SPAWN_EGG.get());
            event.accept(ModItems.AXEGUARD_SPAWN_EGG.get());
            event.accept(ModItems.VANGUARD_SPAWN_EGG.get());
            event.accept(ModItems.BERSERKER_SPAWN_EGG.get());
            event.accept(ModItems.BEASTMASTER_SPAWN_EGG.get());
            event.accept(ModItems.CLERIC_SPAWN_EGG.get());
            event.accept(ModItems.ALCHEMIST_SPAWN_EGG.get());
            event.accept(ModItems.SCOUT_SPAWN_EGG.get());
            event.accept(ModItems.STORMCALLER_SPAWN_EGG.get());
        }
    }
}
