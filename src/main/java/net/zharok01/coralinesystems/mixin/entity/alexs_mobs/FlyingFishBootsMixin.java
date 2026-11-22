package net.zharok01.coralinesystems.mixin.entity.alexs_mobs;

import com.github.alexthe666.alexsmobs.entity.util.FlyingFishBootsUtil;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FlyingFishBootsUtil.class, remap = false)
public class FlyingFishBootsMixin {

    @Inject(method = "tickFlyingFishBoots", at = @At("HEAD"), cancellable = true)
    private static void coraline$disableDiveSwim(LivingEntity fishy, CallbackInfo callbackInfo) {
        callbackInfo.cancel();
    }
}
