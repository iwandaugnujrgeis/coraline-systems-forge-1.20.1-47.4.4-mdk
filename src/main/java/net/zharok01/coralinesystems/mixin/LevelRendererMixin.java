package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.zharok01.coralinesystems.client.weather.FogWeatherClientState;
import net.zharok01.coralinesystems.client.weather.SunshowerClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Redirect(
            method = "renderSky",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F")
    )
    private float coraline$softenCelestialRainFade(ClientLevel level, float partialTick) {
        float softenedRain = SunshowerClientState.softenRainLevel(level.getRainLevel(partialTick), partialTick);
        return FogWeatherClientState.addFogLevel(softenedRain, partialTick);
    }
}