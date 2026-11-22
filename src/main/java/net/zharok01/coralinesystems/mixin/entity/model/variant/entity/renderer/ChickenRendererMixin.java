package net.zharok01.coralinesystems.mixin.entity.model.variant.entity.renderer;

import net.minecraft.client.renderer.entity.ChickenRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.zharok01.coralinesystems.client.entity.variant.CoralineMobVariant;
import net.zharok01.coralinesystems.client.entity.variant.CoralineVariantHolder;
import net.zharok01.coralinesystems.client.entity.variant.CoralineVariants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChickenRenderer.class)
public class ChickenRendererMixin {

    @Inject(
            method = "getTextureLocation(Lnet/minecraft/world/entity/animal/Chicken;)Lnet/minecraft/resources/ResourceLocation;",
            at = @At("HEAD"),
            cancellable = true
    )

    @SuppressWarnings("removal")
    private void coralineSystems$getTexture(Chicken chicken, CallbackInfoReturnable<ResourceLocation> cir) {
        CoralineMobVariant variant = CoralineVariants.getVariantFromNametag(EntityType.CHICKEN, chicken.hasCustomName() ? chicken.getName().getString() : null);
        if (variant == null) {
            variant = ((CoralineVariantHolder) chicken).coralineSystems$getVariant();
        }

        if (variant.isDefault()) {
            return;
        }

        cir.setReturnValue(new ResourceLocation(variant.id().getNamespace(),
                "textures/entity/chicken/" + variant.id().getPath() + ".png"));
    }
}
