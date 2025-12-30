package com.majorbonghits.moderncompanions.entity.personality;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Lightweight personality/Bond container saved on each companion.
 * Keeps the core fields together so NBT and data sync stay consistent.
 */
public class CompanionPersonality {
    public static final String KEY_PRIMARY = "PrimaryTrait";
    public static final String KEY_SECONDARY = "SecondaryTrait";
    public static final String KEY_BOND_XP = "BondXP";
    public static final String KEY_BOND_LEVEL = "BondLevel";
    public static final String KEY_BACKSTORY = "BackstoryId";
    public static final String KEY_MORALE = "Morale";
    public static final String KEY_FIRST_TAMED = "Mem_FirstTamedTime";
    public static final String KEY_TOTAL_KILLS = "Mem_TotalKills";
    public static final String KEY_MAJOR_KILLS = "Mem_MajorKills";
    public static final String KEY_TIMES_DOWNED = "Mem_TimesDowned";
    public static final String KEY_TIMES_RESURRECTED = "Mem_TimesResurrected";
    public static final String KEY_DISTANCE_TRAVELED = "Mem_DistanceTraveled";
    public static final String KEY_AGE_YEARS = "AgeYears";
    public static final String KEY_LAST_AGE_CHECK = "AgeLastCheck";

    // Canonical trait ids from TASK.md
    public static final List<String> TRAITS = List.of(
            "trait_brave",
            "trait_cautious",
            "trait_guardian",
            "trait_reckless",
            "trait_stalwart",
            "trait_quickstep",
            "trait_glutton",
            "trait_disciplined",
            "trait_lucky",
            "trait_night_owl",
            "trait_sun_blessed",
            "trait_jokester",
            "trait_melancholic",
            "trait_devoted"
    );

    // Small starter backstory ids; kept data-driven friendly for later externalization.
    public static final List<String> BACKSTORIES = List.of(
            "backstory_village_guard",
            "backstory_runaway_mage",
            "backstory_exiled_noble",
            "backstory_wandering_merc",
            "backstory_ruins_scout",
            "backstory_redeemed_raider",
            "backstory_cartographer",
            "backstory_survivor",
            "backstory_city_watch_veteran",
            "backstory_temple_acolyte",
            "backstory_graveyard_keeper",
            "backstory_battlefield_medic",
            "backstory_disgraced_knight",
            "backstory_monster_hunter_apprentice",
            "backstory_smugglers_runner",
            "backstory_grave_robber_guide",
            "backstory_wandering_scholar",
            "backstory_ruined_town_refugee",
            "backstory_portal_lost_traveler",
            "backstory_beast_tamers_apprentice",
            "backstory_hermit_of_the_wilds",
            "backstory_arena_gladiator",
            "backstory_ex_cult_initiate",
            "backstory_innkeepers_child",
            "backstory_traveling_bard",
            "backstory_failed_guild_artisan",
            "backstory_cursed_treasure_seeker",
            "backstory_stormwrecked_sailor",
            "backstory_fire_scarred_militia",
            "backstory_orphan_of_the_roads",
            "backstory_silent_penitent",
            "backstory_arcane_relic_keeper",
            "backstory_siege_engineer",
            "backstory_dungeon_escapee",
            "backstory_cathedral_librarian",
            "backstory_witchwood_local",
            "backstory_reformed_poacher",
            "backstory_lost_heir",
            "backstory_memoryless_stranger",
            "backstory_moonlit_duelist",
            "backstory_clocktower_watchman"
    );

    private String primaryTrait = "";
    private String secondaryTrait = "";
    private int bondXp = 0;
    private int bondLevel = 0;
    private String backstoryId = "";
    private float morale = 0.0F;
    private long firstTamedGameTime = -1L;
    private int totalKills = 0;
    private int majorKills = 0;
    private int timesDowned = 0;
    private int timesResurrected = 0;
    private long distanceTraveledWithOwner = 0L;
    private int ageYears = 0;
    private long lastAgeCheckGameTime = -1L;

