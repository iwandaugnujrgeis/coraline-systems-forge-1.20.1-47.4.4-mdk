package net.zharok01.coralinesystems.mixin.accessor;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.ColorResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BiomeColors.class)
public interface BiomeColorsAccessor {

    @Mutable
    @Accessor("GRASS_COLOR_RESOLVER")
    static void coraline$setGrassColorResolver(ColorResolver newResolver) {
        throw new AssertionError();
    }
}