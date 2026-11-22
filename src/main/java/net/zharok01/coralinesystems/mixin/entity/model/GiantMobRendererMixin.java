package net.zharok01.coralinesystems.mixin.entity.model;

import net.minecraft.client.renderer.entity.GiantMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Giant;
import net.zharok01.coralinesystems.CoralineSystems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GiantMobRenderer.class)
public class GiantMobRendererMixin {

    @SuppressWarnings("removal")
    @Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true)
    private void coraline$getTextureLocation(Giant entity, CallbackInfoReturnable<ResourceLocation> cir) {
        long uuidPart = entity.getUUID().getLeastSignificantBits();
        int skinId = (int) (Math.abs(uuidPart) % 2);

        cir.setReturnValue(new ResourceLocation(CoralineSystems.MOD_ID,
                "textures/entity/giant/giant_" + (skinId + 1) + ".png"));
    }
}