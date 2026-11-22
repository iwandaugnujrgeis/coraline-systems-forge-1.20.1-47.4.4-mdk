package net.zharok01.coralinesystems.worldgen.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record MixedMushroomConfiguration(
        BlockStateProvider stateProvider,
        int tries,
        int xzSpread,
        int ySpread,
        int maxLight,
        boolean requireCanopy
) implements FeatureConfiguration {

    public static final Codec<MixedMushroomConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockStateProvider.CODEC.fieldOf("state_provider").forGetter(MixedMushroomConfiguration::stateProvider),
                    Codec.intRange(1, 512).fieldOf("tries").orElse(192).forGetter(MixedMushroomConfiguration::tries),
                    Codec.intRange(1, 32).fieldOf("xz_spread").orElse(12).forGetter(MixedMushroomConfiguration::xzSpread),
                    Codec.intRange(1, 16).fieldOf("y_spread").orElse(4).forGetter(MixedMushroomConfiguration::ySpread),
                    Codec.intRange(0, 15).fieldOf("max_light").orElse(12).forGetter(MixedMushroomConfiguration::maxLight),
                    Codec.BOOL.fieldOf("require_canopy").orElse(false).forGetter(MixedMushroomConfiguration::requireCanopy)
            ).apply(instance, MixedMushroomConfiguration::new)
    );
}