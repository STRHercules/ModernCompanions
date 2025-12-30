package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.core.ModConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid = ModernCompanions.MOD_ID)
public final class CompanionEvents {
    private CompanionEvents() {}

    @SubscribeEvent
    public static void giveExperience(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof AbstractHumanCompanionEntity companion && event.getEntity().level() instanceof ServerLevel) {
            companion.recordKill(event.getEntity());
            companion.giveExperiencePoints(event.getEntity().getExperienceReward());
        }
    }

    @SubscribeEvent
    public static void friendlyFire(LivingHurtEvent event) {
        var source = event.getSource();
        var direct = source.getDirectEntity();
        var attacker = source.getEntity();

        AbstractHumanCompanionEntity companion = null;
        if (attacker instanceof AbstractHumanCompanionEntity comp) {
            companion = comp;
        } else if (direct instanceof net.minecraft.world.entity.projectile.Projectile proj && proj.getOwner() instanceof AbstractHumanCompanionEntity comp) {
            companion = comp;
        }

        if (companion == null || !companion.isTame()) return;

        // Prevent harming owner
        if (!ModConfig.safeGet(ModConfig.FRIENDLY_FIRE_PLAYER) && event.getEntity() instanceof Player player) {
            if (companion.getOwner() == player) {
                event.setCanceled(true);
                return;
            }
        }

        // Prevent harming other tamed companions/pets of same owner
        if (!ModConfig.safeGet(ModConfig.FRIENDLY_FIRE_COMPANIONS)) {
            if (event.getEntity() instanceof TamableAnimal other && other.isTame() && other.getOwner() == companion.getOwner()) {
                event.setCanceled(true);
                return;
            }
            if (event.getEntity() instanceof AbstractHumanCompanionEntity otherComp && otherComp.getOwner() == companion.getOwner()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof AbstractHumanCompanionEntity companion)) return;
        if (!companion.isTame()) return;
        if (!companion.hasTrait("trait_lucky")) return;
        double chance = ModConfig.safeGet(ModConfig.LUCKY_EXTRA_DROP_CHANCE);
        if (companion.getRandom().nextDouble() >= chance) return;
        var drops = event.getDrops();
        if (drops.isEmpty()) return;
        var list = drops.stream().toList();
        var pick = list.get(companion.getRandom().nextInt(list.size()));
        if (pick.getItem().isEmpty()) return;
        var copy = pick.getItem().copy();
        copy.setCount(Math.max(1, copy.getCount()));
        var extra = new net.minecraft.world.entity.item.ItemEntity(event.getEntity().level(), pick.getX(), pick.getY(), pick.getZ(), copy);
        drops.add(extra);
    }
}
