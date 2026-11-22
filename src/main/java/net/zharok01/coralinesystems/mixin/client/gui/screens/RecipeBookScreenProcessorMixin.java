package net.zharok01.coralinesystems.mixin.client.gui.screens;

import dev.isxander.controlify.api.bind.InputBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "dev.isxander.controlify.screenop.compat.vanilla.RecipeBookScreenProcessor", remap = false)
public abstract class RecipeBookScreenProcessorMixin {

    @Redirect(method = "handleScreenVMouse", at = @At(value = "INVOKE", target = "Ldev/isxander/controlify/api/bind/InputBinding;justPressed()Z", ordinal = 2), require = 1)
    private boolean coraline$ignoreRightTrigger(InputBinding binding) {
        return false;
    }

    @Redirect(method = "handleScreenVMouse", at = @At(value = "INVOKE", target = "Ldev/isxander/controlify/api/bind/InputBinding;justPressed()Z", ordinal = 3), require = 1)
    private boolean coraline$ignoreLeftTrigger(InputBinding binding) {
        return false;
    }
}