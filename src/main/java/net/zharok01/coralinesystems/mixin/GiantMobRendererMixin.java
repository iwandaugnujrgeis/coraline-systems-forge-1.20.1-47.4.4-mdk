package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.renderer.entity.GiantMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.zharok01.coralinesystems.CoralineSystems;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GiantMobRenderer.class)
public class GiantMobRendererMixin {

    @Unique
    private static final ResourceLocation CORALINE_GIANT_TEXTURE =
            new ResourceLocation(CoralineSystems.MOD_ID, "textures/entity/giant/giant.png");

    /**
     * Redirects the texture location for the Giant entity.
     */
    @Inject(method = "getTextureLocation*", at = @At("HEAD"), cancellable = true)
    private void coraline$getTextureLocation(net.minecraft.world.entity.monster.Giant entity, CallbackInfoReturnable<ResourceLocation> cir) {
        cir.setReturnValue(CORALINE_GIANT_TEXTURE);
    }
}