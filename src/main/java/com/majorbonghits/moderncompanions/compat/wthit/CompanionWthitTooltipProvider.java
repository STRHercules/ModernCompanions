package com.majorbonghits.moderncompanions.compat.wthit;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.compat.CompanionTooltipUtil;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * WTHIT tooltip provider that prints a compact attribute line on companions.
 */
public enum CompanionWthitTooltipProvider implements IEntityComponentProvider {
    INSTANCE;

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "wthit_companion_attributes");

    @Override
    public void appendBody(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof AbstractHumanCompanionEntity companion)) {
            return;
        }
        CompoundTag tag = accessor.getData().raw();
        int str = CompanionTooltipUtil.readOrFallback(tag, CompanionTooltipUtil.KEY_STR, companion.getStrength());
        int dex = CompanionTooltipUtil.readOrFallback(tag, CompanionTooltipUtil.KEY_DEX, companion.getDexterity());
        int intel = CompanionTooltipUtil.readOrFallback(tag, CompanionTooltipUtil.KEY_INT, companion.getIntelligence());
        int end = CompanionTooltipUtil.readOrFallback(tag, CompanionTooltipUtil.KEY_END, companion.getEndurance());

        Component line = CompanionTooltipUtil.buildAttributesLine(str, dex, intel, end);
        tooltip.addLine(line);
    }
}
