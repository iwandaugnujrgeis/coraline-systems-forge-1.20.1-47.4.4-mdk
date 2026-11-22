package net.zharok01.coralinesystems.worldgen.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record FallenTreeConfiguration(BlockStateProvider log) implements FeatureConfiguration {
    public static final Codec<FallenTreeConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockStateProvider.CODEC.fieldOf("log").forGetter(FallenTreeConfiguration::log)
            ).apply(instance, FallenTreeConfiguration::new)
    );
}