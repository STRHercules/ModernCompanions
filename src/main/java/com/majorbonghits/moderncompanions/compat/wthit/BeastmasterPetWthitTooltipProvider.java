package com.majorbonghits.moderncompanions.compat.wthit;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.compat.BeastmasterPetHudUtil;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * WTHIT tooltip line for Beastmaster pets showing their master's name.
 */
public enum BeastmasterPetWthitTooltipProvider implements IEntityComponentProvider {
    INSTANCE;

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "wthit_beastmaster_owner");

    @Override
    public void appendBody(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
        String owner = accessor.getData().raw().getString(BeastmasterPetHudUtil.KEY_OWNER_NAME);
        if (!owner.isEmpty()) {
            tooltip.addLine(Component.literal("Owner: " + owner));
        }
    }
}
