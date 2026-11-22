package net.zharok01.coralinesystems.mixin.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.ChatOptionsScreen;
import net.minecraft.client.gui.screens.SoundOptionsScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractWidget.class)
public class WidgetTooltipsMixin {

    @Inject(
            method = "setTooltip",
            at = @At("HEAD"),
            cancellable = true
    )
    private void coraline$suppressTooltips(Tooltip tooltip, CallbackInfo ci) {
        if (Minecraft.getInstance().screen instanceof CreateWorldScreen
                || Minecraft.getInstance().screen instanceof AccessibilityOptionsScreen
                || Minecraft.getInstance().screen instanceof SoundOptionsScreen
                || Minecraft.getInstance().screen instanceof ChatOptionsScreen) {
            ci.cancel();
        }
    }
}