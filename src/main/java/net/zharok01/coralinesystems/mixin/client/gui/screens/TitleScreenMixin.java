package net.zharok01.coralinesystems.mixin.client.gui.screens;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.zharok01.coralinesystems.util.CoralineLoadingUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    @Shadow private long fadeInStart;
    @Shadow @Final private boolean fading;

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @WrapWithCondition(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/internal/BrandingControl;forEachLine(ZZLjava/util/function/BiConsumer;)V",
                    remap = false
            )
    )
    private boolean coraline$suppressForgeBrandingLeft(boolean includeMC, boolean reverse, BiConsumer<Integer, String> lineConsumer) {
        return false;
    }

    @WrapWithCondition(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/internal/BrandingControl;forEachAboveCopyrightLine(Ljava/util/function/BiConsumer;)V",
                    remap = false
            )
    )
    private boolean coraline$suppressForgeBrandingRight(BiConsumer<Integer, String> lineConsumer) {
        return false;
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void coraline$removeVanillaCopyrightButton(CallbackInfo ci) {
        this.children().stream()
                .filter(listener -> listener instanceof PlainTextButton b && TitleScreen.COPYRIGHT_TEXT.equals(b.getMessage()))
                .findFirst()
                .ifPresent(button -> this.removeWidget((PlainTextButton) button));
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void coraline$renderCustomBranding(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        float fadeProgress = this.fading ? (float) (Util.getMillis() - this.fadeInStart) / 1000.0F : 1.0F;
        float alpha = this.fading ? Mth.clamp(fadeProgress - 1.0F, 0.0F, 1.0F) : 1.0F;

        CoralineLoadingUtils.renderFooter(guiGraphics, this.minecraft.font, this.width, this.height, Mth.ceil(alpha * 255.0F));
    }
}