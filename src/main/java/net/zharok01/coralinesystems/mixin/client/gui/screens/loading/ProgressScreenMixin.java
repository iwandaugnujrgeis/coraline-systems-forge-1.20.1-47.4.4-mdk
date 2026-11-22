package net.zharok01.coralinesystems.mixin.client.gui.screens.loading;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.zharok01.coralinesystems.util.CoralineLoadingUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProgressScreen.class)
public class ProgressScreenMixin {

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/ProgressScreen;renderBackground(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private boolean coraline$replaceVanillaBackground(ProgressScreen instance, GuiGraphics guiGraphics) {
        CoralineLoadingUtils.renderSusBackgroundOnly(guiGraphics, guiGraphics.guiWidth(), guiGraphics.guiHeight());
        return false;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V", ordinal = 0), index = 3)
    private int coraline$standardizeHeaderY(int originalY) {
        return 40;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V", ordinal = 1), index = 3)
    private int coraline$standardizeStageY(int originalY) {
        return 55;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void coraline$renderBrandingOnTop(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        CoralineLoadingUtils.renderBrandingOnly(guiGraphics, Minecraft.getInstance().font, guiGraphics.guiWidth(), guiGraphics.guiHeight(), true);
    }
}