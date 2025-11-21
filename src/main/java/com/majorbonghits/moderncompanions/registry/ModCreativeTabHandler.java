package com.majorbonghits.moderncompanions.registry;

import com.majorbonghits.moderncompanions.Constants;
import com.majorbonghits.moderncompanions.struct.WeaponType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

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

        Item lastItem = Items.NETHERITE_AXE;
        for (WeaponType type : WeaponType.values()) {
            for (Item item : ModItems.getItemsByType(type)) {
                event.insertAfter(new ItemStack(lastItem), new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                lastItem = item;
            }
        }
    }
}
