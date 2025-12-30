package com.majorbonghits.moderncompanions.core;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.menu.CompanionCuriosMenu;
import com.majorbonghits.moderncompanions.menu.CompanionMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

/**
 * Menu registrations.
 */
public final class ModMenuTypes {
    private ModMenuTypes() {}

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, ModernCompanions.MOD_ID);

    public static final RegistryObject<MenuType<CompanionMenu>> COMPANION_MENU =
            MENU_TYPES.register("companion_menu",
                    () -> IForgeMenuType.create((windowId, inv, buf) -> new CompanionMenu(windowId, inv, buf.readVarInt())));

    /**
     * Populated when Curios is present; left null otherwise to avoid classloading Curios types.
     */
    public static RegistryObject<MenuType<CompanionCuriosMenu>> COMPANION_CURIOS_MENU;

    public static void registerCuriosMenu() {
        if (COMPANION_CURIOS_MENU != null) {
            return;
        }
        COMPANION_CURIOS_MENU = MENU_TYPES.register("companion_curios_menu",
                () -> IForgeMenuType.create((windowId, inv, buf) -> new CompanionCuriosMenu(windowId, inv, buf == null ? -1 : buf.readVarInt())));
    }
}
