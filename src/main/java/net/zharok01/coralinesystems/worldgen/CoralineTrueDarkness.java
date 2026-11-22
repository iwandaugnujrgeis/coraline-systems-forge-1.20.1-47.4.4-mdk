package net.zharok01.coralinesystems.worldgen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import java.util.Objects;

public final class CoralineTrueDarkness {

    private CoralineTrueDarkness() {
    }
    private static final float DARKNESS_MODE_VALUE = 0.0F;
    private static final float TRUE_DARKNESS_BRIGHTNESS_MULTIPLIER = 1.1F;
    public static final double MIN_CHANNEL = 0.03;
    private static final float NEW_MOON_BRIGHT = 0.8F;
    private static final float FULL_MOON_BRIGHT = 1.0F;
    private static final boolean AFFECTED_BY_MOON_PHASE = true;
    private static final boolean BLOCK_LIGHT_ONLY = false;
    private static final boolean ENABLE_ON_NO_SKY_LIGHT = true;
    private static final boolean DARKNESS_BY_DEFAULT = true;
    private static final boolean ENABLE_ON_OVERWORLD = true;
    private static final boolean ENABLE_ON_NETHER = true;
    private static final boolean ENABLE_ON_END = false;
    public static boolean enabled = false;
    private static final float[][] LUMINANCE = new float[16][16];

    public static boolean isDark(Level level) {
        ResourceKey<Level> dimensionKey = level.dimension();
        if (dimensionKey == Level.OVERWORLD) {
            return ENABLE_ON_OVERWORLD;
        } else if (dimensionKey == Level.NETHER) {
            return ENABLE_ON_NETHER;
        } else if (dimensionKey == Level.END) {
            return ENABLE_ON_END;
        }
        return level.dimensionType().hasSkyLight() ? DARKNESS_BY_DEFAULT : ENABLE_ON_NO_SKY_LIGHT;
    }

    public static Vec3 getDarkFogColor(Vec3 vanilla, double factor) {
        if (factor == 1.0D) {
            return vanilla;
        }
        return new Vec3(
            Math.max(MIN_CHANNEL, vanilla.x * factor),
            Math.max(MIN_CHANNEL, vanilla.y * factor),
            Math.max(MIN_CHANNEL, vanilla.z * factor)
        );
    }

    private static float skyFactor(Level level) {
        if (BLOCK_LIGHT_ONLY || !isDark(level)) {
            return 1.0F;
        }
        if (!level.dimensionType().hasSkyLight()) {
            return 0.0F;
        }
        float angle = level.getTimeOfDay(0.0F);
        if (angle > 0.25F && angle < 0.75F) {
            float oldWeight = Math.max(0.0F, Math.abs(angle - 0.5F) - 0.2F) * 20.0F;
            float moon = AFFECTED_BY_MOON_PHASE ? level.getMoonBrightness() : 0.0F;
            float moonInterpolated = (float) Mth.lerp(moon, NEW_MOON_BRIGHT, FULL_MOON_BRIGHT);
            return Mth.lerp(oldWeight * oldWeight * oldWeight, moonInterpolated, 1.0F);
        } else {
            return 1.0F;
        }
    }

    public static int darken(int abgrColor, int blockIndex, int skyIndex) {
        float targetLuminance = LUMINANCE[blockIndex][skyIndex];
        float r = (abgrColor & 255) / 255.0F;
        float g = (abgrColor >> 8 & 255) / 255.0F;
        float b = (abgrColor >> 16 & 255) / 255.0F;
        float currentLuminance = luminance(r, g, b);
        float scale = currentLuminance > 0.0F ? Math.min(1.0F, targetLuminance / currentLuminance) : 0.0F;
        if (scale == 1.0F) {
            return abgrColor;
        }
        return 0xFF000000
            | Math.round(scale * r * 255.0F)
            | Math.round(scale * g * 255.0F) << 8
            | Math.round(scale * b * 255.0F) << 16;
    }

    public static float luminance(float r, float g, float b) {
        return r * 0.2126F + g * 0.7152F + b * 0.0722F;
    }

