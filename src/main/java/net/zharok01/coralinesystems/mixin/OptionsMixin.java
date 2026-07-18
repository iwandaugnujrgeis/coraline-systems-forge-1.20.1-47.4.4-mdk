package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(Options.class)
public class OptionsMixin {

	@Redirect(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/GraphicsStatus;values()[Lnet/minecraft/client/GraphicsStatus;"
			)
	)
	private GraphicsStatus[] removeFabulousGraphics() {
		GraphicsStatus[] array = GraphicsStatus.values();
		return Arrays.copyOfRange(array, 0, array.length - 1);
	}

	/**
	 * Intercepts ALL IntRange constructions inside Options.<init> to selectively
	 * cap the Render Distance and FOV sliders without touching others.
	 */
	@Redirect(
			method = "<init>",
			at = @At(
					value = "NEW",
					target = "net/minecraft/client/OptionInstance$IntRange"
			)
	)
	private OptionInstance.IntRange coraline$limitIntRangeSliders(int min, int max) {
		// 1. Cap Render Distance (Vanilla: 2 to 16 or 32)
		if (min == 2 && (max == 16 || max == 32)) {
			return new OptionInstance.IntRange(2, 10);
		}

		// 2. Cap FOV (Vanilla: 30 to 110)
		if (min == 30 && max == 110) {
			return new OptionInstance.IntRange(30, 80);
		}

		return new OptionInstance.IntRange(min, max);
	}

	/**
	 * Intercepts the label generation for the FOV slider.
	 */
	@Inject(
			method = "genericValueLabel(Lnet/minecraft/network/chat/Component;I)Lnet/minecraft/network/chat/Component;",
			at = @At("HEAD"),
			cancellable = true
	)
	private static void coraline$customFovLabel(Component text, int value, CallbackInfoReturnable<Component> cir) {
		if (value == 80 && text instanceof MutableComponent mutable) {
			if (mutable.getContents() instanceof TranslatableContents translatable) {
				if ("options.fov".equals(translatable.getKey())) {
					cir.setReturnValue(Options.genericValueLabel(text, Component.literal("Quake Semi-Pro")));
				}
			}
		}
	}
}