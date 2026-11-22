package net.zharok01.coralinesystems.mixin.client.gui;

import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.zharok01.coralinesystems.util.helper.TextBackgroundHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = DebugScreenOverlay.class, priority = 1500)
public abstract class DebugScreenOverlayBackgroundMixin {

    @ModifyArg(
            method = "renderLines",
            index = 4,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V")
    )
    private int coraline$applyUnifiedBackground(int backgroundColor) {
        return TextBackgroundHelper.getColor();
    }

    @ModifyArg(
            method = "renderLines",
            index = 5,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I")
    )
    private boolean coraline$forceTextShadow(boolean dropShadow) {
        return true;
    }
}