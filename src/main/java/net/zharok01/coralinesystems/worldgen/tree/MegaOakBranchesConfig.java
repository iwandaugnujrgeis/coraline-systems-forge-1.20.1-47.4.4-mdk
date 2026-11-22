package net.zharok01.coralinesystems.worldgen.tree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;

public record MegaOakBranchesConfig(int count, int countRandom, int startDown, int length, int lengthRandom, float jagChance, float upturnChance, IntProvider upturnHeight) {
    public static final Codec<MegaOakBranchesConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 16).fieldOf("count").forGetter(MegaOakBranchesConfig::count),
            Codec.intRange(0, 16).fieldOf("count_random").orElse(0).forGetter(MegaOakBranchesConfig::countRandom),
            Codec.intRange(4, 48).fieldOf("start_down").forGetter(MegaOakBranchesConfig::startDown),
            Codec.intRange(2, 48).fieldOf("length").forGetter(MegaOakBranchesConfig::length),
            Codec.intRange(0, 32).fieldOf("length_random").orElse(0).forGetter(MegaOakBranchesConfig::lengthRandom),
            Codec.floatRange(0.0F, 1.0F).fieldOf("jag_chance").orElse(0.0F).forGetter(MegaOakBranchesConfig::jagChance),
            Codec.floatRange(0.0F, 1.0F).fieldOf("upturn_chance").orElse(0.0F).forGetter(MegaOakBranchesConfig::upturnChance),
            IntProvider.codec(1, 24).fieldOf("upturn_height").forGetter(MegaOakBranchesConfig::upturnHeight)
    ).apply(instance, MegaOakBranchesConfig::new));
}