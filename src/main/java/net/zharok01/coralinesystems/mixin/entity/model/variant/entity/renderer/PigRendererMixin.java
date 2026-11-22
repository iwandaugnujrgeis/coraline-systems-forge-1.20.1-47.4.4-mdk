package net.zharok01.coralinesystems.mixin.entity.model.variant.entity.renderer;

import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.zharok01.coralinesystems.client.entity.variant.CoralineMobVariant;
import net.zharok01.coralinesystems.client.entity.variant.CoralineVariantHolder;
import net.zharok01.coralinesystems.client.entity.variant.CoralineVariants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PigRenderer.class)
public class PigRendererMixin {

    @Inject(
            method = "getTextureLocation(Lnet/minecraft/world/entity/animal/Pig;)Lnet/minecraft/resources/ResourceLocation;",
            at = @At("HEAD"),
            cancellable = true
    )

    @SuppressWarnings("removal")
    private void coralineSystems$getTexture(Pig pig, CallbackInfoReturnable<ResourceLocation> cir) {
        CoralineMobVariant variant = CoralineVariants.getVariantFromNametag(EntityType.PIG, pig.hasCustomName() ? pig.getName().getString() : null);
        if (variant == null) {
            variant = ((CoralineVariantHolder) pig).coralineSystems$getVariant();
        }

        if (variant.isDefault()) {
            return;
        }

        cir.setReturnValue(new ResourceLocation(variant.id().getNamespace(),
                "textures/entity/pig/" + variant.id().getPath() + ".png"));
    }
}
