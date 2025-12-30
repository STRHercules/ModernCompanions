package com.majorbonghits.moderncompanions.core;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.entity.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Deferred registration of all companion entity types.
 */
public final class ModEntityTypes {
    private ModEntityTypes() {}

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ModernCompanions.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<Knight>> KNIGHT =
            ENTITY_TYPES.register("knight", () -> EntityType.Builder.of(Knight::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("knight")));

    public static final DeferredHolder<EntityType<?>, EntityType<Archer>> ARCHER =
            ENTITY_TYPES.register("archer", () -> EntityType.Builder.of(Archer::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("archer")));

    public static final DeferredHolder<EntityType<?>, EntityType<Arbalist>> ARBALIST =
            ENTITY_TYPES.register("arbalist", () -> EntityType.Builder.of(Arbalist::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("arbalist")));

    public static final DeferredHolder<EntityType<?>, EntityType<Axeguard>> AXEGUARD =
            ENTITY_TYPES.register("axeguard", () -> EntityType.Builder.of(Axeguard::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("axeguard")));

    public static final DeferredHolder<EntityType<?>, EntityType<Vanguard>> VANGUARD =
            ENTITY_TYPES.register("vanguard", () -> EntityType.Builder.of(Vanguard::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("vanguard")));

    public static final DeferredHolder<EntityType<?>, EntityType<Berserker>> BERSERKER =
            ENTITY_TYPES.register("berserker", () -> EntityType.Builder.of(Berserker::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("berserker")));

    public static final DeferredHolder<EntityType<?>, EntityType<Beastmaster>> BEASTMASTER =
            ENTITY_TYPES.register("beastmaster", () -> EntityType.Builder.of(Beastmaster::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("beastmaster")));

    public static final DeferredHolder<EntityType<?>, EntityType<Cleric>> CLERIC =
            ENTITY_TYPES.register("cleric", () -> EntityType.Builder.of(Cleric::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("cleric")));

    public static final DeferredHolder<EntityType<?>, EntityType<Alchemist>> ALCHEMIST =
            ENTITY_TYPES.register("alchemist", () -> EntityType.Builder.of(Alchemist::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("alchemist")));

    public static final DeferredHolder<EntityType<?>, EntityType<Scout>> SCOUT =
            ENTITY_TYPES.register("scout", () -> EntityType.Builder.of(Scout::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("scout")));

    public static final DeferredHolder<EntityType<?>, EntityType<Stormcaller>> STORMCALLER =
            ENTITY_TYPES.register("stormcaller", () -> EntityType.Builder.of(Stormcaller::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("stormcaller")));

    public static final DeferredHolder<EntityType<?>, EntityType<FireMage>> FIRE_MAGE =
            ENTITY_TYPES.register("fire_mage", () -> EntityType.Builder.of(FireMage::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("fire_mage")));

    public static final DeferredHolder<EntityType<?>, EntityType<LightningMage>> LIGHTNING_MAGE =
            ENTITY_TYPES.register("lightning_mage", () -> EntityType.Builder.of(LightningMage::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("lightning_mage")));

    public static final DeferredHolder<EntityType<?>, EntityType<Necromancer>> NECROMANCER =
            ENTITY_TYPES.register("necromancer", () -> EntityType.Builder.of(Necromancer::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("necromancer")));

    public static final DeferredHolder<EntityType<?>, EntityType<SummonedWitherSkeleton>> SUMMONED_WITHER_SKELETON =
            ENTITY_TYPES.register("summoned_wither_skeleton", () -> EntityType.Builder.of(SummonedWitherSkeleton::new, MobCategory.MONSTER)
                    .sized(0.7F, 2.4F)
                    .build(id("summoned_wither_skeleton")));

    // Helper projectiles (client uses vanilla renderers)
    public static final DeferredHolder<EntityType<?>, EntityType<com.majorbonghits.moderncompanions.entity.projectile.NonIgnitingSmallFireball>> FIREBOLT =
            ENTITY_TYPES.register("firebolt", () -> EntityType.Builder.<com.majorbonghits.moderncompanions.entity.projectile.NonIgnitingSmallFireball>of(
                            com.majorbonghits.moderncompanions.entity.projectile.NonIgnitingSmallFireball::new, MobCategory.MISC)
                    .sized(0.3125F, 0.3125F)
                    .build(id("firebolt")));

    public static final DeferredHolder<EntityType<?>, EntityType<com.majorbonghits.moderncompanions.entity.projectile.NonExplodingLargeFireball>> FIREBURST =
            ENTITY_TYPES.register("fireburst", () -> EntityType.Builder.<com.majorbonghits.moderncompanions.entity.projectile.NonExplodingLargeFireball>of(
                            com.majorbonghits.moderncompanions.entity.projectile.NonExplodingLargeFireball::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .build(id("fireburst")));

    public static final DeferredHolder<EntityType<?>, EntityType<com.majorbonghits.moderncompanions.entity.projectile.SoftWitherSkull>> SOFT_WITHER_SKULL =
            ENTITY_TYPES.register("soft_wither_skull", () -> EntityType.Builder.<com.majorbonghits.moderncompanions.entity.projectile.SoftWitherSkull>of(
                            com.majorbonghits.moderncompanions.entity.projectile.SoftWitherSkull::new, MobCategory.MISC)
                    .sized(0.3125F, 0.3125F)
                    .build(id("soft_wither_skull")));

    public static final DeferredHolder<EntityType<?>, EntityType<com.majorbonghits.moderncompanions.entity.projectile.CompanionFishingHook>> COMPANION_FISHING_HOOK =
            ENTITY_TYPES.register("companion_fishing_hook", () -> EntityType.Builder.<com.majorbonghits.moderncompanions.entity.projectile.CompanionFishingHook>of(
                            com.majorbonghits.moderncompanions.entity.projectile.CompanionFishingHook::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build(id("companion_fishing_hook")));

    private static String id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, path).toString();
    }
}
