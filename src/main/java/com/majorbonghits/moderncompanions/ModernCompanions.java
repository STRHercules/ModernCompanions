package com.majorbonghits.moderncompanions;

import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.core.ModEntityAttributes;
import com.majorbonghits.moderncompanions.core.ModEntityTypes;
import com.majorbonghits.moderncompanions.core.ModItems;
import com.majorbonghits.moderncompanions.core.ModMenuTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Modern Companions entrypoint.
 * Registers all deferred registries on the mod event bus; feature logic is ported incrementally.
 */
@Mod(ModernCompanions.MOD_ID)
public class ModernCompanions {
    public static final String MOD_ID = "modern_companions";
    private static final Logger LOGGER = LoggerFactory.getLogger(ModernCompanions.class);

    public ModernCompanions() {
        final IEventBus modBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        ModEntityTypes.ENTITY_TYPES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModMenuTypes.MENU_TYPES.register(modBus);
        modBus.addListener(ModEntityAttributes::registerAttributes);

        ModConfig.register();

        LOGGER.info("Modern Companions bootstrap: registries queued.");
    }
}
