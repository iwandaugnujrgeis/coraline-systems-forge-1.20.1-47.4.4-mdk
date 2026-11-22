package net.zharok01.coralinesystems.client.weather;

import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SunshowerClientState {

    private static final float INTENSITY_LERP_SPEED = 0.02F;
    private static final float SUNSHOWER_RAIN_DARKENING = 0.1F;

    public static boolean active;

    private static float previousIntensity;
    private static float currentIntensity;

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            previousIntensity = currentIntensity;
            currentIntensity = Mth.lerp(INTENSITY_LERP_SPEED, currentIntensity, active ? 1.0F : 0.0F);
        }
    }

    public static float softenRainLevel(float rainLevel, float partialTick) {
        float intensity = Mth.lerp(partialTick, previousIntensity, currentIntensity);
        return rainLevel * Mth.lerp(intensity, 1.0F, SUNSHOWER_RAIN_DARKENING);
    }
}