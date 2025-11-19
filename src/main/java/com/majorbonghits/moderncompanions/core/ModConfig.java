package com.majorbonghits.moderncompanions.core;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Port of the original Human Companions common configuration.
 * TODO: reconnect the values to gameplay once entity logic is fully ported.
 */
public final class ModConfig {
    private ModConfig() {}

    public static ModConfigSpec.IntValue AVERAGE_HOUSE_SEPARATION;
    public static ModConfigSpec.BooleanValue FRIENDLY_FIRE_COMPANIONS;
    public static ModConfigSpec.BooleanValue FRIENDLY_FIRE_PLAYER;
    public static ModConfigSpec.BooleanValue FALL_DAMAGE;
    public static ModConfigSpec.BooleanValue SPAWN_ARMOR;
    public static ModConfigSpec.BooleanValue SPAWN_WEAPON;
    public static ModConfigSpec.IntValue BASE_HEALTH;
    public static ModConfigSpec.BooleanValue LOW_HEALTH_FOOD;
    public static ModConfigSpec.BooleanValue CREEPER_WARNING;

    /**
     * Safely read a config value even during very early lifecycle (e.g., attribute construction) by
     * falling back to its default when the config file has not been loaded yet.
     */
    public static <T> T safeGet(ModConfigSpec.ConfigValue<T> value) {
        try {
            return value.get();
        } catch (IllegalStateException ex) {
            return value.getDefault();
        }
    }

    public static void register() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Settings for world gen (data packs recommended in 1.21.1)").push("worldgen");
        AVERAGE_HOUSE_SEPARATION = builder
                .comment("Average chunk separation between companion houses")
                .defineInRange("averageHouseSeparation", 20, 11, Integer.MAX_VALUE);
        builder.pop();

        builder.push("companion");
        FRIENDLY_FIRE_COMPANIONS = builder
                .comment("Whether companions can hurt each other")
                .define("friendlyFireCompanions", false);
        FRIENDLY_FIRE_PLAYER = builder
                .comment("Whether companion can damage the owning player")
                .define("friendlyFirePlayer", true);
        FALL_DAMAGE = builder
                .comment("Whether companions take fall damage")
                .define("fallDamage", true);
        SPAWN_ARMOR = builder
                .comment("Whether companions spawn with random armor")
                .define("spawnArmor", true);
        SPAWN_WEAPON = builder
                .comment("Whether companions spawn with a weapon")
                .define("spawnWeapon", true);
        BASE_HEALTH = builder
                .comment("Base health for companions; a small random variance is applied on spawn")
                .defineInRange("baseHealth", 20, 5, Integer.MAX_VALUE);
        LOW_HEALTH_FOOD = builder
                .comment("If true, companions ask for food when below half health")
                .define("lowHealthFood", true);
        CREEPER_WARNING = builder
                .comment("If true, companions warn the player about nearby creepers")
                .define("creeperWarning", true);
        builder.pop();

        ModLoadingContext.get().getActiveContainer()
                .registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, builder.build());
    }
}
