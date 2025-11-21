package com.majorbonghits.moderncompanions.compat.wthit;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IWailaClientPlugin;

/**
 * Registers client-side tooltip rendering for WTHIT.
 */
public class CompanionWthitClientPlugin implements IWailaClientPlugin {
    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(CompanionWthitTooltipProvider.INSTANCE, AbstractHumanCompanionEntity.class);
    }
}
