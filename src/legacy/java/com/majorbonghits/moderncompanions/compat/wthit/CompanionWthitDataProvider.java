package com.majorbonghits.moderncompanions.compat.wthit;

import com.majorbonghits.moderncompanions.compat.CompanionTooltipUtil;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import mcp.mobius.waila.api.IDataProvider;
import mcp.mobius.waila.api.IDataWriter;
import mcp.mobius.waila.api.IServerAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.nbt.CompoundTag;

/**
 * Sends companion attributes to WTHIT clients so the HUD can render them.
 */
public enum CompanionWthitDataProvider implements IDataProvider<AbstractHumanCompanionEntity> {
    INSTANCE;

    @Override
    public void appendData(IDataWriter writer, IServerAccessor<AbstractHumanCompanionEntity> accessor, IPluginConfig config) {
        AbstractHumanCompanionEntity companion = accessor.getTarget();
        if (companion == null) {
            return;
        }
        CompoundTag tag = writer.raw();
        CompanionTooltipUtil.writeAttributes(tag, companion);
    }
}
