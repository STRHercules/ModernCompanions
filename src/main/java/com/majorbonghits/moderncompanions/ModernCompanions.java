package com.majorbonghits.moderncompanions;

import com.majorbonghits.moderncompanions.registry.ModCreativeTabHandler;
import com.majorbonghits.moderncompanions.core.ModEntityTypes;
import com.majorbonghits.moderncompanions.core.ModMenuTypes;
import com.majorbonghits.moderncompanions.core.ModEntityAttributes;
import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.registry.ModCreativeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Constants.MOD_ID)
public final class ModernCompanions {
    /** Kept for legacy references across the codebase. */
    public static final String MOD_ID = Constants.MOD_ID;

    public ModernCompanions(IEventBus modBus) {
        // Ensure config values are registered before any entities/spawn eggs are constructed.
        ModConfig.register();

        com.majorbonghits.moderncompanions.registry.ModItems.register(modBus); // weapons
        com.majorbonghits.moderncompanions.core.ModItems.ITEMS.register(modBus); // spawn eggs
        ModCreativeTabs.register(modBus); // dedicated creative tab
        ModEntityTypes.ENTITY_TYPES.register(modBus);
        ModMenuTypes.MENU_TYPES.register(modBus);
        modBus.addListener(ModEntityAttributes::registerAttributes);
        modBus.addListener(this::onCommonSetup);
        ModCreativeTabHandler.register(modBus);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        // Currently nothing to execute; kept for symmetry with upstream weapon setup hooks.
    }
}
