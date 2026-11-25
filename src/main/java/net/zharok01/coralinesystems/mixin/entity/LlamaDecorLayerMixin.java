package net.zharok01.coralinesystems.mixin.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.LlamaDecorLayer;
import net.minecraft.world.entity.animal.horse.Llama;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LlamaDecorLayer.class)
public abstract class LlamaDecorLayerMixin {

    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/animal/horse/Llama;FFFFFF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void coraline$hideLlamaCarpet(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Llama llama,
                                         float limbSwing, float limbSwingAmount, float partialTicks,
                                         float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (llama.getSwag() != null) ci.cancel();
    }
}