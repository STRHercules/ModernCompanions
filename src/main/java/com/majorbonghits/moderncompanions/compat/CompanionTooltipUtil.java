package com.majorbonghits.moderncompanions.compat;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

/**
 * Small formatter/helper shared by Jade and WTHIT integrations to keep the
 * attribute tooltip identical across both overlays.
 */
public final class CompanionTooltipUtil {
    public static final String KEY_STR = "mc_str";
    public static final String KEY_DEX = "mc_dex";
    public static final String KEY_INT = "mc_int";
    public static final String KEY_END = "mc_end";

    private CompanionTooltipUtil() {
    }

    public static void writeAttributes(CompoundTag tag, AbstractHumanCompanionEntity companion) {
        tag.putInt(KEY_STR, companion.getStrength());
        tag.putInt(KEY_DEX, companion.getDexterity());
        tag.putInt(KEY_INT, companion.getIntelligence());
        tag.putInt(KEY_END, companion.getEndurance());
    }

    public static Component buildAttributesLine(AbstractHumanCompanionEntity companion) {
        return buildAttributesLine(companion.getStrength(), companion.getDexterity(), companion.getIntelligence(), companion.getEndurance());
    }

    public static Component buildAttributesLine(int strength, int dexterity, int intelligence, int endurance) {
        return Component.literal(String.format("S:%d | D:%d | I:%d | E:%d", strength, dexterity, intelligence, endurance));
    }

    public static int readOrFallback(CompoundTag tag, String key, int fallback) {
        return tag.contains(key) ? tag.getInt(key) : fallback;
    }
}
