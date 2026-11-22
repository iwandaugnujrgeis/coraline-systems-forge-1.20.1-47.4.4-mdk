package net.zharok01.coralinesystems.mixin.client.gui.screens;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ControlsScreen.class)
public class ControlsScreenMixin {

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"
            ),
            index = 3,
            require = 0
    )
    private int coraline$standardizeHeaderY(int originalY) {
        return 20;
    }

    @ModifyArg(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/Button$Builder;bounds(IIII)Lnet/minecraft/client/gui/components/Button$Builder;",
                    ordinal = 2
            ),
            index = 1
    )
    private int coraline$standardizeDoneButtonY(int originalY) {
        return ((Screen) (Object) this).height - 27;
    }
}