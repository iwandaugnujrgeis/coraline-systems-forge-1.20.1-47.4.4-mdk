package net.zharok01.coralinesystems.client.audio;

import net.minecraft.util.OptionEnum;
import org.jetbrains.annotations.NotNull;

public enum MusicFrequency implements OptionEnum {
    NON_STOP(0, 100, 200, "options.coraline_systems.music_frequency.non_stop"),
    FREQUENT(1, 4800, 7200, "options.coraline_systems.music_frequency.frequent"),
    RESERVED(2, 12000, 24000, "options.coraline_systems.music_frequency.reserved");

    private final int id;
    public final int minDelay;
    public final int maxDelay;
    private final String key;

    MusicFrequency(int id, int minDelay, int maxDelay, String key) {
        this.id = id;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.key = key;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public @NotNull String getKey() {
        return this.key;
    }

    public static MusicFrequency byId(int id) {
        for (MusicFrequency value : values()) {
            if (value.id == id) return value;
        }
        return RESERVED;
    }
}