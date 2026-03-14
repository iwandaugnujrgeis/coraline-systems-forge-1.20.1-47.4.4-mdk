package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(FogRenderer.class)
public class FogRendererMixin {

	@Unique private static final int THICK_END = 2000;
	@Unique private static final int FADE_END = 4000;
	@Unique private static final float PEAK_MULT = 0.25F;

	@Unique
	private static float getMorningFogMultiplier() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return 1;

		long dayTime = mc.level.getDayTime() % 24000;

		if (dayTime >= 22000) {
			// Night ramping into morning: 22000 → 24000, ease toward peak
			float t = (dayTime - 22000) / 2000f;
			return Mth.lerp(easeInOut(t), 1, PEAK_MULT);
		} else if (dayTime < THICK_END) {
			// Peak fog window: flat at minimum
			return PEAK_MULT;
		} else if (dayTime < FADE_END) {
			// Fog lifting: ease back to normal
			float t = (dayTime - THICK_END) / (float)(FADE_END - THICK_END);
			return Mth.lerp(easeInOut(t), PEAK_MULT, 1);
		}

		return 1;
	}

	@Unique
	private static float easeInOut(float t) {
		return t * t * (3 - 2 * t);
	}

	@ModifyArg(
			method = "setupFog",
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V")
	)
	private static float modifyFogStart(float start) {
		return start * getMorningFogMultiplier();
	}

	@ModifyArg(
			method = "setupFog",
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogEnd(F)V")
	)
	private static float modifyFogEnd(float end) {
		return end * (getMorningFogMultiplier() * 2);
	}

}
