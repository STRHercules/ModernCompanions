package com.majorbonghits.moderncompanions.compat.curios;

import com.majorbonghits.moderncompanions.core.ModMenuTypes;
import net.neoforged.bus.api.IEventBus;

/**
 * Registers Curios-specific hooks only when Curios is present to keep the base mod optional.
 */
public final class CuriosCompat {
    private CuriosCompat() {}

    public static void register(IEventBus modBus, boolean isClient) {
        ModMenuTypes.registerCuriosMenu();
        modBus.addListener(CuriosNetwork::register);

        if (isClient) {
            modBus.addListener(CuriosClientEvents::onRegisterMenus);
            modBus.addListener(CuriosClientEvents::onAddLayers);
        }
    }
}
