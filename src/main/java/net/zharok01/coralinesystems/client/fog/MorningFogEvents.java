package net.zharok01.coralinesystems.client.fog;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.client.weather.FogWeatherClientState;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MorningFogEvents {

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            MorningFogManager.tick();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (event.getCamera().getFluidInCamera() != FogType.NONE) return;

        float partialTick = (float) event.getPartialTick();
        float intensity = MorningFogManager.getIntensity(partialTick);
        if (intensity <= 0.001F) return;

        float renderDistance = event.getRenderer().getRenderDistance();

        float currentStart = RenderSystem.getShaderFogStart();
        float currentEnd = RenderSystem.getShaderFogEnd();

        float weatherTightening = Mth.lerp(FogWeatherClientState.getIntensity(partialTick), 1.0F, FogWeatherClientState.MORNING_FOG_TIGHTENING);
        float morningEnd = renderDistance * MorningFogManager.MORNING_FOG_END * weatherTightening;
        float morningStart = renderDistance * MorningFogManager.MORNING_FOG_START * weatherTightening;

        float newEnd   = Math.min(currentEnd,   Mth.lerp(intensity, currentEnd,   morningEnd));
        float newStart = Math.min(currentStart, Mth.lerp(intensity, currentStart, morningStart));

        RenderSystem.setShaderFogEnd(newEnd);
        RenderSystem.setShaderFogStart(newStart);
    }
}