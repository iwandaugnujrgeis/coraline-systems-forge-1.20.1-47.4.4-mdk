package net.zharok01.coralinesystems.mixin.compat.controlify;

import net.minecraft.Util;
import org.hid4java.HidServices;
import org.hid4java.HidServicesSpecification;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.isxander.controlify.hid.ControllerHIDService", remap = false)
public abstract class ControllerHIDServiceMixin {
    @Shadow
    @Final
    private HidServicesSpecification specification;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void coraline$disableHidShutdownHook(CallbackInfo ci) {
        if (Util.getPlatform() == Util.OS.OSX) {
            specification.setAutoShutdown(false);
        }
    }

    @Redirect(
            method = "stop",
            at = @At(value = "INVOKE", target = "Lorg/hid4java/HidServices;shutdown()V")
    )
    private void coraline$skipHidExitOnMac(HidServices services) {
        if (Util.getPlatform() == Util.OS.OSX) {
            services.stop();
        } else {
            services.shutdown();
        }
    }
}