package net.zharok01.coralinesystems.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class RadiantMegaTreeGrower extends AbstractMegaTreeGrower {

    @SuppressWarnings("removal")
    private static final ResourceKey<ConfiguredFeature<?, ?>> RADIANT_TREE =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation("gamma", "radiant_tree"));

    @SuppressWarnings("removal")
    private static final ResourceKey<ConfiguredFeature<?, ?>> MEGA_RADIANT_TREE =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation("gamma", "mega_radiant_tree"));

    @Nullable
    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredMegaFeature(@NotNull RandomSource random) {
        return MEGA_RADIANT_TREE;
    }

    @Nullable
    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(@NotNull RandomSource random, boolean hasFlowers) {
        return RADIANT_TREE;
    }
}