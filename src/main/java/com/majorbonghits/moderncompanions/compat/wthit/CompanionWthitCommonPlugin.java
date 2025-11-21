package com.majorbonghits.moderncompanions.compat.wthit;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import mcp.mobius.waila.api.ICommonRegistrar;
import mcp.mobius.waila.api.IWailaCommonPlugin;

/**
 * Registers server-side data for WTHIT.
 */
public class CompanionWthitCommonPlugin implements IWailaCommonPlugin {
    @Override
    public void register(ICommonRegistrar registrar) {
        registrar.entityData(CompanionWthitDataProvider.INSTANCE, AbstractHumanCompanionEntity.class);
    }
}
