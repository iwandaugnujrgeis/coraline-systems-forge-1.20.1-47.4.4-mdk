package net.zharok01.coralinesystems.mixin.entity.alexs_mobs;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AlexsMobs.class, remap = false)
public abstract class AlexsMobsAprilFoolsMixin {

    @Inject(method = "isAprilFools", at = @At("HEAD"), cancellable = true, remap = false)
    private static void coraline$disableAprilFools(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
