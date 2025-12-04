package com.majorbonghits.moderncompanions.core;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

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
    public static ModConfigSpec.BooleanValue TRAITS_ENABLED;
    public static ModConfigSpec.IntValue SECONDARY_TRAIT_CHANCE;
    public static ModConfigSpec.BooleanValue BOND_ENABLED;
    public static ModConfigSpec.BooleanValue MORALE_ENABLED;
    public static ModConfigSpec.IntValue BOND_TICK_INTERVAL;
    public static ModConfigSpec.IntValue BOND_TIME_XP;
    public static ModConfigSpec.IntValue BOND_FEED_XP;
    public static ModConfigSpec.IntValue BOND_RESURRECT_XP;
    public static ModConfigSpec.DoubleValue MORALE_FEED_DELTA;
    public static ModConfigSpec.DoubleValue MORALE_NEAR_DEATH_DELTA;
    public static ModConfigSpec.DoubleValue MORALE_RESURRECT_DELTA;
    public static ModConfigSpec.DoubleValue MORALE_BOND_LEVEL_DELTA;
    public static ModConfigSpec.DoubleValue LUCKY_EXTRA_DROP_CHANCE;
    public static ModConfigSpec.BooleanValue JOB_LUMBERJACK_ENABLED;
    public static ModConfigSpec.IntValue JOB_LUMBERJACK_RADIUS;
    public static ModConfigSpec.BooleanValue JOB_HUNTER_ENABLED;
    public static ModConfigSpec.IntValue JOB_HUNTER_RADIUS;
    public static ModConfigSpec.BooleanValue JOB_MINER_ENABLED;
    public static ModConfigSpec.IntValue JOB_MINER_RADIUS;
    public static ModConfigSpec.BooleanValue JOB_FISHER_ENABLED;
    public static ModConfigSpec.IntValue JOB_FISHER_RADIUS;
    public static ModConfigSpec.BooleanValue JOB_CHEF_ENABLED;
    public static ModConfigSpec.IntValue JOB_CHEF_RADIUS;
    public static ModConfigSpec.ConfigValue<List<? extends String>> JOB_MINER_ALLOW_BLOCKS;
    public static ModConfigSpec.ConfigValue<List<? extends String>> JOB_MINER_DENY_BLOCKS;
    public static ModConfigSpec.BooleanValue JOB_ASSIGNED_CHESTS_CHUNKLOAD;

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

        builder.push("personality");
        TRAITS_ENABLED = builder
                .comment("Enable birth traits for companions (Primary/Secondary).")
                .define("traitsEnabled", true);
        SECONDARY_TRAIT_CHANCE = builder
                .comment("Chance (percent) for a companion to roll a secondary trait at spawn.")
                .defineInRange("secondaryTraitChance", 40, 0, 100);
        BOND_ENABLED = builder
                .comment("Enable the Bond/Loyalty track.")
                .define("bondEnabled", true);
        MORALE_ENABLED = builder
                .comment("Enable morale tracking and small performance nudges.")
                .define("moraleEnabled", true);
        BOND_TICK_INTERVAL = builder
                .comment("Ticks between passive bond XP awards while near the owner (20 ticks = 1 second).")
                .defineInRange("bondTickInterval", 1200, 20, Integer.MAX_VALUE);
        BOND_TIME_XP = builder
                .comment("Bond XP granted each interval when alive near the owner.")
                .defineInRange("bondTimeXp", 5, 0, 10000);
        BOND_FEED_XP = builder
                .comment("Bond XP granted when the owner feeds the companion.")
                .defineInRange("bondFeedXp", 15, 0, 10000);
        BOND_RESURRECT_XP = builder
                .comment("Bond XP granted when resurrecting a companion.")
                .defineInRange("bondResurrectXp", 80, 0, 100000);
        MORALE_FEED_DELTA = builder
                .comment("Morale change applied when the companion is fed by the owner.")
                .defineInRange("moraleFeedDelta", 0.05D, -1.0D, 1.0D);
        MORALE_NEAR_DEATH_DELTA = builder
                .comment("Morale change applied when the companion nearly dies.")
                .defineInRange("moraleNearDeathDelta", -0.07D, -1.0D, 1.0D);
        MORALE_RESURRECT_DELTA = builder
                .comment("Morale change applied when resurrected.")
                .defineInRange("moraleResurrectDelta", -0.1D, -1.0D, 1.0D);
        MORALE_BOND_LEVEL_DELTA = builder
                .comment("Morale change applied on bond level up.")
                .defineInRange("moraleBondLevelDelta", 0.05D, -1.0D, 1.0D);
        LUCKY_EXTRA_DROP_CHANCE = builder
                .comment("Chance for Lucky trait companions to duplicate one dropped item on a kill (0.0-1.0).")
                .defineInRange("luckyExtraDropChance", 0.05D, 0.0D, 1.0D);
        builder.pop();

        builder.push("jobs");
        JOB_LUMBERJACK_ENABLED = builder
                .comment("Enable the Lumberjack job behaviors.")
                .define("lumberjackEnabled", true);
        JOB_LUMBERJACK_RADIUS = builder
                .comment("Search radius for Lumberjack log scans.")
                .defineInRange("lumberjackRadius", 10, 4, 64);
        JOB_HUNTER_ENABLED = builder
                .comment("Enable the Hunter job behaviors.")
                .define("hunterEnabled", true);
        JOB_HUNTER_RADIUS = builder
                .comment("Search radius for Hunter target scans.")
                .defineInRange("hunterRadius", 20, 6, 64);
        JOB_MINER_ENABLED = builder
                .comment("Enable the Miner job behaviors.")
                .define("minerEnabled", true);
        JOB_MINER_RADIUS = builder
                .comment("Search radius for Miner exposed block scans.")
                .defineInRange("minerRadius", 8, 4, 32);
        JOB_MINER_ALLOW_BLOCKS = builder
                .comment("Optional whitelist of block ids the Miner may break (empty uses default tags).")
                .defineList("minerAllowBlocks", List::of, o -> o instanceof String);
        JOB_MINER_DENY_BLOCKS = builder
                .comment("Blacklist of block ids the Miner should never break.")
                .defineList("minerDenyBlocks", () -> List.of("minecraft:chest", "minecraft:spawner"), o -> o instanceof String);
        JOB_FISHER_ENABLED = builder
                .comment("Enable the Fisher job behaviors.")
                .define("fisherEnabled", true);
        JOB_FISHER_RADIUS = builder
                .comment("Search radius for Fisher water spot scans.")
                .defineInRange("fisherRadius", 10, 4, 32);
        JOB_CHEF_ENABLED = builder
                .comment("Enable the Chef job behaviors.")
                .define("chefEnabled", true);
        JOB_CHEF_RADIUS = builder
                .comment("Search radius for Chef heat source scans.")
                .defineInRange("chefRadius", 8, 3, 24);
        JOB_ASSIGNED_CHESTS_CHUNKLOAD = builder
                .comment("If true, companions keep their assigned drop-off chests chunk-loaded to prevent courier failures.")
                .define("assignedChestsChunkload", false);
        builder.pop();

        ModLoadingContext.get().getActiveContainer()
                .registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, builder.build());
    }
}
