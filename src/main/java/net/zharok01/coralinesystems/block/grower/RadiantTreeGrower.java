package net.zharok01.coralinesystems.block.grower;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class RadiantTreeGrower extends AbstractTreeGrower {

    /**
     * Points at your KubeJS-defined configured feature.
     * Adjust the namespace + path to match your actual file under
     * kubejs/gamma/data/<namespace>/worldgen/configured_feature/<path>.json
     */
    private static final ResourceKey<ConfiguredFeature<?, ?>> RADIANT_TREE =
            ResourceKey.create(
                    Registries.CONFIGURED_FEATURE,
                    new ResourceLocation("gamma", "radiant_tree") // <-- change to match your RL
            );

    @Nullable
    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(@NotNull RandomSource random, boolean hasFlowers) {
        return RADIANT_TREE;
    }
}