package net.zharok01.coralinesystems.mixin.entity;

import net.minecraft.world.entity.animal.horse.Horse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Horse.class)
public class HorseMixin {

    @Inject(method = "canWearArmor", at = @At("HEAD"), cancellable = true)
    private void coraline$preventHorseArmor(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}