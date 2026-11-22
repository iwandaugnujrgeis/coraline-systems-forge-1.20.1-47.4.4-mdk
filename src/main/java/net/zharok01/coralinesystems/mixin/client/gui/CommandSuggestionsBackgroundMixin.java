package net.zharok01.coralinesystems.mixin.client.gui;

import net.minecraft.client.gui.components.CommandSuggestions;
import net.zharok01.coralinesystems.util.helper.TextBackgroundHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsBackgroundMixin {

    @Shadow
    @Final
    @Mutable
    private int fillColor;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void coraline$applyUnifiedBackground(CallbackInfo callbackInfo) {
        this.fillColor = TextBackgroundHelper.replaceVisibleAlpha(this.fillColor);
    }
}