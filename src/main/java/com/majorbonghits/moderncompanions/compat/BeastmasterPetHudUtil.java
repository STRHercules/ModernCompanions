package com.majorbonghits.moderncompanions.compat;

import com.majorbonghits.moderncompanions.entity.Beastmaster;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Shared helpers for Jade/WTHIT so Beastmaster pets can show their owner's name.
 */
public final class BeastmasterPetHudUtil {
    public static final String KEY_OWNER_NAME = "mc_beast_owner";

    private BeastmasterPetHudUtil() {
    }

    public static boolean isBeastmasterPet(Entity entity) {
        return entity instanceof LivingEntity living && living.getPersistentData().hasUUID(Beastmaster.BEASTMASTER_OWNER_TAG);
    }

    public static boolean writeOwnerName(CompoundTag tag, LivingEntity pet) {
        String ownerName = resolveOwnerName(pet);
        if (ownerName != null && !ownerName.isEmpty()) {
            tag.putString(KEY_OWNER_NAME, ownerName);
            return true;
        }
        return false;
    }

    @Nullable
    public static String resolveOwnerName(LivingEntity pet) {
        if (!(pet.level() instanceof ServerLevel server)) {
            return null;
        }
        if (!pet.getPersistentData().hasUUID(Beastmaster.BEASTMASTER_OWNER_TAG)) {
            return null;
        }
        UUID masterId = pet.getPersistentData().getUUID(Beastmaster.BEASTMASTER_OWNER_TAG);
        Entity master = server.getEntity(masterId);
        return master != null ? master.getDisplayName().getString() : null;
    }
}
