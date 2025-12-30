package com.majorbonghits.moderncompanions.compat.jade;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.compat.jade.BeastmasterPetJadeProvider;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade entrypoint registering the companion attribute tooltip.
 */
@WailaPlugin
public class CompanionJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerEntityDataProvider(CompanionJadeProvider.INSTANCE, AbstractHumanCompanionEntity.class);
        registration.registerEntityDataProvider(BeastmasterPetJadeProvider.INSTANCE, LivingEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(CompanionJadeProvider.INSTANCE, AbstractHumanCompanionEntity.class);
        registration.registerEntityComponent(BeastmasterPetJadeProvider.INSTANCE, LivingEntity.class);
    }
}
