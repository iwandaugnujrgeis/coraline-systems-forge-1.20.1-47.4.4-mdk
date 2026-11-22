package net.zharok01.coralinesystems.mixin.client.gui.screens;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LanguageSelectScreen.class)
public abstract class LanguageSelectScreenMixin {

    @Final
    @Shadow
    private static Component WARNING_LABEL;

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"
            )
    )
    private void coraline$renderHeaderAndSuppressWarning(GuiGraphics instance, Font font, Component text, int x, int y, int color) {
        if (text != WARNING_LABEL) {
            instance.drawCenteredString(font, text, x, 20, color);
        }
    }

    @ModifyArg(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/OptionInstance;createButton(Lnet/minecraft/client/Options;III)Lnet/minecraft/client/gui/components/AbstractWidget;"
            ),
            index = 2
    )
    private int coraline$realignUnicodeButtonY(int originalY) {
        LanguageSelectScreen screen = (LanguageSelectScreen) (Object) this;
        return screen.height - 27;
    }

    @ModifyArg(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/Button$Builder;bounds(IIII)Lnet/minecraft/client/gui/components/Button$Builder;"
            ),
            index = 1
    )
    private int coraline$realignDoneButtonY(int originalY) {
        LanguageSelectScreen screen = (LanguageSelectScreen) (Object) this;
        return screen.height - 27;
    }
}