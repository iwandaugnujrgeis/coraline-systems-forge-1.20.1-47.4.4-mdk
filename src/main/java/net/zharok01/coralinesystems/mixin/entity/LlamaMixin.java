package net.zharok01.coralinesystems.mixin.entity;

import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Llama.class)
public abstract class LlamaMixin {

    @Inject(method = "canWearArmor", at = @At("HEAD"), cancellable = true)
    private void coraline$hideArmorSlot(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "isArmor", at = @At("HEAD"), cancellable = true)
    private void coraline$preventCarpetEquipping(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
