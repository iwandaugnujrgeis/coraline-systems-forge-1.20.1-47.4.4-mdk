package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.renderer.blockentity.LecternRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LecternRenderer.class)
public class LecternRendererMixin {

    @Inject(
            method = "render(Lnet/minecraft/world/level/block/entity/LecternBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void coraline$suppressBookRender(LecternBlockEntity blockEntity, float partialTick,
                                             PoseStack poseStack, MultiBufferSource buffer,
                                             int packedLight, int packedOverlay, CallbackInfo ci) {
        ci.cancel();
    }
}