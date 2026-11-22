package net.zharok01.coralinesystems.mixin.client.gui.screens.loading;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.GenericWaitingScreen;
import net.zharok01.coralinesystems.util.CoralineLoadingUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GenericWaitingScreen.class)
public class GenericWaitingScreenMixin {

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/GenericWaitingScreen;renderBackground(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private boolean coraline$replaceVanillaBackground(GenericWaitingScreen instance, GuiGraphics guiGraphics) {
        CoralineLoadingUtils.renderSusBackgroundOnly(guiGraphics, guiGraphics.guiWidth(), guiGraphics.guiHeight());
        return false;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V", ordinal = 0), index = 3)
    private int coraline$standardizeTitleY(int originalY) {
        return 40;
    }

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawCenteredString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V", ordinal = 0))
    private boolean coraline$suppressVanillaDots(GuiGraphics instance, Font font, String text, int x, int y, int color) {
        return false;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/MultiLineLabel;renderCentered(Lnet/minecraft/client/gui/GuiGraphics;II)I", ordinal = 0), index = 2)
    private int coraline$standardizeMultiLineMessageY(int originalY) {
        return 55;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void coraline$renderBrandingOnTop(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        CoralineLoadingUtils.renderBrandingOnly(guiGraphics, Minecraft.getInstance().font, guiGraphics.guiWidth(), guiGraphics.guiHeight(), true);
    }
}