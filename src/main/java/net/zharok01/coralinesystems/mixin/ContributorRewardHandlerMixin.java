package net.zharok01.coralinesystems.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.quark.base.handler.ContributorRewardHandler;

@Mixin(value = ContributorRewardHandler.class, remap = false)
public class ContributorRewardHandlerMixin {

    /**
     * Cancels the execution of the ContributorRewardHandler initialization.
     * This prevents Quark from spinning up a background daemon thread to fetch
     * Patreon data, silencing the SocketTimeoutException in the logs.
     */
    @Inject(
            method = "init()V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void coralinesystems$suppressContributorThread(CallbackInfo ci) {
        ci.cancel();
    }
}