package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@OnlyIn(Dist.CLIENT)
@Mixin(DimensionSpecialEffects.class)
public abstract class DimensionSpecialEffectsMixin {

    @Unique private static final float HORIZON_R = 0.749f;
    @Unique private static final float HORIZON_G = 0.278f;
    @Unique private static final float HORIZON_B = 0.298f;
    @Unique private static final float ZENITH_R = 1.000f;
    @Unique private static final float ZENITH_G = 0.651f;
    @Unique private static final float ZENITH_B = 0.922f;
    
    @Final @Shadow private float[] sunriseCol;

    @Inject(method = "getSunriseColor", at = @At("HEAD"), cancellable = true)
    private void coraline$customSunriseColor(float timeOfDay,
                                                    float partialTicks,
                                                    CallbackInfoReturnable<float[]> cir) {
        float cosAngle = Mth.cos(timeOfDay * ((float) Math.PI * 2f));

        if (cosAngle < -0.4f || cosAngle > 0.4f) {
            return;
        }

        float f3 = cosAngle / 0.4f * 0.5f + 0.5f;

        float alpha = 1.0f - (1.0f - Mth.sin(f3 * (float) Math.PI)) * 0.99f;
        alpha *= alpha;

        this.sunriseCol[0] = Mth.lerp(f3, HORIZON_R, ZENITH_R);
        this.sunriseCol[1] = Mth.lerp(f3, HORIZON_G, ZENITH_G);
        this.sunriseCol[2] = Mth.lerp(f3, HORIZON_B, ZENITH_B);
        this.sunriseCol[3] = alpha;

        cir.setReturnValue(this.sunriseCol);
    }
}