    public static void updateLuminance(float partialTicks, Minecraft minecraft, GameRenderer gameRenderer, float blockLightRedFlicker) {
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }
        boolean darkOnLevel = isDark(level);
        enabled = darkOnLevel
            && !Objects.requireNonNull(minecraft.player).hasEffect(MobEffects.NIGHT_VISION)
            && (!minecraft.player.hasEffect(MobEffects.CONDUIT_POWER) || !(minecraft.player.getWaterVision() > 0.0F))
            && level.getSkyFlashTime() <= 0;
        if (!enabled) {
            return;
        }
        float dimSkyFactor = skyFactor(level);
        float ambientLight = level.getSkyDarken(1.0F);
        DimensionType dimensionType = level.dimensionType();
        for (int skyIndex = 0; skyIndex < 16; skyIndex++) {
            float skyFalloff = 1.0F - (float) skyIndex / 15.0F;
            skyFalloff = 1.0F - skyFalloff * skyFalloff * skyFalloff * skyFalloff;
            skyFalloff *= dimSkyFactor;
            float darknessFloor = DARKNESS_MODE_VALUE;
            if (darknessFloor == -1.0F) {
                throw new IllegalStateException("Darkness value can't be negative");
            }
            float min = Math.max(skyFalloff * 0.05F, darknessFloor);
            float rawAmbient = ambientLight * skyFalloff;
            float minAmbient = rawAmbient * (1.0F - min) + min;
            float skyBase = LightTexture.getBrightness(dimensionType, skyIndex) * minAmbient;
            min = Math.max(0.35F * skyFalloff, darknessFloor);
            float v = skyBase * (rawAmbient * (1.0F - min) + min);
            float skyRed = v;
            float skyGreen = v;
            float skyBlue = skyBase;
            float darkenWorldAmount = gameRenderer.getDarkenWorldAmount(partialTicks);
            if (darkenWorldAmount > 0.0F) {
                skyRed = v * (1.0F - darkenWorldAmount) + v * 0.7F * darkenWorldAmount;
                skyGreen = v * (1.0F - darkenWorldAmount) + v * 0.6F * darkenWorldAmount;
                skyBlue = skyBase * (1.0F - darkenWorldAmount) + skyBase * 0.6F * darkenWorldAmount;
            }
            for (int blockIndex = 0; blockIndex < 16; blockIndex++) {
                float blockFalloff = 1.0F - (float) blockIndex / 15.0F;
                blockFalloff = 1.0F - blockFalloff * blockFalloff * blockFalloff * blockFalloff;
                float blockBase = blockFalloff * LightTexture.getBrightness(dimensionType, blockIndex) * (blockLightRedFlicker * 0.1F + 1.5F);
                min = 0.4F * blockFalloff;
                float blockGreen = blockBase * ((blockBase * (1.0F - min) + min) * (1.0F - min) + min);
                float blockBlue = blockBase * (blockBase * blockBase * (1.0F - min) + min);
                float red = skyRed + blockBase;
                float green = skyGreen + blockGreen;
                float blue = skyBlue + blockBlue;
                float f = Math.max(skyFalloff, blockFalloff);
                min = 0.03F * f;
                red = red * (0.99F - min) + min;
                green = green * (0.99F - min) + min;
                blue = blue * (0.99F - min) + min;
                if (level.dimension() == Level.END) {
                    red = skyFalloff * 0.22F + blockBase * 0.75F;
                    green = skyFalloff * 0.28F + blockGreen * 0.75F;
                    blue = skyFalloff * 0.25F + blockBlue * 0.75F;
                }
                if (red > 1.0F) red = 1.0F;
                if (green > 1.0F) green = 1.0F;
                if (blue > 1.0F) blue = 1.0F;
                float gamma = minecraft.options.gamma().get().floatValue() * f;
                float invRed = 1.0F - red;
                float invGreen = 1.0F - green;
                float invBlue = 1.0F - blue;
                invRed = 1.0F - invRed * invRed * invRed * invRed;
                invGreen = 1.0F - invGreen * invGreen * invGreen * invGreen;
                invBlue = 1.0F - invBlue * invBlue * invBlue * invBlue;
                red = red * (1.0F - gamma) + invRed * gamma;
                green = green * (1.0F - gamma) + invGreen * gamma;
                blue = blue * (1.0F - gamma) + invBlue * gamma;
                min = Math.max(0.03F * f, darknessFloor);
                red = red * (0.99F - min) + min;
                green = green * (0.99F - min) + min;
                blue = blue * (0.99F - min) + min;
                red = Mth.clamp(red, 0.0F, 1.0F);
                green = Mth.clamp(green, 0.0F, 1.0F);
                blue = Mth.clamp(blue, 0.0F, 1.0F);
                LUMINANCE[blockIndex][skyIndex] = Mth.clamp(
                    luminance(red, green, blue) * TRUE_DARKNESS_BRIGHTNESS_MULTIPLIER,
                    0.0F,
                    1.0F
                );
            }
        }
    }
}
