package com.majorbonghits.moderncompanions.compat.wthit;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.world.entity.LivingEntity;
import com.majorbonghits.moderncompanions.compat.wthit.BeastmasterPetWthitDataProvider;
import mcp.mobius.waila.api.ICommonRegistrar;
import mcp.mobius.waila.api.IWailaCommonPlugin;

/**
 * Registers server-side data for WTHIT.
 */
public class CompanionWthitCommonPlugin implements IWailaCommonPlugin {
    @Override
    public void register(ICommonRegistrar registrar) {
        registrar.entityData(CompanionWthitDataProvider.INSTANCE, AbstractHumanCompanionEntity.class);
        registrar.entityData(BeastmasterPetWthitDataProvider.INSTANCE, LivingEntity.class);
    }
}
