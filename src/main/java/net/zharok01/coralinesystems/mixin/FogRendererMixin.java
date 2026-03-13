package net.zharok01.coralinesystems.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FogRenderer.class)
public class FogRendererMixin {

	@Unique private static final int MORNING_START = 3000;
	@Unique private static final int MORNING_END = 6000;

	@Unique
	private static float getMorningFogMultiplier() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return 1;

		long dayTime = mc.level.getDayTime() % 24000L;
		if (dayTime < MORNING_START) {
			float t = dayTime / 3000F;
			// 0.15 -> 0.50
			return 0.15f + t * 0.35f;
		} else if (dayTime < MORNING_END) {
			float t = (dayTime - 3000) / 3000F;
			// 0.50 -> 1.0
			return 0.50f + t * 0.50f;
		}
		return 1;
	}

	@Redirect(
			method = "setupFog",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V"
			)
	)
	private static void redirectFogStart(float start, Camera camera, FogRenderer.FogMode fogMode, float renderDistance, boolean thickFog, float partialTick) {
//		if (camera.getFluidInCamera() != FogType.NONE || thickFog) {
//			RenderSystem.setShaderFogStart(start);
//			return;
//		}
		RenderSystem.setShaderFogStart(start * getMorningFogMultiplier());
	}

	@Redirect(
			method = "setupFog",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogEnd(F)V"
			)
	)
	private static void redirectFogEnd(float end, Camera camera, FogRenderer.FogMode fogMode, float renderDistance, boolean thickFog, float partialTick) {
//		if (camera.getFluidInCamera() != FogType.NONE || thickFog) {
//			RenderSystem.setShaderFogEnd(end);
//			return;
//		}
		RenderSystem.setShaderFogEnd(end * getMorningFogMultiplier());
	}

}
