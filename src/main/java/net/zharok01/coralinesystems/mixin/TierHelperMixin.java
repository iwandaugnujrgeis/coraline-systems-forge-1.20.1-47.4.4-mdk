package net.zharok01.coralinesystems.mixin;

import bagu_chan.bagus_lib.util.TierHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TierHelper.class, remap = false)
public class TierHelperMixin {

    /**
     * Suppresses the network-heavy initialization of TierHelper.
     * This prevents the game from hanging or stalling during startup due
     * to connection timeouts when fetching supporter data.
     */
    @Inject(
            method = "addSuporterContents()V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void coralinesystems$suppressSupporterFetch(CallbackInfo ci) {
        ci.cancel();
    }
}