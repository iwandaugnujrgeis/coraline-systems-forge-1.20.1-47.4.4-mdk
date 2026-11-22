package net.zharok01.coralinesystems.mixin.client.gui.screens;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AccessibilityOptionsScreen.class)
public class AccessibilityOptionsScreenMixin {

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/SimpleOptionsSubScreen;<init>(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/Options;Lnet/minecraft/network/chat/Component;[Lnet/minecraft/client/OptionInstance;)V"
            ),
            index = 2
    )
    private static Component coraline$separateTitleString(Component originalTitle) {
        return Component.translatable("options.coraline_systems.accessibility.title_screen");
    }

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/AccessibilityOptionsScreen;options(Lnet/minecraft/client/Options;)[Lnet/minecraft/client/OptionInstance;"
            )
    )
    private static OptionInstance<?>[] coraline$removeMonochromeLogo(Options options) {
        return new OptionInstance<?>[] {
                options.narrator(), options.showSubtitles(), options.highContrast(),
                options.autoJump(), options.textBackgroundOpacity(), options.backgroundForChatOnly(),
                options.chatOpacity(), options.chatLineSpacing(), options.chatDelay(),
                options.notificationDisplayTime(), options.toggleCrouch(), options.toggleSprint(),
                options.screenEffectScale(), options.fovEffectScale(), options.darknessEffectScale(),
                options.damageTiltStrength(), options.glintSpeed(), options.glintStrength(),
                options.hideLightningFlash(), options.panoramaSpeed()
        };
    }
}