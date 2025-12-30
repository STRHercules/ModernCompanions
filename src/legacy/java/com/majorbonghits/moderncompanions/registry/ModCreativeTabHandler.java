package com.majorbonghits.moderncompanions.registry;

import com.majorbonghits.moderncompanions.Constants;
import com.majorbonghits.moderncompanions.struct.WeaponType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModCreativeTabHandler {
    private ModCreativeTabHandler() {
    }

    public static void register(IEventBus modBus) {
        // Nothing to wire directly; the static subscriber above hooks the MOD bus.
    }

    @SubscribeEvent
    public static void fillCombatTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.COMBAT) return;

        for (WeaponType type : WeaponType.values()) {
            for (Item item : ModItems.getItemsByType(type)) {
                // Forge 1.20.1 only supports appending entries via accept.
                event.accept(item);
            }
        }
    }
}
