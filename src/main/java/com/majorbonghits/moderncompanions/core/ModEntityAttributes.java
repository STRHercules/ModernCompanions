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
    }
}
