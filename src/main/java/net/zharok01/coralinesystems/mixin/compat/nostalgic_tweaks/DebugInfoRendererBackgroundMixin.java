package net.zharok01.coralinesystems.mixin.compat.nostalgic_tweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.zharok01.coralinesystems.util.helper.TextBackgroundHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "mod.adrenix.nostalgic.helper.candy.debug.DebugInfoRenderer", priority = 1500)
public abstract class DebugInfoRendererBackgroundMixin {

    @Unique
    private static final int SCREEN_MARGIN = 2;

    @Unique
    private static final int BOX_PADDING = 1;

    @Shadow(remap = false)
    @Final
    private GuiGraphics graphics;

    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true, remap = false)
    private void coraline$drawUnifiedBackground(String text, int index, boolean leftSide, CallbackInfo callbackInfo) {
        Font font = Minecraft.getInstance().font;
        int guiWidth = this.graphics.guiWidth();
        int textWidth = font.width(text);
        int lineHeight = font.lineHeight;

        int minX = leftSide ? BOX_PADDING : guiWidth - SCREEN_MARGIN - textWidth - BOX_PADDING;
        int maxX = leftSide ? SCREEN_MARGIN + textWidth + BOX_PADDING : guiWidth - SCREEN_MARGIN + BOX_PADDING;
        int minY = SCREEN_MARGIN + lineHeight * index - BOX_PADDING;

        this.graphics.fill(minX, minY, maxX, minY + lineHeight, TextBackgroundHelper.getColor());
        callbackInfo.cancel();
    }

    @ModifyArg(
            method = "drawText",
            index = 5,
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;m_280056_(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I",
                    remap = false
            )
    )
    private boolean coraline$forceTextShadow(boolean dropShadow) {
        return true;
    }
}