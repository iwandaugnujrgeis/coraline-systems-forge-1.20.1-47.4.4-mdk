package net.zharok01.coralinesystems.mixin.entity.alexs_mobs.model;

import com.github.alexthe666.alexsmobs.client.model.ModelRaccoon;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerRaccoonEyes;
import com.github.alexthe666.alexsmobs.entity.EntityRaccoon;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.zharok01.coralinesystems.util.access.RaccoonEyeFadeAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LayerRaccoonEyes.class, remap = false)
public abstract class LayerRaccoonEyesMixin extends RenderLayer<EntityRaccoon, ModelRaccoon> {

    @Unique
    private static final ResourceLocation coraline$EYES_TEXTURE = new ResourceLocation("alexsmobs:textures/entity/raccoon_eyes.png");

    protected LayerRaccoonEyesMixin(RenderLayerParent<EntityRaccoon, ModelRaccoon> renderer) {
        super(renderer);
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(
            method = {
                    "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILcom/github/alexthe666/alexsmobs/entity/EntityRaccoon;FFFFFF)V",
                    "m_6494_(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILcom/github/alexthe666/alexsmobs/entity/EntityRaccoon;FFFFFF)V"
            },
            at = @At("HEAD"),
            cancellable = true
    )
    private void coraline$renderFadingEyes(PoseStack poseStack, MultiBufferSource buffer, int packedLight, EntityRaccoon raccoon,
                                           float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                                           float netHeadYaw, float headPitch, CallbackInfo ci) {
        ci.cancel();

        float fade = ((RaccoonEyeFadeAccess) raccoon).coraline$getEyeFade(partialTick);
        if (fade <= 0.0F) return;

        VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(coraline$EYES_TEXTURE));
        this.getParentModel().renderToBuffer(poseStack, consumer, packedLight,
                LivingEntityRenderer.getOverlayCoords(raccoon, 0.0F), fade, fade, fade, 1.0F);
    }
}