package net.zharok01.coralinesystems.mixin.compat.nostalgic_tweaks;

import mod.adrenix.nostalgic.helper.gameplay.BoatHelper;
import net.minecraft.world.entity.vehicle.Boat;
import net.zharok01.coralinesystems.mixin.accessor.BoatAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = BoatHelper.class, remap = false)
public class BoatHelperMixin {

    @ModifyConstant(method = "applyParticles", constant = @Constant(doubleValue = 0.2625D), remap = false)
    private static double coraline$lowerParticleSpeedThreshold(double threshold) {
        return 0.08D;
    }

    @Inject(method = "applyParticles", at = @At("HEAD"), cancellable = true)
    private static void coraline$suppressIceParticles(Boat boat, CallbackInfo ci) {
        if (((BoatAccessor) boat).coraline$getStatus() == Boat.Status.ON_LAND) {
            ci.cancel();
        }
    }
}