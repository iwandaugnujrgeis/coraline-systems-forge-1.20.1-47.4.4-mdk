package net.zharok01.coralinesystems.world;

import net.minecraft.Util;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.stream.IntStream;

@OnlyIn(Dist.CLIENT)
public class CoralineBiomeColors {

    public static final int NOISE_OCTAVES = 2;
    public static final List<Integer> OCTAVES = IntStream.rangeClosed(0, NOISE_OCTAVES).boxed().toList();

    // We create separate noise maps so grass, foliage, and water don't mirror each other perfectly
    public static final PerlinSimplexNoise GRASS_NOISE = new PerlinSimplexNoise(new XoroshiroRandomSource("NOISE_GRASS".hashCode()), OCTAVES);
    public static final PerlinSimplexNoise FOLIAGE_NOISE = new PerlinSimplexNoise(new XoroshiroRandomSource("NOISE_FOLIAGE".hashCode()), OCTAVES);
    public static final PerlinSimplexNoise WATER_NOISE = new PerlinSimplexNoise(new XoroshiroRandomSource("NOISE_WATER".hashCode()), OCTAVES);

    public static final ColorResolver GRASS_RESOLVER = Util.make(() -> {
        final var baseResolver = BiomeColors.GRASS_COLOR_RESOLVER;
        return (biome, x, z) -> modifyColorBidirectional(GRASS_NOISE, baseResolver, biome, x, z, 18f, 0.25f);
    });

    public static final ColorResolver FOLIAGE_RESOLVER = Util.make(() -> {
        final var baseResolver = BiomeColors.FOLIAGE_COLOR_RESOLVER;
        return (biome, x, z) -> modifyColorBidirectional(FOLIAGE_NOISE, baseResolver, biome, x, z, 12f, 0.15f);
    });

    public static final ColorResolver WATER_RESOLVER = Util.make(() -> {
        final var baseResolver = BiomeColors.WATER_COLOR_RESOLVER;
        return (biome, x, z) -> modifyColorBidirectional(WATER_NOISE, baseResolver, biome, x, z, 16f, 0.15f);
    });

    /**
     * Blends a color towards white or black based on the noise output.
     */
    private static int modifyColorBidirectional(PerlinSimplexNoise generator, ColorResolver resolver, Biome biome, double x, double z, double scale, double maxIntensity) {
        final int baseColor = resolver.getColor(biome, x, z);

        // 1. Get raw noise value based on coordinates
        double value = generator.getValue(x / scale, z / scale, false);

        // 2. Remap noise from the generator's bounds to -1.0 -> 1.0
        double maxNoise = (1 << NOISE_OCTAVES) - 1;
        double normalized = remap(value, -maxNoise, maxNoise, -1.0, 1.0);

        // 3. Apply Ambient Environment's organic easing curve to the absolute value
        double curved = curve(0, 1, Math.abs(normalized));

        // 4. Calculate final blend ratio clamped by our maxIntensity
        float blendRatio = (float) (curved * maxIntensity);

        // 5. Bidirectional Blend: Positive = White, Negative = Black
        if (normalized > 0) {
            return blend(baseColor, 0xFFFFFF, blendRatio);
        } else {
            return blend(baseColor, 0x000000, blendRatio);
        }
    }

    // --- Math & Color Helpers (Sourced from Ambient Environment) ---

    public static double remap(final double value, final double currentLow, final double currentHigh, final double newLow, final double newHigh) {
        return newLow + (value - currentLow) * (newHigh - newLow) / (currentHigh - currentLow);
    }

    public static double curve(final double start, final double end, double amount) {
        amount = Mth.clamp(amount, 0, 1);
        amount = Mth.clamp((amount - start) / (end - start), 0, 1);
        return Mth.clamp(0.5 + 0.5 * Math.sin(Math.cos(Math.PI * Math.tan(90 * amount))) * Math.cos(Math.sin(Math.tan(amount))), 0, 1);
    }

    public static int blend(final int color1, final int color2, final float ratio) {
        final float ir = 1.0f - ratio;
        final float[] rgb1 = getARGB(color2);
        final float[] rgb2 = getARGB(color1);
        return toInt(new float[] {
                rgb1[0] * ratio + rgb2[0] * ir,
                rgb1[1] * ratio + rgb2[1] * ir,
                rgb1[2] * ratio + rgb2[2] * ir,
                rgb1[3] * ratio + rgb2[3] * ir
        });
    }

    private static float[] getARGB(final int hex) {
        return new float[] {
                ((hex >> 24) & 0xff) / 255f,
                ((hex >> 16) & 0xFF) / 255f,
                ((hex >> 8) & 0xFF) / 255f,
                ((hex) & 0xFF) / 255f
        };
    }

    private static int toInt(final float[] argb) {
        final int r = (int) Math.floor(argb[1] * 255) & 0xFF;
        final int g = (int) Math.floor(argb[2] * 255) & 0xFF;
        final int b = (int) Math.floor(argb[3] * 255) & 0xFF;
        final int a = (int) Math.floor(argb[0] * 255) & 0xFF;
        return (a << 24) + (r << 16) + (g << 8) + (b);
    }
}