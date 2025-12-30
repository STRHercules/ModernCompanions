package com.majorbonghits.moderncompanions.compat.jade;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.compat.CompanionTooltipUtil;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IJadeProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade HUD provider that shows compact STR/DEX/INT/END on companions.
 */
public enum CompanionJadeProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = new ResourceLocation(ModernCompanions.MOD_ID, "companion_attributes");

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof AbstractHumanCompanionEntity companion)) {
            return;
        }
        CompoundTag data = accessor.getServerData();
        int str = CompanionTooltipUtil.readOrFallback(data, CompanionTooltipUtil.KEY_STR, companion.getStrength());
        int dex = CompanionTooltipUtil.readOrFallback(data, CompanionTooltipUtil.KEY_DEX, companion.getDexterity());
        int intel = CompanionTooltipUtil.readOrFallback(data, CompanionTooltipUtil.KEY_INT, companion.getIntelligence());
        int end = CompanionTooltipUtil.readOrFallback(data, CompanionTooltipUtil.KEY_END, companion.getEndurance());
        Component line = CompanionTooltipUtil.buildAttributesLine(str, dex, intel, end);
        tooltip.add(line);
    }

    @Override
    public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
        if (accessor.getEntity() instanceof AbstractHumanCompanionEntity companion) {
            CompanionTooltipUtil.writeAttributes(tag, companion);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
