package net.zharok01.coralinesystems.mixin.entity;

import net.minecraft.client.renderer.entity.layers.LlamaDecorLayer;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LlamaDecorLayer.class)
public abstract class LlamaDecorLayerMixin {

    @Redirect(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/animal/horse/Llama;FFFFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/DyeColor;getId()I"
            )
    )
    private int coraline$preventLlamaDecorOutOfBounds(DyeColor dyeColor) {
        int colorId = dyeColor.getId();
        if (colorId < 0 || colorId >= 16) {
            return 0;
        }
        return colorId;
    }
}