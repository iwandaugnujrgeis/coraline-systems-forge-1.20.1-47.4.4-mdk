package net.zharok01.coralinesystems.client.audio;

import com.mojang.serialization.Codec;
import net.minecraft.client.OptionInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;

@OnlyIn(Dist.CLIENT)
public class MusicFrequencySettings {

    public static final OptionInstance<MusicFrequency> INSTANCE = new OptionInstance<>(
            "options.coraline_systems.music_frequency",
            OptionInstance.noTooltip(),
            (caption, value) -> value.getCaption(),
            new OptionInstance.Enum<>(Arrays.asList(MusicFrequency.values()), Codec.INT.xmap(MusicFrequency::byId, MusicFrequency::getId)),
            MusicFrequency.RESERVED,
            frequency -> {}
    );
}