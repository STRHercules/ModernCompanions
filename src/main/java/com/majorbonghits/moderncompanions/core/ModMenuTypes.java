package com.majorbonghits.moderncompanions.core;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.menu.CompanionMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Menu registrations.
 */
public final class ModMenuTypes {
    private ModMenuTypes() {}

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, ModernCompanions.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<CompanionMenu>> COMPANION_MENU =
            MENU_TYPES.register("companion_menu",
                    () -> IMenuTypeExtension.create((windowId, inv, buf) -> new CompanionMenu(windowId, inv, buf.readVarInt())));
}
