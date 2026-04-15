package net.zharok01.coralinesystems.mixin;

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

    @Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true)
    private void coraline$getTextureLocation(Giant entity, CallbackInfoReturnable<ResourceLocation> cir) {
        // Extract a number from the entity's permanent UUID
        long uuidPart = entity.getUUID().getLeastSignificantBits();

        //Change "Math.abs(uuidPart) % _" to the desired amount of skins:
        int skinId = (int) (Math.abs(uuidPart) % 3);

        cir.setReturnValue(new ResourceLocation(CoralineSystems.MOD_ID,
                "textures/entity/giant/giant_" + (skinId + 1) + ".png"));
    }
}