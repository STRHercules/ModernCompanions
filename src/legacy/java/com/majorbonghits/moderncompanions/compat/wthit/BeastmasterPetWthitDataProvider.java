package com.majorbonghits.moderncompanions.compat.wthit;

import com.majorbonghits.moderncompanions.compat.BeastmasterPetHudUtil;
import mcp.mobius.waila.api.IDataProvider;
import mcp.mobius.waila.api.IDataWriter;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import net.minecraft.world.entity.LivingEntity;

/**
 * Sends Beastmaster pet owner name to WTHIT clients so the HUD can show it.
 */
public enum BeastmasterPetWthitDataProvider implements IDataProvider<LivingEntity> {
    INSTANCE;

    @Override
    public void appendData(IDataWriter writer, IServerAccessor<LivingEntity> accessor, IPluginConfig config) {
        LivingEntity pet = accessor.getTarget();
        if (pet == null || !BeastmasterPetHudUtil.isBeastmasterPet(pet)) {
            return;
        }
        BeastmasterPetHudUtil.writeOwnerName(writer.raw(), pet);
    }
}
