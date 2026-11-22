package net.zharok01.coralinesystems.mixin.compat.quark;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.quark.base.handler.ContributorRewardHandler;

@Mixin(value = ContributorRewardHandler.class, remap = false)
public class ContributorRewardHandlerMixin {

    @Inject(
            method = "init()V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void coraline$suppressContributorThread(CallbackInfo ci) {
        ci.cancel();
    }
}