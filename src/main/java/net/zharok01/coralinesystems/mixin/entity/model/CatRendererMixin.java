package net.zharok01.coralinesystems.mixin.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.zharok01.coralinesystems.client.entity.render.cat.CatEyeLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CatRenderer.class)
public class CatRendererMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void coraline$addCatEyeLayer(EntityRendererProvider.Context context, CallbackInfo ci) {
        CatRenderer renderer = (CatRenderer) (Object) this;
        renderer.addLayer(new CatEyeLayer(renderer));
    }

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