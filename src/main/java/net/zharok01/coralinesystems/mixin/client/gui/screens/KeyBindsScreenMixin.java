package net.zharok01.coralinesystems.mixin.client.gui.screens;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(KeyBindsScreen.class)
public class KeyBindsScreenMixin {

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"
            ),
            index = 3
    )
    private int coraline$standardizeHeaderY(int originalY) {
        return 20;
    }

    @ModifyArg(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/Button$Builder;bounds(IIII)Lnet/minecraft/client/gui/components/Button$Builder;"
            ),
            index = 1
    )
    private int coraline$standardizeFooterY(int originalY) {
        Screen screen = (Screen) (Object) this;
        if (originalY == screen.height - 29) {
            return screen.height - 27;
        }
        return originalY;
    }
}