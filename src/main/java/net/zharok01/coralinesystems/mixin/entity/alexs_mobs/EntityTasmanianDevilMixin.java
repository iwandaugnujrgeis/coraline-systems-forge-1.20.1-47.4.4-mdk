package net.zharok01.coralinesystems.mixin.entity.alexs_mobs;

import com.github.alexthe666.alexsmobs.entity.EntityTasmanianDevil;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityTasmanianDevil.class, remap = false)
public class EntityTasmanianDevilMixin {

    @Inject(method = "setSitting", at = @At("HEAD"), cancellable = true)
    private void coraline$preventRandomSitting(boolean sit, CallbackInfo ci) {
        EntityTasmanianDevil devil = (EntityTasmanianDevil) (Object) this;

        if (sit && devil.getAnimation() != EntityTasmanianDevil.ANIMATION_HOWL) {
            ci.cancel();
        }
    }

    @Inject(method = "setBasking", at = @At("HEAD"), cancellable = true)
    private void coraline$preventRandomBasking(boolean bask, CallbackInfo ci) {
        if (bask) {
            ci.cancel();
        }
    }

    @Inject(method = "canTargetItem", at = @At("HEAD"), cancellable = true)
    private void coraline$disableItemPickup(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}