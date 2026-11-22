package net.zharok01.coralinesystems.mixin.client.gui.screens.loading;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.network.chat.Component;
import net.zharok01.coralinesystems.util.CoralineLoadingUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ReceivingLevelScreen.class, priority = 900)
public class ReceivingLevelScreenMixin {

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/ReceivingLevelScreen;renderDirtBackground(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private boolean coraline$replaceDirtBackground(ReceivingLevelScreen instance, GuiGraphics guiGraphics) {
        CoralineLoadingUtils.renderSusBackgroundOnly(guiGraphics, guiGraphics.guiWidth(), guiGraphics.guiHeight());
        return false;
    }

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"))
    private boolean coraline$suppressVanillaTerrainText(GuiGraphics instance, Font font, Component text, int x, int y, int color) {
        return false;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
    private void coraline$hideFancyMenuTextWidgets(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        CoralineLoadingUtils.hideTextWidgets((ReceivingLevelScreen) (Object) this);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void coraline$renderBrandingOnTop(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        CoralineLoadingUtils.renderBrandingOnly(guiGraphics, Minecraft.getInstance().font, guiGraphics.guiWidth(), guiGraphics.guiHeight(), true);
    }
}