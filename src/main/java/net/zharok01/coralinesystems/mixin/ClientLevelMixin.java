package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.client.weather.FogWeatherClientState;
import net.zharok01.coralinesystems.client.weather.SunshowerClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Redirect(
            method = {
                    "getSkyColor(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;",
                    "getCloudColor(F)Lnet/minecraft/world/phys/Vec3;",
                    "getSkyDarken(F)F"
            },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F")
    )
    private float coraline$softenRainDarkening(ClientLevel level, float partialTick) {
        float softenedRain = SunshowerClientState.softenRainLevel(level.getRainLevel(partialTick), partialTick);
        return FogWeatherClientState.addFogLevel(softenedRain, partialTick);
    }

    @Inject(method = "getSkyColor(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"), cancellable = true)
    private void coraline$lightenFogSky(Vec3 pos, float partialTick, CallbackInfoReturnable<Vec3> cir) {
        cir.setReturnValue(FogWeatherClientState.lighten((ClientLevel) (Object) this, cir.getReturnValue(), FogWeatherClientState.SKY_TONE, partialTick));
    }

    @Inject(method = "getCloudColor(F)Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"), cancellable = true)
    private void coraline$lightenFogClouds(float partialTick, CallbackInfoReturnable<Vec3> cir) {
        cir.setReturnValue(FogWeatherClientState.lighten((ClientLevel) (Object) this, cir.getReturnValue(), FogWeatherClientState.HAZE_TONE, partialTick));
    }
}