package net.zharok01.coralinesystems.mixin.compat.nostalgic_tweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Screen.class, priority = 1100)
public abstract class ScreenDisconnectMixin {

    @Shadow protected Minecraft minecraft;
    @Shadow protected abstract void renderDirtBackground(GuiGraphics guiGraphics);
    @Unique private static boolean coraline$ntLoaded = false;
    @Unique private static boolean coraline$ntCheckDone = false;

    @Unique
    private static boolean coraline$isNostalgicTweaksLoaded() {
        if (!coraline$ntCheckDone) {
            coraline$ntLoaded = ModList.get().isLoaded("nostalgic_tweaks");
            coraline$ntCheckDone = true;
        }
        return coraline$ntLoaded;
    }
    
    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void coraline$fixDisconnectBackground(GuiGraphics graphics, CallbackInfo ci) {
        if (!coraline$isNostalgicTweaksLoaded()) {
            return;
        }

        Screen self = (Screen) (Object) this;
        boolean isTransitionScreen = self instanceof GenericDirtMessageScreen
                || self instanceof ProgressScreen;

        if (!isTransitionScreen) {
            return;
        }

        if (this.minecraft == null || this.minecraft.level == null) {
            return;
        }

        this.renderDirtBackground(graphics);
        ci.cancel();
    }
}