package net.zharok01.coralinesystems.mixin.client.gui.screens.loading;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.InactiveProfiler;
import net.zharok01.coralinesystems.mixin.IMinecraftMixin;
import net.zharok01.coralinesystems.util.CoralineLoadingUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(LoadingOverlay.class)
public abstract class LoadingOverlayMixin extends Overlay {

    @Unique private static final Logger coraline$LOGGER = LogManager.getLogger();

    @Shadow @Final private Minecraft minecraft;
    @Shadow private long fadeInStart;
    @Shadow private long fadeOutStart;
    @Shadow @Final private boolean fadeIn;
    @Shadow private float currentProgress;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void coraline$preloadFonts(Minecraft minecraft, ReloadInstance reload, Consumer<?> onFinish, boolean fadeIn, CallbackInfo ci) {
        try {
            FontManager fontManager = ((IMinecraftMixin) minecraft).getFontManager();
            FontManager.Preparation preparation = fontManager.prepare(minecraft.getResourceManager(), Util.backgroundExecutor()).join();
            fontManager.apply(preparation, InactiveProfiler.INSTANCE);
        } catch (Exception ex) {
            coraline$LOGGER.error("[Coraline Systems] Failed to pre-load fonts for the loading screen!", ex);
        }
    }

    @WrapWithCondition(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V")
    )
    private boolean coraline$suppressMojangLogo(GuiGraphics guiGraphics, ResourceLocation atlasLocation, int x, int y, int width, int height, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        return false;
    }

    @Inject(method = "drawProgressBar", at = @At("HEAD"), cancellable = true)
    private void coraline$suppressVanillaProgressBar(GuiGraphics guiGraphics, int minX, int minY, int maxX, int maxY, float partialTick, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void coraline$renderOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        long currentTime = Util.getMillis();

        float fadeInTimer = this.fadeInStart > -1L ? (float) (currentTime - this.fadeInStart) / 500.0F : -1.0F;
        float fadeOutTimer = this.fadeOutStart > -1L ? (float) (currentTime - this.fadeOutStart) / 1000.0F : -1.0F;

        float alpha;
        if (fadeOutTimer >= 1.0F) {
            alpha = 1.0F - Mth.clamp(fadeOutTimer - 1.0F, 0.0F, 1.0F);
        } else if (this.fadeIn) {
            alpha = Mth.clamp(fadeInTimer, 0.0F, 1.0F);
        } else {
            alpha = 1.0F;
        }

        int alphaInt = Mth.ceil(alpha * 255.0F);
        if (alphaInt <= 0) return;

        guiGraphics.fill(0, 0, width, height, FastColor.ARGB32.color(alphaInt, 0, 0, 0));

        float dotsAlpha = fadeOutTimer >= 0.0F ? Mth.clamp(1.0F - fadeOutTimer, 0.0F, 1.0F) : alpha;
        CoralineLoadingUtils.renderStartupProgress(guiGraphics, this.minecraft.font, width, height, Mth.ceil(dotsAlpha * 255.0F), this.currentProgress);
    }
}