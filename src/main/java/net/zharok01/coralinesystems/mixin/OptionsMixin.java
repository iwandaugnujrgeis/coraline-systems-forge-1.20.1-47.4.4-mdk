package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
	 * Intercepts ALL IntRange constructions inside Options.<init> and selectively
	 * caps the render distance one.
	 *
	 * Why no ordinal?
	 *   Options.<init> creates many IntRange sliders (simulation distance, mipmap
	 *   levels, framerate cap, etc.). The render distance one is NOT necessarily
	 *   ordinal 0, and the exact position can differ between Vanilla and modded
	 *   environments. Using a fixed ordinal is therefore fragile.
	 *
	 * How we identify the render distance IntRange:
	 *   Vanilla constructs it as new IntRange(2, minecraft.is64Bit() ? 32 : 16).
	 *   No other vanilla Options slider shares min=2 with max=16 or max=32, so
	 *   this check uniquely targets that one call and leaves every other slider
	 *   completely untouched.
	 */
	@Redirect(
			method = "<init>",
			at = @At(
					value = "NEW",
					target = "net/minecraft/client/OptionInstance$IntRange"
					// No ordinal — we intercept all of them and filter below
			)
	)
	private OptionInstance.IntRange coraline$limitRenderDistanceSlider(int min, int max) {
		// Render distance is the only vanilla slider with min=2 and max=16 or 32.
		// Every other IntRange in this constructor has a different min/max.
		if (min == 2 && (max == 16 || max == 32)) {
			return new OptionInstance.IntRange(2, 10);
		}
		// All other sliders are passed through unchanged.
		return new OptionInstance.IntRange(min, max);
	}
}