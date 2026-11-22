package net.zharok01.coralinesystems.worldgen.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record RockSaltPatchConfiguration(BlockState block, IntProvider radius) implements FeatureConfiguration {
    public static final Codec<RockSaltPatchConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.fieldOf("block").forGetter(RockSaltPatchConfiguration::block),
            IntProvider.codec(1, 6).fieldOf("radius").forGetter(RockSaltPatchConfiguration::radius)
    ).apply(instance, RockSaltPatchConfiguration::new));
}