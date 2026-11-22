package net.zharok01.coralinesystems.mixin.client.gui.screens;

import net.minecraft.client.gui.screens.controls.KeyBindsList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(KeyBindsList.class)
public class KeyBindsListMixin {

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ContainerObjectSelectionList;<init>(Lnet/minecraft/client/Minecraft;IIIII)V"
            ),
            index = 1
    )
    private static int coraline$standardizeWidth(int originalWidth) {
        return originalWidth - 45;
    }

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ContainerObjectSelectionList;<init>(Lnet/minecraft/client/Minecraft;IIIII)V"
            ),
            index = 3
    )
    private static int coraline$standardizeTopMargin(int originalY0) {
        return 32;
    }
}