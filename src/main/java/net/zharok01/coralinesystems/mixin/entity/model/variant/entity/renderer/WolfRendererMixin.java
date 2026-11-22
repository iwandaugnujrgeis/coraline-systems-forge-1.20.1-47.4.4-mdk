package net.zharok01.coralinesystems.mixin.entity.model.variant.entity.renderer;

import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.zharok01.coralinesystems.client.entity.variant.CoralineMobVariant;
import net.zharok01.coralinesystems.client.entity.variant.CoralineVariantHolder;
import net.zharok01.coralinesystems.client.entity.variant.CoralineVariants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WolfRenderer.class)
public class WolfRendererMixin {

    @Inject(
            method = "getTextureLocation(Lnet/minecraft/world/entity/animal/Wolf;)Lnet/minecraft/resources/ResourceLocation;",
            at = @At("HEAD"),
            cancellable = true
    )

    @SuppressWarnings("removal")
    private void coralineSystems$getTexture(Wolf wolf, CallbackInfoReturnable<ResourceLocation> cir) {
        CoralineMobVariant variant = CoralineVariants.getVariantFromNametag(EntityType.WOLF, wolf.hasCustomName() ? wolf.getName().getString() : null);
        if (variant == null) {
            variant = ((CoralineVariantHolder) wolf).coralineSystems$getVariant();
        }

        if (variant.isDefault()) {
            return;
        }

        String state = wolf.isTame() ? "_tame" : (wolf.isAngry() ? "_angry" : "");
        cir.setReturnValue(new ResourceLocation(variant.id().getNamespace(),
                "textures/entity/wolf/" + variant.id().getPath() + state + ".png"));
    }
}
