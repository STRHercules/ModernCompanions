package com.majorbonghits.moderncompanions.core;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

/**
 * Registers entity attribute sets.
 */
public final class ModEntityAttributes {
    private ModEntityAttributes() {}

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        var attrs = AbstractHumanCompanionEntity.createAttributes().build();
        event.put(ModEntityTypes.KNIGHT.get(), attrs);
        event.put(ModEntityTypes.ARCHER.get(), attrs);
        event.put(ModEntityTypes.ARBALIST.get(), attrs);
        event.put(ModEntityTypes.AXEGUARD.get(), attrs);
        event.put(ModEntityTypes.VANGUARD.get(), attrs);
        event.put(ModEntityTypes.BERSERKER.get(), attrs);
        event.put(ModEntityTypes.BEASTMASTER.get(), attrs);
        event.put(ModEntityTypes.CLERIC.get(), attrs);
        event.put(ModEntityTypes.ALCHEMIST.get(), attrs);
        event.put(ModEntityTypes.SCOUT.get(), attrs);
        event.put(ModEntityTypes.STORMCALLER.get(), attrs);
        event.put(ModEntityTypes.FIRE_MAGE.get(), attrs);
        event.put(ModEntityTypes.LIGHTNING_MAGE.get(), attrs);
        event.put(ModEntityTypes.NECROMANCER.get(), attrs);
        event.put(ModEntityTypes.SUMMONED_WITHER_SKELETON.get(), net.minecraft.world.entity.monster.WitherSkeleton.createAttributes().build());
        // Projectile entities have no attributes
    }
}
