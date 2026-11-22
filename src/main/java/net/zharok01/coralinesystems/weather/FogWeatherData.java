package net.zharok01.coralinesystems.weather;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class FogWeatherData extends SavedData {

    public static final String DATA_NAME = "coraline_fog_weather";

    public boolean fogging;

    public static FogWeatherData load(CompoundTag tag) {
        FogWeatherData data = new FogWeatherData();
        data.fogging = tag.getBoolean("Fogging");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("Fogging", fogging);
        return tag;
    }
}