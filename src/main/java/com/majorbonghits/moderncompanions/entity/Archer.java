package com.majorbonghits.moderncompanions.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class Archer extends AbstractHumanCompanionEntity {
    public Archer(EntityType<? extends AbstractHumanCompanionEntity> type, Level level) {
        super(type, level);
    }
}
