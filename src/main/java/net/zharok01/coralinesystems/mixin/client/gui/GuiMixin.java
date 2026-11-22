package net.zharok01.coralinesystems.mixin.client.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Inject(method = "setOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void coraline$suppressDismountOverlayText(Component component, boolean animateColor, CallbackInfo ci) {
        if (component.getContents() instanceof TranslatableContents translatable && "mount.onboard".equals(translatable.getKey())) {
            ci.cancel();
        }
    }
}