    public void saveTo(CompoundTag tag) {
        tag.putString(KEY_PRIMARY, primaryTrait);
        tag.putString(KEY_SECONDARY, secondaryTrait);
        tag.putInt(KEY_BOND_XP, bondXp);
        tag.putInt(KEY_BOND_LEVEL, bondLevel);
        tag.putString(KEY_BACKSTORY, backstoryId);
        tag.putFloat(KEY_MORALE, morale);
        tag.putLong(KEY_FIRST_TAMED, firstTamedGameTime);
        tag.putInt(KEY_TOTAL_KILLS, totalKills);
        tag.putInt(KEY_MAJOR_KILLS, majorKills);
        tag.putInt(KEY_TIMES_DOWNED, timesDowned);
        tag.putInt(KEY_TIMES_RESURRECTED, timesResurrected);
        tag.putLong(KEY_DISTANCE_TRAVELED, distanceTraveledWithOwner);
        tag.putInt(KEY_AGE_YEARS, ageYears);
        tag.putLong(KEY_LAST_AGE_CHECK, lastAgeCheckGameTime);
    }

    public void loadFrom(CompoundTag tag) {
        primaryTrait = tag.getString(KEY_PRIMARY);
        secondaryTrait = tag.getString(KEY_SECONDARY);
        bondXp = tag.getInt(KEY_BOND_XP);
        bondLevel = tag.getInt(KEY_BOND_LEVEL);
        backstoryId = tag.getString(KEY_BACKSTORY);
        morale = Mth.clamp(tag.getFloat(KEY_MORALE), -1.0F, 1.0F);
        firstTamedGameTime = tag.getLong(KEY_FIRST_TAMED);
        totalKills = tag.getInt(KEY_TOTAL_KILLS);
        majorKills = tag.getInt(KEY_MAJOR_KILLS);
        timesDowned = tag.getInt(KEY_TIMES_DOWNED);
        timesResurrected = tag.getInt(KEY_TIMES_RESURRECTED);
        distanceTraveledWithOwner = tag.getLong(KEY_DISTANCE_TRAVELED);
        ageYears = Math.max(0, tag.getInt(KEY_AGE_YEARS));
        lastAgeCheckGameTime = tag.getLong(KEY_LAST_AGE_CHECK);
        recomputeBondLevel();
    }

    public void rollTraits(RandomSource random, boolean traitsEnabled, int secondaryChancePercent) {
        if (!traitsEnabled) {
            primaryTrait = "";
            secondaryTrait = "";
            return;
        }
        if (primaryTrait.isEmpty()) {
            primaryTrait = pickRandom(random, TRAITS);
        }
        if (secondaryTrait.isEmpty() && random.nextInt(100) < Mth.clamp(secondaryChancePercent, 0, 100)) {
            List<String> pool = new ArrayList<>(TRAITS);
            pool.remove(primaryTrait);
            secondaryTrait = pickRandom(random, pool);
        }
    }

    public void rollBackstory(RandomSource random) {
        if (!backstoryId.isEmpty()) return;
        backstoryId = pickRandom(random, BACKSTORIES);
    }

    private static String pickRandom(RandomSource random, List<String> list) {
        if (list.isEmpty()) return "";
        return list.get(random.nextInt(list.size()));
    }

    public void awardBondXp(int amount) {
        if (amount <= 0) return;
        bondXp = Math.min(Integer.MAX_VALUE, bondXp + amount);
        recomputeBondLevel();
    }

    public void incrementTotalKills(boolean major) {
        totalKills = Math.min(Integer.MAX_VALUE, totalKills + 1);
        if (major) {
            majorKills = Math.min(Integer.MAX_VALUE, majorKills + 1);
        }
    }

    public void setMajorKills(int value) {
        majorKills = Math.max(0, value);
    }

