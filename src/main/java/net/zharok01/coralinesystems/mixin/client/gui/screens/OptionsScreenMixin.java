package net.zharok01.coralinesystems.mixin.client.gui.screens;

import net.minecraft.client.gui.screens.OptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin {

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
}