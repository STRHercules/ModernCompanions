package com.majorbonghits.moderncompanions.entity.job;

import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Jobs companions can perform. This stays data-only so both AI and UI can
 * reason about available roles without hard-coding strings everywhere.
 */
public enum CompanionJob {
    NONE("none"),
    LUMBERJACK("lumberjack"),
    HUNTER("hunter"),
    MINER("miner"),
    FISHER("fisher"),
    CHEF("chef");

    private final String id;

    CompanionJob(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public Component displayName() {
        return Component.translatable("job.modern_companions." + id);
    }

    public Component shortDescription() {
        return Component.translatable("job.modern_companions." + id + ".desc");
    }

    public static CompanionJob fromId(String raw) {
        if (raw == null || raw.isBlank()) {
            return NONE;
        }
        String normalized = raw.toLowerCase(Locale.ROOT);
        for (CompanionJob job : values()) {
            if (job.id.equals(normalized)) {
                return job;
            }
        }
        return NONE;
    }
}