    public void noteResurrection() {
        timesResurrected = Math.min(Integer.MAX_VALUE, timesResurrected + 1);
    }

    public void noteDowned() {
        timesDowned = Math.min(Integer.MAX_VALUE, timesDowned + 1);
    }

    public void setFirstTamedGameTime(long gameTime) {
        if (firstTamedGameTime <= 0) {
            firstTamedGameTime = gameTime;
        }
    }

    public void adjustMorale(float delta) {
        morale = Mth.clamp(morale + delta, -1.0F, 1.0F);
    }

    public void setMorale(float value) {
        morale = Mth.clamp(value, -1.0F, 1.0F);
    }

    private void recomputeBondLevel() {
        int[] thresholds = defaultBondThresholds();
        int newLevel = 0;
        for (int i = 0; i < thresholds.length; i++) {
            if (bondXp >= thresholds[i]) {
                newLevel = i + 1;
            } else {
                break;
            }
        }
        bondLevel = newLevel;
    }

    public int xpForNextLevel() {
        int[] thresholds = defaultBondThresholds();
        if (bondLevel >= thresholds.length) return thresholds[thresholds.length - 1];
        return thresholds[bondLevel];
    }

    private int[] defaultBondThresholds() {
        return new int[]{100, 250, 500, 1000, 2000};
    }

    public String getPrimaryTrait() {
        return primaryTrait;
    }

    public String getSecondaryTrait() {
        return secondaryTrait;
    }

    public int getBondXp() {
        return bondXp;
    }

    public int getBondLevel() {
        return bondLevel;
    }

    public String getBackstoryId() {
        return backstoryId;
    }

    public float getMorale() {
        return morale;
    }

    public long getFirstTamedGameTime() {
        return firstTamedGameTime;
    }

    public int getTotalKills() {
        return totalKills;
    }

    public int getMajorKills() {
        return majorKills;
    }

    public int getTimesDowned() {
        return timesDowned;
    }

    public int getTimesResurrected() {
        return timesResurrected;
    }

    public long getDistanceTraveledWithOwner() {
        return distanceTraveledWithOwner;
    }

    public void addDistanceTraveled(long delta) {
        distanceTraveledWithOwner = Math.min(Long.MAX_VALUE, distanceTraveledWithOwner + Math.max(0, delta));
    }

    public void setDistanceTraveled(long value) {
        distanceTraveledWithOwner = Math.max(0, value);
    }

    public void setPrimaryTrait(String trait) {
        this.primaryTrait = trait == null ? "" : trait;
    }

    public void setSecondaryTrait(String trait) {
        this.secondaryTrait = trait == null ? "" : trait;
    }

    public void setBackstoryId(String backstoryId) {
        this.backstoryId = backstoryId == null ? "" : backstoryId;
    }

    public void setBondXp(int bondXp) {
        this.bondXp = Math.max(0, bondXp);
        recomputeBondLevel();
    }

    public void setBondLevel(int bondLevel) {
        this.bondLevel = Math.max(0, bondLevel);
    }

    public int getAgeYears() {
        return ageYears;
    }

    public void setAgeYears(int years) {
        this.ageYears = Math.max(0, years);
    }

    public long getLastAgeCheckGameTime() {
        return lastAgeCheckGameTime;
    }

    public void setLastAgeCheckGameTime(long gameTime) {
        this.lastAgeCheckGameTime = gameTime;
    }

    public String moraleDescriptorKey() {
        if (morale > 0.5F) return "gui.modern_companions.morale.positive";
        if (morale < -0.5F) return "gui.modern_companions.morale.low";
        return "gui.modern_companions.morale.neutral";
    }

    public List<String> traitList() {
        if (secondaryTrait.isEmpty() || secondaryTrait.equals(primaryTrait)) {
            return primaryTrait.isEmpty() ? Collections.emptyList() : List.of(primaryTrait);
        }
        return List.of(primaryTrait, secondaryTrait);
    }
}
