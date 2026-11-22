package net.zharok01.coralinesystems.mixin.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.CatRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CatRenderer.class)
public class CatRendererMixin {

    @Redirect(
            method = "scale*",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"
            )
    )
    private void coraline$suppressCatScaleDown(PoseStack poseStack, float x, float y, float z) {
    }
}