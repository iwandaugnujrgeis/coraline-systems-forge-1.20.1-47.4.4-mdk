package net.zharok01.coralinesystems.worldgen.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record ShoreLilypadConfiguration(UniformInt count) implements FeatureConfiguration {
    public static final Codec<ShoreLilypadConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UniformInt.CODEC.fieldOf("count").forGetter(ShoreLilypadConfiguration::count)
            ).apply(instance, ShoreLilypadConfiguration::new)
    );
}