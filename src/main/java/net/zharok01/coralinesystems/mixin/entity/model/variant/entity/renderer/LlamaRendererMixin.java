package net.zharok01.coralinesystems.mixin.entity.model.variant.entity.renderer;

import net.minecraft.client.renderer.entity.LlamaRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Llama;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LlamaRenderer.class)
public class LlamaRendererMixin {

    @Unique
    private static final ResourceLocation WHITE = new ResourceLocation("textures/entity/llama/white.png");

    @Inject(
            method = "getTextureLocation(Lnet/minecraft/world/entity/animal/horse/Llama;)Lnet/minecraft/resources/ResourceLocation;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void coralineSystems$useWhiteTexture(Llama llama, CallbackInfoReturnable<ResourceLocation> cir) {
        cir.setReturnValue(WHITE);
    }
}
