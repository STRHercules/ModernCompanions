package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.core.ModConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = ModernCompanions.MOD_ID)
public final class CompanionEvents {
    private CompanionEvents() {}

    @SubscribeEvent
    public static void giveExperience(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof AbstractHumanCompanionEntity companion && event.getEntity().level() instanceof ServerLevel serverLevel) {
            companion.incrementKillCount(); // keep kill counter in sync for GUI/overlays
            companion.giveExperiencePoints(event.getEntity().getExperienceReward(serverLevel, companion));
        }
    }

    @SubscribeEvent
    public static void friendlyFire(LivingIncomingDamageEvent event) {
        if (event.getSource().getDirectEntity() instanceof AbstractHumanCompanionEntity companion && companion.isTame()) {
            if (!ModConfig.safeGet(ModConfig.FRIENDLY_FIRE_PLAYER) && event.getEntity() instanceof Player player) {
                if (companion.getOwner() == player) {
                    event.setCanceled(true);
                    return;
                }
            }
            if (!ModConfig.safeGet(ModConfig.FRIENDLY_FIRE_COMPANIONS) && event.getEntity() instanceof TamableAnimal other && other.isTame()) {
                if (other.getOwner() == companion.getOwner()) {
                    event.setCanceled(true);
                }
            }
        }
    }
}
