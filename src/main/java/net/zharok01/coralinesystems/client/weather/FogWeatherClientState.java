package net.zharok01.coralinesystems.client.weather;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.client.fog.MorningFogManager;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class FogWeatherClientState {

    private static final float FADE_PER_TICK = 0.01F;
    private static final float TINT_STRENGTH = 1.0F;

    public static final Vec3 SKY_TONE = Vec3.fromRGB24(0xc2e1ff);
    public static final Vec3 HAZE_TONE = Vec3.fromRGB24(0xd1e8ff);

    // Morning fog uses 0.05 / 0.45
    private static final float FOG_START = 0.1F;
    private static final float FOG_END = 0.55F;

    // Morning fog's distances are multiplied by this
    public static final float MORNING_FOG_TIGHTENING = 0.75F;

    private static final float SUNRISE_HAZE_TINT = 0.9F;

    public static boolean active;

    private static float previousIntensity;
    private static float currentIntensity;

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        boolean inSkylands = level.dimension().location().equals(MorningFogManager.SKYLANDS_DIMENSION);
        boolean outdoors = inSkylands || (level.dimensionType().hasSkyLight() && minecraft.gameRenderer.getMainCamera().getPosition().y >= level.getSeaLevel() - 10);
        float target = active && outdoors ? 1.0F : 0.0F;

        previousIntensity = currentIntensity;
        currentIntensity = Mth.clamp(target, currentIntensity - FADE_PER_TICK, currentIntensity + FADE_PER_TICK);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        active = false;
        previousIntensity = 0.0F;
        currentIntensity = 0.0F;
    }

    public static float getIntensity(float partialTick) {
        return Mth.lerp(partialTick, previousIntensity, currentIntensity);
    }

    public static float addFogLevel(float rainLevel, float partialTick) {
        return Mth.clamp(rainLevel + getIntensity(partialTick), 0.0F, 1.0F);
    }

    public static Vec3 lighten(ClientLevel level, Vec3 color, Vec3 tone, float partialTick) {
        float daylight = Mth.clamp(Mth.cos(level.getTimeOfDay(partialTick) * ((float) Math.PI * 2.0F)) * 2.0F + 0.5F, 0.0F, 1.0F);
        float weight = getIntensity(partialTick) * TINT_STRENGTH * daylight;
        return new Vec3(Mth.lerp(weight, color.x, tone.x), Mth.lerp(weight, color.y, tone.y), Mth.lerp(weight, color.z, tone.z));
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        if (event.getCamera().getFluidInCamera() != FogType.NONE) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        float partialTick = (float) event.getPartialTick();
        Vec3 fogColor = new Vec3(event.getRed(), event.getGreen(), event.getBlue());

        Vec3 hazeTone = HAZE_TONE;
        float[] sunrise = level.effects().getSunriseColor(level.getTimeOfDay(partialTick), partialTick);
        if (sunrise != null) {
            float sunSide = Mth.sin(level.getSunAngle(partialTick)) > 0.0F ? -1.0F : 1.0F;
            float facingSun = Math.max(0.0F, event.getCamera().getLookVector().x() * sunSide);
            float glow = facingSun * sunrise[3] * SUNRISE_HAZE_TINT;
            hazeTone = new Vec3(
                    Mth.lerp(glow, hazeTone.x, sunrise[0]),
                    Mth.lerp(glow, hazeTone.y, sunrise[1]),
                    Mth.lerp(glow, hazeTone.z, sunrise[2]));
        }

        Vec3 lightened = lighten(level, fogColor, hazeTone, partialTick);
        event.setRed((float) lightened.x);
        event.setGreen((float) lightened.y);
        event.setBlue((float) lightened.z);
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (event.getCamera().getFluidInCamera() != FogType.NONE) {
            return;
        }
        float intensity = getIntensity((float) event.getPartialTick());
        if (intensity <= 0.001F) {
            return;
        }

        float renderDistance = event.getRenderer().getRenderDistance();
        float currentStart = RenderSystem.getShaderFogStart();
        float currentEnd = RenderSystem.getShaderFogEnd();

        RenderSystem.setShaderFogStart(Math.min(currentStart, Mth.lerp(intensity, currentStart, renderDistance * FOG_START)));
        RenderSystem.setShaderFogEnd(Math.min(currentEnd, Mth.lerp(intensity, currentEnd, renderDistance * FOG_END)));
    }
}