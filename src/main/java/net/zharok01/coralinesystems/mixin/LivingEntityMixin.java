package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "updateFallFlying", at = @At("HEAD"), cancellable = true)
    private void coraline$keepGlidingWhileLevitating(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof Player && entity.hasEffect(MobEffects.LEVITATION) && entity.isFallFlying()
                && !entity.onGround() && !entity.isInWater() && !entity.isPassenger()) {
            ci.cancel();
        }
    }
}