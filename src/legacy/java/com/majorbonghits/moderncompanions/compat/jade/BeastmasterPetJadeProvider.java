package com.majorbonghits.moderncompanions.compat.jade;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.compat.BeastmasterPetHudUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IJadeProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade overlay line for Beastmaster pets so the owner name no longer shows as ???.
 */
public enum BeastmasterPetJadeProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = new ResourceLocation(ModernCompanions.MOD_ID, "beastmaster_pet_owner");

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        String owner = data.getString(BeastmasterPetHudUtil.KEY_OWNER_NAME);
        if (!owner.isEmpty()) {
            tooltip.add(Component.literal("Owner: " + owner));
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
        if (accessor.getEntity() instanceof LivingEntity living && BeastmasterPetHudUtil.isBeastmasterPet(living)) {
            BeastmasterPetHudUtil.writeOwnerName(tag, living);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
