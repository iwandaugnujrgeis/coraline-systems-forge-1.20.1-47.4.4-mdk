package net.zharok01.coralinesystems.mixin.accessors;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.ColorResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BiomeColors.class)
public interface BiomeColorsAccessor {

    @Mutable
    @Accessor("GRASS_COLOR_RESOLVER")
    static void coralineSystems$setGrassColorResolver(ColorResolver newResolver) {
        throw new AssertionError();
    }

    @Mutable
    @Accessor("FOLIAGE_COLOR_RESOLVER")
    static void coralineSystems$setFoliageColorResolver(ColorResolver newResolver) {
        throw new AssertionError();
    }

    @Mutable
    @Accessor("WATER_COLOR_RESOLVER")
    static void coralineSystems$setWaterColorResolver(ColorResolver newResolver) {
        throw new AssertionError();
    }
}