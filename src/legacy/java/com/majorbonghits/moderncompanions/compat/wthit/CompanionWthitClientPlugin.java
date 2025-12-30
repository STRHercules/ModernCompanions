package com.majorbonghits.moderncompanions.compat.wthit;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.world.entity.LivingEntity;
import com.majorbonghits.moderncompanions.compat.wthit.BeastmasterPetWthitTooltipProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IWailaClientPlugin;

/**
 * Registers client-side tooltip rendering for WTHIT.
 */
public class CompanionWthitClientPlugin implements IWailaClientPlugin {
    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(CompanionWthitTooltipProvider.INSTANCE, AbstractHumanCompanionEntity.class);
        registrar.body(BeastmasterPetWthitTooltipProvider.INSTANCE, LivingEntity.class);
    }
}
