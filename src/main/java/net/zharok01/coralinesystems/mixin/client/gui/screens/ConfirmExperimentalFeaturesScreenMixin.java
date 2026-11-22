package net.zharok01.coralinesystems.mixin.client.gui.screens;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screens.worldselection.ConfirmExperimentalFeaturesScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConfirmExperimentalFeaturesScreen.class)
public class ConfirmExperimentalFeaturesScreenMixin {

    @Shadow @Final private BooleanConsumer callback;
    
    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void coraline$autoConfirmExperimentalWarning(CallbackInfo ci) {
        this.callback.accept(true);
        ci.cancel();
    }
}