package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.ModernCompanions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.UUID;

@EventBusSubscriber(modid = ModernCompanions.MOD_ID)
public final class BeastmasterEvents {
    private BeastmasterEvents() {}

    /**
     * Credit Beastmaster kill counts when their pet secures a kill.
     */
    @SubscribeEvent
    public static void onPetKill(LivingDeathEvent event) {
        Entity killer = event.getSource().getEntity();
        if (!(killer instanceof LivingEntity living)) return;
        if (!(event.getEntity().level() instanceof ServerLevel server)) return;

        var data = living.getPersistentData();
        if (!data.hasUUID(Beastmaster.BEASTMASTER_OWNER_TAG)) return;
        UUID ownerId = data.getUUID(Beastmaster.BEASTMASTER_OWNER_TAG);
        Entity owner = server.getEntity(ownerId);
        if (owner instanceof Beastmaster beastmaster) {
            beastmaster.incrementKillCount();
        }
    }
}
