package net.zharok01.coralinesystems.mixin.client.gui.screens.loading;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.zharok01.coralinesystems.util.CoralineLoadingUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public class DisconnectedScreenMixin {

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/DisconnectedScreen;renderBackground(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private boolean coraline$replaceVanillaBackground(DisconnectedScreen instance, GuiGraphics guiGraphics) {
        CoralineLoadingUtils.renderSusBackgroundOnly(guiGraphics, guiGraphics.guiWidth(), guiGraphics.guiHeight());
        return false;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void coraline$renderBrandingOnTop(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        CoralineLoadingUtils.renderBrandingOnly(guiGraphics, Minecraft.getInstance().font, guiGraphics.guiWidth(), guiGraphics.guiHeight(), false);
    }
}