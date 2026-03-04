package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.GraphicsStatus;
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

}
