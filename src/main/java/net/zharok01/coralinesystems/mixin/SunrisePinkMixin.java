package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@OnlyIn(Dist.CLIENT)
@Mixin(DimensionSpecialEffects.class)
public class SunrisePinkMixin {

    /**
     * Target colour at peak: #f7b09b = RGB(247, 176, 155) → normalised ≈ (0.97, 0.69, 0.61)
     *
     * At f3 = 1.0 (horizon peak), the formulas evaluate to:
     *   R = 0.20 + 0.75 = 0.95   ← bright warm red
     *   G = 0.40 + 0.28 = 0.68   ← enough green to push orange→peach
     *   B = 0.30 + 0.30 = 0.60   ← enough blue to keep it soft, not dirty
     *
     * At f3 = 0.0 (window edge), everything fades toward the base values and the
     * alpha (f4) independently drops to 0, so the transition is always smooth.
     */
    @Inject(method = "getSunriseColor", at = @At("HEAD"), cancellable = true)
    private void coraline$peachySunrise(float timeOfDay, float partialTicks, CallbackInfoReturnable<float[]> cir) {
        float f1 = Mth.cos(timeOfDay * ((float) Math.PI * 2F));

        if (f1 >= -0.4F && f1 <= 0.4F) {
            float f3 = f1 / 0.4F * 0.5F + 0.5F;

            // Alpha — identical to vanilla: smooth fade at the window edges
            float f4 = 1.0F - (1.0F - Mth.sin(f3 * (float) Math.PI)) * 0.99F;
            f4 *= f4;

            float[] col = new float[4];

            // R: dominant and bright — anchors the warm peachy tone
            col[0] = f3 * 0.20F + 0.75F;   // peaks at ~0.95

            // G: raised significantly vs the "dirty pink" version
            // More green = less magenta, shifts toward peach/salmon

            //Changed this here from 0,4 to 0,35:
            col[1] = f3 * 0.35F + 0.28F;   // peaks at ~0.68

            // B: moderate — enough to keep it soft and pastel, not orange
            col[2] = f3 * 0.30F + 0.30F;   // peaks at ~0.60

            col[3] = f4;

            cir.setReturnValue(col);
        }
    }
}